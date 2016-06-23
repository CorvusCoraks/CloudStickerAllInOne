package ru.gremal.cloudsticker.common.tools;

/**
 * статусные сообщения проверки связи
 */
public enum InternetConnectionMessage {
        YES, // связь Ок - облако доступно.
        NO, // Доступа в интернет нет (оба сайта недоступны)
        CLOUD_NOT_FOUND // Облако недоступно (первый доступен, второй - нет)

}
