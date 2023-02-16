@Suppress("DSL_SCOPE_VIOLATION")
buildscript {
	repositories {
		google()
		mavenCentral()
		gradlePluginPortal()
	}

	dependencies {
		classpath(libs.android.gradle)
		classpath(libs.google.gradle)
		classpath(libs.kotlin.gradle)
	}
}


allprojects {
	repositories {
		google()
		gradlePluginPortal()
		maven("https://jitpack.io")
		mavenCentral()
		maven("https://naver.jfrog.io/artifactory/maven/")
	}
}

tasks.register("clean", Delete::class) {
	delete(rootProject.buildDir)
}