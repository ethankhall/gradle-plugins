package io.ehdev.gradle.dependency.plugin

open class DependencyExtension(val libraries: Map<String, String>, val versions: Map<String, String>) {
    override fun toString(): String {
        return "DependencyExtension(libraries=$libraries, versions=$versions)"
    }
}