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

/* =========================
   Tasks
   ========================= */

tasks {
    shadowJar {
        archiveFileName.set("oraxen-$pluginVersion.jar")
    }
    build {
        dependsOn(shadowJar)
    }
}

/* =========================
   Dependencies
   ========================= */

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
}

/*
 ❌ KHÔNG CÓ repositories {}
 Repositories được quản lý 100% bởi settings.gradle.kts
*/

/* =========================
   Publishing
   ========================= */

publishing {
    val publishData = PublishData(project)

    publications {
        create<MavenPublication>("maven") {
            groupId = rootProject.group.toString()
            artifactId = rootProject.name
            version = publishData.getVersion()
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "oraxen"
            url = uri(publishData.getRepository())

            credentials(PasswordCredentials::class) {
                username = System.getenv("MAVEN_USERNAME")
                    ?: project.findProperty("oraxenUsername") as? String
                    ?: ""
                password = System.getenv("MAVEN_PASSWORD")
                    ?: project.findProperty("oraxenPassword") as? String
                    ?: ""
            }

            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
}

/* =========================
   PublishData helper
   ========================= */

class PublishData(private val project: Project) {

    private val type: Type = getReleaseType()
    private val hashLength = 7

    private fun getReleaseType(): Type {
        val branch = getCheckedOutBranch()
        println("Branch: $branch")
        return when (branch) {
            "master" -> Type.RELEASE
            "develop" -> Type.SNAPSHOT
            else -> Type.DEV
        }
    }

    private fun getCheckedOutGitCommitHash(): String =
        System.getenv("GITHUB_SHA")?.substring(0, hashLength) ?: "local"

    private fun getCheckedOutBranch(): String =
        System.getenv("GITHUB_REF")
            ?.removePrefix("refs/heads/")
            ?: grgitService.service.get().grgit.branch.current().name

    fun getVersion(): String = getVersion(false)

    fun getVersion(appendCommit: Boolean): String =
        type.append(getVersionString(), appendCommit, getCheckedOutGitCommitHash())

    private fun getVersionString(): String =
        (rootProject.version as String)
            .removeSuffix("-SNAPSHOT")
            .removeSuffix("-DEV")

    fun getRepository(): String = type.repo

    enum class Type(
        private val append: String,
        val repo: String,
        private val addCommit: Boolean
    ) {
        RELEASE("", "https://repo.oraxen.com/releases/", false),
        DEV("-DEV", "https://repo.oraxen.com/development/", true),
        SNAPSHOT("-SNAPSHOT", "https://repo.oraxen.com/snapshots/", true);

        fun append(name: String, appendCommit: Boolean, commitHash: String): String =
            name + append + if (appendCommit && addCommit) "-$commitHash" else ""
    }
}
