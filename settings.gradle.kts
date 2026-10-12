pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Momentwo"
include(":app")
include(":core:designsystem")
include(":core:model")
include(":core:common")
include(":core:ui")
include(":core:datastore")
include(":core:network")
include(":core:database")
include(":core:data")
include(":core:domain")
include(":core:navigation")
include(":core:testing")
include(":feature:auth:api")
include(":feature:auth:impl")
include(":feature:friend:api")
include(":feature:friend:impl")
include(":feature:subalbum:api")
include(":feature:subalbum:impl")
