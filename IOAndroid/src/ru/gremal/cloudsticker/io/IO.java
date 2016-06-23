package ru.gremal.cloudsticker.io;

import android.content.Context;
import ru.gremal.cloudsticker.common.interfaces.IOSpecific;

import java.io.*;
import java.util.Map;

/**
 * Класс функций ввода-вывода (связи ядра с постоянной памятью, интернетом и т. п.). Т. е. функций, специфичных для данной операционной системы
 * Android implementation
 */
public class IO implements IOSpecific {
    private static Context context;

    public IO(Context context){
        this.context = context;
    }
    // карта пар "имя объекта (параметра) - ссылка на объект
    // инициализирует параметры объекта
    @Override
    public void setBundle(Map<String, Object> bundleMap){
        // "context"
        //this.context = (Context) bundleMap.get("context");
    }

    @Override
    public FileOutputStream FileOutputStream(String path, String fileName) throws FileNotFoundException{
        return this.context.openFileOutput(fileName, Context.MODE_PRIVATE);
    }

    @Override
    public FileInputStream FileInputStream(String path, String fileName) throws FileNotFoundException{
        return this.context.openFileInput(fileName);
    }

    @Override
    public File File(String fileName){
        return new File(context.getFilesDir(), fileName);
    }
}
