plugins {
    application
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("plugin.serialization") version "2.1.20"
}

application {
    mainClass.set("org.darkan.lobby.MainKt")
    tasks.run.get().workingDir = rootProject.projectDir
}

dependencies {
    implementation(project(":core"))
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.darkan.lobby.MainKt"
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    isZip64 = true
    mergeServiceFiles()
    archiveBaseName.set("lobby-server")
}

publishing {
    val ciProjectId: String? = System.getenv("CI_PROJECT_ID")
    val ciJobToken: String? = System.getenv("CI_JOB_TOKEN")
    val ciPipelineId: String? = System.getenv("CI_PIPELINE_ID")
    if (ciProjectId == null || ciJobToken == null || ciPipelineId == null) {
        println("Failed to get project id, job token or pipeline id.")
        return@publishing
    }

    publications.create<MavenPublication>("library") {
        version = "${project.version}-${ciPipelineId}"
        artifact(tasks.shadowJar)
    }

    repositories {
        maven {
            name = "GitLab"
            url = uri("https://gitlab.com/api/v4/projects/${ciProjectId}/packages/maven")
            credentials(HttpHeaderCredentials::class) {
                name = "Job-Token"
                value = ciJobToken
            }
            authentication {
                create<HttpHeaderAuthentication>("header")
            }
        }
    }
}