buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.vanniktech:gradle-maven-publish-plugin:0.19.0")
    }
}

plugins {
    kotlin("jvm") version "1.6.21" apply false
    id("com.android.application") version "7.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.6.20" apply false

    id("com.diffplug.spotless") version "6.4.2"
}

allprojects {
    beforeEvaluate {
        repositories {
            google()
            mavenCentral()
        }

        apply(plugin = "com.diffplug.spotless")
        configure<com.diffplug.gradle.spotless.SpotlessExtension> {
            kotlinGradle {
                target("*.gradle.kts")
                ktlint("0.45.1")
                ktfmt().kotlinlangStyle()
            }
        }

        tasks.withType(Test::class.java).configureEach {
            useJUnitPlatform()
        }

        if (this.path.startsWith(":examples").not()) {
            apply(plugin = "com.vanniktech.maven.publish")
        }

        plugins.whenPluginAdded {
            if (this is JavaPlugin) {
                configure<JavaPluginExtension> {
                    sourceCompatibility = JavaVersion.VERSION_1_8
                    targetCompatibility = JavaVersion.VERSION_1_8
                }

                configure<com.diffplug.gradle.spotless.SpotlessExtension> {
                    kotlin {
                        // by default the target is every '.kt' and '.kts` file in the java sourcesets
                        ktfmt().kotlinlangStyle()   // has its own section below
                        ktlint()   // has its own section below
                    }
                }

                configure<com.vanniktech.maven.publish.MavenPublishBaseExtension> {
                    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.S01)
                }
            }
        }
    }
}
