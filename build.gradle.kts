plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "org.olymika"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.jackson)
    implementation(libs.bundles.okhttp)
    implementation(libs.logback.classic)
    runtimeOnly(libs.ktor.serialization.jackson)
    runtimeOnly(libs.coroutines.core)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

val jvmVersion = JavaVersion.current()

kotlin {
    jvmToolchain {
        when (jvmVersion) {
            JavaVersion.VERSION_11 -> {
                languageVersion = JavaLanguageVersion.of(11)
            }

            JavaVersion.VERSION_17 -> {
                languageVersion = JavaLanguageVersion.of(17)
            }

            JavaVersion.VERSION_21 -> {
                languageVersion = JavaLanguageVersion.of(21)
            }

            else -> {
                languageVersion = JavaLanguageVersion.of(8)
            }
        }
    }
}
