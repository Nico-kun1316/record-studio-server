package net

import kotlin.reflect.KProperty

fun <T> assertParam(param: T, isValid: T.() -> Boolean) {
    if (!param.isValid())
        throw InvalidParameterException("Parameter is invalid")
}
