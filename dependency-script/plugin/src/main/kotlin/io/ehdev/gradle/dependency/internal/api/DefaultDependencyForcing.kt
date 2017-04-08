package io.ehdev.gradle.dependency.internal.api

import io.ehdev.gradle.dependency.api.DependencyForcing

class DefaultDependencyForcing(val version: String) : DependencyForcing {
    var group: String? = null
    var name: String? = null

    override fun withGroup(group: String) {
        this.group = group
    }

    override fun withName(name: String) {
        this.name = name
    }
}