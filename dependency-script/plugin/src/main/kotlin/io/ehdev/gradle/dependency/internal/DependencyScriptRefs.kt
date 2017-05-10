package io.ehdev.gradle.dependency.internal

import java.io.File
import java.net.URL

data class DependencyScriptRefs(val jarFile: File, val classListing: File, val compilerJars: List<URL>) {

    val compilerJarsAsArray = compilerJars.toTypedArray()

    fun readClassNames() = classListing.readLines()
    fun updateClassNames(names: List<String>) {
        classListing.writeText(names.joinToString("\n"))
    }
}