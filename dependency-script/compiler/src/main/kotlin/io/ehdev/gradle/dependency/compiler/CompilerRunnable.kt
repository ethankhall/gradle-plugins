package io.ehdev.gradle.dependency.compiler

import java.io.File

class CompilerRunnable(val scriptFiles: List<File>, val outputDirectory: File, val classNames: MutableList<String>) : Runnable {
    override fun run() {
        val compiler = KotlinScriptCompiler(this::class.java.classLoader)
        classNames.addAll(scriptFiles.sortedBy(File::nameWithoutExtension).map { compiler.compileScript(outputDirectory, it).name })
    }
}