package com.nuvio.app.features.streams

import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.details.MetaVideo
import com.nuvio.app.features.details.VideoPlaybackIdentity

/**
 * What stream addons are actually queried with. Usually this is the video as-is, but
 * addons with synthetic videos supply a [VideoPlaybackIdentity] naming the real title.
 */
data class StreamSearchTarget(
    val type: String,
    val videoId: String,
    val season: Int?,
    val episode: Int?,
)

internal fun VideoPlaybackIdentity?.toStreamSearchTarget(
    type: String,
    videoId: String,
    season: Int?,
    episode: Int?,
): StreamSearchTarget =
    if (this == null) {
        StreamSearchTarget(type = type, videoId = videoId, season = season, episode = episode)
    } else {
        StreamSearchTarget(
            type = this.type,
            videoId = this.videoId,
            season = if (isMovie) null else this.season,
            episode = if (isMovie) null else this.episode,
        )
    }

/** Ordinary IMDb-keyed videos never carry an identity, so they never need a meta lookup. */
internal fun needsPlaybackIdentityLookup(videoId: String): Boolean = !videoId.startsWith("tt")

/** Cache-only lookup of the identity for [videoId] within its parent meta. */
internal fun peekPlaybackIdentity(
    parentMetaType: String,
    parentMetaId: String,
    videoId: String,
): VideoPlaybackIdentity? =
    MetaDetailsRepository.peek(type = parentMetaType, id = parentMetaId)
        ?.videos
        ?.firstOrNull { it.id == videoId }
        ?.playbackIdentity

/** Cached lookup, falling back to a meta fetch only for non-`tt` video ids. */
internal suspend fun resolvePlaybackIdentity(
    parentMetaType: String,
    parentMetaId: String,
    videoId: String,
): VideoPlaybackIdentity? {
    peekPlaybackIdentity(parentMetaType, parentMetaId, videoId)?.let { return it }
    if (!needsPlaybackIdentityLookup(videoId)) return null
    return runCatching { MetaDetailsRepository.fetch(parentMetaType, parentMetaId) }.getOrNull()
        ?.videos
        ?.firstOrNull { it.id == videoId }
        ?.playbackIdentity
}

internal fun MetaVideo.streamSearchTarget(type: String): StreamSearchTarget =
    playbackIdentity.toStreamSearchTarget(type = type, videoId = id, season = season, episode = episode)
