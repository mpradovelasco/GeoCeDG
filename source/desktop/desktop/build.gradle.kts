import Desktop_variants_gradle.Variants.nativesLinuxAmd64
import Desktop_variants_gradle.Variants.nativesMacOSXUniversal
import Desktop_variants_gradle.Variants.nativesWindowsAmd64
import java.nio.charset.StandardCharsets

plugins {
    application
    alias(libs.plugins.geogebra.java)
    alias(libs.plugins.geogebra.checkstyle)
    alias(libs.plugins.geogebra.spotbugs)
    alias(libs.plugins.geogebra.variants)
}

description = "Parts of GeoGebra related to desktop platforms"

data class GeoCeDGRepositoryProvenance(
    val commit: String,
    val state: String,
    val source: String
)

fun runGeoCeDGGit(repository: File, vararg arguments: String): String? = runCatching {
    val command = listOf("git", "-C", repository.absolutePath) + arguments
    val process = ProcessBuilder(command).redirectErrorStream(true).start()
    val output = process.inputStream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }.trim()
    if (process.waitFor() == 0) output else null
}.getOrNull()

fun resolveGeoCeDGRepositoryProvenance(): GeoCeDGRepositoryProvenance {
    val repository = rootProject.file("../..")
    val commitPattern = Regex("[0-9a-fA-F]{40}|[0-9a-fA-F]{64}")
    fun checkedCommit(value: String?, authority: String): String? {
        val candidate = value?.trim() ?: return null
        if (!commitPattern.matches(candidate)) {
            throw GradleException("$authority must be a 40- or 64-digit hexadecimal commit")
        }
        return candidate.lowercase()
    }
    val propertyCommit = checkedCommit(
        providers.gradleProperty("geocedgRepositoryCommit").orNull,
        "geocedgRepositoryCommit"
    )
    val githubCommit = checkedCommit(
        providers.environmentVariable("GITHUB_SHA").orNull,
        "GITHUB_SHA"
    )
    val gitCommit = checkedCommit(
        runGeoCeDGGit(repository, "rev-parse", "HEAD"),
        "Git HEAD"
    )
    val candidate = propertyCommit ?: githubCommit ?: gitCommit
    val commit = candidate ?: "UNAVAILABLE"
    val source = when {
        commit == "UNAVAILABLE" -> "UNAVAILABLE"
        propertyCommit != null -> "GRADLE_PROPERTY"
        githubCommit != null -> "GITHUB_SHA"
        else -> "GIT_HEAD"
    }
    val configuredState = providers.gradleProperty("geocedgRepositoryState").orNull
        ?: providers.environmentVariable("GEOCEDG_REPOSITORY_STATE").orNull
    val gitStatus = runGeoCeDGGit(repository, "status", "--porcelain=v1", "--untracked-files=normal")
    val state = if (commit == "UNAVAILABLE") {
        if (configuredState != null && configuredState.trim().lowercase() != "unavailable") {
            throw GradleException("Repository state cannot be established without a commit")
        }
        "UNAVAILABLE"
    } else {
        when (configuredState?.trim()?.lowercase()) {
            "clean" -> "CLEAN"
            "dirty" -> "DIRTY"
            "unavailable" -> "UNAVAILABLE"
            null -> if (gitStatus == null) {
                "UNAVAILABLE"
            } else if (gitStatus.isEmpty()) {
                "CLEAN"
            } else {
                "DIRTY"
            }
            else -> throw GradleException(
                "geocedgRepositoryState must be clean, dirty, or unavailable"
            )
        }
    }
    return GeoCeDGRepositoryProvenance(commit, state, source)
}

val geoCeDGRepositoryProvenance = resolveGeoCeDGRepositoryProvenance()
val geoCeDGPackageProfile = rootProject.file("../../packaging/windows/package.yml")
val geoCeDGApplicationVersion = run {
    val matches = Regex("\\\"version\\\"\\s*:\\s*\\\"([0-9]+\\.[0-9]+\\.[0-9]+)\\\"")
        .findAll(geoCeDGPackageProfile.readText(StandardCharsets.UTF_8))
        .map { it.groupValues[1] }
        .toList()
    if (matches.size != 1) {
        throw GradleException(
            "packaging/windows/package.yml must declare exactly one semantic application version"
        )
    }
    matches.single()
}
val generatedGeoCeDGProvenanceDirectory = layout.buildDirectory.dir(
    "generated/resources/geocedgProvenance"
)
val generatedGeoCeDGProvenanceFile = generatedGeoCeDGProvenanceDirectory.map {
    it.file("org/geocedg/desktop/export/geocedg-build-provenance.properties")
}
val generateGeoCeDGBuildProvenance by tasks.registering {
    inputs.file(geoCeDGPackageProfile)
    inputs.property("applicationVersion", geoCeDGApplicationVersion)
    inputs.property("repositoryCommit", geoCeDGRepositoryProvenance.commit)
    inputs.property("repositoryState", geoCeDGRepositoryProvenance.state)
    inputs.property("resolutionSource", geoCeDGRepositoryProvenance.source)
    outputs.file(generatedGeoCeDGProvenanceFile)
    doLast {
        val output = generatedGeoCeDGProvenanceFile.get().asFile
        output.parentFile.mkdirs()
        output.writeText(
            "schema.version=2\n" +
                "application.version=$geoCeDGApplicationVersion\n" +
                "repository.commit=${geoCeDGRepositoryProvenance.commit}\n" +
                "repository.state=${geoCeDGRepositoryProvenance.state}\n" +
                "resolution.source=${geoCeDGRepositoryProvenance.source}\n",
            StandardCharsets.UTF_8
        )
    }
}

sourceSets.named("main") {
    resources.srcDir(generatedGeoCeDGProvenanceDirectory)
}

val e2eTest: SourceSet by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

val desktopJavaLauncher = project.javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(25))
}
val desktopJvmArgs = listOf(
    "--add-exports", "java.base/java.lang=ALL-UNNAMED",
    "--add-exports", "java.desktop/sun.awt=ALL-UNNAMED",
    "--add-exports", "java.desktop/sun.java2d=ALL-UNNAMED",
    "--enable-native-access=ALL-UNNAMED"
)

tasks.getByName<JavaExec>("run") {
    javaLauncher = desktopJavaLauncher
}

val e2eTestImplementation: Configuration by configurations.getting
e2eTestImplementation.extendsFrom(configurations.testImplementation.get())

dependencies {
    implementation("org.geogebra:common")
    implementation("org.geogebra:common-jre")
    implementation(project(":canvas-desktop"))
    implementation(project(":editor-desktop"))
    implementation(project(":jogl2"))
    implementation("org.geogebra:giac-jni")
    implementation("com.formdev:flatlaf:3.7")
    implementation(libs.jsObject)
    implementation(libs.openGeoProver)
    implementation(libs.jna)
    implementation(libs.echosvg)
    implementation(libs.mozilla.rhino.engine)

    implementation(nativesLinuxAmd64(libs.jogl))
    implementation(nativesWindowsAmd64(libs.jogl))
    implementation(nativesMacOSXUniversal(libs.jogl))

    runtimeOnly(nativesLinuxAmd64(libs.gluegen.rt))
    runtimeOnly(nativesWindowsAmd64(libs.gluegen.rt))
    runtimeOnly(nativesMacOSXUniversal(libs.gluegen.rt))

    runtimeOnly(nativesLinuxAmd64(libs.giac.java))
    runtimeOnly(nativesWindowsAmd64(libs.giac.java))
    runtimeOnly(nativesMacOSXUniversal(libs.giac.java))

    testImplementation("org.geogebra:keyboard-base")
    testImplementation("org.geogebra:ggbjdk")
    testImplementation(testFixtures("org.geogebra:common-jre"))
    testImplementation(libs.junit)
    testImplementation(libs.mockito.core)
    testImplementation(libs.hamcrest)

    // Junit 5 support with backward compatibility
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.vintage)
    // Add launcher explicitly to avoid legacy loading
    // https://docs.gradle.org/8.12/userguide/upgrading_version_8.html#manually_declaring_dependencies
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass = "org.geogebra.desktop.GeoGebra3D"
 // https://forum.jogamp.org/Unable-to-determine-Graphics-Configuration-Am-I-setting-up-something-wrong-tp4041606p4041636.html
    applicationDefaultJvmArgs = desktopJvmArgs
}

tasks.processResources {
    dependsOn(generateGeoCeDGBuildProvenance)
    from(rootProject.file("../../apps/geocedg")) {
        include("application-profile.yml", "application-profile-v1.yml")
        into("org/geocedg/desktop")
    }
    from(rootProject.file("../../geocedg/specs/ui")) {
        include("application-profile.schema.json", "application-profile-v1.schema.json")
        into("org/geocedg/desktop")
    }
    from(rootProject.file("../../docs/user/geocedg_user_guide.md")) {
        into("org/geocedg/desktop")
    }
    from(rootProject.file("../../docs/user/geocedg_construction_quick_guide.md")) {
        into("org/geocedg/desktop")
    }
}

tasks.register<JavaExec>("runGeoCeDG") {
    group = "application"
    description = "Run the GeoCeDG Desktop product profile."
    dependsOn(tasks.named("classes"))
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.geocedg.desktop.GeoCeDG"
    javaLauncher = desktopJavaLauncher
    setJvmArgs(desktopJvmArgs)
    standardInput = System.`in`
}

tasks.register<JavaExec>("runLocusV2Laboratory") {
    group = "application"
    description = "Run the opt-in GeoCeDG Locus V2 developer laboratory."
    dependsOn(tasks.named("classes"))
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = "org.geocedg.desktop.locus.LocusV2Laboratory"
    javaLauncher = desktopJavaLauncher
    setJvmArgs(desktopJvmArgs)
    standardInput = System.`in`
}

run {
    // Copying JOGL related native JARs into the same directory where the non-native JAR takes place.
    // JOGL is simply dumb, it cannot work neither with java.library.path nor classpath or anything. Arrgh.
    val joglVersion = libs.versions.jogl.get()
    val gluegen = project.configurations.runtimeClasspath.get().find { it.name == "gluegen-rt-${joglVersion}.jar" }
    val gluegenNatives = project.configurations.runtimeClasspath.get().filter { it.name.startsWith("gluegen-rt-$joglVersion-natives") }
    val gluegenDir = gluegen!!.parent
    for (gluegenNative in gluegenNatives) {
        copy {
            from(gluegenNative.path)
            into(gluegenDir)
        }
    }
    val jogl = project.configurations.runtimeClasspath.get().find { it.name == "jogl-all-${joglVersion}.jar" }
    val joglNatives = project.configurations.runtimeClasspath.get().filter { it.name.startsWith("jogl-all-$joglVersion-natives") }
    val joglDir = jogl!!.parent
    for (joglNative in joglNatives) {
        copy {
            from(joglNative.path)
            into(joglDir)
        }
    }
}

tasks {
    test {
        ignoreFailures = System.getenv("CI") != null
        outputs.upToDateWhen { false }
    }

    jar {
        manifest {
            attributes["Class-Path"] = configurations.runtimeClasspath.get().joinToString(" ") { it.name }
            attributes["Main-Class"] = "org.geogebra.desktop.GeoGebra3D"
        }
    }

    register<Zip>("debugJars") {
        dependsOn("jar")
        description = "Collect all jar files in a single archive. Fast: no proguard or code signing."
        archiveBaseName = "jars"
        destinationDirectory = layout.buildDirectory
        from(layout.buildDirectory.file("libs"))
        doLast {
            configurations.runtimeClasspath.get().forEach { jarFile ->
                copy {
                    from(jarFile)
                    into(layout.buildDirectory.file("libs"))
                }
            }
        }
    }

    register<Test>("e2eTest") {
        description = "Run end-to-end tests"
        testClassesDirs = e2eTest.output.classesDirs
        classpath = e2eTest.runtimeClasspath
    }
}
