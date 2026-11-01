package org.jellyfin.androidtv.ui.playback.overlay.action

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import org.jellyfin.androidtv.R
import org.jellyfin.androidtv.preference.UserPreferences
import org.jellyfin.androidtv.ui.playback.PlaybackController
import org.jellyfin.androidtv.ui.playback.overlay.CustomPlaybackTransportControlGlue
import org.jellyfin.androidtv.ui.playback.overlay.VideoPlayerAdapter
import org.jellyfin.sdk.model.api.MediaStream
import org.jellyfin.sdk.model.api.MediaStreamType

/**
 * Player bar toggle for the stereo to 5.1 surround upmix. The upmix preference is only read when the
 * audio output is configured, so toggling it restarts the stream at the current position.
 */
class SurroundUpmixAction(
	context: Context,
	customPlaybackTransportControlGlue: CustomPlaybackTransportControlGlue,
	private val userPreferences: UserPreferences,
) : CustomAction(context, customPlaybackTransportControlGlue) {
	init {
		val onIcon = requireNotNull(ContextCompat.getDrawable(context, R.drawable.ic_surround_sound))
		val offIcon = requireNotNull(ContextCompat.getDrawable(context, R.drawable.ic_surround_sound)).mutate().apply {
			alpha = OFF_ICON_ALPHA
		}
		setDrawables(arrayOf(onIcon, offIcon))
		refreshState()
	}

	/** Sync the icon and label with the stored preference. */
	fun refreshState() {
		index = if (userPreferences[UserPreferences.surroundUpmixEnabled]) INDEX_ON else INDEX_OFF
	}

	override fun handleClickAction(
		playbackController: PlaybackController,
		videoPlayerAdapter: VideoPlayerAdapter,
		context: Context,
		view: View,
	) {
		val enabled = SurroundUpmixToggle.toggle(userPreferences) { playbackController.refreshStream() }
		index = if (enabled) INDEX_ON else INDEX_OFF
	}

	companion object {
		const val INDEX_ON = 0
		const val INDEX_OFF = 1
		private const val OFF_ICON_ALPHA = 0x66
	}
}

object SurroundUpmixToggle {
	/**
	 * Flip the surround upmix preference and restart playback so the audio output is reconfigured.
	 * Returns the new state.
	 */
	fun toggle(userPreferences: UserPreferences, restartPlayback: () -> Unit): Boolean {
		val enabled = !userPreferences[UserPreferences.surroundUpmixEnabled]
		userPreferences[UserPreferences.surroundUpmixEnabled] = enabled
		restartPlayback()
		return enabled
	}

	/**
	 * The upmix only touches stereo (or mono) audio, so the toggle is only useful when the selected audio
	 * stream has at most 2 channels. Unknown streams or channel counts are treated as applicable.
	 */
	@JvmStatic
	fun appliesTo(mediaStreams: List<MediaStream>?, audioStreamIndex: Int): Boolean {
		val stream = mediaStreams?.firstOrNull { it.type == MediaStreamType.AUDIO && it.index == audioStreamIndex }
		val channels = stream?.channels ?: return true
		return channels <= 2
	}
}
