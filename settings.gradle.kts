rootProject.name = "mobius-timetravel"
include("mobius-timetravel-core")
include("mobius-timetravel-server-http")
include("mobius-timetravel-server-noop")
include("examples:Counter")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
