package com.caicai.kEventBus_demo.bean

open class User(val name: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        return name == other.name
    }

    override fun hashCode(): Int {
        return 31 * (name.hashCode())
    }
    override fun toString(): String {
        return "User(name ='$name')"
    }
}