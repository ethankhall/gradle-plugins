package io.ehdev.gradle.dependency.compiler

import io.ehdev.gradle.dependency.api.DependencyDefinitions
import io.ehdev.gradle.dependency.compiler.KotlinDependencyScriptMarker
import org.jetbrains.kotlin.script.ScriptTemplateDefinition

@ScriptTemplateDefinition
@KotlinDependencyScriptMarker
abstract class KotlinDependencyScript(definition: DependencyDefinitions) : DependencyDefinitions by definition