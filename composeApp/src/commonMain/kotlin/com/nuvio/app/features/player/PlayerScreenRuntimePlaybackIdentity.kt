package com.nuvio.app.features.player

import com.nuvio.app.features.details.VideoPlaybackIdentity
import com.nuvio.app.features.streams.StreamSearchTarget
import com.nuvio.app.features.streams.peekPlaybackIdentity
import com.nuvio.app.features.streams.toStreamSearchTarget

/**
 * Real-world identity supplied by the addon for the video that is playing, if any.
 *
 * Addons that build synthetic series (for example chronological watch orders spanning
 * several shows) number their videos S1E1..N under a synthetic meta ID. Such videos may
 * carry a `playbackIdentity` so stream searches, scrobbling and skip-segment lookups use
 * the real title.
 */
internal fun PlayerScreenRuntime.currentVideoPlaybackIdentity(): VideoPlaybackIdentity? {
    val videoId = activeVideoId?.takeIf { it.isNotBlank() } ?: return null
    return videoPlaybackIdentity(videoId)
}

private fun PlayerScreenRuntime.videoPlaybackIdentity(videoId: String): VideoPlaybackIdentity? {
    val videos = playerMetaVideos.ifEmpty {
        metaUiState.meta?.takeIf { it.id == parentMetaId }?.videos.orEmpty()
    }
    return videos.firstOrNull { it.id == videoId }?.playbackIdentity
        ?: peekPlaybackIdentity(parentMetaType, parentMetaId, videoId)
}

/** Type/id/season/episode that stream addons should be queried with for the active video. */
internal fun PlayerScreenRuntime.activeStreamSearchTarget(videoId: String): StreamSearchTarget =
    videoPlaybackIdentity(videoId).toStreamSearchTarget(
        type = contentType ?: parentMetaType,
        videoId = videoId,
        season = activeSeasonNumber,
        episode = activeEpisodeNumber,
    )
