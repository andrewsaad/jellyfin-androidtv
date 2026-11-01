package org.jellyfin.androidtv.ui.playback

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.audio.AudioSink
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.jellyfin.androidtv.preference.UserPreferences

class SurroundUpmixTests : FunSpec({
	val stereo = Format.Builder()
		.setSampleMimeType(MimeTypes.AUDIO_RAW)
		.setChannelCount(2)
		.setSampleRate(48_000)
		.setPcmEncoding(C.ENCODING_PCM_16BIT)
		.build()

	test("Surround upmix preference is enabled by default") {
		UserPreferences.surroundUpmixEnabled.defaultValue shouldBe true
	}

	test("createUpmixAudioSink passes audio through untouched when the preference is off") {
		val userPreferences = mockk<UserPreferences>()
		every { userPreferences[UserPreferences.surroundUpmixEnabled] } returns false
		val delegate = mockk<AudioSink>(relaxed = true)

		createUpmixAudioSink(delegate, userPreferences).configure(stereo, 0, null)

		verify { userPreferences[UserPreferences.surroundUpmixEnabled] }
		verify(exactly = 1) { delegate.configure(stereo, 0, null) }
		verify(exactly = 0) { delegate.supportsFormat(any()) }
	}
})
