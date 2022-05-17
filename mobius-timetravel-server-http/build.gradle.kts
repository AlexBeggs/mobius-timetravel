plugins { kotlin("jvm") }

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":mobius-timetravel-core"))
    implementation(libs.jetty.server)
}
