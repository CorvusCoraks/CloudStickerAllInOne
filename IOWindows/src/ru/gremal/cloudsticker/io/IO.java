package ru.gremal.cloudsticker.io;

import ru.gremal.cloudsticker.common.interfaces.IOSpecific;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Map;

/**
 * Класс функций ввода-вывода (связи ядра с постоянной памятью, интернетом и т. п.). Т. е. функций, специфичных для данной операционной системы
 * Windows implementation
 */
public class IO implements IOSpecific {
    // карта пар "имя объекта (параметра) - ссылка на объект
    // инициализирует параметры объекта
    @Override
    public void setBundle(Map<String, Object> bundleMap){}

    @Override
    public FileOutputStream FileOutputStream(String path, String fileName) throws FileNotFoundException{
        return new FileOutputStream(new StringBuilder(path).append(fileName).toString());
    }

    @Override
    public FileInputStream FileInputStream(String path, String fileName) throws FileNotFoundException{
        return new FileInputStream(new StringBuilder(path).append(fileName).toString());
    }

    @Override
    public File File(String fileName){
        return new File(fileName);
    }
}
