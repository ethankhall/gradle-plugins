package io.ehdev.gradle.dependency.plugin

import io.ehdev.gradle.dependency.internal.DependencyScriptExecutor
import io.ehdev.gradle.dependency.internal.DependencyScriptRefs
import io.ehdev.gradle.dependency.internal.api.DefaultDependencyDefinitions
import io.ehdev.gradle.dependency.internal.md5Digest
import netflix.nebula.dependency.recommender.DependencyRecommendationsPlugin
import netflix.nebula.dependency.recommender.provider.RecommendationProvider
import netflix.nebula.dependency.recommender.provider.RecommendationProviderContainer
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Properties

open class GradleDependencyScriptPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(DependencyRecommendationsPlugin::class.java)

        val dependencyDefinitions =
                if (project.rootProject.extensions.findByType(DefaultDependencyDefinitions::class.java) == null) {
                    val dependencyDefinitions = getDependencyDefinitions(project)
                    project.rootProject.extensions.add(dependencyDefinitions.javaClass.simpleName, dependencyDefinitions)
                    dependencyDefinitions
                } else {
                    project.rootProject.extensions.findByType(DefaultDependencyDefinitions::class.java)
                }

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
        val cachedJar = File(project.rootDir, "/.gradle/dependency-script/$hex.jar")
        val classListing = File(project.rootDir, "/.gradle/dependency-script/$hex.txt")
        val compilerJars = extractJars(File(project.rootDir, ".gradle/dependency-script/" + findVersion()))

        val refs = DependencyScriptRefs(cachedJar, classListing, compilerJars)

        return DependencyScriptExecutor(refs).executeScripts(dependencyFiles)
    }

    private fun extractJars(directory: File): List<URL> {
        directory.mkdirs()
        return listOf("dependency-script-compiler.jar", "kotlin-compiler-embeddable.jar").map {
            val resource = GradleDependencyScriptPlugin::class.java.classLoader.getResourceAsStream("compiler/$it")
            val dest = directory.toPath().resolve(it)
            if (!Files.exists(dest)) {
                val tempFile = directory.toPath().resolve(it + ".tmp")
                Files.copy(resource, tempFile, StandardCopyOption.REPLACE_EXISTING)
                Files.move(tempFile, dest, StandardCopyOption.ATOMIC_MOVE)
            }
            dest.toUri().toURL()
        }
    }
}