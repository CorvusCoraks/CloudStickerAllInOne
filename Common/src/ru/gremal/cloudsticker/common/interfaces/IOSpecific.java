package ru.gremal.cloudsticker.common.interfaces;

import java.io.*;
import java.util.Map;

/**
 * Шаблон специфичных функций ввода вывода. Реализация в отдельном модуле IO.jar.
 * Реализация данного интерфейса индивидуальна для каждой операционной системы, в частности для Андроид и Windows.
 *
 * todo проверить, используется ли данный класс где-нибудь вобще
 */
public interface IOSpecific {
    public void setBundle(Map<String, Object> bundleMap);
    public FileOutputStream FileOutputStream(String path, String fileName) throws FileNotFoundException;
    public FileInputStream FileInputStream(String path, String fileName) throws FileNotFoundException;
    public File File(String fileName);
}
