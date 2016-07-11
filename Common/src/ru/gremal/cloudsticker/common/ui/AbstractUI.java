package ru.gremal.cloudsticker.common.ui;

import ru.gremal.cloudsticker.common.interfaces.UISide;

/**
 * Буферный класс междё реализацией пользовательского интерфейса и программным интерфейсом UISide
 *
 * зачем так много вызовов функций, которые дублированы со стороны ядра????
 * зачем они нужны, если можно пользоваться вызовами интерфейса ядра?
 */
public abstract class AbstractUI implements UISide {
    // Количество устройств в круге. Устанавливается модулем Core путём вызова setMaxCompanyCount
    private int MAX_COMPANY_COUNT;
    // Максимальное количество символов в заметке
    private int MAX_CHARS_IN_NOTE;
    private int MAX_CHARS_IN_LABEL;
    private int MAX_CHARS_IN_INVITATION_PASS;
    // версия ядра
    private float CORE_VERSION;
    // версия UI
    private float UI_VERSION;
    // статус готовности UI.
    // Используется, например, при запуске ядра, чтобы дать знать ядру, что пользовательский интерфейс готов принимать,
    // и давать какие-либо сигналы от/к ядру. То есть, готов взаимодействовать с ядром
    private boolean uiReady = false;
    // Программа завершает работу. Завершить все нити-демоны.
    private boolean finishAllThreads = false;

    protected final int FIELD_CHANGE_TIMEOUT = 5000;

    //todo на фига оно надо, если в CoreSide есть метод getMaxCompanyCount();
    protected void setMaxCompanyCount(int MaxCompanyCount) {
        this.MAX_COMPANY_COUNT = MaxCompanyCount;
    }

    //todo на фига оно надо, если в CoreSide есть метод getMaxCompanyCount();
    protected int getMaxCompanyCount(){
        return this.MAX_COMPANY_COUNT;
    }

    //todo на фига оно надо, если в CoreSide есть метод getMaxCharsInNote()
    protected void setMaxCharsInNote(int maxChars) {
        this.MAX_CHARS_IN_NOTE = maxChars;
    }

    //todo на фига оно надо, если в CoreSide есть метод getMaxCharsInNote()
    protected int getMaxCharsInNote(){ return this.MAX_CHARS_IN_NOTE; }

    //todo на фига оно надо, если в CoreSide есть метод getMaxCharsInLabel();
    protected int getMaxCharsInLabel() {
        return this.MAX_CHARS_IN_LABEL;
    }

    //todo на фига оно надо, если в CoreSide есть метод getMaxCharsInLabel();
    protected void setMaxCharsInLabel(int count) {
        this.MAX_CHARS_IN_LABEL = count;
    }

    //todo на фига оно надо, если в CoreSide есть метод getMaxCharsInInvitationPass()
    protected int getMaxCharsInInvitationPass() {
        return this.MAX_CHARS_IN_INVITATION_PASS;
    }

    //todo на фига оно надо, если в CoreSide есть метод getMaxCharsInInvitationPass()
    protected void setMaxCharsInInvitationPass(int count) {
        this.MAX_CHARS_IN_INVITATION_PASS = count;
    }

    //todo на фига оно надо, если в CoreSide есть метод getCoreVersion
    protected void setCoreVersion(float ver) {
        this.CORE_VERSION = ver;
    }

    protected void setUIVersion(float ver){ this.UI_VERSION = ver; }

    //todo на фига оно надо, если в CoreSide есть метод getCoreVersion
    protected float getCoreVersion(){ return this.CORE_VERSION; }

    protected float getUIVersion(){ return this.UI_VERSION; }

    @Override
    public boolean getReady(){ return this.uiReady; }

    @Override
    public void setReady(boolean value){ this.uiReady = value;}

    public void setFinishAllThreadsTrue(){
        this.finishAllThreads = true;
    }

    @Override
    public boolean isFinishAllThreadsTrue(){
        return this.finishAllThreads;
    }

}
