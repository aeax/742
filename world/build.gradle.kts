plugins {
    application
}

application {
    mainClass.set("org.darkan.world.MainKt")
    tasks.run.get().workingDir = rootProject.projectDir
}

dependencies {
    implementation(project(":core"))
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "org.darkan.world.MainKt"
    }
}