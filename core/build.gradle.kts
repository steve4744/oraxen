plugins {
    id("java")
    //id("io.papermc.paperweight.userdev") version "1.6.0"
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.7"
    id("org.ajoberstar.grgit.service") version "5.2.0"
}

val pluginVersion = project.property("pluginVersion") as String
tasks {
    //publish.get().dependsOn(shadowJar)
    shadowJar.get().archiveFileName.set("oraxen-${pluginVersion}.jar")
    build.get().dependsOn(shadowJar)
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    //paperweight.paperDevBundle("1.20.4-R0.1-SNAPSHOT")
}

    private fun getCheckedOutGitCommitHash(): String =
        System.getenv("GITHUB_SHA")?.substring(0, hashLength) ?: "local"

    private fun getCheckedOutBranch(): String =
        System.getenv("GITHUB_REF")?.replace("refs/heads/", "") ?: grgitService.service.get().grgit.branch.current().name

    fun getVersion(): String = getVersion(false)

    fun getVersion(appendCommit: Boolean): String =
        type.append(getVersionString(), appendCommit, getCheckedOutGitCommitHash())

    fun getVersionString(): String =
        (rootProject.version as String).removeSuffix("-SNAPSHOT").removeSuffix("-DEV")

    fun getRepository(): String = type.repo

    enum class Type(private val append: String, val repo: String, private val addCommit: Boolean) {
        RELEASE("", "https://repo.oraxen.com/releases/", false),
        DEV("-DEV", "https://repo.oraxen.com/development/", true),
        SNAPSHOT("-SNAPSHOT", "https://repo.oraxen.com/snapshots/", true);

        fun append(name: String, appendCommit: Boolean, commitHash: String): String =
            name.plus(append).plus(if (appendCommit && addCommit) "-".plus(commitHash) else "")
    }
}
