package ru.gremal.cloudsticker.common.interfaces;

import ru.gremal.cloudsticker.common.tools.CommonTools;
import ru.gremal.cloudsticker.common.ui.AbstractUIControl;

import java.util.logging.FileHandler;
import java.util.logging.Level;

/**
 * Интерфейс ядра программы.
 */
public interface CoreSide {
    public float getCoreVersion();
    //public void testCS();
    public void setNoteWasChangedFlagToTrue();
    public void setDeviceLabelWasChangedFlagToTrue(AbstractUIControl textField);
    public boolean isWasChangedTrue();
    public void jerkThreadWakeUp();
    public void jerkThreadSleep();
    public boolean isJerkThreadActive();
    public void enterToCircleButtonPressed();
    public void startSynchronization() throws InterruptedException;
    public void inviteOrKickButtonPressed(AbstractUIControl button);
    public boolean isTextFieldFree(AbstractUIControl textField);
    public void writeInit();
    public String getPairFromIniDataMap(String key);
    public void putPairToIniDataMap(String key, String value);
    public boolean getReady();
    public LogInterface getLogFileService();
    /* Установить максимальное количество членов круга */
    public int getMaxCompanyCount();
    /* Установить максимальное количество символов в заметке */
    public int getMaxCharsInNote();
    public int getMaxCharsInLabel();
    public int getMaxCharsInInvitationPass();
}
