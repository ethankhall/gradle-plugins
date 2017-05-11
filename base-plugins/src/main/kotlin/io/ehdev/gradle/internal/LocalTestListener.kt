package io.ehdev.gradle.internal

import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestListener
import org.gradle.api.tasks.testing.TestResult

abstract class LocalTestListener: TestListener {
    override fun beforeTest(testDescriptor: TestDescriptor) {
        //noop
    }

    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
        //noop
    }

    override fun beforeSuite(suite: TestDescriptor) {
        //noop
    }

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) {
        //noop
    }
}