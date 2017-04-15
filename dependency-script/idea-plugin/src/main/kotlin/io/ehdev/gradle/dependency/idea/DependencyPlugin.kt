package io.ehdev.gradle.dependency.idea

import com.intellij.openapi.project.Project
import com.intellij.openapi.projectRoots.JavaSdk
import com.intellij.openapi.projectRoots.ProjectJdkTable
import com.intellij.openapi.projectRoots.ex.PathUtilEx
import com.intellij.util.PathUtil.getJarPathForClass
import io.ehdev.gradle.dependency.compiler.KotlinDependencyScript
import io.ehdev.gradle.dependency.compiler.getClassesForClasspath
import org.jetbrains.kotlin.script.ScriptTemplatesProvider

class DependencyPlugin(val project: Project) : ScriptTemplatesProvider {
    override val isValid: Boolean = true

    override val dependenciesClasspath: Iterable<String> by lazy {
        getClassesForClasspath().map(::getJarPathForClass)
    }

    override val environment: Map<String, Any?>? get() = mapOf(
            "USE_NULL_RESOLVE_SCOPE" to true,
            "sdk" to getScriptSDK(project)
    )

    private fun getScriptSDK(project: Project): String? {
        val jdk = PathUtilEx.getAnyJdk(project) ?:
                ProjectJdkTable.getInstance().allJdks.firstOrNull { sdk -> sdk.sdkType is JavaSdk }

        return jdk?.homePath
    }

    override val id: String = "DependencyScriptPlugin"
    override val templateClassNames: Iterable<String> get() = listOf(KotlinDependencyScript::class.qualifiedName!!)

}