package io.ehdev.gradle.dependency.compiler

import java.io.File

class CompilerRunnable(val scriptFiles: List<File>, val jarFile: File, val classNames: MutableList<String>) : Runnable {
    override fun run() {
        val compiler = KotlinScriptCompiler()
        classNames.addAll(compiler.compileScript(jarFile, scriptFiles.sortedBy(File::nameWithoutExtension)))
    }
}