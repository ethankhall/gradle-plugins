package io.ehdev.gradle.dependency.internal

import com.moandjiezana.toml.Toml
import io.ehdev.gradle.dependency.internal.api.DefaultDependencyDefinitions
import org.gradle.api.GradleException
import java.io.File

object DependencyTomlResolver {
    fun resolve(tomlFile: File) : DefaultDependencyDefinitions {
        val definitions = DefaultDependencyDefinitions()
        val toml = Toml().read(tomlFile)

        resolveVersions(toml, definitions)
        resolveLibraries(toml, definitions)
        resolveExclude(toml, definitions)

        return definitions
    }

    private fun resolveExclude(toml: Toml, definitions: DefaultDependencyDefinitions) {
        val excludeTable = toml.getTables("exclude")
        for (exclude in excludeTable) {
            definitions.excludeLibrary(exclude.getString("group"), exclude.getString("module"))
        }
    }

    private fun resolveLibraries(toml: Toml, definitions: DefaultDependencyDefinitions) {
        val librariesTable = toml.getTable("libraries")
        for ((library, value) in librariesTable.entrySet()) {
            when (value) {
                is List<*> -> definitions.defineLibrary(library, value)
                is String -> definitions.defineLibrary(library, value)
                else -> throw GradleException("Unknown type! " + value.javaClass)
            }
        }
    }

    private fun resolveVersions(toml: Toml, definitions: DefaultDependencyDefinitions) {
        val versionTable = toml.getTable("versions")
        for (name in versionTable.toMap().keys) {
            val entryTable = versionTable.getTable(name)
            val version = entryTable.getString("version")
            val group = entryTable.getString("group")

            val definedVersion = definitions.defineVersion(name, version)
            if (group != null) {
                definedVersion.lock { withGroup(group) }
            }
        }
    }

}