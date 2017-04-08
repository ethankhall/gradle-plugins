package io.ehdev.gradle.dependency

import io.ehdev.gradle.dependency.compiler.KotlinScriptCompiler
import io.ehdev.gradle.dependency.compiler.KotlinDependencyScript
import io.ehdev.gradle.dependency.internal.api.DefaultDependencyDefinitions
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class KotlinScriptCompilerKtTest extends Specification {

    @Rule TemporaryFolder temporaryFolder

    def 'can load script'() {
        setup:
        def resource = KotlinScriptCompilerKtTest.classLoader.getResourceAsStream('samples/simpleCompile.kts')
        def file = temporaryFolder.newFile('test-kotlin-file.kts')
        file.text = resource.text

        when:
        def clazz = new KotlinScriptCompiler(KotlinScriptCompilerKtTest.classLoader).compileScript(file)

        then:
        noExceptionThrown()
        def instance = clazz.newInstance(new DefaultDependencyDefinitions()) as KotlinDependencyScript
    }
}
