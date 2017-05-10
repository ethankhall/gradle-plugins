package io.ehdev.gradle.dependency

import io.ehdev.gradle.dependency.internal.DependencyScriptExecutor
import io.ehdev.gradle.dependency.internal.DependencyScriptRefs
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class KotlinScriptCompilerKtTest extends Specification {

    @Rule TemporaryFolder temporaryFolder

    def 'can load script'() {
        setup:
        def resource = KotlinScriptCompilerKtTest.classLoader.getResourceAsStream('samples/simpleCompile.dep.kts')
        def file = temporaryFolder.newFile('test-kotlin-file.kts')
        file.text = resource.text

        def sharedFolder = temporaryFolder.newFolder("cache")
        def jarFile = new File(sharedFolder, "compiled.jar")
        def classFile = new File(sharedFolder, "compiled.txt")
        def refs = new DependencyScriptRefs(jarFile, classFile, [])

        when:
        def definitions = new DependencyScriptExecutor(refs).executeScripts([file])

        then:
        noExceptionThrown()
        jarFile.exists()

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
