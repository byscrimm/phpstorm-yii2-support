import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.18.1"
}

group = "com.yii2support"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        val localIde = providers.gradleProperty("localIdePath")
        if (localIde.isPresent) local(localIde.get())
        else phpstorm(providers.gradleProperty("platformVersion").get())
        bundledPlugin("com.jetbrains.php")
        bundledPlugin("com.intellij.database")
        bundledPlugin("org.jetbrains.plugins.terminal")
        bundledPlugin("org.jetbrains.plugins.phpstorm-remote-interpreter")
        bundledPlugin("com.jetbrains.twig")
        testFramework(TestFrameworkType.Platform)
        pluginVerifier()
    }
    testImplementation("junit:junit:4.13.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_25
    targetCompatibility = JavaVersion.VERSION_25
}
sourceSets {
    main { java.setSrcDirs(listOf("src")); resources.setSrcDirs(listOf("resources")) }
    test { java.setSrcDirs(listOf("tests", "psi-tests", "gradle-tests")); resources.setSrcDirs(listOf("testData")) }
}

intellijPlatform {
    pluginConfiguration {
        name = "Yii2 Support Extended"
        ideaVersion { sinceBuild = "262"; untilBuild = "262.*" }
    }
    pluginVerification {
        ides { create(IntelliJPlatformType.PhpStorm, providers.gradleProperty("platformVersion").get()) }
    }
}

tasks {
    buildPlugin { dependsOn(check); archiveFileName = "yii2-support-extended-${project.version}.zip" }
    test {
        maxHeapSize = "2g"
        useJUnit()
        systemProperty("java.awt.headless", "true")
        systemProperty("php.allowed.parser.advancement.count", "10000")
        systemProperty("php.use.proper.incremental.psi.builder", "true")
    }
}
