package io.ehdev.gradle.internal

import java.io.File

object AddTestSourceSets {
    fun addSourceSet(project: org.gradle.api.Project, name: String) {
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


        val integTest = project.tasks.create(name, org.gradle.api.tasks.testing.Test::class.java) { test ->
            test.shouldRunAfter(project.tasks.getByName("test"))
            test.testClassesDir = newSourceSet.output.classesDir
            test.classpath = newSourceSet.runtimeClasspath
            test.reports.html.setDestination(java.io.File(project.buildDir, "/reports/" + name))
        }

        project.tasks.getByName("check").dependsOn(integTest)

        val idea = project.extensions.getByType(org.gradle.plugins.ide.idea.model.IdeaModel::class.java)

        val sourceSetDir = java.io.File(project.projectDir, "src/" + name)

        val sourceDirs = java.util.ArrayList<File>()
        sourceDirs.addAll(java.util.Arrays.asList(java.io.File(sourceSetDir, "java"), java.io.File(sourceSetDir, "groovy")))
        sourceDirs.addAll(idea.module.testSourceDirs)
        sourceDirs.addAll(newSourceSet.resources.srcDirs)

        idea.module.testSourceDirs = java.util.HashSet(sourceDirs)

        idea.module.scopes["TEST"]!!["plus"]!!.add(compileConfig)
        idea.module.scopes["TEST"]!!["plus"]!!.add(runtimeConfig)
    }
}
