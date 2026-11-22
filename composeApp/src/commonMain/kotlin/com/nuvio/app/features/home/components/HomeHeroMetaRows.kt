package com.nuvio.app.features.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.nuvio.app.core.format.formatReleaseDateForDisplay
import com.nuvio.app.core.i18n.localizedMediaStatusLabel
import com.nuvio.app.features.details.components.DetailHeroMetaBadge
import com.nuvio.app.features.details.components.ImdbRatingSourceLabel
import com.nuvio.app.features.details.components.ImdbYellow
import com.nuvio.app.features.details.components.RatingsRow
import com.nuvio.app.features.details.components.formatOneDecimal
import com.nuvio.app.features.details.formatRuntimeForDisplay
import com.nuvio.app.features.home.MetaPreview
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.media_movie
import nuvio.composeapp.generated.resources.media_series
import org.jetbrains.compose.resources.stringResource

private const val HERO_RATINGS_LIMIT = 3

@Composable
internal fun HomeHeroMetaRows(
    item: MetaPreview,
    isTablet: Boolean,
    showImdbRating: Boolean,
    showMdbListRatings: Boolean,
    scale: Float = 1f,
    showRatingsRow: Boolean = true,
) {
    val type = item.type.trim().lowercase()
    val isSeries = type == "series" || type == "tv"
    val typeLabel = when (type) {
        "movie" -> stringResource(Res.string.media_movie)
        "series" -> stringResource(Res.string.media_series)
        else -> item.type.replaceFirstChar(Char::uppercase)
    }
    val genre = item.genres.firstOrNull()?.takeIf { it.isNotBlank() }
    val runtime = formatRuntimeForDisplay(item.runtime)?.takeIf { it.isNotBlank() }
    val release = item.releaseInfo?.takeIf { it.isNotBlank() }?.let(::formatReleaseDateForDisplay)
    val status = item.status?.trim()?.takeIf { it.isNotBlank() }
        ?.let { localizedMediaStatusLabel(it, isSeries).uppercase() }
    val badges = listOfNotNull(item.ageRating?.trim()?.takeIf { it.isNotBlank() }, status)
    val countryOrLanguage = resolveHeroCountryOrLanguage(country = item.country, language = item.language)
    val ratings = if (showRatingsRow && showMdbListRatings) item.externalRatings else emptyList()
    val imdbRating = item.imdbRating?.trim()
        ?.takeIf { showImdbRating && ratings.isEmpty() }
        ?.toDoubleOrNull()
        ?.takeIf { it > 0.0 }
        ?.let(::formatOneDecimal)
    val primaryImdbRating = imdbRating?.takeIf { !isSeries && badges.isEmpty() }
    val secondaryImdbRating = imdbRating?.takeIf { primaryImdbRating == null }
    val horizontalAlignment = if (isTablet) Alignment.Start else Alignment.CenterHorizontally
    val scaledGap = (8f * scale).dp
    val rowArrangement = Arrangement.spacedBy(scaledGap, horizontalAlignment)
    val baseLabelStyle = MaterialTheme.typography.labelLarge
    val textStyle = if (scale == 1f) {
        baseLabelStyle
    } else {
        baseLabelStyle.copy(
            fontSize = baseLabelStyle.fontSize.scaleHeroMetaUnit(scale),
            lineHeight = baseLabelStyle.lineHeight.scaleHeroMetaUnit(scale),
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment,
        verticalArrangement = Arrangement.spacedBy(if (scale == 1f) 8.dp else 6.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = rowArrangement,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HeroMetaText(text = typeLabel, style = textStyle)
            genre?.let {
                HeroMetaDot(scale = scale)
                HeroMetaText(text = it, style = textStyle, modifier = Modifier.weight(1f, fill = false))
            }
            runtime?.let {
                HeroMetaDot(scale = scale)
                HeroMetaText(text = it, style = textStyle)
            }
            release?.let {
                HeroMetaDot(scale = scale)
                HeroMetaText(text = it, style = textStyle)
            }
            primaryImdbRating?.let {
                HeroMetaDot(scale = scale)
                HeroImdbRating(rating = it, textStyle = textStyle, scale = scale)
            }
        }

        if (badges.isNotEmpty() || secondaryImdbRating != null || countryOrLanguage != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = rowArrangement,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (badges.isNotEmpty()) {
                    DetailHeroMetaBadge(
                        text = badges.first(),
                        secondaryText = badges.getOrNull(1),
                        contentColor = MaterialTheme.colorScheme.onBackground,
                        scale = scale,
                    )
                }
                secondaryImdbRating?.let {
                    if (badges.isNotEmpty()) HeroMetaDot(scale = scale)
                    HeroImdbRating(rating = it, textStyle = textStyle, scale = scale)
                }
                countryOrLanguage?.let {
                    if (badges.isNotEmpty() || secondaryImdbRating != null) HeroMetaDot(scale = scale)
                    HeroMetaText(text = it, style = textStyle)
                }
            }
        }

        if (ratings.isNotEmpty()) {
            RatingsRow(
                ratings = ratings,
                maxItems = HERO_RATINGS_LIMIT,
            )
        }
    }
}

internal fun resolveHeroCountryOrLanguage(country: String?, language: String?): String? {
    val normalizedCountry = country
        ?.split(",")
        ?.firstOrNull { it.isNotBlank() }
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.uppercase()
    if (normalizedCountry != null) return normalizedCountry
    return language?.trim()?.takeIf { it.isNotBlank() }?.uppercase()
}

private fun TextUnit.scaleHeroMetaUnit(scale: Float): TextUnit =
    if (isSpecified && scale != 1f) this * scale else this

@Composable
private fun HeroImdbRating(
    rating: String,
    textStyle: TextStyle,
    scale: Float = 1f,
) {
    val resolvedStyle = textStyle.copy(fontWeight = FontWeight.SemiBold)
    Row(verticalAlignment = Alignment.CenterVertically) {
        ImdbRatingSourceLabel(storeTextStyle = resolvedStyle, scale = scale)
        Spacer(modifier = Modifier.width((5f * scale).dp))
        Text(
            text = rating,
            style = resolvedStyle,
            color = ImdbYellow,
            maxLines = 1,
        )
    }
}

@Composable
private fun HeroMetaText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun HeroMetaDot(scale: Float = 1f) {
    Box(
        modifier = Modifier
            .size((4f * scale).dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)),
    )
}
