package io.ehdev.gradle.dependency

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinToJVMBytecodeCompiler
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoots
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer.dispose
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer.newDisposable
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.config.addKotlinSourceRoots
import org.jetbrains.kotlin.script.KotlinScriptDefinition
import org.jetbrains.kotlin.utils.PathUtil
import java.io.File

fun compileScript(file: File, classLoader: ClassLoader, messageCollector: MessageCollector): Class<*> {
    withRootDisposable { rootDisposable ->
        val configuration = CompilerConfiguration().apply {
            addKotlinSourceRoots(listOf(file.canonicalPath))
            addJvmClasspathRoots(PathUtil.getJdkClassesRoots())
            put(CommonConfigurationKeys.MODULE_NAME, "dependencyScript")
            add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, KotlinScriptDefinition(KotlinDependencyScript::class))
            put<MessageCollector>(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
        }

        val environment = KotlinCoreEnvironment.createForProduction(rootDisposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES)
        return KotlinToJVMBytecodeCompiler.compileScript(environment, classLoader)
                ?: throw IllegalStateException("Internal error: unable to compile script, see log for details")
    }
}

private inline fun <T> withRootDisposable(action: (Disposable) -> T): T {
    val rootDisposable = newDisposable()
    try {
        return action(rootDisposable)
    } finally {
        dispose(rootDisposable)
    }
}