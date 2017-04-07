package io.ehdev.gradle.dependency

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class KotlinScriptCompilerKtTest extends Specification {

    @Rule TemporaryFolder temporaryFolder

    def 'can load script'() {
        setup:
        def resource = KotlinScriptCompilerKtTest.classLoader.getResourceAsStream('samples/simpleCompile.kts.kt')
        def file = temporaryFolder.newFile('test-kotlin-file.kts')
        file.text = resource.text

        when:
        KotlinScriptCompilerKt.compileScript(file, KotlinScriptCompilerKtTest.classLoader, new QueueingMessageCollector())

        then:
        noExceptionThrown()
    }
}
