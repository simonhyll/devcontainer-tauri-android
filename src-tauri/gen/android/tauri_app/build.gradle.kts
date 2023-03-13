import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.3.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

tasks.register("clean").configure {
    delete("build")
}

// https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains-support
tasks.withType(JavaCompile::class.java) {
    sourceCompatibility = "11"
    targetCompatibility = "11"
}

tasks.withType(KotlinCompile::class.java) {
    kotlinOptions {
        jvmTarget = "11"
    }
}