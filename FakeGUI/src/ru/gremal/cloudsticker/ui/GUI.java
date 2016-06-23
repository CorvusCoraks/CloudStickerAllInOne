package ru.gremal.cloudsticker.ui;

import ru.gremal.cloudsticker.common.tools.InternetConnectionMessage;
import ru.gremal.cloudsticker.common.tools.StatusSender;
import ru.gremal.cloudsticker.common.ui.AbstractUI;
import ru.gremal.cloudsticker.common.ui.AbstractUIControl;

/**
 * Фэйковый пользовательский интекфейс, необходимый, если реальный интерфейс инициализируется раньше ядра,
 * а само ядро инициализируется через класс CoreDetonator.
 *
 * Необходим для работоспособности, так как в ядре изначально зашиты ссылки на некий класс GUI
 */
public class GUI extends AbstractUI {
    @Override
    public void setInternetConnectionStatuses(InternetConnectionMessage status) {

    }

    @Override
    public void setInitGUIParameters() {

    }

    @Override
    public AbstractUIControl getThisDeviceTextField() {
        return null;
    }

    @Override
    public AbstractUIControl getThisDeviceButton() {
        return null;
    }

    @Override
    public AbstractUIControl[] getOtherCircleDevicesTextField() {
        return new AbstractUIControl[0];
    }

    @Override
    public AbstractUIControl[] getOtherCircleDevicesButton() {
        return new AbstractUIControl[0];
    }

    @Override
    public AbstractUIControl getNoteTextArea() {
        return null;
    }

    @Override
    public AbstractUIControl getTextPaired(AbstractUIControl button) {
        return null;
    }

    @Override
    public AbstractUIControl getInvitationTextField() {
        return null;
    }

    @Override
    public AbstractUIControl getFreeOtherDeviceTextField() throws InterruptedException {
        return null;
    }

    @Override
    public AbstractUIControl getButtonByTextField(AbstractUIControl auic) {
        return null;
    }

    @Override
    public void invertTextOnButton(AbstractUIControl auic) {

    }

    @Override
    public void clearFreeTextField() throws InterruptedException {

    }

    @Override
    public void putNewStatusInStatusString(StatusSender sender, String status) {

    }

    @Override
    public void putNewStatusInStatusString(StatusSender sender, String status, int showCount) {

    }

    @Override
    public String getLocalisationValueByKey(String key) {
        return null;
    }

    @Override
    public void setUIEnable() {

    }

    @Override
    public void setUIDisable() {

    }

    public static class UIDetonator{

    }
}
