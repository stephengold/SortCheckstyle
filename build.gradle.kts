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

// Register cleanup tasks:

tasks.named("clean") {
    dependsOn(":app:clean")
}
