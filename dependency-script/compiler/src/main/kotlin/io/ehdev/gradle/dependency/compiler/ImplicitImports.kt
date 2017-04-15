package io.ehdev.gradle.dependency.compiler

import io.ehdev.gradle.dependency.api.DependencyDefinitions

object ImplicitImports {
    val classpath = listOf(Unit::class.java, KotlinDependencyScript::class.java, DependencyDefinitions::class.java)
}