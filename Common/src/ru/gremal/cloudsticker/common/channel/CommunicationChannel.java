package ru.gremal.cloudsticker.common.channel;

/**
 * Класс, инкапсулирующий канал связи ядра и пользовательского интерфейса. На каждый затребованный пакет данных, параметр и пр. формируется отдельный канал связи. Одинаковый тип каналов нивелирует разнородность перетекающих данных. Этот класс используется, как замена прямых вызовов функций Ядра из Пользовательского Интерфейса.
 * */

/*
* ВНИМАНИЕ!
*
* Отправка запроса в Core, со всеми предварительными проверками, должна производиться в едином synchronize блоке с захватом мютекса соответствующего объекта.
*
* В таком жеключе должно производиться и получение результата.
*
* Аналогично должна производиться работа и со стороны Core.
*
* Это необходимо, чтобы другой поток не смог бы втиснуться между вызовами поочерёдных процедур обрабатываемого объекта. Разрыв в очерёдности возможен только на этапе подготовки/ожидания ответа.
* */

    /*
    * Внешние классы получают ссылку на объект данного типа через спец функцию, описанную в UISide. Получение ссылки, не означает возможность работы с данным объектом. Все возможности определяются установкой соответствующих флагов.
    *
    * Core.
    * - Проверяет значение флага Go. Если истина, значит ему дана команда на выдачу данных.
    * - Проверяет значение флага Running. Если истина, значит уже кто-то запрос получил и обработка запроса уже идёт. Core из обработки выходит.
    * - Running устанавливается в истину, канал занимается данным процессом.
    * - Core подготавливает данные и записывает их в поле answer.
    * - Core устанавливает флаг Ready в истину, сигнализируя, что ответ отправлен в UI.
    *
    * UI.
    * - Проверяет флаг Go. Если он истина, значит уже кто-то дал эту команду. UI ждёт её исполнения (пока флаг Go не станет ложью)
    * - Если флаг Go ложь, UI устанавливает его в истину, сигнализируя Core и другим потокам, что запрос на данные отправлен.\
    * - UI ждёт момента, когда флаг Ready станет истиной, а значит данные можно забирать. И забирает их.
    * - UI присваевает null полю данных.
    * - присваивает ложь флагу Ready
    * - присваивает ложь флагам Running и Go, освобождая канал.
    *
    * */

public class CommunicationChannel {
    private volatile boolean commandGo = false; // Команда UI на запуск коммуникации. Ставит и убирает UI
    private volatile boolean commandRunning = false; // Канал занят, устанавливает Core, снимает UI.
    private volatile boolean commandReady = false; // Данные переданы. Устанавливает Core, снимает UI
    private volatile Object answer = null; // ответ Core. Ставит Сore, обнуляет UI
    private volatile Class objectClass = null; // Тип хранимых данных. Устанавливается констуктором.

    public synchronized boolean isCommandRunning() {
        return commandRunning;
    }

    public synchronized void setCommandRunning(boolean value){
        this.commandRunning = value;
    }

    // Для UI. Проверка. Исполнил ли Core команду
    protected synchronized boolean isAnswerReady() {
        return commandReady;
    }

    // Для Core. Установить флаг, что Core задание выполнил. UI снимает флаг
    public synchronized void setAnswerReady(boolean value) {
        this.commandReady = value;
    }

    // Для UI. Получить объект ответа Core
    protected synchronized Object getAnswer() {
        // Очищаем флаги и возвращаем результат
        //synchronized (this) {
            Object result = this.answer;
            this.commandGo = false;
            this.commandRunning = false;
            this.commandReady = false;
            this.answer = null;
            return result;
        //}
    }

    // Для Core. Дать ответ на запрос UI. Если возвращает false - несовпадение типов
    public synchronized boolean setAnswer(Object answer) {
        //synchronized (this) {
            if (this.objectClass.equals(answer.getClass())) {
                // если классы ожидаемого и присланного объекта совпадают
                this.answer = answer;
                this.commandReady = true;
                return true;
            }
            return false; // если вдруг несовпадение типов
        //}
    }

    // Отправлена ли команда на исполнения в Core
    public synchronized boolean isCommandGo() {
        return this.commandGo;
    }

    // Для UI. Посылает Core команду на исполение.
    protected synchronized void setCommandGo(boolean value) {
            this.commandGo = value;
    }

/*    protected synchronized void clearCommunicationChannal(){
        synchronized (this){

        }
    };*/

    public CommunicationChannel(Class objectClass) { this.objectClass = objectClass; }
    /*
    public CommunicationChannel(Class objectClass, Object value){
        this.objectClass = objectClass;
        this.answer = false;
    }
    */
/*
    // Core запрашиввает данный канал. Если канал занят, то возвращает null
    public synchronized CommunicationChannel coreAskChannal(){
            if ((this.commandGo) && (!this.commandRunning)) {
                // Установлен флаг "Выполнять", а флаг "Уже исполняется" не установлен. Значит канал свободен.
                this.commandRunning = true; // занимаем канал
                return this;
            } else {
                // канал занят
                return null;
            }
    }

    // UI Запрашиввает данный канал. Если канал занят, то возвращает null
    protected synchronized CommunicationChannel uiAskChannal(){
        if (this.commandGo) {
            // Установлен флаг "Выполнять". Значит канал уже занят.
            return null;
        } else {
            // Иначе - канал свободен.
            this.commandRunning = true; // занимаем канал
            return this;
        }
    }
*/
}