package io.ehdev.gradle.dependency.internal

import io.ehdev.gradle.dependency.api.DependencyDefinitions
import io.ehdev.gradle.dependency.internal.api.DefaultDependencyDefinitions
import org.codehaus.groovy.tools.RootLoader
import org.gradle.api.logging.Logging
import java.io.File
import java.net.URLClassLoader

class DependencyScriptExecutor(val scriptRefs: DependencyScriptRefs) {
    private val logger = Logging.getLogger(DependencyScriptExecutor::class.java)!!

    fun executeScripts(dependencyFiles: List<File>): DefaultDependencyDefinitions {
        if (!scriptRefs.jarFile.exists()) {
            if (!scriptRefs.jarFile.parentFile.exists()) {
                scriptRefs.jarFile.parentFile.mkdirs()
            }
            compileScriptToClass(dependencyFiles, scriptRefs)
        }

        logger.lifecycle("Executing dependency script")

        return executeClassesInJar()
    }

    private fun compileScriptToClass(dependenciesKts: List<File>, scriptContainer: DependencyScriptRefs) {
        val loader = RootLoader(scriptContainer.compilerJarsAsArray, this::class.java.classLoader)

        logger.debug("compiler jars: {}", scriptContainer.compilerJars)
        logger.lifecycle("Compile dependency script")

        val classNames = mutableListOf<String>()
        val runnableClass = loader.loadClass("io.ehdev.gradle.dependency.compiler.CompilerRunnable")
        val runnable = runnableClass
                .getConstructor(List::class.java, File::class.java, MutableList::class.java)
                .newInstance(dependenciesKts, scriptContainer.jarFile, classNames) as Runnable
        runnable.run()

        scriptContainer.updateClassNames(classNames)

        logger.debug("new jar {} exists: {}", scriptContainer.jarFile, scriptContainer.jarFile.exists())
    }

    private fun executeClassesInJar(): DefaultDependencyDefinitions {
        logger.lifecycle("Executing dependency script")

        val classLoader = URLClassLoader(scriptRefs.compilerJarsAsArray + scriptRefs.jarFile.toURI().toURL(),
                this::class.java.classLoader)

        val definitions = DefaultDependencyDefinitions()

        scriptRefs.readClassNames().forEach {
            classLoader.loadClass(it).getConstructor(DependencyDefinitions::class.java).newInstance(definitions)
        }

        return definitions
    }
}