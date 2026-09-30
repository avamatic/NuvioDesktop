package com.nuvio.app.features.player

import com.nuvio.app.features.details.VideoTrackingIdentity

/**
 * Real-world identity supplied by the addon for the video that is playing, if any.
 *
 * Addons that build synthetic series (for example chronological watch orders spanning
 * several shows) number their videos S1E1..N under a synthetic meta ID. Such videos may
 * carry a `trackingIdentity` so scrobbling and skip-segment lookups use the real title.
 */
internal fun PlayerScreenRuntime.currentVideoTrackingIdentity(): VideoTrackingIdentity? {
    val videoId = activeVideoId?.takeIf { it.isNotBlank() } ?: return null
    val videos = playerMetaVideos.ifEmpty {
        metaUiState.meta?.takeIf { it.id == parentMetaId }?.videos.orEmpty()
    }
    return videos.firstOrNull { it.id == videoId }?.trackingIdentity
}
