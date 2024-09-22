import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.serialization") version "2.0.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("com.vanniktech.maven.publish") version "0.29.0"
    id("signing")
}

group = "org.olymika"
version = "0.0.5"

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

signing {
    sign(publishing.publications)
}

mavenPublishing {
    signAllPublications()

    JavadocJar.None()
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    coordinates("org.olymika", "chzzk-with-me", version.toString())

    pom {
        name = "chzzk-with-me"
        description = "chzzk coroutine sdk"
        inceptionYear = "2024"
        url = "olymika.org"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                id by "olymika"
                name by "olymika Teams"
                url by "https://github.com/olymika"
            }
        }
        scm {
            url = "https://github.com/olymika/chzzk-with-me"
            connection = "scm:git:git://github.com/olymika/chzzk-with-me.git"
            developerConnection = "scm:git:ssh://github.com/olymika/chzzk-with-me.git"
        }
    }
}

infix fun <T> Property<T>.by(value: T) {
    set(value)
}
