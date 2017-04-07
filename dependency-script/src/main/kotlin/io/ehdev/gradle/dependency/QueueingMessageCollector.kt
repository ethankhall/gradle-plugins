package io.ehdev.gradle.dependency

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector

class QueueingMessageCollector: MessageCollector {

    var errors = false

    override fun clear() {
    }

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation) {
        when(severity) {
            CompilerMessageSeverity.ERROR -> errors = true
            else -> { }
        }

        println(message)
    }

    override fun hasErrors(): Boolean = errors

}