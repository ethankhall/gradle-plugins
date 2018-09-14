package io.ehdev.gradle.klint

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

open class KlintPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.apply("java")

        val klintConfiguration = project.configurations.maybeCreate("ktlint")
        klintConfiguration.isVisible = false

        project.dependencies.add("ktlint", "com.github.shyiko:ktlint:0.28.0")

        val klintTask = project.tasks.create("klint", JavaExec::class.java) { javaExec ->
            javaExec.main = "com.github.shyiko.ktlint.Main"
            javaExec.args("src/main/kotlin/**/*.kt")

            javaExec.doFirst {
                javaExec.classpath = klintConfiguration
            }
        }

        project.tasks.getByName("check").dependsOn(klintTask)

        project.tasks.create("ktlintFormat", JavaExec::class.java) { javaExec ->
            javaExec.main = "com.github.shyiko.ktlint.Main"
            javaExec.args("-F", "src/main/kotlin/**/*.kt")

            javaExec.doFirst {
                javaExec.classpath = klintConfiguration
            }
        }
    }

}
