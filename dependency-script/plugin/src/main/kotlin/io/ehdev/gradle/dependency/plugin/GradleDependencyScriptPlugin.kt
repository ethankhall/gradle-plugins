package io.ehdev.gradle.dependency.plugin

import io.ehdev.gradle.dependency.api.DependencyDefinitions
import io.ehdev.gradle.dependency.api.internal.md5Digest
import io.ehdev.gradle.dependency.api.internal.toHex
import io.ehdev.gradle.dependency.internal.api.DefaultDependencyDefinitions
import netflix.nebula.dependency.recommender.DependencyRecommendationsPlugin
import netflix.nebula.dependency.recommender.provider.RecommendationProviderContainer
import org.codehaus.groovy.tools.RootLoader
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.util.*
import java.util.concurrent.atomic.AtomicReference

open class GradleDependencyScriptPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(DependencyRecommendationsPlugin::class.java)

        val dependencyDefinitions = getDependencyDefinitions(project)

        val container = project.extensions.getByType(RecommendationProviderContainer::class.java)
        container.add(dependencyDefinitions.buildRecommendationProvider())

        configureSpringVersioning(project, dependencyDefinitions)
        configureExcludes(project, dependencyDefinitions)
    }

    private fun configureSpringVersioning(project: Project, dependencyDefinitions: DefaultDependencyDefinitions) {
        project.extensions.extraProperties["versions"] = dependencyDefinitions.versions
        project.extensions.extraProperties["libraries"] = dependencyDefinitions.buildLibrariesAsMap()
        project.extensions.extraProperties["hibernate.version"] = dependencyDefinitions.versions["hibernate"]
        project.extensions.extraProperties["jackson.version"] = dependencyDefinitions.versions["jackson"]
        project.extensions.extraProperties["jooq.version"] = dependencyDefinitions.versions["jooq"]
    }

    private fun configureExcludes(project: Project, dependencyDefinitions: DefaultDependencyDefinitions) {
        project.configurations.all { conf ->
            dependencyDefinitions.exclude.forEach {
                conf.exclude(mapOf(Pair("group", it.group), Pair("name", it.name)))
            }
        }
    }

    private fun findVersion(): String {
        val inputStream = this::class.java.classLoader.getResourceAsStream("dependencies/module-version.properties")
        val properties = Properties()
        properties.load(inputStream)
        return properties["dependency.script.version"] as String
    }

    private fun getDependencyDefinitions(project: Project): DefaultDependencyDefinitions {
        val dependenciesKts = File(project.rootDir, "gradle/dependencies.kts")
        if (!dependenciesKts.exists()) {
            throw GradleException("Unable to find ${dependenciesKts.path}")
        }

        val extension = project.findOrCreateExtension<DependencyCompilerCache>()

        val hex = toHex(dependenciesKts.md5Digest())
        val clazz = extension.cache.getOrPut(hex) {
            compileScriptToClass(dependenciesKts, project)
        }

        val definitions = DefaultDependencyDefinitions()
        clazz.getConstructor(DependencyDefinitions::class.java).newInstance(definitions)

        return definitions
    }

    private fun compileScriptToClass(dependenciesKts: File, project: Project): Class<*> {
        val detachedConfiguration = project.configurations.detachedConfiguration(
                project.dependencies.create("io.ehdev.gradle:dependency-script-compiler:${findVersion()}")
        )

        project.logger.lifecycle("Files: " + detachedConfiguration.files.map { it.absolutePath }.joinToString(":"))
        val compilerJars = detachedConfiguration.files.map { it.toURI().toURL() }.toTypedArray()
        val loader = RootLoader(compilerJars, this::class.java.classLoader)

        val atomicReference = AtomicReference<Class<*>?>(null)

        val runnableClass = loader.loadClass("io.ehdev.gradle.dependency.compiler.CompilerRunnable")
        val runnable = runnableClass
                .getConstructor(File::class.java, atomicReference::class.java)
                .newInstance(dependenciesKts, atomicReference) as Runnable
        runnable.run()
        return atomicReference.get()!!
    }

    inline private fun <reified T: Any> Project.findOrCreateExtension(): T {
        val findByName = project.rootProject.extensions.findByName(T::class.java.name) as T?
        return findByName ?: project.rootProject.extensions.create(T::class.java.name, T::class.java)
    }
}