rootProject.name = "oraxen"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.mineinabyss.com/releases")
    }
}

dependencyResolutionManagement {

    // 👉 Khuyến nghị bật để tránh repo trôi lung tung
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)

    repositories {
        // 🔥 BẮT BUỘC – ProtocolLib, Kyori, Gson, v.v.
        mavenCentral()

        // MineInAbyss
        maven("https://repo.mineinabyss.com/releases")
        maven("https://repo.mineinabyss.com/snapshots")

        // Paper / Spigot
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven("https://oss.sonatype.org/content/repositories/snapshots")
        maven("https://libraries.minecraft.net/")

        // Plugin ecosystem
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://maven.elmakers.com/repository/")
        maven("https://repo.triumphteam.dev/snapshots")
        maven("https://mvn.lumine.io/repository/maven-public/") {
            metadataSources { artifact() }
        }
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots")

        // Oraxen / partners
        maven("https://repo.oraxen.com/releases")
        maven("https://repo.oraxen.com/snapshots")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://jitpack.io")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://repo.codemc.org/repository/maven-public/")

        mavenLocal()
    }

    versionCatalogs {
        create("oraxenLibs").from(files("gradle/oraxenLibs.versions.toml"))
    }
}

include(
    "core",
    "v1_20_R1",
    "v1_20_R2",
    "v1_20_R3",
    "v1_20_R4",
    "v1_21_R1",
    "v1_21_R2",
    "v1_21_R3",
    "v1_21_R4",
    "v1_21_R5"
)
