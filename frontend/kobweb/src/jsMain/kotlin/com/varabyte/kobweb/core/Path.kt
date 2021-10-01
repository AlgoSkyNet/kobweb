package com.varabyte.kobweb.core

private const val HEX_REGEX = "[0-9A-F]"
private val PATH_REGEX = Regex("""^/(([a-z0-9]|%${HEX_REGEX}${HEX_REGEX})+/?)*$""")

class Path(value: String) {
    companion object {
        fun isLocal(path: String) = tryCreate(path) != null
        fun tryCreate(path: String) = try { Path(path) } catch (ex: IllegalArgumentException) { null }
    }

    init {
        require(value.matches(PATH_REGEX)) { "URL path not formatted properly: $value"}
    }

    val value = value
    val parts = value.removePrefix("/").split("/")
}