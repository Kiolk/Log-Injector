import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

version = "1.0.1"
group = "com.github.kiolk.loggingplugin"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugin("com.intellij.java")
        bundledPlugin("org.jetbrains.kotlin")
        instrumentationTools()
        pluginVerifier()
        testFramework(TestFrameworkType.Platform)
        zipSigner()
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.10.2")
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        id = "com.github.kiolk.loggingplugin"
        name = "Log Injector"
        vendor {
            name = "Yauheni Slizh"
        }
        ideaVersion {
            sinceBuild = "243"
            untilBuild = "253.*"
        }
    }

    pluginVerification {
        ides {
            ide("2024.3")
        }
    }

    signing {
        val cert = providers.environmentVariable("CERTIFICATE_CHAIN")
            .orElse(providers.gradleProperty("certificateChain"))
            .orNull?.replace("\\n", "\n")

        val key = providers.environmentVariable("PRIVATE_KEY")
            .orElse(providers.gradleProperty("privateKey"))
            .orNull?.replace("\\n", "\n")

        val keyPassword = providers.environmentVariable("PRIVATE_KEY_PASSWORD")
            .orElse(providers.gradleProperty("privateKeyPassword"))
            .orNull

        if (cert != null && key != null && keyPassword != null) {
            certificateChain.set(cert)
            privateKey.set(key)
            password.set(keyPassword)
        }
    }

    publishing {
        token.set(providers.environmentVariable("PUBLISH_TOKEN").orElse(providers.gradleProperty("publishToken")))
        channels.set(listOf(providers.environmentVariable("PUBLISH_CHANNEL").getOrElse("default")))
    }
}

// Kotlin JVM toolchain is automatically configured by IntelliJ Platform Plugin
// kotlin {
//     jvmToolchain(21)
// }

ktlint {
    // Correcting property names if they were causing errors
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    buildSearchableOptions {
        enabled = false
    }
}
