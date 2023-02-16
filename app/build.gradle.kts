@Suppress("DSL_SCOPE_VIOLATION")
plugins {
	id(libs.plugins.android.application.get().pluginId)
	id(libs.plugins.kotlin.android.get().pluginId)
}

android {
	namespace = "com.tejnote.richtext.apps"
	compileSdk = libs.versions.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "com.tejnote.richtext.apps"
		minSdk = libs.versions.minSdk.get().toInt()
		targetSdk = libs.versions.targetSdk.get().toInt()
		versionCode = libs.versions.versionCode.get().toInt()
		versionName = libs.versions.versionName.get()

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildFeatures { // 뷰바인딩
		viewBinding = true
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
	implementation(libs.androidx.activity)
	implementation(libs.androidx.fragment)
	implementation(libs.androidx.recyclerview)
	implementation(libs.androidx.swiperefreshlayout)

	implementation(libs.google.android.material)
}