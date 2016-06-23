package ru.gremal.cloudsticker.common.ui;

import ru.gremal.cloudsticker.common.interfaces.CoreSide;
import ru.gremal.cloudsticker.common.interfaces.UISide;

/**
 * Created by GreMal on 19.04.2016.
 */
public abstract class AbstractUIDetonator {
    static protected CoreSide core;
    static protected UISide ui = null;

    // Данную функцию обязательно переопределить после наследования от данного класса
    public static UISide createUiInstance(CoreSide core){return AbstractUIDetonator.ui;}
}
