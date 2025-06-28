// Gradle script to build and run the SortCheckstyle project

plugins {
    application // to build JVM applications
    checkstyle  // to analyze Java sourcecode for style violations
}

val javaVersion = JavaVersion.current()
java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

application {
    mainClass = "org.github.stephengold.sortcheckstyle.Main"
}
tasks.named<Jar>("jar") {
    manifest {
        attributes["Main-Class"] = application.mainClass
    }
}

checkstyle {
    toolVersion = libs.versions.checkstyle.get()
}

dependencies {
    implementation(libs.jcommander)
}

tasks.register<JavaExec>("runGoogle") {
    args("-u", "https://raw.githubusercontent.com/checkstyle/checkstyle/refs/heads/master/src/main/resources/google_checks.xml")
    description = "Process the Checkstyle configuration for Google Java Style."
    mainClass = "org.github.stephengold.sortcheckstyle.Main"
}
tasks.register<JavaExec>("runHelp") {
    args("-h")
    description = "Display the usage message and then exit."
    mainClass = "org.github.stephengold.sortcheckstyle.Main"
}
tasks.register<JavaExec>("runNoSort") {
    args("--noSortAttributes", "--noSortChildren")
    description = "Process the default input without sorting anything."
    mainClass = "org.github.stephengold.sortcheckstyle.Main"
}
tasks.register<JavaExec>("runSelf") {
    args("-i", "../config/checkstyle/checkstyle.xml")
    description = "Process the SortCheckstyle configuration file."
    mainClass = "org.github.stephengold.sortcheckstyle.Main"
}
tasks.register<JavaExec>("runSun") {
    args("-u", "https://raw.githubusercontent.com/checkstyle/checkstyle/refs/heads/master/src/main/resources/sun_checks.xml")
    description = "Process the Checkstyle configuration for Sun's Java Style."
    mainClass = "org.github.stephengold.sortcheckstyle.Main"
}

tasks.withType<JavaCompile>().all { // Java compile-time options:
    options.compilerArgs.add("-Xdiags:verbose")
    if (javaVersion.isCompatibleWith(JavaVersion.VERSION_20)) {
        // Suppress warnings that source value 8 is obsolete.
        options.compilerArgs.add("-Xlint:-options")
    }
    options.compilerArgs.add("-Xlint:unchecked")
    options.encoding = "UTF-8"
    options.isDeprecation = true // to provide detailed deprecation warnings
    if (javaVersion.isCompatibleWith(JavaVersion.VERSION_1_10)) {
        options.release = 8
    }
}

tasks.withType<JavaExec>().all { // Java runtime options:
    classpath = sourceSets.main.get().runtimeClasspath
    enableAssertions = true
}
