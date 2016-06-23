package ru.gremal.cloudsticker.core;

import ru.gremal.cloudsticker.common.interfaces.CoreSide;
import ru.gremal.cloudsticker.common.interfaces.IOSpecific;
import ru.gremal.cloudsticker.common.interfaces.LogInterface;
import ru.gremal.cloudsticker.common.interfaces.UISide;
import ru.gremal.cloudsticker.common.tools.CommonTools;

/**
 * Класс предназначен для использования в том случае, если модуль ядра импортирован в модуль UI.
 * То есть, если запуск приложения производится из некого внешнего модуля, куда импортирован данный модуль ядра,
 * а не запуском файла ядра.
 *
 * Запланировано для использования, например, для создания интерфейса на Андроиде
 */
public class CoreDetonator {
    private volatile static CoreSide core = null;
    private volatile static UISide ui = null;
    private volatile static LogInterface logFileService = null;
    protected static final int SLEEP_TIME = 50;

    // В функцию передаётся ссылка на объект пользовательского интерфейса, а возвращает она ссылку на объект ядра
    // Запуск ядра и организация связи ядра и пользовательского интерфейса производятся именно путём вызова этой
    // функции из пользовательского интерфейса
    public static CoreSide runCoreFromOuterModule(UISide outerUI, IOSpecific io, LogInterface lfs) throws InterruptedException{
        setUi(outerUI);
        logFileService = lfs;
        Controller.io = io;
        new coreThread().start();
        // ждём, пока ядро установит ссылку на core
        while (getCore() == null){Thread.sleep( SLEEP_TIME); }
        return getCore();
    }

    protected synchronized static CoreSide getCore() {
        return CoreDetonator.core;
    }


    protected synchronized static void setCore(CoreSide core) {
        CoreDetonator.core = core;
    }

    protected synchronized static UISide getUi() {
        return CoreDetonator.ui;
    }

    protected synchronized static void setUi(UISide ui) {
        CoreDetonator.ui = ui;
    }

    protected static class coreThread extends Thread{
        // Бутафорская строка заменяет массив args[]
        String[] str = {"",""};
        @Override
        public void run(){
            try {
                Controller.main(str);
            } catch (Exception e) {
                e.getStackTrace();
            }
        }
    }

    protected synchronized static LogInterface getLogFileService(){
        return logFileService;
    }
}
