import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("com.github.johnrengelman.shadow")
    id("com.gradle.plugin-publish")
}

fun version(): String = version.toString()
val noRelocate = project.hasProperty("disable-relocation")
if (noRelocate) {
    if (version().contains("-SNAPSHOT")) {
        version = version().substringBefore("-SNAPSHOT") + "-NO-RELOCATE-SNAPSHOT"
    } else {
        version = version() + "-NO-RELOCATE"
    }
}

val shade: Configuration by configurations.creating
configurations.implementation {
    extendsFrom(shade)
}

fun ShadowJar.configureStandard() {
    configurations = listOf(shade)

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:.*:.*"))
    }

    exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA", "OSGI-INF/**", "*.profile", "module-info.class", "ant_tasks/**")

    mergeServiceFiles()
}

val sourcesJar by tasks.existing(AbstractArchiveTask::class) {
    from(
        zipTree(project(":paperweight-lib").tasks
            .named("sourcesJar", AbstractArchiveTask::class)
            .flatMap { it.archiveFile })
    ) {
        exclude("META-INF/**")
    }
}

val prefix = project.name.substringAfter("fandweight-")

gradlePlugin {
    website.set("https://github.com/FandMC/fandweight")
    vcsUrl.set("https://github.com/FandMC/fandweight")
    plugins.create("fandweight-$prefix") {
        id = "com.fandmc.fandweight.$prefix"
        displayName = "fandweight $prefix"
        tags.set(listOf("paper", "leaves", "fand" ,"minecraft"))
    }
}

val shadowJar by tasks.existing(ShadowJar::class) {
    archiveClassifier.set(null as String?)
    configureStandard()

    inputs.property("noRelocate", noRelocate)
    if (noRelocate) {
        return@existing
    }

    val prefix = "paper.libs"
    listOf(
        "com.github.salomonbrys.kotson",
        "com.google.errorprone.annotations",
        "com.google.gson",
        "dev.denwav.hypo",
        "io.sigpipe.jbsdiff",
        "me.jamiemansfield",
        "net.fabricmc",
        "org.apache.commons",
        "org.apache.felix",
        "org.apache.http",
        "org.cadixdev",
        "org.eclipse",
        "org.jgrapht",
        "org.jheaps",
        "org.objectweb.asm",
        "org.osgi",
        "org.tukaani.xz",
        "org.slf4j",
        "codechicken.diffpatch",
        "codechicken.repack"
    ).forEach { pack ->
        relocate(pack, "$prefix.$pack")
    }
}

val isSnapshot = project.version.toString().endsWith("-SNAPSHOT")

publishing {
    repositories {
        val url = if (isSnapshot) {
            "https://repo.leavesmc.org/snapshots/"
        } else {
            "https://repo.leavesmc.org/releases/"
        }

        maven(url) {
            name = "fand"
            credentials(PasswordCredentials::class) {
                username = System.getenv("FAND_USERNAME")
                password = System.getenv("FANDS_PASSWORD")
            }
        }
    }

    publications {
        withType(MavenPublication::class).configureEach {
            pom {
                pomConfig()
            }
        }
    }
}

fun MavenPom.pomConfig() {
    val repoPath = "FandMC/fandweight"
    val repoUrl = "https://github.com/$repoPath"

    name.set("fandweight")
    description.set("Gradle plugin for the FandMC project")
    url.set(repoUrl)
    inceptionYear.set("2020")

    licenses {
        license {
            name.set("LGPLv2.1")
            url.set("$repoUrl/blob/master/license/LGPLv2.1.txt")
            distribution.set("repo")
        }
    }

    issueManagement {
        system.set("GitHub")
        url.set("$repoUrl/issues")
    }

    developers {
        developer {
            id.set("MC20018")
            name.set("FandMC")
            email.set("fand.20018@outlook.com")
            url.set("https://github.com/FandMC")
        }
    }

    scm {
        url.set(repoUrl)
        connection.set("scm:git:$repoUrl.git")
        developerConnection.set("scm:git:git@github.com:$repoPath.git")
    }
}
