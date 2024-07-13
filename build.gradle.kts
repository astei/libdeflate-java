plugins {
    java
    id("com.diffplug.spotless") version "6.13.0"
}

group = "me.steinborn"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()
    }

    spotless {
        java {
            importOrder()
            removeUnusedImports()

            googleJavaFormat("1.7")

            formatAnnotations()  // fixes formatting of type annotations, see below

            licenseHeaderFile(rootProject.file("./license-header.txt"))
        }
    }
}