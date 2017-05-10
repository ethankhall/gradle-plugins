package io.ehdev.gradle.dependency.compiler

import io.ehdev.gradle.dependency.api.DependencyDefinitions
import io.ehdev.gradle.dependency.api.internal.DependencyScript
import org.jetbrains.kotlin.script.ScriptTemplateDefinition

@KotlinDependencyScriptMarker
@ScriptTemplateDefinition(resolver = DepKtsScriptDependenciesResolver::class, scriptFilePattern = ".*\\.dep\\.kts")
abstract class KotlinDependencyScript(definition: DependencyDefinitions) : DependencyDefinitions by definition, DependencyScript