package io.ehdev.gradle.dependency.compiler

import org.jetbrains.kotlin.script.KotlinScriptExternalDependencies
import org.jetbrains.kotlin.script.ScriptContents
import org.jetbrains.kotlin.script.ScriptDependenciesResolver
import org.jetbrains.kotlin.script.asFuture
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File
import java.util.concurrent.Future

class DepKtsScriptDependenciesResolver : ScriptDependenciesResolver {

    override fun resolve(script: ScriptContents,
                         environment: Map<String, Any?>?,
                         report: (ScriptDependenciesResolver.ReportSeverity, String, ScriptContents.Position?) -> Unit,
                         previousDependencies: KotlinScriptExternalDependencies?): Future<KotlinScriptExternalDependencies?> {

        if (environment == null || script.file == null) {
            return previousDependencies.asFuture()
        }

        val dependencies = findDeps(script.file!!)
        return dependencies.asFuture()
    }

    fun findDeps(file: File): KotlinScriptExternalDependencies {
        val classpath = ImplicitImports.classpath.map { PathUtil.getResourcePathForClass(it) }
        return KotlinDepScriptDeps(classpath, listOf("io.ehdev.gradle.dependency.api.DependencyDefinitions.*"), listOf(file))
    }
}

class KotlinDepScriptDeps(
        override val classpath: Iterable<File>,
        override val imports: Iterable<String>,
        override val sources: Iterable<File>) : KotlinScriptExternalDependencies {

    override fun toString(): String {
        return "KotlinDepScriptDeps(classpath=$classpath, imports=$imports, sources=$sources)"
    }
}