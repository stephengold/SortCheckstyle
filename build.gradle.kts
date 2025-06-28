// Gradle script to build and run the SortCheckstyle project

plugins {
    base // to add a "clean" task to the root project
}

tasks.register("checkstyle") {
    dependsOn(":app:checkstyleMain")
    description = "Checks the style of all Java sourcecode."
}
tasks.register("run") {
    dependsOn(":app:run")
}
tasks.register("runGoogle") {
    dependsOn(":app:runGoogle")
    description = "Process the Checkstyle configuration for Google Java Style."
}
tasks.register("runHelp") {
    dependsOn(":app:runHelp")
    description = "Display the usage message and then exit."
}
tasks.register("runNoSort") {
    dependsOn(":app:runNoSort")
    description = "Process the default input without sorting anything."
}
tasks.register("runSelf") {
    dependsOn(":app:runSelf")
    description = "Process the SortCheckstyle configuration file."
}
tasks.register("runSun") {
    dependsOn(":app:runSun")
    description = "Process the Checkstyle configuration for Sun's Java Style."
}

// Register cleanup tasks:

tasks.named("clean") {
    dependsOn(":app:clean")
}
