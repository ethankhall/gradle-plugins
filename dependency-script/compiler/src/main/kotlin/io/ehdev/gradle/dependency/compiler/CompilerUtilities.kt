package io.ehdev.gradle.dependency.compiler

import io.ehdev.gradle.dependency.api.DependencyDefinitions

fun getClassesForClasspath() = listOf(Unit::class.java, KotlinDependencyScript::class.java, DependencyDefinitions::class.java)