plugins {
    application

    id("org.openjfx.javafxplugin") version "0.1.0"

    id("io.github.goooler.shadow") version "8.1.7"
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // This dependency is used by the application.
    implementation(libs.guava)

    // JCEF Maven for Chromium embedding
    implementation("me.friwi:jcefmaven:141.0.10")
}

// Apply a specific Java toolchain. 
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// JavaFX Configuration
javafx {
    version = "21"
    modules("javafx.controls", "javafx.graphics", "javafx.base", "javafx.swing")
}

application {
    // Define the main class for the application.
    mainClass = "ingstudios.turtlebrowse.Main"
}

tasks.shadowJar {
    manifest {
        attributes["Main-Class"] = "ingstudios.turtlebrowse.Main"
    }
    mergeServiceFiles()
}

tasks.register<Exec>("jpackage") {
    dependsOn(tasks.shadowJar)

    val jarFile = tasks.shadowJar.get().archiveFile.get().asFile
    val icon = file("src/main/resources/logo_full_trans.ico")
    val outputDir = file("build/jpackage")

    commandLine(
        "jpackage",
        "--input", jarFile.parent,
        "--main-jar", jarFile.name,
        "--main-class", "ingstudios.turtlebrowse.Main",
        "--type", "exe",
        "--name", "Turtlebrowse",
        "--app-version", "0.0.1",
        "--vendor", "(ing) Studios",
        "--icon", icon.absolutePath,
        "--dest", outputDir.absolutePath,
        "--java-options", "-Dapp.dir=\$APPDIR",
        "--win-dir-chooser",
        "--win-menu",
        "--win-shortcut",
        "--win-shortcut-prompt",
        "--install-dir", "ingStudios\\Turtlebrowse"
    )
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}