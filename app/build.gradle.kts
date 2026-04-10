plugins {
    application

    id("org.openjfx.javafxplugin") version "0.1.0"

    id("com.gradleup.shadow") version "9.3.2"

    id("org.panteleyev.jpackageplugin") version "2.0.1"
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
    implementation("me.friwi:jcefmaven:143.0.14")

    // Material icons from Ikonli
    implementation("org.kordamp.ikonli:ikonli-javafx:12.4.0")
    implementation("org.kordamp.ikonli:ikonli-material2-pack:12.4.0")

    // Monet theme builder for JavaFX
    implementation("org.glavo:MonetFX:0.4.0")
}

// Apply a specific Java toolchain. 
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

// JavaFX Configuration
javafx {
    version = "24"
    modules("javafx.controls", "javafx.graphics", "javafx.base", "javafx.swing")
}

application {
    mainClass.set("dev.ingstudios.turtlebrowse.Main")
    applicationDefaultJvmArgs = listOf(
        "--enable-native-access=ALL-UNNAMED,javafx.graphics",
        "--add-modules=jdk.incubator.vector",
        "-Dglass.platform=gtk",
        "-Djava.library.path=build/natives",
        "-Dsun.java2d.opengl=false",
        "-Dsun.java2d.xrender=false",
        "-Dswing.defaultlaf=javax.swing.plaf.metal.MetalLookAndFeel"
    )
}


tasks.jpackage {
    verbose = true

    runtimeImage = file(System.getProperty("java.home"))

    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(24)
    }
    
    appName = "Turtlebrowse"
    vendor = "(ing) Studios"
    appVersion = "0.1.0"
    copyright = "2026 (ing) Studios and Ethan Lee"

    input = layout.buildDirectory.dir("libs")
    
    mainJar = "app-all.jar"
    dependsOn(tasks.shadowJar)
    mainClass = "dev.ingstudios.turtlebrowse.Main"

    destination = layout.buildDirectory.dir("dist")

    icon = when {
        System.getProperty("os.name").lowercase().contains("win") -> 
            layout.projectDirectory.file("src/main/resources/icon.ico")
        System.getProperty("os.name").lowercase().contains("mac") -> 
            layout.projectDirectory.file("src/main/resources/icon.icns")
        else -> 
            layout.projectDirectory.file("src/main/resources/logo_full_trans.png")
    }

    javaOptions = listOf("--enable-native-access=ALL-UNNAMED,javafx.graphics", "-Dapp.dir=\$APPDIR")

    windows {
        type = org.panteleyev.jpackage.ImageType.EXE
        winDirChooser = true
        winMenu = true
        winShortcut = true
        winShortcutPrompt = true
        installDir = "ingStudios\\Turtlebrowse"
        winUpgradeUuid = "6f701d42-0c33-443a-98fa-6543c3e7b3df"
        winConsole = true
    }

    mac {
        type = org.panteleyev.jpackage.ImageType.PKG
        macPackageName = "Turtlebrowse"
        macPackageIdentifier = "dev.ingstudios.turtlebrowse"
    }

    linux {
        type = org.panteleyev.jpackage.ImageType.DEB
        linuxShortcut = true
        linuxAppCategory = "Network;WebBrowser;"
        linuxPackageName = "turtlebrowse"
        linuxDebMaintainer = "contact@ingstudios.dev"
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
}

tasks.named<JavaExec>("run") {
    environment("_JAVA_AWT_WM_NONREPARENTING", "0")
    environment("GDK_BACKEND", "x11")
    environment("WAYLAND_DISPLAY", "")
}