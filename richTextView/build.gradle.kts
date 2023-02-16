@Suppress("DSL_SCOPE_VIOLATION")
plugins {
	id(libs.plugins.android.library.get().pluginId)
	id(libs.plugins.kotlin.android.get().pluginId)
}

android {
	namespace = "com.tejnote.richtextview.lib"
	compileSdk = libs.versions.compileSdk.get().toInt()

	defaultConfig {
		minSdk = libs.versions.minSdk.get().toInt()
		targetSdk = libs.versions.targetSdk.get().toInt()

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}

	kotlinOptions {
		jvmTarget = "11"
	}
}

dependencies {

	implementation(libs.androidx.core)
	implementation(libs.androidx.appcompat)

	implementation(libs.google.android.material)

	implementation(libs.markdown)
	implementation(libs.disklrucache)
}