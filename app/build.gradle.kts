plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
}

android {
  namespace = "com.example"
  compileSdk = 37

  defaultConfig {
    // Kept unchanged so the update installs over the existing app and keeps its data
    applicationId = "com.aistudio.badmintoncoach.pzxqkw"
    minSdk = 24
    targetSdk = 36
    versionCode = 2
    versionName = "2.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures { compose = true }
}

dependencies {
  implementation(project(":shared"))
  implementation(libs.androidx.activity.compose)
  implementation(libs.compose.ui)
  implementation(libs.compose.material3)
  // Keeps the app and androidTest classpaths on the same version (androidx.test needs 1.2)
  implementation("androidx.concurrent:concurrent-futures:1.2.0")

  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.junit)
  androidTestImplementation(libs.junit)
  androidTestImplementation(libs.compose.ui.test.junit4.android)
  debugImplementation(libs.compose.ui.test.manifest)
}
