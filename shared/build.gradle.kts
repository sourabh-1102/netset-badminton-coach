plugins {
  alias(libs.plugins.kotlin.multiplatform)
  alias(libs.plugins.android.kotlin.multiplatform.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.compose.multiplatform)
  alias(libs.plugins.ksp)
}

kotlin {
  android {
    namespace = "com.example.shared"
    compileSdk = 37
    minSdk = 24
    androidResources { enable = true }
  }

  listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
    target.binaries.framework {
      baseName = "Shared"
      isStatic = true
      // Room's bundled SQLite driver ships a native sqlite3 library
      linkerOpts.add("-lsqlite3")
    }
  }

  compilerOptions {
    freeCompilerArgs.add("-Xexpect-actual-classes")
    optIn.addAll(
      "kotlin.time.ExperimentalTime",
      "kotlin.uuid.ExperimentalUuidApi",
      "androidx.compose.material3.ExperimentalMaterial3Api",
      "kotlinx.coroutines.ExperimentalCoroutinesApi",
      "kotlinx.cinterop.ExperimentalForeignApi",
      "kotlinx.cinterop.BetaInteropApi",
    )
  }

  sourceSets {
    commonMain.dependencies {
      implementation(libs.compose.runtime)
      implementation(libs.compose.foundation)
      implementation(libs.compose.ui)
      implementation(libs.compose.material3)
      implementation(libs.compose.material.icons.extended)
      implementation(libs.compose.components.resources)
      implementation(libs.androidx.lifecycle.viewmodel.compose)
      implementation(libs.androidx.lifecycle.runtime.compose)
      implementation(libs.room.runtime)
      implementation(libs.sqlite.bundled)
      implementation(libs.kotlinx.coroutines.core)
      implementation(libs.kotlinx.datetime)
      implementation(libs.kotlinx.serialization.json)
    }
    androidMain.dependencies {
      implementation(libs.androidx.activity.compose)
      implementation(libs.androidx.core.ktx)
      implementation(libs.kotlinx.coroutines.android)
    }
  }
}

compose.resources {
  publicResClass = true
  packageOfResClass = "com.example.resources"
  generateResClass = always
}

dependencies {
  add("kspAndroid", libs.room.compiler)
  add("kspIosArm64", libs.room.compiler)
  add("kspIosSimulatorArm64", libs.room.compiler)
}
