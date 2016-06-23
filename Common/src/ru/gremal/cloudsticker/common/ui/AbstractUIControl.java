package ru.gremal.cloudsticker.common.ui;

/**
 * Интерфейс текстового поля для работы с элементами UI (ведь мы не знаем изначально, что за UI нам предоставлен).
 * Главное, что мы знаем, что данный интерфейс актуален для всех, необходимых нам, элементов управления.
 */
public interface AbstractUIControl {
    public void setSText(String text);
    public String getSText();
    public void setSEnable();
    public void setSDisable();
    public boolean isSEnable();
}
