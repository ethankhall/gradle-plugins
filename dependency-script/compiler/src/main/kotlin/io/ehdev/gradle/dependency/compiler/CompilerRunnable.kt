package io.ehdev.gradle.dependency.compiler

import java.io.File
import java.util.concurrent.atomic.AtomicReference


class CompilerRunnable(val scriptFile: File, val ref: AtomicReference<Class<*>?>) : Runnable {
    override fun run() {
        val compiler = KotlinScriptCompiler(this::class.java.classLoader)
        ref.set(compiler.compileScript(scriptFile))
    }
}