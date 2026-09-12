plugins {
    kotlin("jvm") version "2.0.21"
    idea
    id("com.diffplug.spotless") version "7.0.2"
}

group = "gay.nyaa.purritems"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

val paperVersion = findProperty("paperVersion") as String? ?: "1.21.10-R0.1-SNAPSHOT"

dependencies {
    compileOnly("io.papermc.paper:paper-api:$paperVersion")
    compileOnly(files("../PurrCore/build/libs/purrcore-1.0.0.jar"))
    compileOnly(files("../PurrSkills/build/libs/purrskills-1.0.0.jar"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
    testImplementation("io.papermc.paper:paper-api:$paperVersion")
    testImplementation(files("../PurrCore/build/libs/purrcore-1.0.0.jar"))
    testImplementation(files("../PurrSkills/build/libs/purrskills-1.0.0.jar"))
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

tasks.processResources {
    filteringCharset = "UTF-8"
}

tasks.jar {
    archiveBaseName.set("purritems")
}

val shadowJar by tasks.registering(Jar::class) {
    archiveBaseName.set("purritems")
    archiveClassifier.set("")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })
}

tasks.build {
    dependsOn(shadowJar)
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
        excludeDirs.addAll(files(".gradle", "build", "out", ".idea/workspace.xml", ".idea/tasks.xml"))
    }
}

spotless {
    kotlin {
        target("src/**/*.kt")
        ktlint("1.5.0").editorConfigOverride(
            mapOf(
                "indent_size" to "4",
                "continuation_indent_size" to "4",
                "max_line_length" to "off",
                "ktlint_standard_max-line-length" to "disabled",
                "ktlint_standard_no-wildcard-imports" to "disabled",
            ),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
    kotlinGradle {
        target("*.kts", "gradle/*.kts")
        ktlint("1.5.0").editorConfigOverride(
            mapOf(
                "ktlint_standard_max-line-length" to "disabled",
            ),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }
    format("misc") {
        target("*.md", "*.yml", "*.yaml", "*.json", ".editorconfig")
        trimTrailingWhitespace()
        endWithNewline()
        leadingTabsToSpaces(2)
    }
}

tasks.test {
    useJUnitPlatform()
}
