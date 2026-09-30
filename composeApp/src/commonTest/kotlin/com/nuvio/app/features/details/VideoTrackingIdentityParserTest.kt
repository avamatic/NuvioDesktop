package com.nuvio.app.features.details

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class VideoTrackingIdentityParserTest {

    @Test
    fun `episode identity is parsed with real coordinates`() {
        val identity = parseIdentity("""{"type":"series","id":"tt0092455","name":"Star Trek: The Next Generation","season":3,"episode":15}""")

        assertEquals(
            VideoTrackingIdentity(type = "series", id = "tt0092455", name = "Star Trek: The Next Generation", season = 3, episode = 15),
            identity,
        )
        assertEquals("tt0092455:3:15", identity?.videoId)
    }

    @Test
    fun `movie identity drops episode coordinates`() {
        val identity = parseIdentity("""{"type":"movie","id":"tt0079945","season":1,"episode":1}""")

        assertEquals(VideoTrackingIdentity(type = "movie", id = "tt0079945"), identity)
        assertEquals("tt0079945", identity?.videoId)
    }

    @Test
    fun `invalid identities are ignored`() {
        assertNull(parseIdentity("""{"type":"series","id":"tt0092455"}"""))
        assertNull(parseIdentity("""{"type":"series","id":"chronio:star-trek","season":1,"episode":1}"""))
        assertNull(parseIdentity("""{"type":"channel","id":"tt0092455"}"""))
    }

    private fun parseIdentity(identityJson: String): VideoTrackingIdentity? =
        MetaDetailsParser.parse(
            """{"meta":{"id":"chronio:star-trek","type":"series","name":"Star Trek","videos":[{"id":"chronio:star-trek:x:1:1","title":"Entry","season":1,"episode":1,"trackingIdentity":$identityJson}]}}""",
        ).videos.single().trackingIdentity
}
