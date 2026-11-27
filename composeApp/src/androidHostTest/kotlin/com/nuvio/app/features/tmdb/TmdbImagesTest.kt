package com.nuvio.app.features.tmdb

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbImagesTest {
    @Test
    fun selectHeroCardPosterPathRanksLanguagelessThreeTwoPostersByVoteAverage() {
        val posters = listOf(
            TmdbImage(
                filePath = "/localized_en.jpg",
                iso6391 = "en",
                aspectRatio = 0.667,
                width = 2000,
                height = 3000,
                voteAverage = 9.0,
            ),
            TmdbImage(
                filePath = "/neutral_low_res.jpg",
                iso6391 = null,
                aspectRatio = 0.667,
                width = 1000,
                height = 1500,
                voteAverage = 8.5,
            ),
            TmdbImage(
                filePath = "/neutral_high_res.jpg",
                iso6391 = "xx",
                aspectRatio = 0.667,
                width = 2000,
                height = 3000,
                voteAverage = 7.0,
            ),
            TmdbImage(
                filePath = "/neutral_landscape.jpg",
                iso6391 = null,
                aspectRatio = 1.778,
                width = 3840,
                height = 2160,
                voteAverage = 9.5,
            ),
        )

        // Languageless ~2:3 posters only, ranked by vote average (include_image_language=null spec).
        assertEquals("/neutral_low_res.jpg", posters.selectHeroCardPosterPath())
    }

    @Test
    fun selectHeroCardPosterPathFallsBackToLocalizedPortraitWhenNoNeutralExists() {
        val posters = listOf(
            TmdbImage(
                filePath = "/en_wide.jpg",
                iso6391 = "en",
                aspectRatio = 1.778,
                width = 3840,
            ),
            TmdbImage(
                filePath = "/en_poster.jpg",
                iso6391 = "en",
                aspectRatio = 0.667,
                width = 2000,
            ),
        )

        assertEquals("/en_poster.jpg", posters.selectHeroCardPosterPath())
    }

    @Test
    fun selectHeroCardPosterPathReturnsNullForEmptyOrBlankPaths() {
        val posters = listOf(TmdbImage(filePath = "   ", iso6391 = null))

        assertNull(posters.selectHeroCardPosterPath())
        assertNull(emptyList<TmdbImage>().selectHeroCardPosterPath())
    }

    @Test
    fun upgradeTmdbPosterUrlUpgradesSizedTmdbUrlsToOriginalAndLeavesOthersUntouched() {
        assertEquals(
            "https://image.tmdb.org/t/p/original/abc123.jpg",
            upgradeTmdbPosterUrl("https://image.tmdb.org/t/p/w500/abc123.jpg"),
        )
        assertEquals(
            "https://image.tmdb.org/t/p/original/abc123.jpg",
            upgradeTmdbPosterUrl("https://image.tmdb.org/t/p/original/abc123.jpg"),
        )
        assertEquals(
            "https://cdn.example.com/posters/w500/abc123.jpg",
            upgradeTmdbPosterUrl("https://cdn.example.com/posters/w500/abc123.jpg"),
        )
        assertNull(upgradeTmdbPosterUrl("  "))
        assertNull(upgradeTmdbPosterUrl(null))
    }
}
