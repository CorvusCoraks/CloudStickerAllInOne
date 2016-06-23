package ru.gremal.cloudsticker.core;

import ru.gremal.cloudsticker.common.interfaces.CoreSide;
import ru.gremal.cloudsticker.common.interfaces.LogInterface;
import ru.gremal.cloudsticker.common.tools.CommonTools;
import ru.gremal.cloudsticker.common.ui.AbstractUIControl;

/**
 * Created by GreMal on 07.02.2016.
 * В данном модуле собраны функции, которые вызываются внешним модулем с UI. Фактически - интерфейсные функции.
 */
public class CoreSideImplementation implements CoreSide {
    /* Todo Tools.readFromIniFile(locFileName)*/

    /* Получить пару "ключ-значение" из iniData (сохраняемые данные между запусками программы)
    * Если искомый ключ отстутствует в iniData, то функция возвращает null */
    @Override
    public String getPairFromIniDataMap(String key){
        if(!getReady()){ return null; }
        if(Controller.model.iniData.containsKey(key)){
            return Controller.model.iniData.get(key);
        }
        return null;
    }
    /* Записать пару "ключ-значение" в iniData (сохраняемые данные между запусками программы)
    */
    @Override
    public void putPairToIniDataMap(String key, String value){
        if(!getReady()){ return; }
        Controller.model.iniData.put(key, value);
    }

    // Получить статус готовности модели
    @Override
    public boolean getReady(){ return Controller.model.getReady(); }
    // protected void setReady(boolean value){ Con }
    protected static void setUIDisable(){ Controller.gui.setUIDisable(); };
    @Override
    public void startSynchronization() throws InterruptedException { Controller.model.startSynchronization(); };
    @Override
    public void writeInit(){ Controller.model.writeInit(); };
    protected static void setUIEnable(){ Controller.gui.setUIEnable(); };
    @Override
    public boolean isTextFieldFree(AbstractUIControl textField){ return Controller.model.isTextFieldFree(textField); }
    @Override
    public boolean isWasChangedTrue(){ return Controller.model.isWasChangedTrue(); }
    @Override
    public void enterToCircleButtonPressed(){ Controller.model.EnterToCircleButtonPressed(); }
    protected static boolean isDeviceLabelEquals(AbstractUIControl auic){ return Controller.model.isDeviceLabelEquals(auic); }
    @Override
    public void setDeviceLabelWasChangedFlagToTrue(AbstractUIControl textField){ Controller.model.setDeviceLabelWasChangedFlagToTrue(textField); }
    protected static boolean isNoteEquals(String str){ return isNoteEquals(str); }
    @Override
    public void setNoteWasChangedFlagToTrue(){ Controller.model.setNoteWasChangedFlagToTrue(); }
    @Override
    public void inviteOrKickButtonPressed(AbstractUIControl button){ Controller.model.InviteOrKickButtonPressed(button); }
    @Override
    public LogInterface getLogFileService(){ return Controller.logFileService; }
    @Override
    public float getCoreVersion(){
        return Controller.PROGRAM_VERSION;
    }
    @Override
    public void jerkThreadWakeUp(){Controller.jerkThread.controller.wakeUp();}
    @Override
    public void jerkThreadSleep(){Controller.jerkThread.controller.pause();}
    @Override
    public boolean isJerkThreadActive(){return Controller.jerkThread.controller.isPause();}
    @Override
    public int getMaxCompanyCount() {
        return Controller.model.MAX_COMPANY_COUNT;
    }
    @Override
    public int getMaxCharsInNote() { return Controller.MAX_CHARS_IN_NOTE; }
    @Override
    public int getMaxCharsInLabel() {return Controller.MAX_CHARS_IN_LABEL;}
    @Override
    public int getMaxCharsInInvitationPass(){return Controller.CHARS_IN_INVITATION_PASS; }
}
