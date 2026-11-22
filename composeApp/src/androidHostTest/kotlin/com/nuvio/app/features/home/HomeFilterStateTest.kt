package com.nuvio.app.features.home

import com.nuvio.app.features.catalog.CatalogTarget
import com.nuvio.app.features.collection.Collection
import com.nuvio.app.features.collection.CollectionFolder
import com.nuvio.app.features.collection.CollectionSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeFilterStateTest {
    @Test
    fun categoriesIsDisabledWhenBothTvShowsAndMoviesAreDisabled() {
        val initial = HomeFilterState()
        assertTrue(initial.isCategoriesEnabled)

        val onlyMovies = initial.toggleTvShows()
        assertFalse(onlyMovies.tvShowsEnabled)
        assertTrue(onlyMovies.moviesEnabled)
        assertTrue(onlyMovies.isCategoriesEnabled)

        val noneEnabled = onlyMovies.toggleMovies()
        assertFalse(noneEnabled.tvShowsEnabled)
        assertFalse(noneEnabled.moviesEnabled)
        assertFalse(noneEnabled.isCategoriesEnabled)

        // Category selection is ignored while Categories is disabled.
        assertEquals(HomeCategoryFilter.ALL, noneEnabled.selectCategory(HomeCategoryFilter.ANIME).category)

        val reEnabled = noneEnabled.toggleTvShows()
        assertTrue(reEnabled.isCategoriesEnabled)
        assertEquals(
            HomeCategoryFilter.ANIME,
            reEnabled.selectCategory(HomeCategoryFilter.ANIME).category,
        )
    }

    @Test
    fun metaPreviewMatchesTypeAndAnimeCategoryFilters() {
        val series = MetaPreview(id = "tt1", type = "series", name = "Wednesday", genres = listOf("Mystery"))
        val animeSeries = MetaPreview(id = "kitsu:123", type = "series", name = "Undead Unluck", genres = listOf("Animation"))
        val animeMovie = MetaPreview(id = "tt2", type = "movie", name = "Spirited Away", genres = listOf("Anime"))
        val liveActionMovie = MetaPreview(id = "tt3", type = "movie", name = "Dune", genres = listOf("Sci-Fi"))

        val allFilter = HomeFilterState()
        assertTrue(series.matchesHomeFilter(allFilter))
        assertTrue(animeSeries.matchesHomeFilter(allFilter))
        assertTrue(animeMovie.matchesHomeFilter(allFilter))
        assertTrue(liveActionMovie.matchesHomeFilter(allFilter))

        val tvOnly = HomeFilterState(tvShowsEnabled = true, moviesEnabled = false)
        assertTrue(series.matchesHomeFilter(tvOnly))
        assertTrue(animeSeries.matchesHomeFilter(tvOnly))
        assertFalse(animeMovie.matchesHomeFilter(tvOnly))
        assertFalse(liveActionMovie.matchesHomeFilter(tvOnly))

        val animeOnly = HomeFilterState(category = HomeCategoryFilter.ANIME)
        assertFalse(series.matchesHomeFilter(animeOnly))
        assertTrue(animeSeries.matchesHomeFilter(animeOnly))
        assertTrue(animeMovie.matchesHomeFilter(animeOnly))
        assertFalse(liveActionMovie.matchesHomeFilter(animeOnly))

        val noneEnabled = HomeFilterState(tvShowsEnabled = false, moviesEnabled = false)
        assertFalse(series.matchesHomeFilter(noneEnabled))
        assertFalse(animeSeries.matchesHomeFilter(noneEnabled))
    }

    @Test
    fun catalogSectionMatchesTypeAndAnimeCategoryFilters() {
        val popularSeries = HomeCatalogSection(
            key = "addon:series:popular",
            title = "Popular Series",
            subtitle = "Cinemeta",
            addonName = "Cinemeta",
            target = CatalogTarget.Addon("https://addon.test/manifest.json", "series", "popular"),
            items = listOf(MetaPreview(id = "tt1", type = "series", name = "Wednesday")),
        )
        val animeCatalog = HomeCatalogSection(
            key = "kitsu:anime:top",
            title = "Trending Anime",
            subtitle = "Kitsu",
            addonName = "Kitsu",
            target = CatalogTarget.Addon("https://kitsu.test/manifest.json", "series", "anime_top"),
            items = listOf(MetaPreview(id = "kitsu:1", type = "series", name = "Bleach")),
        )
        val popularMovies = HomeCatalogSection(
            key = "addon:movie:popular",
            title = "Popular Movies",
            subtitle = "Cinemeta",
            addonName = "Cinemeta",
            target = CatalogTarget.Addon("https://addon.test/manifest.json", "movie", "popular"),
            items = listOf(MetaPreview(id = "tt2", type = "movie", name = "Inception")),
        )

        val allFilter = HomeFilterState()
        assertTrue(popularSeries.matchesHomeFilter(allFilter))
        assertTrue(animeCatalog.matchesHomeFilter(allFilter))
        assertTrue(popularMovies.matchesHomeFilter(allFilter))

        val animeFilter = HomeFilterState(category = HomeCategoryFilter.ANIME)
        assertFalse(popularSeries.matchesHomeFilter(animeFilter))
        assertTrue(animeCatalog.matchesHomeFilter(animeFilter))
        assertFalse(popularMovies.matchesHomeFilter(animeFilter))

        val moviesOnly = HomeFilterState(tvShowsEnabled = false, moviesEnabled = true)
        assertFalse(popularSeries.matchesHomeFilter(moviesOnly))
        assertFalse(animeCatalog.matchesHomeFilter(moviesOnly))
        assertTrue(popularMovies.matchesHomeFilter(moviesOnly))
    }

    @Test
    fun collectionMatchesTypeAndAnimeCategoryFiltersAcrossAddonAndTmdbSources() {
        val animeCollection = Collection(
            id = "col_anime",
            title = "Seasonal Picks",
            folders = listOf(
                CollectionFolder(
                    id = "f1",
                    title = "Shonen",
                    sources = listOf(
                        CollectionSource(provider = "tmdb", mediaType = "tv", title = "Anime Top"),
                    ),
                ),
            ),
        )
        val moviesCollection = Collection(
            id = "col_movies",
            title = "Cinema",
            folders = listOf(
                CollectionFolder(
                    id = "f2",
                    title = "Blockbusters",
                    sources = listOf(
                        CollectionSource(provider = "addon", addonId = "a", type = "movie", catalogId = "top"),
                    ),
                ),
            ),
        )

        val allFilter = HomeFilterState()
        assertTrue(animeCollection.matchesHomeFilter(allFilter))
        assertTrue(moviesCollection.matchesHomeFilter(allFilter))

        val animeOnly = HomeFilterState(category = HomeCategoryFilter.ANIME)
        assertTrue(animeCollection.matchesHomeFilter(animeOnly))
        assertFalse(moviesCollection.matchesHomeFilter(animeOnly))

        val tvOnly = HomeFilterState(tvShowsEnabled = true, moviesEnabled = false)
        assertTrue(animeCollection.matchesHomeFilter(tvOnly))
        assertFalse(moviesCollection.matchesHomeFilter(tvOnly))
    }
}
