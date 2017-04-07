package io.ehdev.gradle.dependency

import org.jetbrains.kotlin.script.ScriptTemplateDefinition

@DslMarker
annotation class KotlinDependencyScriptMarker


//@SamWithReceiverAnnotations("io.ehdev.gradle.dependency.HasImplicitReceiver")
@ScriptTemplateDefinition()
@KotlinDependencyScriptMarker
abstract class KotlinDependencyScript(thingy: Thingy): Thingy by thingy {
}