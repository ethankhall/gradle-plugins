package io.ehdev.gradle.dependency.compiler

import java.io.File


class CompilerRunnable(val scriptFile: File, val outputDirectory: File) : Runnable {
    override fun run() {
        val compiler = KotlinScriptCompiler(this::class.java.classLoader)
        compiler.compileScript(outputDirectory, scriptFile)
    }
}