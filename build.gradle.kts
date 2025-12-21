import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

plugins {
    id("java")
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.17" apply false
    id("io.github.goooler.shadow") version "8.1.8"
}

/* =========================
   Versions & metadata
   ========================= */

class NMSVersion(val nmsVersion: String, val serverVersion: String)
infix fun String.toNms(that: String) = NMSVersion(this, that)

val SUPPORTED_VERSIONS = listOf(
    "v1_20_R1" toNms "1.20.1-R0.1-SNAPSHOT",
    "v1_20_R2" toNms "1.20.2-R0.1-SNAPSHOT",
    "v1_20_R3" toNms "1.20.4-R0.1-SNAPSHOT",
    "v1_20_R4" toNms "1.20.6-R0.1-SNAPSHOT",
    "v1_21_R1" toNms "1.21.1-R0.1-SNAPSHOT",
    "v1_21_R2" toNms "1.21.3-R0.1-SNAPSHOT",
    "v1_21_R3" toNms "1.21.4-R0.1-SNAPSHOT",
    "v1_21_R4" toNms "1.21.5-R0.1-SNAPSHOT",
    "v1_21_R5" toNms "1.21.8-R0.1-SNAPSHOT"
)

val pluginVersion: String by project

val commandApiVersion = "10.1.2"
val adventureVersion = "4.17.0"
val platformVersion = "4.3.4"
val googleGsonVersion = "2.10.1"
val apacheLang3Version = "3.14.0"

group = "io.th0rgal"
version = pluginVersion

/* =========================
   Global configuration
   ========================= */

allprojects {
    apply(plugin = "java")
}

/* =========================
   Shared dependencies
   ========================= */

subprojects {
    dependencies {
        val actionsVersion = "1.0.0-SNAPSHOT"

        compileOnly("gs.mclo:java:2.2.1")

        compileOnly("net.kyori:adventure-text-minimessage:$adventureVersion")
        compileOnly("net.kyori:adventure-text-serializer-plain:$adventureVersion")
        compileOnly("net.kyori:adventure-text-serializer-ansi:$adventureVersion")
        compileOnly("net.kyori:adventure-platform-bukkit:$platformVersion")

        // ✅ ProtocolLib – bản STABLE, chạy Folia 1.21.8
        compileOnly("com.comphenix.protocol:ProtocolLib:5.2.0")

        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("me.gabytm.util:actions-core:$actionsVersion")
        compileOnly("org.springframework:spring-expression:6.0.6")
        compileOnly("io.lumine:Mythic-Dist:5.7.0-SNAPSHOT")
        compileOnly("io.lumine:MythicCrucible:1.6.0-SNAPSHOT")
        compileOnly("com.sk89q.worldedit:worldedit-bukkit:7.2.9")
        compileOnly("commons-io:commons-io:2.11.0")
        compileOnly("com.google.code.gson:gson:$googleGsonVersion")
        compileOnly("org.apache.commons:commons-lang3:$apacheLang3Version")
        compileOnly("org.joml:joml:1.10.5")

        implementation("team.unnamed:creative-api:1.7.3") {
            exclude(group = "net.kyori")
        }
        implementation("dev.jorel:commandapi-bukkit-shade:$commandApiVersion")
        implementation("org.bstats:bstats-bukkit:3.0.0")
        implementation("org.glassfish:javax.json:1.1.4")
        implementation("io.th0rgal:protectionlib:1.8.0")
        implementation("com.github.stefvanschie.inventoryframework:IF:0.10.12")
        implementation("com.jeff-media:custom-block-data:2.2.2")
        implementation("com.jeff-media:MorePersistentDataTypes:2.4.0")
        implementation("com.jeff-media:persistent-data-serializer:1.0")
        implementation("org.jetbrains:annotations:24.1.0") {
            isTransitive = false
        }
        implementation("dev.triumphteam:triumph-gui:3.1.10") {
            exclude(group = "net.kyori")
        }
    }
}

/* =========================
   Root dependencies
   ========================= */

dependencies {
    implementation(project(":core"))
    SUPPORTED_VERSIONS.forEach {
        implementation(project(path = ":${it.nmsVersion}", configuration = "reobf"))
    }
}

/* =========================
   Java toolchain
   ========================= */

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

/* =========================
   Tasks
   ========================= */

tasks {

    compileJava {
        options.encoding = "UTF-8"
    }

    shadowJar {
        SUPPORTED_VERSIONS.forEach {
            dependsOn(":${it.nmsVersion}:reobfJar")
        }

        archiveFileName.set("oraxen-$pluginVersion.jar")

        relocate("org.bstats", "io.th0rgal.oraxen.shaded.bstats")

        manifest {
            attributes(
                mapOf(
                    "Built-By" to System.getProperty("user.name"),
                    "Version" to pluginVersion,
                    "Build-Timestamp" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                        .format(Date.from(Instant.now()))
                )
            )
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

/* =========================
   plugin.yml (generated)
   ========================= */

bukkit {
    main = "io.th0rgal.oraxen.OraxenPlugin"
    name = "Oraxen"
    version = pluginVersion
    apiVersion = "1.18"

    // ✅ BẮT BUỘC CHO FOLIA
    foliaSupported = true

    softDepend = listOf(
        "ProtocolLib",
        "PlaceholderAPI",
        "WorldEdit",
        "MythicMobs",
        "MMOItems"
    )
}
