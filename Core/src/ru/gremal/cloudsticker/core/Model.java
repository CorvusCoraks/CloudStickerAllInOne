package ru.gremal.cloudsticker.core;

import ru.gremal.cloudsticker.common.tools.InternetConnectionMessage;
import ru.gremal.cloudsticker.common.tools.StatusSender;
import ru.gremal.cloudsticker.common.ui.AbstractUIControl;
import ru.gremal.cloudsticker.common.tools.CommonTools;

import java.io.*;
import java.util.*;
//import CloudStickerSwingUI;

/**
 * Created by GreMal on 21.02.2015.
 */
public class Model {
    protected Map<String, String> iniData = new HashMap<String, String>();
    final private static String iniFileName = "cloudsticker.ini";
    // Флаг готовности модели
    private boolean isReady = false;
    /* максимальное количество устройств в компании, включая и данное устройство.
    *  в связи с этим, в интерфейсе, в разделе "Компания" должно быть текстовых полей и кнопок,
    *  связанных с другими устройствами, на одну меньше, т. е. MAX_COMPANY_COUNT - 1*/
    protected final static int MAX_COMPANY_COUNT = 8;
    //protected DeviceInfo[] devicesInCircle = new DeviceInfo[MAX_COMPANY_COUNT];
    private Map<String, DeviceInfo> devicesInCircle = new HashMap<String, DeviceInfo>(); // deviceID, device
    //protected Internet.DBMessages devicesRequestDBStatus = Internet.DBMessages.VOID;
    private NoteInfo noteInfo = new NoteInfo();
    protected static Calendar calendar = new GregorianCalendar();
    private Date nextSynchronisationTime = new Date();
    //private static Map<GUI.StatusSender, GUI.StatusStringObject> lazyStatusForGui = new HashMap<GUI.StatusSender, GUI.StatusStringObject>();

    static{


    }

    // Получить статус готовности модели
    protected boolean getReady(){ return Controller.model.isReady; }

    /* Функция читает сохранённые настройки программы из соотеветствующего ini-файла в список Мэп*/
    protected void readInit() throws IOException
    {
        iniData = CommonTools.readFromIniFile(Controller.io, iniFileName);
    }

    /*
* Сохранение всех параметвров настройки (и GUI в том числе) в файле ini
* */
    protected void writeInit()
    {
        try {
            FileWriter writer = new FileWriter(Controller.io.File(iniFileName));

            Iterator<Map.Entry<String, String>> it = iniData.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> pair = it.next();
                String Key = pair.getKey();
                String Value = pair.getValue();
                StringBuilder builder = new StringBuilder();
                builder.append(Key).append((char) 32).append((char) 61).append((char) 32).append(Value).append("\r\n");
                // Ключ, пробел, равно, пробел, значение, перевод на новую строку
                writer.write(builder.toString());
            }
            writer.close();
        }catch(IOException ignore) {/*NOP*/}
    }

    /* Инициализация начальных не GUI-данных (возможно, после установки программы, то есть, при первом запуске)
*  изменение элементов GUI на основании полученных данных*/
    protected void initialization(){
        if(!isInternerConnectionActive()){ return; }
        // если хотя в файле настроек нет хотя бы одного из нижеследующих параметров, то создаём абсолютно новую запись
        if(!(iniData.containsKey("userID")&&iniData.containsKey("deviceID")/*&&iniData.containsKey("noteID")*/)){
            createNewNote();
        }else {
            /* возможно, что в файле настроек параметры есть, но доступ к заметке закрыт (запрещён, круг не найден).
            *  в этом случае тоже автоматически создаём новую заметку. */
            // данный запрос используется исключительно для проверки доступности заметки
            Internet.Result result = Internet.getNote(iniData.get("userID"), iniData.get("deviceID"));
            if((result.dbStatus == Internet.DBMessage.ACCESS_DENIED)||(result.dbStatus == Internet.DBMessage.CIRCLE_NOT_FOUND)){
                createNewNote();
            }
        }
        // таким образом, с этого момента, в любом случае заметка существует. Либо изначальная, либо созданная чуть выше.
        // здесь идут запросы в ui, т. е., к этому моменту ui должен быть сформирован

        isReady = true;
    }

    /* Создаём новую заметку */
    private void createNewNote(){
        /* Внимание! Проверка связи сознательно здесь не производится! Так как производилась в вызывающей функции! */
        iniData.put("userID", Tools.getRandomID());
        iniData.put("deviceID", Tools.getRandomID());

        devicesInCircle.clear();

        Internet.Result internetAnswer= Internet.createNewNote(iniData.get("userID"), iniData.get("deviceID"));
        if(internetAnswer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(internetAnswer.dbStatus, "Ошибка создания новой записи.");
            return;
        }
    }


    /* Получить данные по СУЩЕСТВУЮЩЕЙ и ДОСТУПНОЙ заметке из Базы Данных и настроить Модель и GUI
    * Т. е. либо заметка была создана непосредственно перед вызовом этой функции, либо была проведена проверка
    * её существования и доступности */
    protected void getInitialisationDataFromDB(){
        /* Внимание! Проверка связи сознательно здесь не производится! Так как производилась в вызывающей функции! */

        Internet.Result internetAnswer = Internet.getAllDevicesInfo(iniData.get("userID"), iniData.get("deviceID"));
        /* неудачная попытка доступа к заметке */
        if(internetAnswer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(internetAnswer.dbStatus, "Получение из облака информации об устройствах в круге.");
            return;
        }else{
            // devicesRequestDBStatus = Internet.DBMessages.VOID; // возвращаем значение по умолчанию
             /*Ошибок нет, данные получены. */
            {
                // заполенение объекта noteInfo
                noteInfo.noteId = iniData.get("userID");

                Internet.Result noteResult = Internet.getNote(iniData.get("userID"), iniData.get("deviceID"));
                if(noteResult.dbStatus != Internet.DBMessage.SUCCESS){
                    connectionErrorHandler(noteResult.dbStatus, "Получение из облака информации о заметке.");
                    return;
                }else{
                    for (Map.Entry<Date, String> temp : ((Map<Date, String>) noteResult.content).entrySet()) {
                        noteInfo.noteTimeStamp = temp.getKey();
                        noteInfo.note = temp.getValue();
                    }
                    noteInfo.textArea = Controller.gui.getNoteTextArea();
                    noteInfo.textArea.setSText(noteInfo.note);

                    // раздача кнопок и текстовых полей устройствам

                    AbstractUIControl[] buttons = Controller.gui.getOtherCircleDevicesButton();
                    AbstractUIControl[] textFields = Controller.gui.getOtherCircleDevicesTextField();

                    int i = 0;

                    for (DeviceInfo info : (DeviceInfo[]) internetAnswer.content) {
                        if (info.deviceId.equals(iniData.get("deviceID"))) {
                            // текущему устройству
                            info.textField = Controller.gui.getThisDeviceTextField();
                            info.button = Controller.gui.getThisDeviceButton();
                            info.textField.setSText(info.deviceLabel);
                        } else {
                            // другим устройствам
                            info.textField = textFields[i];
                            info.button = buttons[i];
                            info.textField.setSText(info.deviceLabel);
                            info.button.setSText(Controller.gui.getLocalisationValueByKey("btKickFromCircle"));
                            i++;
                        }
                        devicesInCircle.put(info.deviceId, info);
                    }
                }
            }
        }
    }

    protected void setNextSynchronisationTime(long ms){
        long currentDate = (new Date()).getTime();
        nextSynchronisationTime = new Date(currentDate + ms);
    }

    /* Фукция синхронизации заметки, меток устройств и всего прочего, что нужно синхронизировать. */
    protected synchronized void startSynchronization()throws InterruptedException{
        if(!isInternerConnectionActive()){ return; }

        boolean isSynchronisation = true;

        Internet.Result answer = Internet.getTimeStamps(iniData.get("userID"), iniData.get("deviceID"), isSynchronisation);
        /* неудачная попытка доступа к заметке */
        if((answer.dbStatus == Internet.DBMessage.ACCESS_DENIED)||(answer.dbStatus == Internet.DBMessage.CIRCLE_NOT_FOUND)){
            // Доступ в круг запрещён. Скорее всего устройство кикнули. А значит, создаём новую заметку.
            // Или же не найдена эта заметка. Либо удалена из базы, либо сменён её userID
            // запоминаем метку устройства
            String thisDeviceLabel = devicesInCircle.get(iniData.get("deviceID")).deviceLabel;
            createNewNote();
            // с этого момента заметка существует
            getInitialisationDataFromDB();
            DeviceInfo device = devicesInCircle.get(iniData.get("deviceID"));
            device.deviceLabel = thisDeviceLabel; // восстанавливаем метку устройства
            device.textField.setSText(thisDeviceLabel); // Выводим старую метку устройства в текстовое поле.
            device.labelWasChanged = true; // чтобы метка синхронизировалась на сервер
            device.labelTimeStamp = new Date(); // чтобы метка синхронизировалась на сервер
            Controller.gui.clearFreeTextField();
            answer = Internet.getTimeStamps(iniData.get("userID"), iniData.get("deviceID"), isSynchronisation);
        }else if(answer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(answer.dbStatus, "Получение из облака информации о TimeStamps меток устройств.");
            return;
        }

        Map<String, Date> mapTimeStamps = (Map<String, Date>) answer.content;

        // Синхронизация заметки
        if(noteInfo.noteWasChanged){
            noteInfo.note = Controller.gui.getNoteTextArea().getSText();
            noteInfo.noteTimeStamp = new Date();
            noteInfo.noteWasChanged = false;
        }
        if(noteInfo.noteTimeStamp.compareTo(mapTimeStamps.get(noteInfo.noteId)) > 0){
            /* TimeStamp у заметки на стороне клиента больше, чем на сервере.
            Значит, обновляем данные на сервере */
            answer = Internet.updateNote(iniData.get("userID"), iniData.get("deviceID"), noteInfo.note, noteInfo.noteTimeStamp);
            if(answer.dbStatus != Internet.DBMessage.SUCCESS){
                connectionErrorHandler(answer.dbStatus, "Обновление записи с клиента на сервер.");
                return;
            }
        }else if (noteInfo.noteTimeStamp.compareTo(mapTimeStamps.get(noteInfo.noteId)) < 0){
            /* TimeStamp у заметки на стороне клиента меньше, чем на сервере.
            Значит, обновляем данные на клиенте */
            answer = Internet.getNote(iniData.get("userID"), iniData.get("deviceID"));
            if(answer.dbStatus != Internet.DBMessage.SUCCESS){
                connectionErrorHandler(answer.dbStatus, "Получение заметки с сервера.");
                return;
            }
            noteInfo.noteTimeStamp = mapTimeStamps.get(iniData.get("userID"));
            noteInfo.note = ((Map<Date, String>) answer.content).get(noteInfo.noteTimeStamp);
            noteInfo.textArea.setSText(noteInfo.note);
        }else{ /* = 0 -> ничего не делаем */ }

        // получаем из базы информацию обо всех устройствах круга
        answer = Internet.getAllDevicesInfoMap(iniData.get("userID"), iniData.get("deviceID"));
        if(answer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(answer.dbStatus, "Получение информации обо всех устройствах круга.");
            return;
        }
        // Синхронизация членов круга
        {
            Map<String, DeviceInfo> tempMap = new HashMap<String, DeviceInfo>();
            for (Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()) {
                if (!mapTimeStamps.containsKey(pair.getKey())) {
                    // удалить из клиенетского списка данное устройство, так как его нет на сервере
                    // новые устройства попадают в devicesInCircle только через сервер
                    pair.getValue().textField.setSText(""); // очищаем текстовое поле удалённого устройства
                    Controller.gui.invertTextOnButton(pair.getValue().button); // инвертируем надпись на кнопке
                    continue;
                }
                tempMap.put(pair.getKey(), pair.getValue());
            }
            //tempMap.remove(iniData.get("userID")); //удаляем, попавший в
            devicesInCircle.clear();
            devicesInCircle.putAll(tempMap);

            Controller.logFileService.publishToLog(new StringBuilder("Число устройств круга: ").append(devicesInCircle.size()).toString());
            Controller.logFileService.publishToLog(new StringBuilder("mapTimeStamps : ").append(mapTimeStamps).toString());
            Controller.logFileService.publishToLog(new StringBuilder("iniData : ").append(iniData).toString());
            Controller.logFileService.publishToLog(new StringBuilder("answer : ").append(answer).toString());
            Controller.logFileService.publishToLog(new StringBuilder("answer.content : ").append(answer.content).toString());

            for (Map.Entry<String, Date> pair : mapTimeStamps.entrySet()) {

                //Controller.logFileService.publishToLog("deviceInCir");
                // проверяме, чтобы в список устройств не попала заметка (так как она заведомо содержится в mapTimeStamps с сервера)
                if (!devicesInCircle.containsKey(pair.getKey())&&!iniData.get("userID").equals(pair.getKey())) {
                    // добавить в клиентский список данное устройство, так как оно есть на сервере, а у клиента нет
                    // устройства удаляются из deviceInCircle только через сервер
                    devicesInCircle.put(pair.getKey(), ((Map<String, DeviceInfo>) answer.content).get(pair.getKey()));
                }
            }
        }

        // Синхронизация меток членов клуба
        Map<String, String> newLabels = new HashMap<String, String>(); // новые метки для отправки на сервер
        for(Map.Entry<String, Date> pair : mapTimeStamps.entrySet()){
            // исключаем из перебора в mapTimeStamps запись с информацией о заметке (она же не устройство)
            if(!pair.getKey().equals(iniData.get("userID"))) {
                DeviceInfo device = devicesInCircle.get(pair.getKey());
                if (device.labelWasChanged) {
                    device.deviceLabel = device.textField.getSText();
                    device.labelTimeStamp = new Date();
                    device.labelWasChanged = false;
                }
                if (device.labelTimeStamp.compareTo(pair.getValue()) > 0) {
                    // TimeStamp больше у клиента, начинаем комплектовать мапу для отправки на сервер
                    newLabels.put(pair.getKey(), device.deviceLabel);
                } else if (device.labelTimeStamp.compareTo(pair.getValue()) < 0) {
                    // TimeStamp меньше у клиента, обновляем данные у клиента
                    device.labelTimeStamp = ((Map<String, DeviceInfo>) answer.content).get(pair.getKey()).labelTimeStamp;
                    device.deviceLabel = ((Map<String, DeviceInfo>) answer.content).get(pair.getKey()).deviceLabel;
                    device.textField.setSText(device.deviceLabel);
                } else { /* = 0 -> либо метка не менялась нигде, либо только что скачали данные по новому устройству круга */
                    if(device.textField == null){
                        /* если текстовое поле null, значит это точно - новое устройство */
                        device.textField = Controller.gui.getFreeOtherDeviceTextField(); // Получаем в GUI свободное текстовое поле
                        device.textField.setSText(device.deviceLabel); // обновляем текст в текстовом поле
                        device.button = Controller.gui.getButtonByTextField(device.textField); // получаем парную кнопку
                        Controller.gui.invertTextOnButton(device.button); // инвертируем надпись на кнопке
                    }
                }
            }
        }

        if(!newLabels.isEmpty()){
            // Обновляем данные на сервере
            answer = Internet.updateDevecesLabels(iniData.get("userID"), iniData.get("deviceID"), newLabels);
            if(answer.dbStatus != Internet.DBMessage.SUCCESS){
                connectionErrorHandler(answer.dbStatus, "Обновление меток устройств.");
                return;
            }
            // Map<String, Date> idAndTimeStamps = (Map<String, Date>) answer.content;
            /* Раздача новых LabelTimeStamp, полученных из БД. Таким образом, синхронизируются LabelTimeStamps */
            for(Map.Entry<String, String> pair : newLabels.entrySet()){
                devicesInCircle.get(pair.getKey()).labelTimeStamp = (Date) answer.content;
            }
        }
    }

    // вызывается из UI
    protected synchronized void InviteOrKickButtonPressed(AbstractUIControl button){
        if(!isReady){ return; } // модель ещё не готова
        if(!isInternerConnectionActive()){ return; }
        Internet.Result answer;
        // временный мэп, в котом легче искать по кнопкам среди устройств круга. Ключ - кнопка
        Map<AbstractUIControl, DeviceInfo> buttonKey = new HashMap<AbstractUIControl, DeviceInfo>();
        // временный мэп, в котом легче искать по текстовым полям среди устройств круга. Ключ - текстовое поле
        Map<AbstractUIControl, DeviceInfo> textfieldKey = new HashMap<AbstractUIControl,DeviceInfo>();
        for(Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()){
            DeviceInfo value = pair.getValue();
            buttonKey.put(value.button, value);
            textfieldKey.put(value.textField, value);
        }
        // Принадлежит ли данная кнопка, какому либо устройству из deviceCircle
        // Если принадлежит, значит требуемое действие - Kick
        // Если не принадлежит, значит требуемое действие - Invite
        if(buttonKey.containsKey(button)){
            // Кикаем устройство и выходим из процедуры
            // Удалить устройство из базы на сервере
            answer = Internet.kickDeviceFromCircle(iniData.get("userID"), iniData.get("deviceID"), buttonKey.get(button).deviceId);
            if(answer.dbStatus != Internet.DBMessage.SUCCESS){
                connectionErrorHandler(answer.dbStatus, "Удаление устройства из Базы данных.");
                return;
            }
            buttonKey.get(button).textField.setSText(""); // очистка поля с меткой устройства
            Controller.gui.invertTextOnButton(button); // инвертирование надписи на кнопке
            devicesInCircle.remove(buttonKey.get(button).deviceId); // удалить устройство из списка на клиенте
            return;
        }

        // Данная кнопка не принадлежит устройства, значит - приглашение.
        // Очищаем все поля, которые не принадлежат какому либо устройству (удаляем дублирующий пароль)
        AbstractUIControl[] textFieldsInGui = Controller.gui.getOtherCircleDevicesTextField();
        for(int i = 0; i < textFieldsInGui.length; i++){
            if(!textfieldKey.containsKey(textFieldsInGui[i])){ textFieldsInGui[i].setSText(""); }
        }
        // Генерируем пароль.
        String newPass = Tools.generate5DigitPass();
        // Закинуть пароль на сервер (предыдущий неотработанный пароль удалится автоматически)
        answer = Internet.setPassForNewDevice(iniData.get("userID"), iniData.get("deviceID"), newPass);
        if(answer.dbStatus != Internet.DBMessage.SUCCESS){
            connectionErrorHandler(answer.dbStatus, "Отправка нового пароля в БД.");
            return;
        }
        // Вывести в строку GUI пароль
        AbstractUIControl tf = Controller.gui.getTextPaired(button);
        tf.setSText(newPass);
    }

    /* Это происходит, если нажата кнопка "войти в круг" */
    protected synchronized void EnterToCircleButtonPressed(){
        if(!isReady){ return; } // модель ещё не готова
        if(!isInternerConnectionActive()){ return; }
        String password = Controller.gui.getInvitationTextField().getSText();
        // если длина пароля ноль или больше допустимого
        if((password.length() == 0)||(password.length() > Controller.CHARS_IN_INVITATION_PASS)){ return; }
        // если в поле пароля введены не числа
        try{
            int test = Integer.parseInt(password);
            if(test == 0){ return; }
        }catch (NumberFormatException ex){ return; }
        // Отправляем запрос на сервер
        Internet.Result answer = Internet.addDeviceInCircle(iniData.get("userID"), iniData.get("deviceID"), password);
        if(answer.dbStatus != Internet.DBMessage.SUCCESS){
            /* Если неудачная попытка вступить в круг (не важно по какой причине), просто выходим из этой функции,
            оставляя старые параметры заметки.
            Хотя, обязательно надо уведомить о событии пользователя. */
            Controller.gui.putNewStatusInStatusString(StatusSender.ENTER_TO_CIRCLE, "Couldn't enter to circle.", 5);
            connectionErrorHandler(answer.dbStatus, "Получение userID круга, куда вступаем.");
            return;
        }
        // получив userID, меняем его на этом клиенте и загружаем заметку.
        iniData.put("userID", (String) answer.content);
        // Инициализация возможна только у существущей и доступной заметки, что к этому моменту обеспечено.
        getInitialisationDataFromDB();
    }

    /* Функция обрабатывае стаусные ситуации во время связи с сервером. Т. е., пишется в логи,
    пишется в статусную строку GUI и т. п.
    message - статус
    textKey - текстовая строка характеризующая место и момент возникновения данного статуса

    В случае создания вызовов в ui, предусмотреть проверку на готовность ui*/
    protected void connectionErrorHandler(Internet.DBMessage message, String textKey){
        switch (message) {
            case ACCESS_DENIED:
                /* Доступ к кругу запрещён. Данное устройство не в круге */
                //Controller.gui.putNewStatusInStatusString(GUI.StatusSender.DB_ERRORS, "Access denied. Create new note.", 5);
                break;
            case CIRCLE_NOT_FOUND:
                /* Круг не найден */
                //Controller.gui.putNewStatusInStatusString(GUI.StatusSender.DB_ERRORS, "Note not found. Create new.", 5);
                break;
            case VOID:
                /* Неопределённое значение. */
                break;
            case FIELDS_COUNT_ERROR:
                /* Ошибки. Число строк в ответе из интернета не преобразуется в осмысленный результат. */
                break;
            case SERVER_CONNECTION_ERROR:
                /* Ошибки. Связь с сервером не сложилась. */
                break;
            case SUCCESS:
                /* Успех */
                break;
        }
    }

    /* ****************************************************************************/
    /* Интерфейсные функции, чтобы GUI мог получить необходимые данные из модели.
    * Перенести всё в CoreSide */
    /* ****************************************************************************/
    /* провека данного текстового поля на занятость */
    protected boolean isTextFieldFree(AbstractUIControl textField){
        if(!isReady){ return false; } // модель ещё не готова

        for(Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()){
            if(pair.getValue().textField == null){ continue; }
            if(pair.getValue().textField == textField){ return false; }
        }
        return true;
    }

    protected synchronized void setNoteWasChangedFlagToTrue(){
        if(!isReady){ return; } // модель ещё не готова
        noteInfo.noteWasChanged = true;
    }
    //protected void setThisDeviceLabelWasChangedFlagToTrue(){ devicesInCircle.get(iniData.get("deviceID")).labelWasChanged = true; }
    protected synchronized void setDeviceLabelWasChangedFlagToTrue(AbstractUIControl textField){
        if(!isReady){ return; } // модель ещё не готова
        for (Map.Entry<String, DeviceInfo> info : devicesInCircle.entrySet()) {
            if (info.getValue().textField == textField) {
                info.getValue().labelWasChanged = true;
                return;
            }
        }
    }

    // вспомогательный класс. Содержит информацию об устройстве входящим в круг.
    protected static class DeviceInfo{
        protected String deviceId;
        protected String deviceLabel;
        protected Date labelTimeStamp;
        protected boolean labelWasChanged = false;
        /* Метка WasChanged ставится в true, когда происходит редактирование соответствующего поля,
        * ибо часто подрачивать лучше boolean, чем так же часто создавать новый объект Date для занесения в TimeStamp
        * TimeStamp будет изменён непосредственно перед синхронизацией, согласно состояния флага WasChahged
        * Необходимость синхронизации данного поля будет решаться исключительно из сравнения TimeStamp */

        AbstractUIControl textField;
        AbstractUIControl button;

        protected DeviceInfo(String deviceId, String deviceLabel, Date labelTimeStamp){
            this.deviceId = deviceId;
            this.deviceLabel = deviceLabel;
            this.labelTimeStamp = labelTimeStamp;
        }

        protected DeviceInfo(){}
    }

    protected static class NoteInfo{
        protected String noteId;
        protected String note;
        protected Date noteTimeStamp;
        protected boolean noteWasChanged = false;
        /* Метка WasChanged ставится в true, когда происходит редактирование соответствующего поля,
        * ибо часто подрачивать лучше boolean, чем так же часто создавать новый объект Date для занесения в TimeStamp
        * TimeStamp будет изменён непосредственно перед синхронизацией, согласно состояния флага WasChahged
        * Необходимость синхронизации данного поля будет решаться исключительно из сравнения TimeStamp */

        protected AbstractUIControl textArea;
    }

    /* проверка интернет соединения. */
    private boolean isInternerConnectionActive(){
        InternetConnectionMessage message = InternetConnectionTest.isCloudReachable();
        if (Controller.gui != null) {
            if (Controller.gui.getReady()) {
                // Проверки, этапа инициализации
                Controller.gui.setInternetConnectionStatuses(message);
            }
        }
        if(message == InternetConnectionMessage.YES){ return true; }
        return false;
    }

    /* Функция определяет, есть ли несинхронизированные данные */
    protected boolean isWasChangedTrue(){
        if(!isReady){ return false; } // модель ещё не готова
        if(noteInfo.noteWasChanged){ return true; }
        for(Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()){
            if(pair.getValue().labelWasChanged){ return true; }
        }
        return false;
    }

    // равна ли заметка в модели содержимому текстовой области
    // функция может показать, вносились ли изменения в содержимое заметки
    protected boolean isNoteEquals(String str){
        if(!isReady){ return false; } // модель ещё не готова
        return this.noteInfo.note.equals(str);
    }

    // равна ли метка устройства в модели содержимому соответствующего текстового поля
    // Функция может показать, производились ли изменения с данным полем
    protected boolean isDeviceLabelEquals(AbstractUIControl auic){
        if(!isReady){ return false; } // модель ещё не готова
        for(Map.Entry<String, DeviceInfo> pair : devicesInCircle.entrySet()){
            if(pair.getValue().textField == auic) {
                if (pair.getValue().deviceLabel.equals(auic.getSText())) {
                    return true;
                }else{ break; }
            }
        }
        return false;
    }
}