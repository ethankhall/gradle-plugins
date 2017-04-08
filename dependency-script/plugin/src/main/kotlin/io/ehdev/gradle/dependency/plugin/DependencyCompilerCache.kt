package io.ehdev.gradle.dependency.plugin

open class DependencyCompilerCache {
    val cache: MutableMap<String, Class<*>> = mutableMapOf()
}