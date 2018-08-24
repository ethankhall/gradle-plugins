package io.ehdev.gradle.dependency.plugin

import io.ehdev.gradle.dependency.internal.DependencyTomlResolver
import io.ehdev.gradle.dependency.internal.api.DefaultDependencyDefinitions
import netflix.nebula.dependency.recommender.DependencyRecommendationsPlugin
import netflix.nebula.dependency.recommender.provider.RecommendationProvider
import netflix.nebula.dependency.recommender.provider.RecommendationProviderContainer
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

open class GradleDependencyTomlPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.apply(DependencyRecommendationsPlugin::class.java)

        val dependencyDefinitions =
                if (project.rootProject.extensions.findByType(DefaultDependencyDefinitions::class.java) == null) {
                    val dependencyDefinitions = getDependencyDefinitions(project)
                    project.rootProject.extensions.add(dependencyDefinitions.javaClass.simpleName, dependencyDefinitions)
                    dependencyDefinitions
                } else {
                    project.rootProject.extensions.findByType(DefaultDependencyDefinitions::class.java)!!
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

    private fun getDependencyDefinitions(project: Project): DefaultDependencyDefinitions {
        val tomlFile = File(project.rootDir, "gradle${File.separatorChar}dependencies.toml")
        if (!tomlFile.exists()) {
            throw GradleException("Unable to find ${tomlFile.absolutePath}")
        }

        return DependencyTomlResolver.resolve(tomlFile)
    }
}