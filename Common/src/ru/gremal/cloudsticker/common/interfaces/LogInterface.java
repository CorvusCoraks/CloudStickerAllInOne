package ru.gremal.cloudsticker.common.interfaces;

/**
 * Универсальный интерфейс логирования.
 * Необходим, чтобы ядро могло пользоваться, возможно, более удобными сервисами, специфичными для конкретной OS,
 * а не только, стандартными методами java
 */
public interface LogInterface {
    // выдать в лог строку с информацией
    public void publishToLog(String string);
}
