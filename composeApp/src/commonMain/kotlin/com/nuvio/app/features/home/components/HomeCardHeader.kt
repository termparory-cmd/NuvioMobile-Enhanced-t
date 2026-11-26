package com.nuvio.app.features.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nuvio.app.features.home.HomeCategoryFilter
import com.nuvio.app.features.home.HomeFilterState
import nuvio.composeapp.generated.resources.Res
import nuvio.composeapp.generated.resources.hero_filter_categories
import nuvio.composeapp.generated.resources.hero_filter_category_all
import nuvio.composeapp.generated.resources.hero_filter_category_anime
import nuvio.composeapp.generated.resources.hero_filter_movies
import nuvio.composeapp.generated.resources.hero_filter_shows
import nuvio.composeapp.generated.resources.hero_search_content_description
import org.jetbrains.compose.resources.stringResource

private val HeaderScrimColor = Color(0xFF141414)
private val HeaderChipShape = RoundedCornerShape(9.dp)
private const val HEADER_SCRIM_MAX_ALPHA = 0.7f

/**
 * Pinned top bar, per the reference design: profile name + Search on the first line,
 * Shows / Movies / Categories nav chips on the second. The bar stays static; only the
 * scrim behind it follows the scroll ("edge vanish" — invisible at the very top,
 * fading in as the content scrolls underneath).
 */
@Composable
internal fun HomeCardHeader(
    profileName: String,
    filterState: HomeFilterState,
    onShowsClick: () -> Unit,
    onMoviesClick: () -> Unit,
    onSelectCategory: (HomeCategoryFilter) -> Unit,
    onSearchClick: () -> Unit,
    scrimFraction: () -> Float,
    modifier: Modifier = Modifier,
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    var categoriesMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(filterState.isCategoriesEnabled) {
        if (!filterState.isCategoriesEnabled) {
            categoriesMenuExpanded = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(HeaderScrimColor.copy(alpha = HEADER_SCRIM_MAX_ALPHA * scrimFraction().coerceIn(0f, 1f))),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = statusBarTopPadding + 4.dp,
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 10.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = profileName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.2.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp),
                )

                // Single search action; no download / cast / airplay icons here.
                Box(
                    modifier = Modifier
                        .size(width = 36.dp, height = 44.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSearchClick,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(Res.string.hero_search_content_description),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val showsSelected = filterState.tvShowsEnabled && !filterState.moviesEnabled
                val moviesSelected = !filterState.tvShowsEnabled && filterState.moviesEnabled

                HomeNavChip(
                    label = stringResource(Res.string.hero_filter_shows),
                    selected = showsSelected,
                    enabled = true,
                    onClick = onShowsClick,
                )

                HomeNavChip(
                    label = stringResource(Res.string.hero_filter_movies),
                    selected = moviesSelected,
                    enabled = true,
                    onClick = onMoviesClick,
                )

                Box {
                    HomeNavChip(
                        label = stringResource(Res.string.hero_filter_categories),
                        selected = false,
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
                                    alpha = if (filterState.isCategoriesEnabled) 1f else 0.4f,
                                ),
                                modifier = Modifier.size(11.dp),
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
}

/** Reference chip design: 34dp tall, 9dp radius, hairline stroke, white fill when selected. */
@Composable
private fun HomeNavChip(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "home_nav_chip_press",
    )

    val containerColor = when {
        !enabled -> Color.White.copy(alpha = 0.08f)
        selected -> Color.White
        else -> Color.White.copy(alpha = 0.15f)
    }
    val contentColor = if (selected) Color.Black else Color.White

    Row(
        modifier = Modifier
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                alpha = if (pressed) 0.8f else if (!enabled) 0.55f else 1f
            }
            .clip(HeaderChipShape)
            .background(containerColor)
            .then(
                if (selected || !enabled) {
                    Modifier
                } else {
                    Modifier.border(0.75.dp, Color.White.copy(alpha = 0.22f), HeaderChipShape)
                },
            )
            .clickable(enabled = enabled, interactionSource = interaction, indication = null, onClick = onClick)
            .padding(horizontal = 14.dp)
            .height(34.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
            color = contentColor,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
        )
        trailingIcon?.invoke()
    }
}
