package ru.gremal.cloudsticker.core; /**
 * Created by GreMal on 22.02.2015.
 */
import ru.gremal.cloudsticker.common.interfaces.UISide;
import ru.gremal.cloudsticker.common.tools.InternetConnectionMessage;

import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.*;

public class Internet {
    //private final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:35.0) Gecko/20100101 Firefox/35.0"; // рабочий вариант
    //private static final String USER_AGENT = "Mozilla/5.0 Gecko/20100101 Firefox/35.0"; // убрал информацию в скобках (видимо, о системе). Тоже рабочий вариант
    private static final String USER_AGENT = "Mozilla/5.0 Gecko/20100101 Firefox/35.0 CloudStickerAgent/0.0"; // добавил в конец информацию о том, что это моя программа. Работает.
    //private final String USER_AGENT = "Mozilla/5.0"; // в таком виде: сервер выдаёт ошибку 406
    private static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    private static final String ACCEPT_ENCODING = "gzip, deflate"; // если использовать - уродует ответ сервера
    private static final String ACCEPT_LANGUAGE = "ru-RU,ru;q=0.8,en-US;q=0.5,en;q=0.3"; // ответ сервера не меняет
    private static final String ACCEPT_CHARSET = "windows-1251, KOI8-R, iso-8859-1"; // ответ сервера не меняет
    private static final int ANSWER_WAIT_TIME = 15000; // Время ожидания ответа от хоста, мс
    private static final String myURL = "http://cn.gremal.ru/cgi-bin/cloudgate.cgi";
    private static final int port = 80;
    private static final String FIELDS_DELIMITER = "<42>"; // разделитель полей в возвращаемой записи
    private static final String RECORDS_DELIMITER = "<128>"; // разделитель записей


    protected static void test() throws IOException
    {
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "test");
        List<String> list = sendHTTPRequest(map);

        for (String str : list)
        {
            System.out.println(str);
        }
    }

        /*
    * Отправка на удалённый сервер HTTP-запроса. Возвращамый результат - массив байт.
    * Лист байт может быть равен null, это значит, что данный запрос вернул ошибку
    *
    * Все ошибки отрабатываются в вызывающей функции!
    *
    * Функция скачивает архив с обновлённой версией программы*/
    protected static byte[] getLastVerCloudNotes(String fileURL) throws IOException
    {


        /* Ошибки порождаемые в этом модуле:
        * MalformedURLException - маловероятное исключение. Просто пробрасываем его на верх до конца.
        * IOException - отработать
        * ProtocolException - отработать*/

        URL oUrl = new URL(fileURL); // MalformedURLException - не отрабатываем
        /*
        * openConnection возвращает объект класса URLConnection
        * А HttpURLConnection является потомком URLConnection. Тo есть, в данном случае мы делаем сужающее
        * преобразование вниз. Т. е., у объекта connection мы сможем пользоваться только теми методами, которые были
        * унаследованы от URLConnection
        * */
        HttpURLConnection connection = (HttpURLConnection) oUrl.openConnection(); // IOException

        // add request header
        connection.setRequestMethod("GET"); //HTTPUrlConnection // ProtocolException - отработать
        connection.setRequestProperty("User-Agent", USER_AGENT); //URLConnection
        connection.setRequestProperty("Accept", ACCEPT);
        connection.setRequestProperty("Accept-Language", ACCEPT_LANGUAGE);
        connection.setRequestProperty("Accept-Charset", ACCEPT_CHARSET);

        Integer responseCode = connection.getResponseCode(); //IOException

        // Если с сервера возвращается код ошибки - покидаем функцию
        if (responseCode >= 400 ){ return null; } // отработать объяснимую ошибку

        InputStream inputStream = connection.getInputStream();

        byte[] buffer = new byte[1000];
        int readedBytes = -1;
        List<Byte> result = new ArrayList<Byte>();
/*      в таком варианте файл правильно не считывается. Количество считанных байт меняется от случая к случаю
        while (inputStream.available() > 0){
            ...
            }
        }*/
        while((readedBytes = inputStream.read(buffer)) > -1){
            for(int i = 0; i < readedBytes; i++){
                result.add(Byte.valueOf(buffer[i]));
            }
        }
        inputStream.close();
        connection.disconnect();

        // Преобразовываем лист Byte в массив byte
        byte[] result_b = new byte[result.size()];
        for(int i = 0; i < result_b.length; i++){
            result_b[i] = result.get(i);
        }

        return result_b;
    }

    // Перегрузка метода sendHTTPRequest только с Map на входе
    protected static List<String> sendHTTPRequest(Map<String, String> map) throws IOException { return sendHTTPRequest(map, null); }

    /*
    * Отправка на удалённый сервер HTTP-запроса. Возвращамый результат - массив строк (ответ сервера)
    * В нулевой строке массива идёт трёхзначное число - статус ответа сервераж,
    * в первой строке идёт статус ответа БД (если в нулевой строке успех), включая статус SUCCESS
    * Параметры функции: список параметров (ключ - значение), лист строк (собственно - заметка)
    * Лист строк может быть равен null, это значит, что данный запрос отправки заметки не производит
    * Отправка заметки в этой функции НЕ РЕАЛИЗОВАНА! Заметка влючается в запрос уровнем выше!
    *
    * Все ошибки отрабатываются в вызывающей функции!*/
    private static List<String> sendHTTPRequest(Map<String, String> map, ArrayList<String> note) throws IOException
    {


        /* Ошибки порождаемые в этом модуле:
        * MalformedURLException - маловероятное исключение. Просто пробрасываем его на верх до конца.
        * IOException - отработать
        * ProtocolException - отработать*/

        URL oUrl = new URL(myURL); // MalformedURLException - не отрабатываем
        /*
        * openConnection возвращает объект класса URLConnection
        * А HttpURLConnection является потомком URLConnection. Тo есть, в данном случае мы делаем сужающее
        * преобразование вниз. Т. е., у объекта connection мы сможем пользоваться только теми методами, которые были
        * унаследованы от URLConnection
        * */
        HttpURLConnection connection = (HttpURLConnection) oUrl.openConnection(); // IOException
        //HttpURLConnection connection = new HttpURLConnection(new URL(URL));

        // add request header
        connection.setRequestMethod("POST"); //HTTPUrlConnection // ProtocolException - отработать
        connection.setRequestProperty("User-Agent", USER_AGENT); //URLConnection
        //connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
        //connection.setRequestProperty("Content-Type", "text/html; charset=iso-8859-1");
        connection.setRequestProperty("Accept", ACCEPT);
        //connection.setRequestProperty("Accept-Encoding", ACCEPT_ENCODING);
        connection.setRequestProperty("Accept-Language", ACCEPT_LANGUAGE);
        connection.setRequestProperty("Accept-Charset", ACCEPT_CHARSET);

        String urlParameters;
        if (map != null) {
            StringBuilder builder = new StringBuilder();
            //StringBuilder.append('?');
            Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = iterator.next();
                //builder.append(pair.getKey()).append((char) 32).append((char) 61).append((char) 32).append(pair.getValue()).append('&');
                builder.append(pair.getKey()).append((char) 61).append(pair.getValue()).append('&');
            }
            // Удаление последнего символа (&) из полученного билдера и преобразование в строку
            urlParameters = builder.deleteCharAt(builder.length() - 1).toString();
        } else {urlParameters = "";}

        // добавление к строке параметров содержимого заметки
        if (note != null){}

        // Send post request
        connection.setDoOutput(true); //URLConnection
        DataOutputStream output = new DataOutputStream(connection.getOutputStream()); //IOException
        output.writeBytes(urlParameters); //IOException
        output.flush(); //IOException
        output.close(); //IOException

        Integer responseCode = connection.getResponseCode(); //IOException

        List<String> list = new ArrayList<String>();
        list.add(responseCode.toString());

        // Если с сервера возвращается код ошибки - покидаем функцию
        if (responseCode >= 400 ){ return list; }

        // получаем из потока ответ сервера
        String str;
        InputStream inStream = connection.getInputStream(); //IOException
/*        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream())); //IOException
        while((str = reader.readLine()) != null){ list.add(str); } //IOException
        reader.close(); //IOException*/
        InputStreamReader reader = new InputStreamReader(inStream);
        char[] buffer = new char[64];
        int chars = 0;
        StringBuilder bld = new StringBuilder();
        while(reader.ready()){
            chars = reader.read(buffer);
            if(chars != -1){ bld.append(buffer, 0, chars); }
        }
        reader.close();
        connection.disconnect();
        //String[] answerArray = bld.toString().split("(<42>)|(<128>)");
        String[] answerArray = bld.toString().split(String.format("(%1$s)|(%2$s)", FIELDS_DELIMITER, RECORDS_DELIMITER));
        for(String el : answerArray){ list.add(el); }

        return list;
    }

    protected static Result createNewNote(String userID, String deviceID){
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "newNote");
        map.put("userID", userID);
        map.put("deviceID", deviceID);

        List<String> list = getServerAnswer("createNewNote", map);
        String str = list.get(0);

        return new Result(str);
    }

    // обновление заметки
    // В случае успеха вернёт 1.
    // В случае провала - null
    //protected static String updateNote(String userID, String deviceID, String note, Date timeStamp){
    protected static Result updateNote(String userID, String deviceID, String note, Date timeStamp){
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "updateNote");
        map.put("userID", userID);
        map.put("deviceID", deviceID);

        // контроль длины заметки
        if(note.length() > Controller.MAX_CHARS_IN_NOTE){ note = note.substring(0, Controller.MAX_CHARS_IN_NOTE); }
        // чтобы заметка была не пустая.
        if(note.length() == 0){ note = "Print note here."; }
            /* все поля, которые соделжат пробелы и всякие прочие символы (включая национальные алфавиты)
            должны быть закодированы для нормальной передачи методами Get и Post */
        try {
            //map.put("note", URLEncoder.encode(note, "UTF-8"));
            map.put("note", URLEncoder.encode(backSlashReplace(note), Charset.defaultCharset().displayName()));
            map.put("timeStamp", URLEncoder.encode(Tools.Date2String(timeStamp), Charset.defaultCharset().displayName()));
        }catch (UnsupportedEncodingException ex){ System.out.println(ex.toString()); return new Result(DBMessage.VOID); }

        //List<String> list = null;
        List<String> list = getServerAnswer("updateNote", map);

        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }

        String dbStatus = list.get(0);
        //list.remove(0); // удаляем ответ Базы Данных.

        return new Result(dbStatus);
    }

    // обновление заметки
    protected static void updateNoteOLDandWorking(String userID, String deviceID, String note, Date timeStamp){
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "updateNote");
        map.put("userID", userID);
        map.put("deviceID", deviceID);
        /* все поля, которые соделжат пробелы и всякие прочие символы (включая национальные алфавиты)
        должны быть закодированы для нормальной передачи методами Get и Post */
        try {
            //map.put("note", URLEncoder.encode(note, "UTF-8"));
            map.put("note", URLEncoder.encode(note, Charset.defaultCharset().displayName()));
            map.put("timeStamp", URLEncoder.encode(Tools.Date2String(timeStamp), Charset.defaultCharset().displayName()));
        }catch (UnsupportedEncodingException ex){ System.out.println(ex.toString()); return;}
        List<String> list = null;
        try{
            list = sendHTTPRequest(map);
        }catch(ProtocolException ex){ /*скорее всего нет связи с интернетом. вывести инфо в строку статуса. */ return;}
        catch(MalformedURLException ex){ System.out.println(ex.toString()); return;}
        catch(IOException ex){ /*скорее всего нет связи с интернетом. вывести инфо в строку статуса. */ return; }

        int connectionStatus = Integer.parseInt(list.get(0));
        if(connectionStatus >= 400){ System.out.println(String.format("updateNote. HTTP: ошибка №%s.", connectionStatus)); return; }
    }
    //private static void requestToCloud(){}

/*    функция работает через вызов getAllDevicesInfo, но возвращает карту deviceID, deviceInfo, чтобы легче было
    находить информацию об устройстве по его ID*/
    //protected static Map<String, Model.DeviceInfo> getAllDevicesInfoMap(String userID, String deviceID){
    protected static Result getAllDevicesInfoMap(String userID, String deviceID){
        //Model.DeviceInfo[] tempArray = getAllDevicesInfo(userID, deviceID);
        Result temp = getAllDevicesInfo(userID, deviceID);
        Map<String, Model.DeviceInfo> result = new HashMap<String, Model.DeviceInfo>();
        //if(tempArray.length == 0){ return result; }
        if(temp.dbStatus != DBMessage.SUCCESS){ return temp; }

        Model.DeviceInfo[] tempArray = (Model.DeviceInfo[]) temp.content;

        for(int i = 0; i < tempArray.length; i++){
            result.put(tempArray[i].deviceId, tempArray[i]);
        }
        return new Result(temp.dbStatus.name(), result);
    }

    // получить параметры всех устройств круга. Если возвращает массив нулевой длниы, значит попытка связи была неудачной.
    //protected static Model.DeviceInfo[] getAllDevicesInfo(String userID, String deviceID){
    protected static Result getAllDevicesInfo(String userID, String deviceID){
        final int RETURNED_FIELD_NUMBER = 4; // количество возвращаемых полей в запросе
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "getDevices");
        map.put("userID", userID);
        map.put("deviceID", deviceID);
        /* Параметры устройств идут по строкам сплошным потоком без дазбивки по устройствам
        *  Параметры устройств разбиты по строкам. Учесть, что метке устройства тоже возможны пробелы
        *  Параметры идут в следующем порядке: deviceId, MastetFlag, labelTimeStamp, deviceLabel
        *  deviceLabel специально идёт в конце строки!*/

        List<String> list = getServerAnswer("getAllDevicesInfo", map);
        //if(list == null){ return new Model.DeviceInfo[0];  }

        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }

        String dbStatus = list.get(0);

        // Красиво выходим из функции с неудачным статусом
        if(!dbStatus.equals("SUCCESS")){ return new Result(dbStatus); }

        list.remove(0); // удаляем ответ Базы Данных.

        /* Проверяем дист на то, что его количество строк кратно возвращаемому количеству полей.
        Проверка целостности. */
        if(!(list.size() % RETURNED_FIELD_NUMBER == 0)){ return new Result(DBMessage.FIELDS_COUNT_ERROR); }

        // Заполняем результат полями из листа, формируя из них записи
        Model.DeviceInfo[] result = new Model.DeviceInfo[list.size()/RETURNED_FIELD_NUMBER];
        for(int i = 0, j = 0; i < list.size(); i = i + RETURNED_FIELD_NUMBER, j++){
            Model.DeviceInfo device = new Model.DeviceInfo();
            device.deviceId = list.get(i);
            // MasterFlag i+1 пропускаем, то есть, в обще, не передаём и не используем
            device.labelTimeStamp = Tools.String2Date(list.get(i + 2));
            device.deviceLabel = backSlashRestore(list.get(i+3));
            result[j] = device;
        }
        return new Result(dbStatus, result);
    }

    // возвращает TimeStamp и заметку (одна единственная пара в Мапе)
    //protected static Map<Date, String> getNote(String userID, String deviceID){
    protected static Result getNote(String userID, String deviceID){
        Map<String, Date> mapResult = new HashMap<String, Date>();
        final int RETURNED_FIELD_NUMBER = 2; // количество возвращаемых полей в запросе
        // TimeStamp и Note
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "getNote");
        map.put("userID", userID);
        map.put("deviceID", deviceID);

        List<String> list = getServerAnswer("getNote", map);
        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }

        String dbStatus = list.get(0);
        // Красиво выходим из функции с неудачным статусом
        if(!dbStatus.equals("SUCCESS")){ return new Result(dbStatus); }
        list.remove(0); // удаляем ответ Базы Данных.

        /* Проверяем дист на то, что его количество строк кратно возвращаемому количеству полей.
        Проверка целостности. */
        if(!(list.size() % RETURNED_FIELD_NUMBER == 0)){ return new Result(DBMessage.FIELDS_COUNT_ERROR); }
        // в листе: 0 - статус базы. 1 - TimeStamp. а все остальные строки - заметка
        StringBuilder bld = new StringBuilder(list.get(1));
        // в этом цикле собираем заметку в одну строку из массива list
        for(int i = 2; i < list.size(); i++){ bld.append(System.getProperty("line.separator")).append(list.get(i)); }
        Map<Date, String> result = new HashMap<Date, String>();
        result.put(Tools.String2Date(list.get(0)), backSlashRestore(bld.toString()));
        return new Result(dbStatus, result);
    }

    /* Обёрточная функция. На тот случай, если где потребуется запросить TimeStamps вне процедуры синхронизации  */
    protected static Result getTimeStamps(String userID, String deviceID){
        return getTimeStamps(userID, deviceID, false);
    }

    /* Функция возвращает Map Id - TimeStamp для всех синхронизируемых данных,
        где id - идентификатор синхронизируемого поля (заметка, мекта устройства и т. п.),
     * а TimeStamp - TimeStamp :-)
     * Если возвращает null попытка связи с сервером была неудачной*/
    //protected static Map<String, Date> getTimeStamps(String userID, String deviceID){
    protected static Result getTimeStamps(String userID, String deviceID, boolean isSynchronisation){
        Map<String, Date> mapResult = new HashMap<String, Date>();
        final int RETURNED_FIELD_NUMBER = 2; // количество возвращаемых полей в запросе
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "getTimeStamps");
        map.put("userID", userID);
        map.put("deviceID", deviceID);
        map.put("sync", "");
        if(isSynchronisation){ map.put("sync", "true"); }

        /* SUCCESS-nextSyncDelay-ТimeStampsBlocks */
        List<String> list = getServerAnswer("getTimeStamps", map);
        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }

        String dbStatus = list.get(0);
        list.remove(0); // удаляем ответ Базы Данных.

        // Красиво выходим из функции с неудачным статусом
        if(!dbStatus.equals("SUCCESS")){ return new Result(dbStatus); }

        long syncDelay;
        syncDelay = Long.parseLong(list.get(0));
        list.remove(0); // удаляем рекомендуемую задержку синхронизации
        Controller.model.setNextSynchronisationTime(syncDelay);

        /* Проверяем дист на то, что его количество строк кратно возвращаемому количеству полей.
        Проверка целостности. */
        if(!(list.size() % RETURNED_FIELD_NUMBER == 0)){ return new Result(DBMessage.FIELDS_COUNT_ERROR); }
        // Заполняем результат полями из листа, формируя из них записи
        for(int i = 0; i < list.size(); i = i + RETURNED_FIELD_NUMBER){
            mapResult.put(list.get(i), Tools.String2Date(list.get(i+1)));
        }
        return new Result(dbStatus, mapResult);
    }

    /* Функция обновления меток устройств. В мапе ключ - deviceID, значение - новая метка устройства
    * TimeStamp пусть генерируется на стороне сервера, и возвращается в качестве результата в эту функцию
    * одним значением на все обновлённые метки */
    protected static Result updateDevecesLabels(String userID, String deviceID, Map<String, String> newLabels){
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "updateLabels");
        map.put("userID", userID);
        map.put("deviceID", deviceID);

        try {
            for (Map.Entry<String, String> pair : newLabels.entrySet()) {
                // контроль длины метки
                if(pair.getValue().length() > Controller.MAX_CHARS_IN_LABEL){ pair.setValue(pair.getValue().substring(0, Controller.MAX_CHARS_IN_LABEL)); }
                // если устройство есть, но метки нет, вставляем метку по умолчанию.
                if(pair.getValue().equals("")){ pair.setValue("Print device label here."); }
                //здесь  в мапу вводятся пары ID - newLabel (отдельными параметрами, так как метка может содержать пробелы)
                map.put(pair.getKey(), URLEncoder.encode(backSlashReplace(pair.getValue()), Charset.defaultCharset().displayName()));
            }
            // параметр содержит ID устройств, у которых обновляется метка (через пробелы)
        }catch(UnsupportedEncodingException ex){ System.out.println(ex.toString()); return new Result(DBMessage.VOID); }

        List<String> list = getServerAnswer("updateDevecesLabels", map);
        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }

        // Красиво выходим из функции с неудачным статусом
        if(!list.get(0).equals("SUCCESS")){ return new Result(list.get(0)); }

        return new Result(list.get(0), Tools.String2Date(list.get(1)));
    }

    /* Удаление пароля из базы данных */
    protected static Result delPass(String userID, String deviceID){
        return setPassForNewDevice(userID, deviceID, "");
    }

    /* Функция заброски пароля в БД, для подключения нового устройства. Если пароль "", значит удаляем его из БД */
    protected static Result setPassForNewDevice(String userID, String deviceID, String newPass){
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "setPass");
        map.put("userID", userID);
        map.put("deviceID", deviceID);
        map.put("devicePass", newPass);

        List<String> list = getServerAnswer("setPassForNewDevice", map);
        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }

        return new Result(list.get(0));
    }

    /* Функция исключения устройства из круга */
    protected static Result kickDeviceFromCircle(String userID, String deviceID, String pariahID){
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "kickDevice");
        map.put("userID", userID);
        map.put("deviceID", deviceID);
        map.put("pariahID", pariahID);

        List<String> list = getServerAnswer("kickDeviceFromCircle", map);
        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }

        return new Result(list.get(0));
    }

    /* Функция включения устройства в круг
    * Возвращает userID*/
    protected static Result addDeviceInCircle(String userID, String deviceID, String invitePass){
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "addDevice");
        map.put("userID", userID);
        map.put("deviceID", deviceID);
        map.put("invitePass", invitePass);

        List<String> list = getServerAnswer("addDeviceInCircle", map);
        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }
        // добавляем пустую строку (в случае неудачи), чтобы был нормальный выход из функции
        if(!list.get(0).equals("SUCCESS")){ list.add(""); }
        // Возвращаем статус БД и userID
        return new Result(list.get(0), list.get(1));
    }

    /* Возвращает последнюю версию программы с сервера - значение list(0) */
    protected static Result getLastProgramVer(){
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "getLastVer");

        List<String> list = getServerAnswer("getLastProgramVer", map);
        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }
        // добавляем пустую строку (в случае неудачи), чтобы был нормальный выход из функции
        if(!list.get(0).equals("SUCCESS")){ list.add(""); }
        // Возвращаем статус БД и версию
        return new Result(list.get(0), list.get(1));
    }

    /* Отправка статистики на сервер */
    protected static Result sendStatistics(Map<String, String> data){
        Map<String, String> map = new HashMap<String, String>();
        map.put("task", "sendStat");
        map.putAll(data);

        List<String> list = getServerAnswer("sendStatistics", map);
        if(list == null){ return new Result(DBMessage.SERVER_CONNECTION_ERROR); }
        // добавляем пустую строку (в случае неудачи), чтобы был нормальный выход из функции
        if(!list.get(0).equals("SUCCESS")){ list.add(""); }
        // Возвращаем статус БД и версию
        return new Result(list.get(0), list.get(1));
    }

    /* Вспомогательная подпрограмма. Если возвращает null, попытка связи с сервером была неудачной.
     * В случае удачи, возвращает List<String> содержащий построчный ответ сервера без статусных первых строк
     * (HTTP-статус и DB-статус)
     * В случае неудачи ДОЛЖНА выдать сообщения в статусную строку программы без краха.
     * Ещу требует в этом смысле доработки.
     * Первый параметр - имя вызвавшей функции. Можно, конечно запускать трассировку, в случае ошибки. Но это на будущее */
    private static List<String> getServerAnswer(String subName, Map<String, String> requestMap){

        List<String> list = null;
        try{
            list = sendHTTPRequest(requestMap);
        }catch(ProtocolException ex){ /*скорее всего нет связи с интернетом. вывести инфо в строку статуса. */ return null;}
        catch(MalformedURLException ex){ System.out.println(ex.toString()); return null; }
        catch(IOException ex){ /*скорее всего нет связи с интернетом. вывести инфо в строку статуса. */ return null; }

        int connectionStatus = Integer.parseInt(list.get(0));
        if(connectionStatus >= 400){ System.out.println(String.format("%s. HTTP: ошибка №%s.", subName, connectionStatus)); return null; }
        // Сообщения БД: Круг не найден (нет такого пользователя), доступа в круг нет, успех
        //DBMessages dbStatus = DBMessages.valueOf(list.get(1));

        // Удаляем из результата запроса его статусы (HTTP - статус запроса, и DB-статус запроса)
        list.remove(0); //list.remove(0); Ответ базы данных не удаляем!
        return list;
    }

    // заменяем в строке бэкслэши на маркер
    private static String backSlashReplace(String str){ return str.replaceAll("\\\\", "<92>"); }

    // заменяем в строке маркеры на бэкслэши
    private static String backSlashRestore(String str){ return str.replaceAll("<92>", "\\\\"); }

    enum DBMessage{
        ACCESS_DENIED, // Доступ запрещён
        CIRCLE_NOT_FOUND, // Круг не найден
        SUCCESS, // Успешно
        VOID, // Неопределённое значение
        FIELDS_COUNT_ERROR, // Ошибка в количестве возвращённых в запросе строк
        SERVER_CONNECTION_ERROR // Ошибка связи с сервером, в том числе HTTP - ошибки
    }

    protected static class Result{
        DBMessage dbStatus;
        Object content;

        protected Result(String dbStatus, Object content){ this.dbStatus = DBMessage.valueOf(dbStatus); this.content = content; }
        protected Result(String dbStatus){ this.dbStatus = DBMessage.valueOf(dbStatus); this.content = null; }
        protected Result(DBMessage message){ this.dbStatus = message; }
    }
}

/*-------------------------------------------------------------------------------------------------------------------*/
/*-------------------------------------------------------------------------------------------------------------------*/
/*                                Класс проверки интернет соединения. Статические методы.                            */
/*-------------------------------------------------------------------------------------------------------------------*/
/*-------------------------------------------------------------------------------------------------------------------*/
class InternetConnectionTest{
    private static final String[] PING_URLS = {"http://www.yandex.ru/", "http://cn.gremal.ru/test.html"};
    //private static final String[] PING_URLS = {"http://www.yandex.ru/", "http://192.185.236.166/~gremal/cn/test.html"};

    /* Интерфейсная функция. Проверка доступности облака и интернета через неё. */
    protected static InternetConnectionMessage isCloudReachable(){
        /* Доступа в интернет нет (оба сайта недоступны), Облако недоступно (первый доступен, второй - нет),
         * связь Ок - Облако доступно */
        boolean result1 = isCloudReachable(PING_URLS[0]);
        boolean result2 = isCloudReachable(PING_URLS[1]);
        //if(result2){ return InternerConnectionMessage.YES; }
        if(!result1 && !result2){
            return InternetConnectionMessage.NO;
        }
        if(result1 && !result2){
            return InternetConnectionMessage.CLOUD_NOT_FOUND;
        }
        return InternetConnectionMessage.YES;
    }

    /* Пинг. Функция сигнализирует, доступно ли облако с этого устройства. Используется для проверки доступа в Интернет,
     * например. Для теста используются несколько, хостов */
    private static boolean isCloudReachable(String urlString){
        for(int i = 1; i <= 3; i++) {
            //Трёккратная проверка связи, в случае неудачи.
                try {
                    //make a URL to a known source
                    URL url = new URL(urlString);

                    //open a connection to that source
                    HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();

                    Object objData = null;
                    //trying to retrieve data from the source. If there
                    //is no connection, this line will fail
                    objData = urlConnect.getContent();

                    // todo где-то тут затревает нить в Андроиде.

                    break; // результат положительный - выходим из цикла

                } catch (Exception e) {
                    //e.printStackTrace();
                    if(i == 3) {return false;} // три попытки были неудачными - облако недоступно
                }
        }

        return true;
    }
}

/*-------------------------------------------------------------------------------------------------------------------*/
/*-------------------------------------------------------------------------------------------------------------------*/
/*       Параллельная отдельная нить, которая существует с начала запуска программы и умирает с её концом            */
/*       В рамках этой нити происходит периодическое тестирование интернет-коннекта и доступности облака             */
/*                   Включение/выключение ведётся с помощью методов нижеследующего контроллера                       */
/*-------------------------------------------------------------------------------------------------------------------*/
/*-------------------------------------------------------------------------------------------------------------------*/

/* Отдельный поток, который подрачивает наличие связи
* Как только gui исчезает, данная нить завершается */
class TestInternetConnectionThread extends Thread
{
    private static final long TEST_CONNECTION_DELAY = 15000;
    UISide gui;
    TestController controller;

    public void run(){
        // Запуск нити производится, когда начинает существовать gui
        if(gui != null) {
            // Нить живёт, пока нет команты на прекращение работы
            while (!gui.isFinishAllThreadsTrue()) {
                //System.out.println(String.format("Проверка наличия несинхронизированных данных - %s", Controller.model.isWasChangedTrue()));
                // Тестируем связь, только если есть несинхронизированные данные
                if (Controller.model.isWasChangedTrue()) {
                    //if(gui.getIsNoteWasChanged()||gui.getIsDeveceLabelWasChanged()){
                    //System.out.println("Тестируем соединение.");
                    this.controller.testConnection();
                }
                try {
                    Thread.sleep(TEST_CONNECTION_DELAY);
                } catch (InterruptedException ignore) { /*NOP Отработать!*/ }
            }
        }
    }

    protected TestInternetConnectionThread(UISide gui) {
        this.gui = gui;
        this.controller = new TestController();
    }
}

/*-------------------------------------------------------------------------------------------------------------------*/
/*-------------------------------------------------------------------------------------------------------------------*/
/*           Контроллер, включающий/выключающий тестирование связи с помощью методов wait() и notify()               */
/*-------------------------------------------------------------------------------------------------------------------*/
/*-------------------------------------------------------------------------------------------------------------------*/

class TestController{
    // статус процесса тестирования связи
    private volatile boolean isPause = false;

    // собственно, функция, которая вызывает продпрограмму тестирования
    synchronized protected void testConnection(){
        // Раскомметировать для реального запуска тестирования связи
        InternetConnectionMessage message = InternetConnectionTest.isCloudReachable();
        // если только gui уже готов для работы, тогда и заполняем статусное сообщение.
        if(Controller.gui != null) {
            if (Controller.gui.getReady()) {
                Controller.gui.setInternetConnectionStatuses(message);
            }
        }
        //System.out.println(message);

        try {
            while (isPause) { this.wait(); }
        }catch(InterruptedException ignore){
            //if(Controller.gui != null){  }
                /* NOP */
        }
    }

    // возобновить процесс периодического тестирования. Включатель.
    synchronized protected void wakeUp(){
        this.isPause = false;
        this.notify();
    }

    // остановить процесс тестирования. Выключатель.
    synchronized protected void pause(){ this.isPause = true; }

    synchronized protected boolean isPause(){return this.isPause;}
}