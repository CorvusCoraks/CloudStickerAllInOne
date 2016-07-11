package ru.gremal.cloudsticker.common.ui;

/**
 * Интерфейс текстового поля для работы с элементами UI (ведь мы не знаем изначально, что за UI нам предоставлен).
 * Главное, что мы знаем, что данный интерфейс актуален для всех, необходимых нам, элементов управления.
 *
 * Если элемент управления:
 * - редактируемое поле, то setSText(UiTextId id) реализуется пустой функцией (объявить в реализации @Deprecated),
 * а setSText(String text) - рабочий метод
 * - нередактируемый (кнопка, метка), то setSText(String text) реализуется пустой функцией (объявить в реализации @Deprecated),
 * а setSText(UiTextId id) - рабочий метод
 */
public interface AbstractUIControl {
    public void setSText(String text); // для работы только с редактируемыми элементами UI
    public void setSText(UiTextId id); // для работы только с нередактируемыми элементами UI
    public String getSText();
    public void setSEnable();
    public void setSDisable();
    public boolean isSEnable();
}
