plugins {
    id("java")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.8"
    id("org.ajoberstar.grgit.service") version "5.2.0"
}

val pluginVersion = project.property("pluginVersion") as String

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        archiveFileName.set("oraxen-$pluginVersion.jar")
    }
    build {
        dependsOn(shadowJar)
    }
}

dependencies {
    // ✅ Folia API
    compileOnly("dev.folia:folia-api:1.21.8-R0.1-SNAPSHOT")

    // ✅ ProtocolLib dùng được cho Folia
    compileOnly("com.comphenix.protocol:ProtocolLib:5.2.0")
}

/* publishing giữ nguyên như bạn gửi */
