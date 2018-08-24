package io.ehdev.gradle.dependency.internal.api

data class DependencyNotation(val group: String, val name: String, val version: String?,
                              val configuration: String?, val classification: String?) {
    fun asMap(): Map<String, String> {
        val map = mutableMapOf(Pair("group", group), Pair("name", name))
        if (version != null) map.put("version", version)
        if (configuration != null) map.put("configuration", configuration)
        if (classification != null) map.put("classification", classification)
        return map
    }
}