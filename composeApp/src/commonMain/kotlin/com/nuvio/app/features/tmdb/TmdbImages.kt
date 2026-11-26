package com.nuvio.app.features.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal fun tmdbImageLanguages(language: String): String {
    val normalizedLanguage = normalizeTmdbLanguage(language)
    return listOf(normalizedLanguage.substringBefore("-"), normalizedLanguage, "en", "null")
        .distinct()
        .joinToString(",")
}

internal fun List<TmdbImage>.selectBestLocalizedImagePath(normalizedLanguage: String): String? {
    val languageCode = normalizedLanguage.substringBefore("-")
    val regionCode = normalizedLanguage.substringAfter("-", "").uppercase().takeIf { it.length == 2 }
        ?: defaultLanguageRegions[languageCode]
    return filter { !it.filePath.isNullOrBlank() }.sortedWith(
        compareByDescending<TmdbImage> { it.iso6391 == languageCode && it.iso31661 == regionCode }
            .thenByDescending { it.iso6391 == languageCode && it.iso31661 == null }
            .thenByDescending { it.iso6391 == languageCode }
            .thenByDescending { it.iso6391 == "en" }
            .thenByDescending { it.iso6391 == null },
    ).firstOrNull()?.filePath?.trim()
}

internal fun List<TmdbImage>.selectTitleLogoPath(
    normalizedLanguage: String,
    originalLanguage: String?,
    preferOriginal: Boolean,
): String? {
    val languageCode = normalizedLanguage.substringBefore("-").lowercase()
    val regionCode = normalizedLanguage.substringAfter("-", "").uppercase().takeIf { it.length == 2 }
        ?: defaultLanguageRegions[languageCode]
    val original = originalLanguage?.substringBefore("-")?.lowercase()?.takeIf(String::isNotBlank)
    fun rank(image: TmdbImage): Int? {
        val iso = image.iso6391?.lowercase()
        return when {
            preferOriginal && original != null && iso == original -> 0
            iso == languageCode && image.iso31661 == regionCode -> 1
            iso == languageCode && image.iso31661 == null -> 2
            iso == languageCode -> 3
            iso == "en" -> 4
            original != null && iso == original -> 5
            iso == null -> 6
            else -> null
        }
    }
    return asSequence()
        .filter { !it.filePath.isNullOrBlank() }
        .mapNotNull { image -> rank(image)?.let { image to it } }
        .minByOrNull { it.second }
        ?.first
        ?.filePath
        ?.trim()
}

private const val PORTRAIT_THREE_TWO_ASPECT_RATIO = 2.0 / 3.0
private const val MAX_PORTRAIT_ASPECT_RATIO = 0.78
private const val HERO_POSTER_ASPECT_TOLERANCE = 0.03
private val TMDB_SIZE_SEGMENT_REGEX = Regex("""^(https?://image\.tmdb\.org/t/p/)([^/]+)(/.*)$""")

/**
 * Picks the hero-card key art the way the reference client does: the languageless
 * portrait posters from `images?include_image_language=null` (no spoken-language tag,
 * ~2:3 ratio) ranked by vote average, falling back to the best localized portrait
 * poster when a title has no languageless artwork.
 */
internal fun List<TmdbImage>.selectHeroCardPosterPath(): String? {
    val validPosters = filter { !it.filePath.isNullOrBlank() }
    if (validPosters.isEmpty()) return null

    fun isLanguageNeutral(image: TmdbImage): Boolean =
        image.iso6391.isNullOrBlank() || image.iso6391.equals("xx", ignoreCase = true)

    fun isThreeTwoPortrait(image: TmdbImage): Boolean {
        val ratio = image.aspectRatio ?: return true
        return kotlin.math.abs(ratio - PORTRAIT_THREE_TWO_ASPECT_RATIO) <= HERO_POSTER_ASPECT_TOLERANCE
    }

    val languagelessPortraits = validPosters.filter { isLanguageNeutral(it) && isThreeTwoPortrait(it) }
    if (languagelessPortraits.isNotEmpty()) {
        return languagelessPortraits.sortedWith(
            compareByDescending<TmdbImage> { it.voteAverage ?: 0.0 }
                .thenByDescending { it.width ?: 0 },
        ).firstOrNull()?.filePath?.trim()
    }

    fun isPortrait(image: TmdbImage): Boolean {
        val ratio = image.aspectRatio ?: return true
        return ratio > 0.0 && ratio <= MAX_PORTRAIT_ASPECT_RATIO
    }

    fun ratioDistanceToThreeTwo(image: TmdbImage): Double {
        val ratio = image.aspectRatio ?: return 0.05
        return kotlin.math.abs(ratio - PORTRAIT_THREE_TWO_ASPECT_RATIO)
    }

    return validPosters.sortedWith(
        compareByDescending<TmdbImage> { isLanguageNeutral(it) }
            .thenByDescending { isPortrait(it) }
            .thenBy { ratioDistanceToThreeTwo(it) }
            .thenByDescending { it.width ?: 0 }
            .thenByDescending { it.voteAverage ?: 0.0 },
    ).firstOrNull()?.filePath?.trim()
}

internal fun upgradeTmdbPosterUrl(url: String?, targetSize: String = "original"): String? {
    val trimmed = url?.trim()?.takeIf { it.isNotBlank() } ?: return null
    val match = TMDB_SIZE_SEGMENT_REGEX.matchEntire(trimmed) ?: return trimmed
    val currentSize = match.groupValues[2]
    val shouldUpgrade = currentSize.startsWith("w") && currentSize != "original"
    return if (shouldUpgrade) {
        "${match.groupValues[1]}$targetSize${match.groupValues[3]}"
    } else {
        trimmed
    }
}

private val defaultLanguageRegions = mapOf(
    "pt" to "PT",
    "es" to "ES",
)

@Serializable
internal data class TmdbImagesResponse(
    val logos: List<TmdbImage> = emptyList(),
    val backdrops: List<TmdbImage> = emptyList(),
    val posters: List<TmdbImage> = emptyList(),
)

@Serializable
internal data class TmdbImage(
    @SerialName("file_path") val filePath: String? = null,
    @SerialName("iso_639_1") val iso6391: String? = null,
    @SerialName("iso_3166_1") val iso31661: String? = null,
    @SerialName("aspect_ratio") val aspectRatio: Double? = null,
    val width: Int? = null,
    val height: Int? = null,
    @SerialName("vote_average") val voteAverage: Double? = null,
)
