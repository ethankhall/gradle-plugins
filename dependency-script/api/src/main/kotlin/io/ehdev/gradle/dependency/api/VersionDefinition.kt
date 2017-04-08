package io.ehdev.gradle.dependency.api

import java.io.Serializable

interface VersionDefinition : Serializable {
    fun lock(body: DependencyForcing.() -> Unit): VersionDefinition
}