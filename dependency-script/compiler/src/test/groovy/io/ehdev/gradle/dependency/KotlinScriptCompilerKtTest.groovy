package io.ehdev.gradle.dependency

import io.ehdev.gradle.dependency.compiler.KotlinScriptCompiler
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

        def compiledDir = temporaryFolder.newFolder("compiled")
        def definitions = new DefaultDependencyDefinitions()

        when:
        def clazz = new KotlinScriptCompiler(KotlinScriptCompilerKtTest.classLoader).compileScript(compiledDir, file)

        then:
        noExceptionThrown()
        clazz.newInstance(definitions)
        compiledDir.list().length == 2

        definitions.versions['kotlin'] == '1.1.1'
        definitions.versions.size() == 1

        definitions.library['jooq'].size() == 3

        definitions.library['kotlin'].size() == 2
        definitions.library['kotlin'].findAll { it.version == '1.1.1'}.size() == 2

        definitions.exclude.size() == 1
        definitions.exclude[0].group == 'org.springframework.boot'
        definitions.exclude[0].name == 'spring-boot-starter-tomcat'
    }
}
