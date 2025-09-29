package com.grupo03.solea.utils

interface FromMap<T> {
    fun fromMap(map: Map<String, Any?>): T?
}

interface ToMap {
    fun toMap(): Map<String, Any?>?
}