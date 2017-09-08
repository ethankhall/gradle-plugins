package io.ehdev.gradle.internal

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.ide.idea.model.IdeaModel
import java.io.File
import java.util.Arrays

object AddTestSourceSets {

    @JvmStatic
    fun addSourceSet(project: Project, name: String) {
        val javaPlugin = project.convention.getPlugin(org.gradle.api.plugins.JavaPluginConvention::class.java)
        val sourceSets = javaPlugin.sourceSets

        val mainSourceSet = sourceSets.getByName("main")
        val testSourceSet = sourceSets.getByName("test")

        val newSourceSet = sourceSets.create(name) { sourceSet ->
            val testClasspath = mainSourceSet.output.plus(testSourceSet.output)
            sourceSet.compileClasspath = sourceSet.compileClasspath.plus(testClasspath)
            sourceSet.runtimeClasspath = sourceSet.runtimeClasspath.plus(testClasspath)
            sourceSet.java.srcDir(project.file("src/$name/java"))
            sourceSet.resources.srcDir(project.file("src/$name/resources"))
        }

        val compileConfig = project.configurations.getByName(name + "Compile")
        val runtimeConfig = project.configurations.getByName(name + "Runtime")

        compileConfig.extendsFrom(project.configurations.getByName("testCompile"))
        runtimeConfig.extendsFrom(project.configurations.getByName("testRuntime"))

        val integTest = project.tasks.create(name, Test::class.java) { test ->
            test.shouldRunAfter(project.tasks.getByName("test"))
            test.testClassesDirs = newSourceSet.output.classesDirs
            test.classpath = newSourceSet.runtimeClasspath
            test.reports.html.destination = File(project.buildDir, "/reports/" + name)
        }

        project.tasks.getByName("check").dependsOn(integTest)

        val idea = project.extensions.getByType(IdeaModel::class.java)

        val sourceSetDir = File(project.projectDir, "src/" + name)

        val sourceDirs = ArrayList<File>()
        sourceDirs.addAll(Arrays.asList(File(sourceSetDir, "java"), File(sourceSetDir, "groovy")))
        sourceDirs.addAll(idea.module.testSourceDirs)
        sourceDirs.addAll(newSourceSet.resources.srcDirs)

        idea.module.testSourceDirs = HashSet(sourceDirs)

        idea.module.scopes["TEST"]!!["plus"]!!.add(compileConfig)
        idea.module.scopes["TEST"]!!["plus"]!!.add(runtimeConfig)
    }
}
