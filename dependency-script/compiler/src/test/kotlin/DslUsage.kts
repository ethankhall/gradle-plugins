import io.ehdev.gradle.dependency.api.DependencyDefinitions

fun take(body: DependencyDefinitions.() -> Unit) {

}

take({
    defineVersion("kotlin", "1.1.1").lock { withGroup("org.jetbrains.kotlin") }

    defineLibrary("jooq", listOf("org.jooq:jooq", "org.jooq:jooq-meta", "org.jooq:jooq-codegen"))
    defineLibrary("kotlin", listOf(
            "org.jetbrains.kotlin:kotlin-stdlib-jre8:${usingVersion("kotlin")}",
            "org.jetbrains.kotlin:kotlin-runtime:${usingVersion("kotlin")}"))

    excludeLibrary("org.springframework.boot", "spring-boot-starter-tomcat")
})