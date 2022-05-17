rootProject.name = "mobius-timetravel"
include("mobius-timetravel-core")
include("mobius-timetravel-server-http")
include("mobius-timetravel-server-noop")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
