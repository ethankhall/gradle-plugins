package io.ehdev.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

open class DependencyCachePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("resolveDependencies") { task ->
            task.doLast {
                project.configurations.filter { conf -> conf.isCanBeResolved }.forEach { it.files }
            }
        }
    }
}