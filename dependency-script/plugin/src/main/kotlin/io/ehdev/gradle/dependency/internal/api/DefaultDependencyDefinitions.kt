package io.ehdev.gradle.dependency.internal.api

import io.ehdev.gradle.dependency.api.DependencyDefinitions
import io.ehdev.gradle.dependency.api.DependencyForcing
import io.ehdev.gradle.dependency.api.VersionDefinition
import netflix.nebula.dependency.recommender.provider.RecommendationProvider

class DefaultDependencyDefinitions : DependencyDefinitions {

    val versions: MutableMap<String, String> = mutableMapOf()
    val forcedDependencies: MutableList<DefaultDependencyForcing> = mutableListOf()
    val library: MutableMap<String, List<DependencyNotation>> = mutableMapOf()
    val exclude: MutableList<ExcludedDependency> = mutableListOf()

    override fun defineVersion(name: String, version: String): VersionDefinition {
        versions[name] = version

        return object : VersionDefinition {
            override fun lock(body: DependencyForcing.() -> Unit): VersionDefinition {
                val base = DefaultDependencyForcing(version)
                base.body()
                if(base.group == null) {
                    throw RuntimeException("Group cannot be null")
                }
                forcedDependencies += base
                return this
            }
        }
    }

    override fun defineLibrary(name: String, vararg libraries: Any) {
        defineLibrary(name, libraries.toList())
    }

    override fun defineLibrary(name: String, libraries: List<Any>) {
        library[name] = libraries.map {
            when (it) {
                is String -> it.toDependencyNotation()
                is Map<*, *> -> it.toDependencyNotation()
                else -> throw RuntimeException("${it.javaClass} is an unknown type. Please use String or Map.")
            }
        }
    }

    private fun Map<*, *>.toDependencyNotation(): DependencyNotation {
        return DependencyNotation(this["group"]!! as String, this["name"]!! as String, this["version"] as String?,
                this["configuration"] as String?, this["classification"] as String?)
    }

    private fun String.toDependencyNotation(): DependencyNotation {
        val split = this.split(":")
        if (split.size == 4) {
            return DependencyNotation(split[0], split[1], split[2], split[3], null)
        } else if (split.size == 3) {
            return DependencyNotation(split[0], split[1], split[2], null, null)
        } else if (split.size == 2) {
            return DependencyNotation(split[0], split[1], null, null, null)
        }

        throw RuntimeException("Can't parse `$this`")
    }

    override fun excludeLibrary(group: String, name: String?) {
        exclude += ExcludedDependency(group, name)
    }

    override fun usingVersion(name: String): String = versions[name]!!

    fun buildLibrariesAsMap() : Map<String, List<Map<String, String>>> = library.map { it.key to it.value.map { it.asMap() }}.toMap()

    fun buildRecommendationProvider(): RecommendationProvider {
        val convertedDeps = forcedDependencies.map {
            val key = if (it.name == null) it.group!! else "${it.group}:${it.name}"
            key to it.version
        }.toMap()

        return object : RecommendationProvider {
            override fun setName(name: String?) { /* NOOP */ }

            override fun getVersion(org: String, name: String): String? {
                return convertedDeps["$org:$name"] ?: convertedDeps[org]
            }

            override fun getName(): String = "DependencyDefinitions"
        }
    }
}

