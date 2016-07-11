package ru.gremal.cloudsticker.common.interfaces;

import ru.gremal.cloudsticker.common.tools.StatusSender;
import ru.gremal.cloudsticker.common.tools.InternetConnectionMessage;
import ru.gremal.cloudsticker.common.ui.AbstractUIControl;

/**
 * Программный интерфейс для связи Ядра с Пользовательским Интерфейсом.
 *
 * Сам пользовательский интерфейс находится в модуле UI.jar,
 * в пакете ru.gremal.cloudsticker.ui,
 * в классе GUI extended AbstractUI, с обязательным конструктором GUI(CoreSide core)
 *
 */
public interface UISide {
    /* Функция проверки связи для вызова из внешних модулей. Формирование статусной строки в GUI,
    запуск процедур активации и деактивации элементов GUI, в зависимости от наличия или отсутствия связи */
    public void setInternetConnectionStatuses(InternetConnectionMessage status);
    /* Функция читает планируемые параметры GUI из соответствующего Мэпа контроллера */
    public void setInitGUIParameters();
    public boolean getReady();
    public void setReady(boolean value);
    // Получить ссылку на текстовое поле с именем данного устройства
    public AbstractUIControl getThisDeviceTextField();
    // Получить ссылку на кнопку "Сохранить" название данного устройства
    public AbstractUIControl getThisDeviceButton();
    // Получить массив ссылок на текстовые поля других устройств круга
    public AbstractUIControl[] getOtherCircleDevicesTextField();
    // получить массив ссылок на кнопки "Пригласить - Выгнать"
    public AbstractUIControl[] getOtherCircleDevicesButton();
    // Получить ссылку на текстовую область с заметкой
    public AbstractUIControl getNoteTextArea();
    // возвращает ссылку на текстовое поле по ссылке на спаренную кнопку
    public AbstractUIControl getTextPaired(AbstractUIControl button);
    // Ссылка на текстовое поле с пригласительным паролем
    public AbstractUIControl getInvitationTextField();
    // возвращает свободное текстовое поле, в GUI-таблице устройств круга. Если null - свободных полей нет
    public AbstractUIControl getFreeOtherDeviceTextField() throws InterruptedException;
    // возвращает ссылку на кнопку по парному текстовому полю
    public AbstractUIControl getButtonByTextField(AbstractUIControl auic);
    // инвертируем текст кнопок Kick/Invite
    public void invertTextOnButton(AbstractUIControl auic);
    /*    Чистка свободного текстового поля в GUI-массиве устройств круга и, если на парной кнопке осталась старая
        надпись Kick, меняем её на Invite */
    public void clearFreeTextField() throws InterruptedException;
    // вставить новый статус в массив статусов
    public void putNewStatusInStatusString(StatusSender sender, String status);
    // вставить новый статус в массив статусов, с указанием количества показов.
    // После указанного количества показов, статус удаляется из массива показов.
    public void putNewStatusInStatusString(StatusSender sender, String status, int showCount);
    public String getLocalisationValueByKey(String key);
    /* Сделать доступными элементы окна программы */
    public void setUIEnable();
    /* Сделать недоступными элементы окна программы */
    public void setUIDisable();
    // Дана ли команда на завершение всех нитей
    public boolean isFinishAllThreadsTrue();
}
