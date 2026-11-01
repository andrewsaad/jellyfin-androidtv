package org.jellyfin.androidtv.ui.playback.overlay.action

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.sdk.model.api.MediaStream
import org.jellyfin.sdk.model.api.MediaStreamType

class SurroundUpmixToggleTests : FunSpec({
	fun preferences(enabled: Boolean) = mockk<UserPreferences> {
		every { this@mockk[UserPreferences.surroundUpmixEnabled] } returns enabled
		every { this@mockk[UserPreferences.surroundUpmixEnabled] = any() } just runs
	}

	fun stream(type: MediaStreamType, index: Int, channels: Int?) = mockk<MediaStream> {
		every { this@mockk.type } returns type
		every { this@mockk.index } returns index
		every { this@mockk.channels } returns channels
	}

	test("toggle turns the upmix off and restarts playback") {
		val userPreferences = preferences(enabled = true)
		val restart = mockk<() -> Unit>(relaxed = true)

		SurroundUpmixToggle.toggle(userPreferences, restart) shouldBe false

		verifyOrder {
			userPreferences[UserPreferences.surroundUpmixEnabled] = false
			restart()
		}
	}

	test("toggle turns the upmix on and restarts playback") {
		val userPreferences = preferences(enabled = false)
		val restart = mockk<() -> Unit>(relaxed = true)

		SurroundUpmixToggle.toggle(userPreferences, restart) shouldBe true

		verify(exactly = 1) { userPreferences[UserPreferences.surroundUpmixEnabled] = true }
		verify(exactly = 1) { restart() }
	}

	test("appliesTo is true for stereo and mono audio") {
		SurroundUpmixToggle.appliesTo(listOf(stream(MediaStreamType.AUDIO, 1, 2)), 1) shouldBe true
		SurroundUpmixToggle.appliesTo(listOf(stream(MediaStreamType.AUDIO, 1, 1)), 1) shouldBe true
	}

	test("appliesTo is false for multichannel audio") {
		val streams = listOf(
			stream(MediaStreamType.VIDEO, 0, null),
			stream(MediaStreamType.AUDIO, 1, 2),
			stream(MediaStreamType.AUDIO, 2, 6),
		)

		SurroundUpmixToggle.appliesTo(streams, 2) shouldBe false
		SurroundUpmixToggle.appliesTo(streams, 1) shouldBe true
	}

	test("appliesTo is true when the stream or channel count is unknown") {
		SurroundUpmixToggle.appliesTo(null, 1) shouldBe true
		SurroundUpmixToggle.appliesTo(listOf(stream(MediaStreamType.AUDIO, 1, 6)), -1) shouldBe true
		SurroundUpmixToggle.appliesTo(listOf(stream(MediaStreamType.AUDIO, 1, null)), 1) shouldBe true
	}
})
