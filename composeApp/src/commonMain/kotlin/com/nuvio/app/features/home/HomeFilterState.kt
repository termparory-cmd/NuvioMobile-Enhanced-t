package com.nuvio.app.features.home

import com.nuvio.app.features.collection.Collection
import com.nuvio.app.features.collection.CollectionFolder

internal enum class HomeCategoryFilter {
    ALL,
    ANIME,
}

internal data class HomeFilterState(
    val tvShowsEnabled: Boolean = true,
    val moviesEnabled: Boolean = true,
    val category: HomeCategoryFilter = HomeCategoryFilter.ALL,
) {
    val isCategoriesEnabled: Boolean
        get() = tvShowsEnabled || moviesEnabled

    fun toggleTvShows(): HomeFilterState = copy(tvShowsEnabled = !tvShowsEnabled)

    fun toggleMovies(): HomeFilterState = copy(moviesEnabled = !moviesEnabled)

    fun selectCategory(next: HomeCategoryFilter): HomeFilterState =
        if (!isCategoriesEnabled) this else copy(category = next)
}

private val SERIES_TYPES = setOf("series", "tv", "show", "tvshow")
private val MOVIE_TYPES = setOf("movie", "movies", "film")
private val ANIME_TEXT_MARKERS = listOf("anime", "kitsu", "anilist", "myanimelist")

internal fun MetaPreview.isMovieMedia(): Boolean =
    type.trim().lowercase() in MOVIE_TYPES

internal fun MetaPreview.isTvShowMedia(): Boolean =
    type.trim().lowercase() in SERIES_TYPES

internal fun MetaPreview.isAnimeMedia(): Boolean {
    val normalizedType = type.trim().lowercase()
    if (normalizedType == "anime") return true
    val normalizedId = id.trim().lowercase()
    if (normalizedId.startsWith("kitsu:") || normalizedId.startsWith("anilist:") || normalizedId.startsWith("mal:")) {
        return true
    }
    return genres.any { genre -> genre.trim().equals("anime", ignoreCase = true) }
}

internal fun MetaPreview.matchesHomeFilter(filter: HomeFilterState): Boolean {
    if (!filter.isCategoriesEnabled) return false
    val matchesCategory = when (filter.category) {
        HomeCategoryFilter.ALL -> true
        HomeCategoryFilter.ANIME -> isAnimeMedia()
    }
    if (!matchesCategory) return false

    return when {
        isMovieMedia() -> filter.moviesEnabled
        isTvShowMedia() -> filter.tvShowsEnabled
        isAnimeMedia() -> filter.tvShowsEnabled || filter.moviesEnabled
        else -> filter.tvShowsEnabled || filter.moviesEnabled
    }
}

internal fun HomeCatalogSection.isAnimeSection(): Boolean {
    val normalizedType = target.contentType.trim().lowercase()
    if (normalizedType == "anime") return true
    val combinedMeta = listOf(title, subtitle, addonName, key).joinToString(" ").lowercase()
    if (ANIME_TEXT_MARKERS.any { marker -> combinedMeta.contains(marker) }) return true
    return items.isNotEmpty() && items.all { it.isAnimeMedia() }
}

internal fun HomeCatalogSection.matchesHomeFilter(filter: HomeFilterState): Boolean {
    if (!filter.isCategoriesEnabled) return false
    if (filter.category == HomeCategoryFilter.ANIME && !isAnimeSection()) return false

    val normalizedType = target.contentType.trim().lowercase()
    return when {
        normalizedType in MOVIE_TYPES -> filter.moviesEnabled
        normalizedType in SERIES_TYPES -> filter.tvShowsEnabled
        normalizedType == "anime" -> {
            val hasMovies = items.any { it.isMovieMedia() }
            val hasSeries = items.any { it.isTvShowMedia() }
            when {
                hasMovies && !hasSeries -> filter.moviesEnabled
                hasSeries && !hasMovies -> filter.tvShowsEnabled
                else -> filter.tvShowsEnabled || filter.moviesEnabled
            }
        }
        else -> {
            val hasMovies = items.any { it.isMovieMedia() }
            val hasSeries = items.any { it.isTvShowMedia() }
            when {
                hasMovies && !hasSeries -> filter.moviesEnabled
                hasSeries && !hasMovies -> filter.tvShowsEnabled
                else -> filter.tvShowsEnabled || filter.moviesEnabled
            }
        }
    }
}

internal fun Collection.isAnimeCollection(): Boolean {
    if (title.lowercase().let { text -> ANIME_TEXT_MARKERS.any(text::contains) }) return true
    return folders.isNotEmpty() && folders.any { folder -> folder.isAnimeFolder() }
}

private fun CollectionFolder.isAnimeFolder(): Boolean {
    if (title.lowercase().let { text -> ANIME_TEXT_MARKERS.any(text::contains) }) return true
    return resolvedSources.any { source ->
        source.type?.trim().equals("anime", ignoreCase = true) ||
            source.mediaType?.trim().equals("anime", ignoreCase = true) ||
            source.catalogId?.lowercase()?.let { id -> ANIME_TEXT_MARKERS.any(id::contains) } == true ||
            source.title?.lowercase()?.let { name -> ANIME_TEXT_MARKERS.any(name::contains) } == true ||
            source.genre?.equals("anime", ignoreCase = true) == true
    }
}

internal fun Collection.matchesHomeFilter(filter: HomeFilterState): Boolean {
    if (!filter.isCategoriesEnabled) return false
    if (filter.category == HomeCategoryFilter.ANIME && !isAnimeCollection()) return false

    val sourceTypes = folders
        .flatMap { folder ->
            folder.resolvedSources.mapNotNull { source ->
                (source.type ?: source.mediaType)
                    ?.trim()
                    ?.lowercase()
                    ?.takeIf(String::isNotBlank)
            }
        }
    if (sourceTypes.isEmpty()) return filter.tvShowsEnabled || filter.moviesEnabled

    val hasMovies = sourceTypes.any { it in MOVIE_TYPES }
    val hasSeries = sourceTypes.any { it in SERIES_TYPES }
    val hasAnime = sourceTypes.any { it == "anime" }
    return (hasMovies && filter.moviesEnabled) ||
        (hasSeries && filter.tvShowsEnabled) ||
        (hasAnime && (filter.tvShowsEnabled || filter.moviesEnabled)) ||
        (!hasMovies && !hasSeries && !hasAnime && (filter.tvShowsEnabled || filter.moviesEnabled))
}
