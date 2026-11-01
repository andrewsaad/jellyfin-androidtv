// Stereo-to-5.1 upmix encoded as Dolby Digital (AC-3) so bonded surround speakers (e.g. a Sonos soundbar's
// rears over HDMI ARC) play stereo channels too. Native AC-3 encoder: native/build.sh -> src/main/jniLibs.
plugins {
	id("com.android.library")
	kotlin("android")
}

android {
	namespace = "com.streamvault.surround"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		minSdk = libs.versions.android.minSdk.get().toInt()
	}

	lint {
		lintConfig = file("$rootDir/android-lint.xml")
		abortOnError = false
	}

	testOptions {
		unitTests.isReturnDefaultValues = true
	}
}

dependencies {
	// media3
	implementation(libs.androidx.media3.exoplayer)

	// Testing (JUnit 4 + Truth, kept from the upstream module so its tests stay unchanged)
	testImplementation(libs.junit)
	testImplementation(libs.truth)
}
