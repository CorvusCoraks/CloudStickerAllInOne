package ru.gremal.cloudsticker.common.channel;

/**
 * Прстейший канал связи из UI в Core.
 * UI отдаёт приказ в Core через поле Order.
 * Core сообщает об исполнении приказа через поле Report.
 *
 * Через поле object может передаваять дополнительная информация в ту или иную сторону. Опционально.
 *
 * Процедуры проверок производятся аналогично CommunicationChannel
 */
public class SimpleCommunicationChannel {
    // private boolean orderB; // состояние при вызове конструктора
    private boolean order; // Приказ UI
    private boolean report = false; // Отчёт Core об исполнении
    private Object object; // Дополнительный параметр
    private Class objectClass; // Тип дополнительного параметра

    /*
    public SimpleCommunicationChannel(Class objectClass) {
        this.objectClass = objectClass;
    }
    */

    public SimpleCommunicationChannel(Class objectClass, boolean order){
        this.objectClass = objectClass;
        this.order = order;
    }

    /*
    public SimpleCommunicationChannel(Class objectClass, Object value){
        this.objectClass = objectClass;
        this.object = value;
    }
    */
    // устанавливаем начальное значение приказа, если это необходимо
    public SimpleCommunicationChannel(boolean order){this.order = order;}

    // public SimpleCommunicationChannel (){}

    // Запрашивают и UI, и Core
    public synchronized boolean isOrder() {
        return this.order;
    }

    // Устанавливает UI для Core.
    protected synchronized void setOrder(boolean order) {
        this.order = order;
    }

    public synchronized boolean isReport() {
        return this.report;
    }

    public synchronized void setReport(boolean report) {
        this.report = report;
    }

    public synchronized Object getObject() {
        return this.object;
    }

    // если возвращает false, значит типы не совпадают
    public synchronized boolean setObject(Object object) {
        if(object.getClass().equals(this.objectClass)){
            this.report = true;
            this.object = object;
            return true;
        }else{
            return false;
        }

    }

    // Вернуть канал в начальное положение полей
    /*
    public synchronized void setToBegin(){

    }

    */
    public synchronized Class getObjectClass() {
        return this.objectClass;
    }

    public synchronized void setObjectClass(Class objectClass) {
        this.objectClass = objectClass;
    }
}
