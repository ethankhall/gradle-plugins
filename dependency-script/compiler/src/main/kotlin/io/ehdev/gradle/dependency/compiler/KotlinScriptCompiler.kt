package io.ehdev.gradle.dependency.compiler

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

internal class KotlinScriptCompiler {

    val messageCollector = DelegatingMessageCollector()

    fun compileScript(outputJar: File, scriptFile: List<File>): List<String> {
        return withRootDisposable { rootDisposable ->
            val configuration = CompilerConfiguration().apply {
                addKotlinSourceRoots(scriptFile.map { it.canonicalPath })
                addJvmClasspathRoots(PathUtil.getJdkClassesRoots())
                ImplicitImports.classpath.forEach { addJvmClasspathRoot(PathUtil.getResourcePathForClass(it)) }
                put(CommonConfigurationKeys.MODULE_NAME, outputJar.nameWithoutExtension)
                put(JVMConfigurationKeys.OUTPUT_JAR, outputJar)
                add(JVMConfigurationKeys.SCRIPT_DEFINITIONS, KotlinScriptDefinition(KotlinDependencyScript::class))
                put<MessageCollector>(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollector)
            }

            val environment = KotlinCoreEnvironment.createForProduction(rootDisposable, configuration,
                    EnvironmentConfigFiles.JVM_CONFIG_FILES)

            KotlinToJVMBytecodeCompiler.compileBunchOfSources(environment)

            environment.getSourceFiles().map { it.script?.name }.filterNotNull()
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