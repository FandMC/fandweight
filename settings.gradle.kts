plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "fandweight"

include("paperweight-lib", "fandweight-patcher")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
