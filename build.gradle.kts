import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.0"
    application
}

group = "me.cdh"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("me.cdh.MainKt")
}

tasks.register<Copy>("copyDependencies") {
    from(configurations.runtimeClasspath)
    into(layout.buildDirectory.dir("libs/lib"))
}

tasks.jar {
    dependsOn("copyDependencies")
    manifest {
        attributes(
            "Main-Class" to "me.cdh.MainKt",
            "Class-Path" to configurations.runtimeClasspath.get().joinToString(" ") { "lib/" + it.name }
        )
    }
}

tasks.register("buildExecutableJar") {
    dependsOn("jar")
    description = "Build executable JAR with dependencies for jDeploy"
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        destinationDirectory.set(layout.buildDirectory.dir("classes/kotlin/main"))
    }
}

tasks.withType<JavaCompile> {
    destinationDirectory.set(layout.buildDirectory.dir("classes/kotlin/main"))
}

tasks.named("compileJava", JavaCompile::class.java) {
    options.compilerArgumentProviders.add(CommandLineArgumentProvider {
        listOf("--patch-module", "me.cdh=${sourceSets["main"].output.asPath}")
    })
}