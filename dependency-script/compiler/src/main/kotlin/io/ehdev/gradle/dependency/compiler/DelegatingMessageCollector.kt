package io.ehdev.gradle.dependency.compiler

import org.jetbrains.kotlin.cli.common.messages.CompilerMessageLocation
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.slf4j.LoggerFactory

internal class DelegatingMessageCollector : MessageCollector {

    val logger = LoggerFactory.getLogger(this::class.java)!!
    var errors = false

    override fun clear() {
    }

    override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageLocation) {
        when (severity) {
            CompilerMessageSeverity.ERROR -> logger.error(message)
            CompilerMessageSeverity.EXCEPTION -> logger.error(message)
            CompilerMessageSeverity.STRONG_WARNING -> logger.warn(message)
            CompilerMessageSeverity.WARNING -> logger.warn(message)
            CompilerMessageSeverity.INFO -> logger.info(message)
            CompilerMessageSeverity.LOGGING -> logger.debug(message)
            CompilerMessageSeverity.OUTPUT -> logger.info(message)
        }

        if (severity.isError) {
            errors = true
        }
    }

    override fun hasErrors(): Boolean = errors

}