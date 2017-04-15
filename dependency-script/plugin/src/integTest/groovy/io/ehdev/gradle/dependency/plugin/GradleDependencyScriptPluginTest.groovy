package io.ehdev.gradle.dependency.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class GradleDependencyScriptPluginTest extends Specification {

    @Rule TemporaryFolder testProjectDir

    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
    }

    def 'will fail if file does not exist'() {
        given:
        buildFile << """
        plugins {
            id 'io.ehdev.gradle.dependency-script'
        }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('tasks', '-s')
                .withPluginClasspath()
                .buildAndFail()

        then:
        result.output.contains('Unable to find any *.dep.kts files')
    }

    def 'will run and compile script'() {
        given:
        buildFile << """
        plugins {
            id 'io.ehdev.gradle.dependency-script'
        }
        """

        testProjectDir.newFolder("gradle")
        testProjectDir.newFile("gradle/main.dep.kts")

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('tasks', '-s')
                .withPluginClasspath()
                .build()

        then:
        result.task(":tasks").outcome == TaskOutcome.SUCCESS
    }

    def 'will run and compile multiple scripts'() {
        given:
        buildFile << """
        plugins {
            id 'io.ehdev.gradle.dependency-script'
        }
        
        task printDependencies {
          doLast {
            assert deps.versions.spring == '5.0.0.M1'
            assert deps.versions.kotlin == '1.1.1'
            assert deps.versions.size() == 2
            assert deps.libraries.size() == 0
          }
        }
        """

        testProjectDir.newFolder("gradle")
        testProjectDir.newFile("gradle/main.dep.kts").text = 'defineVersion("kotlin", "1.1.1")'
        testProjectDir.newFile("gradle/extra.dep.kts").text = 'defineVersion("spring", "5.0.0.M1")'

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('printDependencies', '-s', '-d')
                .withPluginClasspath()
                .build()
        println result.output

        then:
        noExceptionThrown()
        result.task(":printDependencies").outcome == TaskOutcome.SUCCESS
    }
}
