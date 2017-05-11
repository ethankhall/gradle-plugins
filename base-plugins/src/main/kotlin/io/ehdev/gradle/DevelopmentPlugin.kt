package io.ehdev.gradle

import io.ehdev.gradle.internal.AddTestSourceSets
import io.ehdev.gradle.internal.LocalTestListener
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension
import org.gradle.testing.jacoco.tasks.JacocoReport
import java.io.File

open class DevelopmentPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply("java")
        project.plugins.apply("groovy")
        project.plugins.apply("idea")
        project.plugins.apply("jacoco")

        val javaPluginConvention = project.convention.plugins["java"] as JavaPluginConvention
        javaPluginConvention.sourceCompatibility = JavaVersion.VERSION_1_8

        AddTestSourceSets.addSourceSet(project, "integTest")

        project.tasks.withType(Test::class.java) { task ->
            task.addTestListener(object: LocalTestListener() {
                override fun afterSuite(suite: TestDescriptor, result: TestResult) {
                    if(suite.parent == null) {
                        project.logger.lifecycle("Results: {} ({} tests, {} successes, {} failures, {} skipped)",
                                result.resultType, result.testCount, result.successfulTestCount,
                                result.failedTestCount, result.skippedTestCount)
                    }
                }
            })

            val jacoco = task.extensions.getByType(JacocoTaskExtension::class.java)
            jacoco.apply {
                isAppend = true
                destinationFile = File(project.buildDir, "/jacoco/${ task.name }-jacocoTest.exec")
                classDumpDir = File(project.buildDir, "/jacoco/${ task.name }-classpathdumps")
            }
        }

        project.tasks.getByName("check").dependsOn(project.tasks.withType(JacocoReport::class.java))
    }
}
