package io.ehdev.gradle.dependency.plugin

import io.ehdev.gradle.dependency.api.DependencyDefinitions
import io.ehdev.gradle.dependency.internal.api.DefaultDependencyDefinitions
import io.ehdev.gradle.dependency.internal.md5Digest
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
import java.util.Properties

open class GradleDependencyScriptPlugin : Plugin<Project> {

    val logger = LoggerFactory.getLogger(GradleDependencyScriptPlugin::class.java)!!

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
        project.extensions.create("deps", DependencyExtension::class.java,
                dependencyDefinitions.buildLibrariesAsMap(), dependencyDefinitions.versions)
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
        val dependencyFiles = File(project.rootDir, "gradle")
                .listFiles { _, name -> name?.endsWith(".dep.kts") ?: false }
                ?.filter { it.isFile } ?: emptyList()

        if (dependencyFiles.isEmpty()) {
            throw GradleException("Unable to find any *.dep.kts files in ${File(project.rootDir, "gradle").absolutePath}")
        }

        val hex = dependencyFiles.map { it.readText() }.joinToString("\n").md5Digest()
        val cacheDir = File(project.rootDir, "/.gradle/dependency-script/$hex")

        val scriptClassNames = if (!cacheDir.exists()) {
            cacheDir.mkdir()
            compileScriptToClass(dependencyFiles.toList(), cacheDir, extractJars())
        } else {
            emptyList()
        }
        val definitions = DefaultDependencyDefinitions()
        val classLoader = URLClassLoader(extractJars() + cacheDir.toURI().toURL(), this::class.java.classLoader)
        scriptClassNames.forEach {
            classLoader.loadClass(it).getConstructor(DependencyDefinitions::class.java).newInstance(definitions)
        }

        return definitions
    }

    private fun extractJars(): Array<URL> {
        return listOf("dependency-script-compiler.jar", "kotlin-compiler-embeddable.jar").map {
            GradleDependencyScriptPlugin::class.java.classLoader.getResource("compiler/$it")
        }.toTypedArray()
    }

    private fun compileScriptToClass(dependenciesKts: List<File>, cacheDir: File, compilerJars: Array<URL>): List<String> {
        val loader = RootLoader(compilerJars, this::class.java.classLoader)

        logger.debug("compiler jars: {}", compilerJars)

        val classNames: MutableList<String> = mutableListOf()

        val runnableClass = loader.loadClass("io.ehdev.gradle.dependency.compiler.CompilerRunnable")
        val runnable = runnableClass
                .getConstructor(List::class.java, File::class.java, MutableList::class.java)
                .newInstance(dependenciesKts, cacheDir, classNames) as Runnable
        runnable.run()

        logger.debug("class names: {}", classNames)
        return classNames
    }
}