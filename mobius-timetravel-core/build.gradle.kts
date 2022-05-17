plugins { kotlin("jvm") }

dependencies {
    implementation(kotlin("stdlib"))
    api(libs.mobius.core)

    testImplementation(libs.junit)
}
