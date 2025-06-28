// global build settings for the SortCheckstyle project

rootProject.name = "SortCheckstyle"

dependencyResolutionManagement {
    repositories {
        //mavenLocal() // to find libraries installed locally
        mavenCentral() // to find libraries released to the Maven Central repository
    }
}

include("app")
