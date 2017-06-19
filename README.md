# gradle-plugins

A collections of plugins that I apply to all my projects.

## Usage
### Settings.gradle

```
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url 'https://dl.bintray.com/ethankhall/gradle-plugins/' }
    }
    resolutionStrategy {
        def gradlePlugins = new JsonSlurper().parse(new URL("http://api.crom.tech/api/v1/project/ethankhall/repo/gradle-plugins/version/latest"))
        eachPlugin { details ->
            def id = details.requested.id.id
            if (id in ['io.ehdev.gradle.dependency-resolve', 'io.ehdev.gradle.klint',
                       'io.ehdev.gradle.build-dir', 'io.ehdev.gradle.dependency-script'] && details.requested.version == 'LATEST') {
                println "Using the latest version of $id, which is ${gradlePlugins.version}"
                details.useVersion(gradlePlugins.version)
            }
        }
    }
}
```

### Build.gradle
```
plugins {
    id 'io.ehdev.gradle.klint' version 'LATEST' apply false
    id 'io.ehdev.gradle.dependency-resolve' version 'LATEST' apply false
    id 'io.ehdev.gradle.build-dir' version 'LATEST' apply false
    id 'io.ehdev.gradle.dependency-script' version 'LATEST' apply false
}

allprojects {
    apply plugin: 'io.ehdev.gradle.dependency-resolve'
    apply plugin: 'io.ehdev.gradle.build-dir'

    group = 'io.ehdev.gradle'

    plugins.withType(org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper) {
        plugins.apply('io.ehdev.gradle.klint')
    }
}
```
