package io.ehdev.gradle.dependency.api

interface DependencyForcing {
    fun withGroup(group: String)

    fun withName(name: String)
}