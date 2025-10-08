package com.grupo03.solea.utils

/**
 * Interface for objects that can be converted from a Firestore Map representation.
 *
 * Firestore stores documents as Maps. This interface allows domain objects to define
 * how they should be constructed from Firestore data.
 *
 * @param T The type of object to create from the map
 */
interface FromMap<T> {
    /**
     * Creates an instance of type T from a map representation.
     *
     * @param map The map containing the object's data (typically from Firestore)
     * @return An instance of T if the map can be converted, null if conversion fails
     */
    fun fromMap(map: Map<String, Any?>): T?
}

/**
 * Interface for objects that can be converted to a Firestore Map representation.
 *
 * This interface allows domain objects to define how they should be serialized
 * for storage in Firestore.
 */
interface ToMap {
    /**
     * Converts this object to a map representation suitable for Firestore.
     *
     * @return A map representation of this object, or null if conversion fails
     */
    fun toMap(): Map<String, Any?>?
}
