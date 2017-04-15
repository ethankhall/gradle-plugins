package io.ehdev.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

open class BuildDirectoryPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project.path != ":") {
            project.setBuildDir(File(project.rootDir, "build/" + project.path.replace(':', '-').substring(1)))
        } else {
            project.setBuildDir(File(project.rootDir, "build/" + project.name))
        }
    }
}