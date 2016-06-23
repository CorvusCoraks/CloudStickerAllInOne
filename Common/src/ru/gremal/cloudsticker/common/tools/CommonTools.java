package ru.gremal.cloudsticker.common.tools;

import ru.gremal.cloudsticker.common.interfaces.CoreSide;
import ru.gremal.cloudsticker.common.interfaces.IOSpecific;
import ru.gremal.cloudsticker.common.interfaces.LogInterface;
import ru.gremal.cloudsticker.io.IO;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * Created by Пользователь on 07.03.2016.
 */
public class CommonTools {
    // интервал sleep в случае ожидания какого-либо события
    // Во внешних модулях вызовы производятся исключительно с префиксом в виде имени класса
    public static final int SLEEP_INTERVAL = 50; // миллисекунд
    /* функция чтения в Map данных из ini-файла */
    public  static Map<String, String> readFromIniFile(IOSpecific io, String fileName) throws IOException {
        Map<String, String> map = new HashMap<String, String>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(io.FileInputStream("", fileName)));
        } catch (FileNotFoundException e) {
            io.File(fileName).createNewFile();
            reader = new BufferedReader(new InputStreamReader(io.FileInputStream("", fileName)));
        }
        String str = "";
        while((str = reader.readLine()) != null){
            String[] temp = str.split("=");
            if(temp.length == 0){ continue; } // строку не удалось разбить на две части.
            map.put(temp[0].trim(), temp[1].trim());
        }
        reader.close();

        return map;
    }

    public static class LogFileService implements LogInterface{
        private FileHandler fh = null;
        private String logFilePattern;
        private int logFileSize;
        private int logFilesNumber;
        private boolean logFileAppend;
        private Level logLevel;

        public static LogFileService createInstance(String logFilePattern, int logFileSize, int logFilesNumber, boolean logFileAppend, Level logLevel) throws IOException{
            if(logLevel == Level.OFF){
                // Запись логов отключена
                LogFileService result = new LogFileService();
                result.logFilePattern = logFilePattern;
                result.logFileSize = logFileSize;
                result.logFilesNumber = logFilesNumber;
                result.logFileAppend = logFileAppend;
                result.logLevel = logLevel;
                return result;
            }else{
                // Ведётся запись лога
                return new LogFileService(logFilePattern, logFileSize, logFilesNumber, logFileAppend, logLevel);
            }
        }

        // Конструктор на случай отключения логирования
        private LogFileService() throws IOException {}

        private LogFileService(String logFilePattern, int logFileSize, int logFilesNumber, boolean logFileAppend, Level logLevel) throws IOException{
            //delLogFiles();
            //super(logFilePattern, logFileSize, logFilesNumber, logFileAppend);
            this.fh = new FileHandler(logFilePattern, logFileSize, logFilesNumber, logFileAppend);
            this.fh.setFormatter(new SimpleFormatter());
            this.fh.setLevel(logLevel);
            //this.delFilePattern = delFilePattern;
        }

        public void setLevel(Level newLevel) throws IOException{
            this.logLevel = newLevel;
            if(this.fh == null){
                this.fh = new FileHandler(this.logFilePattern, this.logFileSize, this.logFilesNumber, this.logFileAppend);
                this.fh.setFormatter(new SimpleFormatter());
            }
            this.fh.setLevel(newLevel);
        }

        @Override
        public void publishToLog(String message){
            try {
                publishToLogFile(message);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        public void publishToLogFile(String message) throws ClassNotFoundException {
            //if(core == null){ return; }
            if(this.fh == null){ return; } // запись лога производится не будет
            String fs = " , " ;
            //FileHandler fh = core.getLogFileHandler();
            //Level l = core.getLogLevel();
            StackTraceElement[] steArray= Thread.currentThread().getStackTrace();
            StackTraceElement ste = steArray[2];
            StringBuilder builder = new StringBuilder(">> ")
                    .append(Class.forName(ste.getClassName()).getPackage().toString())
                    .append(fs).append(ste.getClassName().toString())
                    .append(fs).append(ste.getMethodName().toString())
                    .append(fs).append(message)
                    .append(System.getProperty("line.separator"));
            this.fh.publish(new LogRecord(this.fh.getLevel(), builder.toString()));
        };

        // перед созданием нового объекта, можно/нужно запустить эту функцию для удаления предыдущих лог-файлов
        public static void delLogFiles(String delFilePattern){
            File fileName;
            //SimpleFormatter
            int i = 0;
            while((fileName = new File(String.format(delFilePattern, i++))).exists()){
                fileName.delete();
            }

            i = 0;
            while((fileName = new File(String.format(new StringBuilder(delFilePattern).append(".lck").toString(), i++))).exists()){
                fileName.delete();
            }
        }

        public void close(){
            if(this.fh != null){this.fh.close();}
        }
    }
}
