package ru.gremal.cloudsticker.core; /**
 * Created by GreMal on 21.02.2015.
 */

import ru.gremal.cloudsticker.common.interfaces.IOSpecific;
import ru.gremal.cloudsticker.common.interfaces.LogInterface;
import ru.gremal.cloudsticker.common.interfaces.UISide;
import ru.gremal.cloudsticker.common.tools.InternetConnectionMessage;
import ru.gremal.cloudsticker.common.tools.CommonTools;
import ru.gremal.cloudsticker.ui.GUI;
import ru.gremal.cloudsticker.io.IO;
import sun.rmi.log.ReliableLog;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/*
* Компания - устройства, участвующие в обслуживании данной конкректной заметки (одного конкретного пользователя
*
* Если в директории программы отсутствуе файл Start.jar, значит обновление программы через Start.jar не требуется
* (например, это делается другим способом, через магазин приложений). В общем, другим путём.
*
* Так же, это удобно в случае отладки программы через IDE
* */

/* Todo Сделать отдельный поток, в котором будет периодически проверяться состояние флагов в объекте GUI. Перед этим, обязательная проверка на существование объекта GUI. Если объекта GUI нет - поток завершается */
public class Controller {
    protected final static String DEFAULT_DATA_STAMP = "0000-00-00 00:00:00";
    protected final static float PROGRAM_VERSION = 0.02f;
    protected final static int MAX_CHARS_IN_LABEL = 25;
    protected final static int MAX_CHARS_IN_NOTE = 1000; // максимальное количество символов в заметке
    protected final static int CHARS_IN_INVITATION_PASS = 5; // количество символов в пригласительном пароле
    protected final static String OS_NAME = System.getProperty("os.name");
    protected final static String LAST_VER_FILE_LOCATION = "http://cn.gremal.ru/files/lastver/cloudsticker.zip";
    protected static TestInternetConnectionThread jerkThread;
    protected static UISide gui = null;
    protected static Model model = null;
    // файл создаётся с первоначальными настройками во время инсталляции программы

    // Настройки файлов лога
    private static final String LOGFILE_PATTERN = "./cs%g.log"; // имя лог-файла
    private static final int LOGFILE_SIZE = 10000; // размер одного лог-файла
    private static final int LOGFILES_NUMBER = 5; // количество лог-файлов
    private static final boolean LOGFILE_APPEND = true;
    protected static final Level LOG_LEVEL = Level.OFF; // уровень сообщений лога
    //protected static CommonTools.LogFileService logFileService;
    protected static LogInterface logFileService;
    protected static IOSpecific io;

    public static void main(String[] args) throws IOException, InterruptedException, Exception {
        if(CoreDetonator.getUi() == null) {
            // инициализируем объект специфичных методов работы с файлами
            io = new IO();
            CommonTools.LogFileService.delLogFiles("cs%1$s.log");
            logFileService = CommonTools.LogFileService.createInstance(LOGFILE_PATTERN, LOGFILE_SIZE, LOGFILES_NUMBER, LOGFILE_APPEND, LOG_LEVEL);
        }else{
            logFileService = CoreDetonator.getLogFileService();
        }

        final CoreSideImplementation csi = new CoreSideImplementation();

        //logFileService.publishToLog("Вход в main");

        // обновление файла start.jar
        File oldStartFile = new File("./start.jar");
        File newStartFile = new File("./new_start.jar");
        Boolean isStartFilePresent = false;
        if (oldStartFile.exists()){isStartFilePresent = true;}

        // Если объект UI уже есть, то ни о каких обновлениях нет и речи (программа уже запущена)
        if(CoreDetonator.getUi() == null ) {
            // Если Start.jar присутствует, работаем с попыткой обновления программы
            //logFileService.publishToLog("getUi() == null");
            if (isStartFilePresent) {
                if (newStartFile.exists()) {
                    oldStartFile.delete();
                    newStartFile.renameTo(oldStartFile);
                }

                // Если запуск программы произошёл не через start.jar, то перекидываем управление принудительно в start.jar
                //System.out.println("на входе в main");
                List<String> argsList = Arrays.asList(args);
                //logFileService.publishToLog("В аргументах start: " + argsList.contains("start"));
                if (!argsList.contains("start")) {
                    // был произведён запуск через модуль Core
                    // необходимо передать управление в модуль start
            /* ------------------------------------------------------------- */
                    // todo ВНИМАНИЕ раскомментировать после отладки
                    //Process proc = Runtime.getRuntime().exec("java -jar start.jar");
                    //return;
            /* ------------------------------------------------------------- */
                }
            }
        }
        /* Сначала модель и ГУИ создаются БЕЗ взаимных связей, до полной сборки самих себя.
         * Только после этого устанавливаются связи.
         *
         * - independentModelTune() - настройка автономных (независячих от ui) параметров модели
         * - Модель подаёт ui сигнал готовности.
         * - new GUI() - создание ui [в следующем порядке: получение настроенных автономных параметров модели (1),
         * настройка своих автономных {независимых от модели} параметров (2)]. Пункт 1 начинает действовать ТОЛЬКО после
         * получения сигнала готовности модели.
         * - ui подаёт сигнал готовности.
         * - dependentModelTune() - настройка параметров модели, зависимых от ui*/

        /* Так как, не равенство null ссылок на gui и model НЕ означает, что их формирование и инициализация завершены,
        * надо в этих объектах завести поля, которые будут true ПОСЛЕ полной инициализации этих объектов
        *
        * Вызов констурктора gui не означает, что в следующей строке gui будет не null
        */
        //logFileService.publishToLog("перед independentModelTune()");
        independentModelTune();
        //logFileService.publishToLog("после independModelTune()");

        if(CoreDetonator.getUi() == null) {
            // Убедились, что запуск UI произведён не из внешнего модуля, уже создавшего UI
            gui = GUI.UIDetonator.createUiInstance(csi);
            //logFileService.publishToLog("GUI0: " + gui);
        }else{
            // Если модуль ядра является импортированным, то UI уже создан внешним модулем
            gui = CoreDetonator.getUi();
            CoreDetonator.setCore(csi);
        }
        /* todo Внимание! Сделать так, чтобы после этой развилки, model и ui инициализировались и подготавливались к работе СТРОГО НЕЗАВИСИМО.
        Либо, чётко и контролируемо последовательно.
        В любом случае, они должны выполнять обращения к партнёрам только после их абсолютной готовности. */

        // Задержка, чтобы позволить GUI полностью сформироваться до инициализации модели
        //logFileService.publishToLog("GUI1: " + gui);
        while (gui == null) { Thread.sleep(CommonTools.SLEEP_INTERVAL); }
        //logFileService.publishToLog("GUI2: " + gui);
        while (!gui.getReady()) { Thread.sleep(CommonTools.SLEEP_INTERVAL); }
        //logFileService.publishToLog("GUI3: " + gui);
        //logFileService.publishToLog("перед dependModelTune()");
        dependModelTune();

        // Первоначальный запуск нити проверки связи с Интернет
        jerkThread = new TestInternetConnectionThread(gui);
        jerkThread.start();

        if(CoreDetonator.getUi() == null) {
            // Скачивание обновлённой версии
            if (isStartFilePresent) {
                if (new InternetConnectionTest().isCloudReachableGC() == InternetConnectionMessage.YES) {
                    boolean isRefreshNeeded = false;
                    Internet.Result answer = Internet.getLastProgramVer();
                    float ver = Float.parseFloat((String) answer.content);
                    if (PROGRAM_VERSION < ver) {
                        //gui.putNewStatusInStatusString(GUI.StatusSender.CONTROLLER, "New version CloudSticker ready.", 10);
                        File fileName = new File("./cloudsticker.zip");
                        // Если файл новоё версии уже есть в каталоге программы, то скачиваеть обновление не следует.
                        if (!fileName.exists()) {
                            byte[] fileContent = Internet.getLastVerCloudNotes(LAST_VER_FILE_LOCATION);
                            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
                            fileOutputStream.write(fileContent);
                            fileOutputStream.close();
                        }
                    }
                }
            }
        }
        // Передача статистики
        if(new InternetConnectionTest().isCloudReachableGC() == InternetConnectionMessage.YES) {
            Map<String, String> hash = new HashMap<String, String>();
            hash.put("os", OS_NAME);
            Internet.Result answer = Internet.sendStatistics(hash);
        }

        if (CoreDetonator.getUi() != null) {
            ((CommonTools.LogFileService) logFileService).close();
        }
    }

    // подготовка модели, независимая от ui
    private static void independentModelTune() throws IOException{
        // обращений к ui нет
        model = new Model();

        // чтение файла ini, обращений к ui нет
        model.readInit();

        // инициализация данных, обращений к ui нет
        model.initialization();
    }

    // подготовка модели, зависимая от ui (запросы в ui)
    // Теоретически, в этой функции производятся те настроечные процедуры модели, источники которых запрятаны в ui.
    // То есть, если для создания этих настроек ui что-то и требовалось от модели, то ui уже должен иметь доступ к этим данным
    private static void dependModelTune(){
        // чтение данных о заметке из базы данных
        model.getInitialisationDataFromDB();
    }
}

