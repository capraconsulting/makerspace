/**
 * Gradle build file.
 * Building the microservice with the Kotlin plugin for gradle.
 *
 * @see <a href="https://kotlinlang.org/docs/reference/using-gradle.html">Using Gradle in Official Kotlin doc.</a
 */
plugins {
    kotlin("jvm")
    kotlin("kapt")
    kotlin("plugin.allopen")

    id("io.micronaut.application") version "4.4.0"
    id("io.micronaut.test-resources") version "4.4.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.1"
}

group = "io.nordlab.legocity.server"

val version: String by project

val micronautVersion: String by project
val targetJvmVersion: String by project

fun getProperty(name: String) = if (project.properties[name] != null) {
    project.properties[name].toString()
} else {
    System.getenv(name)
}

repositories {
    mavenCentral()
}

micronaut {
    version(micronautVersion)
    runtime("netty")
    testRuntime("kotest5")
    processing {
        incremental(true)
        annotations("io.nordlab.legocity.*")
    }
}

kotlin {
    sourceSets.all {
        languageSettings {
            languageVersion = "2.0"
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    /**
     * Kotlin dependencies.
     */
    implementation(kotlin("reflect"))

    /**
     * Micronaut framework dependencies.
     *
     * micronaut-inject-java and micronaut-validation are omitted
     * due to the micronaut application plugin adding them by default.
     */
    annotationProcessor("io.micronaut.validation:micronaut-validation-processor")
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor")

    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut.security:micronaut-security")

    implementation("io.micronaut.mqtt:micronaut-mqtt-hivemq")
    implementation("org.yaml:snakeyaml:2.0")

    implementation("io.micronaut.serde:micronaut-serde-jackson")
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-inject")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-management")
    implementation("io.micronaut.security:micronaut-security")
    implementation("io.micronaut.problem:micronaut-problem-json")

    /**
     * Third-party dependencies.
     */
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("io.swagger.core.v3:swagger-annotations")

    /**
     * kotlin coroutines
     */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")

    /**
     * Micronaut supports context propagation from Reactor’s context to coroutine context.
     * To enable this propagation you need to include following dependency
     */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.1")

    /**
     * Test dependency configurations.
     */
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
    testImplementation("org.wiremock:wiremock:3.6.0")
    testImplementation("io.mockk:mockk")
    testImplementation("io.micronaut:micronaut-inject-java")
    testImplementation("org.assertj:assertj-core")
    testImplementation("io.micronaut.test:micronaut-test-kotest5")
    testImplementation("io.kotest:kotest-runner-junit5-jvm")
}

application {
    mainClass.set("io.nordlab.legocity.server.Application")
}

tasks {

    // configure graalvm native-image to include reflection classes.
    // classes not auto-discovered needs to be added manually as either Proxy, Resource or reflect config json files.
    graalvmNative {
        binaries {
            named("main") {
//                verbose.set(true) // To see what configurations are auto-discovere by native-image when starting build.
                buildArgs.add("-H:+ReportUnsupportedElementsAtRuntime")
                buildArgs.add("-H:ClassInitialization=org.slf4j:build_time")
            }
        }
    }

    // use Google Distroless mostly-static image when generating the native-image build Dockerfile.
    dockerfileNative {
        baseImage("gcr.io/distroless/cc-debian11")
    }

    test {
        useJUnitPlatform()
        systemProperty("micronaut.environments", "test")
        systemProperty("micronaut.env.deduction", false)
    }

    runnerJar {
        manifest {
            attributes(
                mapOf(
                    "Implementation-Title" to rootProject.name,
                    "Implementation-Version" to project.version,
                ),
            )
        }
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }
    }
}