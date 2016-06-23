package ru.gremal.cloudsticker.common.ui;

import ru.gremal.cloudsticker.common.interfaces.UISide;

/**
 * Буферный класс междё реализацией пользовательского интерфейса и программным интерфейсом UISide
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

    protected void setMaxCompanyCount(int MaxCompanyCount) {
        this.MAX_COMPANY_COUNT = MaxCompanyCount;
    }

    protected int getMaxCompanyCount(){
        return this.MAX_COMPANY_COUNT;
    }

    protected void setMaxCharsInNote(int maxChars) {
        this.MAX_CHARS_IN_NOTE = maxChars;
    }

    protected int getMaxCharsInNote(){ return this.MAX_CHARS_IN_NOTE; }

    protected int getMaxCharsInLabel() {
        return this.MAX_CHARS_IN_LABEL;
    }

    protected void setMaxCharsInLabel(int count) {
        this.MAX_CHARS_IN_LABEL = count;
    }

    protected int getMaxCharsInInvitationPass() {
        return this.MAX_CHARS_IN_INVITATION_PASS;
    }

    protected void setMaxCharsInInvitationPass(int count) {
        this.MAX_CHARS_IN_INVITATION_PASS = count;
    }

    protected void setCoreVersion(float ver) {
        this.CORE_VERSION = ver;
    }

    protected void setUIVersion(float ver){ this.UI_VERSION = ver; }

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
