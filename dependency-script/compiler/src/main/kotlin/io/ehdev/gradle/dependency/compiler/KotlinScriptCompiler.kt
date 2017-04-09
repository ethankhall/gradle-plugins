package io.ehdev.gradle.dependency.compiler

import io.ehdev.gradle.dependency.api.DependencyDefinitions
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.addKotlinSourceRoots
import org.jetbrains.kotlin.script.KotlinScriptDefinition
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

internal class KotlinScriptCompiler(val classLoader: ClassLoader) {

    val messageCollector = DelegatingMessageCollector()

    fun compileScript(outputDirectory: File, scriptFile: File): Class<*> {
        withRootDisposable { rootDisposable ->
            val configuration = CompilerConfiguration().apply {
                addKotlinSourceRoots(listOf(scriptFile.canonicalPath))
                addJvmClasspathRoots(PathUtil.getJdkClassesRoots())
                addJvmClasspathRoot(PathUtil.getResourcePathForClass(Unit::class.java))
                addJvmClasspathRoot(PathUtil.getResourcePathForClass(KotlinDependencyScript::class.java))
                addJvmClasspathRoot(PathUtil.getResourcePathForClass(DependencyDefinitions::class.java))
                put(CommonConfigurationKeys.MODULE_NAME, "dependencyScript")
                put(JVMConfigurationKeys.OUTPUT_DIRECTORY, outputDirectory)
                put(JVMConfigurationKeys.RETAIN_OUTPUT_IN_MEMORY, true)
                add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, KotlinScriptDefinition(KotlinDependencyScript::class))
                put<MessageCollector>(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
            }

            val environment = KotlinCoreEnvironment.Companion.createForProduction(rootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
            return KotlinToJVMBytecodeCompiler.compileScript(environment, classLoader)
                    ?: throw IllegalStateException("Internal error: unable to compile script, see log for details")
        }
    }

    private inline fun <T> withRootDisposable(action: (Disposable) -> T): T {
        val rootDisposable = Disposer.newDisposable()
        try {
            return action(rootDisposable)
        } finally {
            Disposer.dispose(rootDisposable)
        }
    }
}