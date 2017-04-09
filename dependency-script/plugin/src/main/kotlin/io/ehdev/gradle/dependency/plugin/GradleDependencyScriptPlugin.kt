package io.ehdev.gradle.dependency.plugin

import io.ehdev.gradle.dependency.api.DependencyDefinitions
import io.ehdev.gradle.dependency.api.internal.md5Digest
import io.ehdev.gradle.dependency.api.internal.toHex
import io.ehdev.gradle.dependency.internal.api.DefaultDependencyDefinitions
import netflix.nebula.dependency.recommender.DependencyRecommendationsPlugin
import netflix.nebula.dependency.recommender.provider.RecommendationProvider
import netflix.nebula.dependency.recommender.provider.RecommendationProviderContainer
import org.codehaus.groovy.tools.RootLoader
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URL
import java.net.URLClassLoader
import java.util.*

open class GradleDependencyScriptPlugin : Plugin<Project> {

    val logger = LoggerFactory.getLogger(this::class.java)!!

    override fun apply(project: Project) {
        project.plugins.apply(DependencyRecommendationsPlugin::class.java)

        val dependencyDefinitions = getDependencyDefinitions(project)

        val container = project.extensions.getByType(RecommendationProviderContainer::class.java)
        val recommendationProvider = dependencyDefinitions.buildRecommendationProvider()
        container.add(recommendationProvider)

        configureVersionForcing(project, recommendationProvider)
        configureExtraProperties(project, dependencyDefinitions)
        configureExcludes(project, dependencyDefinitions)
    }

    private fun configureVersionForcing(project: Project, provider: RecommendationProvider) {
        project.configurations.all { configuration ->
            configuration.resolutionStrategy.eachDependency { details ->
                val version = provider.getVersion(details.requested.group, details.requested.name)
                if (version != null) {
                    details.useVersion(version)
                }
            }
        }
    }

    private fun configureExtraProperties(project: Project, dependencyDefinitions: DefaultDependencyDefinitions) {
        project.extensions.extraProperties["versions"] = dependencyDefinitions.versions
        project.extensions.extraProperties["libraries"] = dependencyDefinitions.buildLibrariesAsMap()
    }

    private fun configureExcludes(project: Project, dependencyDefinitions: DefaultDependencyDefinitions) {
        project.configurations.all { conf ->
            dependencyDefinitions.exclude.forEach {
                conf.exclude(mapOf(Pair("group", it.group), Pair("module", it.name)))
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
        val dependenciesKts = File(project.rootDir, "gradle/dependencies.dep.kts")
        if (!dependenciesKts.exists()) {
            throw GradleException("Unable to find ${dependenciesKts.path}")
        }

        val hex = toHex(dependenciesKts.md5Digest())
        val cacheDir = File(project.rootDir, "/.gradle/dependency-script/$hex")

        val detachedConfiguration = project.configurations.detachedConfiguration(
                project.dependencies.create("io.ehdev.gradle:dependency-script-compiler:${findVersion()}")
        )
        val compilerJars = detachedConfiguration.files.map { it.toURI().toURL() }.toTypedArray()

        if (!cacheDir.exists()) {
            cacheDir.mkdir()
            compileScriptToClass(dependenciesKts, cacheDir, compilerJars)
        }
        val definitions = DefaultDependencyDefinitions()
        val classLoader = URLClassLoader(compilerJars + cacheDir.toURI().toURL(), this::class.java.classLoader)
        classLoader.loadClass("Dependencies_dep").getConstructor(DependencyDefinitions::class.java).newInstance(definitions)

        return definitions
    }

    private fun compileScriptToClass(dependenciesKts: File, cacheDir: File, compilerJars: Array<URL>) {
        logger.warn("Files: " + compilerJars.map { it.file }.joinToString(":"))
        val loader = RootLoader(compilerJars, this::class.java.classLoader)

        val runnableClass = loader.loadClass("io.ehdev.gradle.dependency.compiler.CompilerRunnable")
        val runnable = runnableClass
                .getConstructor(File::class.java, File::class.java)
                .newInstance(dependenciesKts, cacheDir) as Runnable
        runnable.run()
    }
}