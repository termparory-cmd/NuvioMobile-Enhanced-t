package com.nuvio.app.features.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nuvio.app.features.home.HomeCategoryFilter
import com.nuvio.app.features.home.HomeFilterState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.hero_filter_categories
import nuvio.composeapp.generated.resources.hero_filter_category_all
import nuvio.composeapp.generated.resources.hero_filter_category_anime
import nuvio.composeapp.generated.resources.hero_filter_movies
import nuvio.composeapp.generated.resources.hero_filter_tv_shows
import nuvio.composeapp.generated.resources.hero_search_content_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun HomeCardHeader(
    profileName: String,
    filterState: HomeFilterState,
    onToggleTvShows: () -> Unit,
    onToggleMovies: () -> Unit,
    onSelectCategory: (HomeCategoryFilter) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var categoriesMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(filterState.isCategoriesEnabled) {
        if (!filterState.isCategoriesEnabled) {
            categoriesMenuExpanded = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                top = statusBarTopPadding + 8.dp,
                start = HERO_CARD_HORIZONTAL_PADDING,
                end = HERO_CARD_HORIZONTAL_PADDING,
                bottom = 12.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = profileName,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp),
            )

            IconButton(
                onClick = onSearchClick,
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(Res.string.hero_search_content_description),
                    tint = Color.White,
                    modifier = Modifier.size(26.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeFilterPill(
                label = stringResource(Res.string.hero_filter_tv_shows),
                selected = filterState.tvShowsEnabled,
                enabled = true,
                onClick = onToggleTvShows,
            )

            HomeFilterPill(
                label = stringResource(Res.string.hero_filter_movies),
                selected = filterState.moviesEnabled,
                enabled = true,
                onClick = onToggleMovies,
            )

            Box {
                val categoryActive = filterState.category == HomeCategoryFilter.ANIME
                HomeFilterPill(
                    label = stringResource(Res.string.hero_filter_categories),
                    selected = categoryActive,
                    enabled = filterState.isCategoriesEnabled,
                    onClick = {
                        if (filterState.isCategoriesEnabled) {
                            categoriesMenuExpanded = true
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White.copy(
                                alpha = if (filterState.isCategoriesEnabled) 0.92f else 0.4f,
                            ),
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )

                DropdownMenu(
                    expanded = categoriesMenuExpanded && filterState.isCategoriesEnabled,
                    onDismissRequest = { categoriesMenuExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.hero_filter_category_all)) },
                        trailingIcon = if (filterState.category == HomeCategoryFilter.ALL) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                        onClick = {
                            categoriesMenuExpanded = false
                            onSelectCategory(HomeCategoryFilter.ALL)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(Res.string.hero_filter_category_anime)) },
                        trailingIcon = if (filterState.category == HomeCategoryFilter.ANIME) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        } else {
                            null
                        },
                        onClick = {
                            categoriesMenuExpanded = false
                            onSelectCategory(HomeCategoryFilter.ANIME)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeFilterPill(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val containerColor = when {
        !enabled -> Color.Black.copy(alpha = 0.25f)
        selected -> Color.White.copy(alpha = 0.14f)
        else -> Color.Black.copy(alpha = 0.35f)
    }
    val borderColor = when {
        !enabled -> Color.White.copy(alpha = 0.22f)
        selected -> Color.White.copy(alpha = 0.72f)
        else -> Color.White.copy(alpha = 0.38f)
    }
    val contentAlpha = when {
        !enabled -> 0.4f
        selected -> 1f
        else -> 0.72f
    }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(containerColor)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp)
            .alpha(contentAlpha),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1,
        )
        trailingIcon?.invoke()
    }
}
