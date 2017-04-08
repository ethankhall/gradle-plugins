package io.ehdev.gradle.dependency.api

import java.io.Serializable

interface DependencyDefinitions : Serializable {

    fun defineVersion(name: String, version: String): VersionDefinition

    fun defineLibrary(name: String, libraries: List<Any>)

    fun excludeLibrary(group: String, name: String? = null)

    fun usingVersion(name: String): String
}