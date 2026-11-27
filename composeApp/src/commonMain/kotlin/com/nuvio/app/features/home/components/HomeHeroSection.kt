package com.nuvio.app.features.home.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.SingletonImageLoader
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.nuvio.app.core.build.AppFeaturePolicy
import com.nuvio.app.core.build.TrailerPlaybackMode
import com.nuvio.app.core.format.formatReleaseDateForDisplay
import com.nuvio.app.core.ui.LocalDynamicArtworkBackgroundColor
import com.nuvio.app.core.ui.platformPhysicalTopInset
import com.nuvio.app.core.ui.rememberDynamicArtworkBackgroundEnabled
import com.nuvio.app.core.ui.PrefetchArtworkDominantColors
import com.nuvio.app.core.ui.focusedAlignment
import com.nuvio.app.core.ui.rememberBackdropFocusState
import com.nuvio.app.core.ui.revealAlpha
import com.nuvio.app.core.ui.heroStretchHeight
import com.nuvio.app.core.ui.LocalVerticalBarPadding
import com.nuvio.app.core.ui.ScreenActivityEffect
import com.nuvio.app.core.ui.heroStretchZoom
import com.nuvio.app.features.details.HeroTrailerAudioState
import com.nuvio.app.features.details.HeroTrailerSurface
import com.nuvio.app.features.details.MetaDetailsRepository
import com.nuvio.app.features.details.components.DetailHeroMetaBadge
import com.nuvio.app.features.details.components.HeroTrailerPlayerSurface
import com.nuvio.app.features.details.selectHeroTrailer
import com.nuvio.app.features.details.youtubePlaybackUrl
import com.nuvio.app.features.home.HomeHeroArtworkSource
import com.nuvio.app.features.home.HomeHeroStyle
import com.nuvio.app.features.home.MetaPreview
import com.nuvio.app.features.player.MiniPlayerController
import com.nuvio.app.features.tmdb.upgradeTmdbPosterUrl
import com.nuvio.app.features.trailer.TrailerPlaybackResolver
import com.nuvio.app.features.trailer.TrailerPlaybackSource
import com.nuvio.app.core.ui.centeredVerticalBarPadding
import com.nuvio.app.core.ui.withVerticalBar
import com.nuvio.app.features.home.stableKey
import com.nuvio.app.navigation.LocalUseNativeNavigation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nuvio.composeapp.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs

private const val HERO_BACKGROUND_PARALLAX = 0.055f
private const val HERO_BACKGROUND_SCALE = 1.14f
private const val HERO_CONTENT_PARALLAX = 0.18f
private const val HERO_SCROLL_PARALLAX = 0.3f
private const val HERO_SCROLL_DOWN_SCALE_MULTIPLIER = 0.0001f
private const val HERO_SCROLL_UP_SCALE_MULTIPLIER = 0.002f
private const val HERO_SCROLL_MAX_SCALE = 1.3f
private const val HERO_SWIPE_THRESHOLD_FRACTION = 0.16f
private const val HERO_SWIPE_VELOCITY_THRESHOLD = 300f
private const val HERO_AUTO_SCROLL_INTERVAL_MS = 8_000L
private const val MOBILE_HERO_VIEWPORT_RATIO = 0.82f
private const val MOBILE_PORTRAIT_HERO_WIDTH_RATIO = 1.5f
private const val MOBILE_HERO_MIN_HEIGHT_DP = 360f
private const val MOBILE_HERO_MAX_HEIGHT_DP = 760f
private const val TABLET_LANDSCAPE_HERO_HEIGHT_MULTIPLIER = 1.5f

internal object HomeHeroTrailerPlaybackController {
    private var stopCallback: (() -> Unit)? = null

    fun register(stop: () -> Unit) {
        stopCallback = stop
    }

    fun unregister(stop: () -> Unit) {
        if (stopCallback === stop) stopCallback = null
    }

    fun forceStop() {
        stopCallback?.invoke()
    }
}

private object HomeHeroTrailerPlaybackPositionStore {
    private val positions = mutableMapOf<String, Long>()

    fun get(key: String): Long = positions[key] ?: 0L

    fun set(key: String, positionMs: Long) {
        if (positionMs > 0L) positions[key] = positionMs
    }

    fun clear(key: String) {
        positions.remove(key)
    }
}

internal const val TMDB_POSTER_HEIGHT_RATIO = 1.5f
internal const val HERO_CARD_PORTRAIT_HEIGHT_RATIO = 1.4f
internal const val HERO_CARD_POSTER_PARALLAX = 0.5f
internal val HERO_CARD_CORNER_RADIUS = 12.dp
internal val HERO_CARD_HORIZONTAL_PADDING = 25.6.dp
internal val HERO_CARD_TOP_PADDING = 8.dp
internal val HERO_CARD_CONTENT_HORIZONTAL_PADDING = 16.dp
internal val HERO_CARD_CONTENT_VERTICAL_PADDING = 16.dp
internal val HERO_CARD_CONTENT_SPACING = 12.dp
internal val HERO_CARD_BOTTOM_SPACING = 12.dp
internal val HERO_CARD_LOGO_MAX_HEIGHT = 100.dp
internal const val HERO_CARD_LOGO_MAX_WIDTH_FRACTION = 0.92f

internal const val BACKDROP_HEIGHT_RATIO = 9f / 16f

private const val CARD_WIDE_MAX_VIEWPORT_FRACTION = 0.72f

internal fun heroUsesWideArtwork(
    maxWidth: Dp,
    viewportHeight: Dp?,
    isTablet: Boolean,
): Boolean = isTablet || (viewportHeight != null && maxWidth > viewportHeight)

internal fun cardHeroHeight(
    maxWidth: Dp,
    viewportHeight: Dp?,
    isTablet: Boolean,
): Dp = if (heroUsesWideArtwork(maxWidth, viewportHeight, isTablet)) {
    val wideHeight = maxWidth * BACKDROP_HEIGHT_RATIO
    if (viewportHeight != null) {
        minOf(wideHeight, viewportHeight * CARD_WIDE_MAX_VIEWPORT_FRACTION)
    } else {
        wideHeight
    }
} else {
    maxWidth * HERO_CARD_PORTRAIT_HEIGHT_RATIO
}

internal data class HomeHeroLayout(
    val isTablet: Boolean,
    val heroHeight: Dp,
    val contentMaxWidth: Dp,
    val contentWidthFraction: Float,
    val contentHorizontalPadding: Dp,
    val contentVerticalPadding: Dp,
    val bottomFadeHeight: Dp,
    val logoWidthFraction: Float,
)

@Composable
internal fun HomeHeroSection(
    items: List<MetaPreview>,
    modifier: Modifier = Modifier,
    viewportHeight: Dp? = null,
    mobileBelowSectionHeightHint: Dp? = null,
    heroStyle: HomeHeroStyle = HomeHeroStyle.FULL_BLEED,
    listState: LazyListState? = null,
    stretchPx: () -> Float = { 0f },
    trailerPlaybackEnabled: Boolean = false,
    trailerStartDelaySeconds: Int = 0,
    trailerStartUnmuted: Boolean = false,
    showImdbRating: Boolean = true,
    showMdbListRatings: Boolean = false,
    isItemSaved: (MetaPreview) -> Boolean = { false },
    onItemClick: ((MetaPreview) -> Unit)? = null,
    onPlayClick: ((MetaPreview) -> Unit)? = null,
    onToggleSavedClick: ((MetaPreview) -> Unit)? = null,
    onActiveArtworkChange: ((String?) -> Unit)? = null,
) {
    if (items.isEmpty()) return

    val pagerState = key(items.size) {
        rememberPagerState(
            initialPage = if (items.size > 1) {
                Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2) % items.size
            } else {
                0
            },
            pageCount = { if (items.size > 1) Int.MAX_VALUE else 1 },
        )
    }
    val coroutineScope = rememberCoroutineScope()
    val autoScrollPage = pagerState.settledPage
    val effectiveTrailerPlaybackEnabled = trailerPlaybackEnabled &&
        AppFeaturePolicy.heroTrailerPlaybackSupported &&
        AppFeaturePolicy.trailerPlaybackMode == TrailerPlaybackMode.IN_APP &&
        !MiniPlayerController.isActive

    LaunchedEffect(pagerState) {
        pagerState.scrollToPage(pagerState.currentPage)
    }

    ScreenActivityEffect(pagerState) { active ->
        if (!active) {
            pagerState.stopScroll(MutatePriority.PreventUserInput)
            pagerState.scrollToPage(pagerState.currentPage)
        }
    }

    ScreenActivityEffect(autoScrollPage, items.size, effectiveTrailerPlaybackEnabled) { active ->
        if (!active || items.size <= 1 || effectiveTrailerPlaybackEnabled) return@ScreenActivityEffect
        delay(HERO_AUTO_SCROLL_INTERVAL_MS)
        while (pagerState.isScrollInProgress) {
            delay(100L)
        }

        val nextPage = pagerState.currentPage + 1
        pagerState.animateScrollToPage(nextPage)
    }

    val isCardStyle = heroStyle == HomeHeroStyle.CARD
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val useNativeNavigation = LocalUseNativeNavigation.current
    val heroFadeColorState = LocalDynamicArtworkBackgroundColor.current
    val themeBackground = MaterialTheme.colorScheme.background
    val effectiveStretchPx: () -> Float = stretchPx
    val cardInsetModifier = if (isCardStyle) {
        Modifier.padding(
            top = HERO_CARD_TOP_PADDING,
            start = HERO_CARD_HORIZONTAL_PADDING,
            end = HERO_CARD_HORIZONTAL_PADDING,
        )
    } else {
        Modifier
    }
    val heroShape = if (isCardStyle) {
        RoundedCornerShape(HERO_CARD_CORNER_RADIUS)
    } else {
        RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
    }
    // Reference elevation style: soft black drop shadow + hairline white ring —
    // deliberately different from the standard poster-card edge glow.
    val cardElevationModifier = if (isCardStyle) {
        Modifier
            .shadow(
                elevation = 28.dp,
                shape = heroShape,
                clip = false,
                ambientColor = Color.Black.copy(alpha = 0.6f),
                spotColor = Color.Black.copy(alpha = 0.6f),
            )
            .clip(heroShape)
            .border(0.5.dp, Color.White.copy(alpha = 0.12f), heroShape)
    } else {
        Modifier.clip(heroShape)
    }
    val cardClickInteraction = remember { MutableInteractionSource() }
    val cardPressed by cardClickInteraction.collectIsPressedAsState()
    val cardPressScale by animateFloatAsState(
        targetValue = if (cardPressed) 0.99f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "hero_card_press_scale",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .then(cardInsetModifier)
                .graphicsLayer {
                    if (isCardStyle) {
                        // Press feedback only — the scroll bounce now moves the whole list.
                        scaleX = cardPressScale
                        scaleY = cardPressScale
                    }
                }
                .homeHeroPagerGesture(
                    pagerState = pagerState,
                    itemCount = items.size,
                    coroutineScope = coroutineScope,
                )
                .then(cardElevationModifier),
        ) {
            val baseLayout = homeHeroLayout(
                maxWidthDp = maxWidth.value,
                viewportHeightDp = viewportHeight?.value,
                mobileBelowSectionHeightHintDp = mobileBelowSectionHeightHint?.value,
            )
            val layout = if (isCardStyle) {
                baseLayout.copy(
                    heroHeight = cardHeroHeight(
                        maxWidth = maxWidth,
                        viewportHeight = viewportHeight,
                        isTablet = baseLayout.isTablet,
                    ),
                )
            } else {
                baseLayout
            }
            val effectiveArtworkSource = when {
                !isCardStyle -> HomeHeroArtworkSource.BACKDROP
                heroUsesWideArtwork(maxWidth, viewportHeight, baseLayout.isTablet) ->
                    HomeHeroArtworkSource.BACKDROP
                else -> HomeHeroArtworkSource.HERO_CARD_POSTER
            }
            // CloudStream-style layered motion: the poster drifts at half the swipe speed
            // while the shared logo/tags/buttons chrome stays put on screen.
            val artworkParallax = if (isCardStyle) {
                HERO_CARD_POSTER_PARALLAX
            } else {
                HERO_BACKGROUND_PARALLAX
            }
            val artworkBaseScale = if (isCardStyle) 1f else HERO_BACKGROUND_SCALE
            val heroWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
            val heroHeightPx = with(LocalDensity.current) { layout.heroHeight.toPx() }
            val scrollOffsetPx by remember(listState, heroHeightPx) {
                derivedStateOf {
                    when {
                        listState == null -> 0f
                        listState.firstVisibleItemIndex > 0 -> heroHeightPx
                        else -> listState.firstVisibleItemScrollOffset.toFloat()
                    }
                }
            }
            val isHeroScrolledAway by remember(listState, heroHeightPx) {
                derivedStateOf { scrollOffsetPx >= heroHeightPx * 0.6f }
            }
            val currentPage = pagerState.currentPage
            val visiblePages = remember(currentPage, pagerState.pageCount, items.size) {
                listOf(
                    (currentPage - 1).coerceAtLeast(0),
                    (currentPage + 1).coerceAtMost(pagerState.pageCount - 1),
                    currentPage,
                ).distinct()
                    .map { page -> HeroPageLayer(page = page, itemIndex = page % items.size) }
            }
            val currentItem = items[currentPage % items.size]

            val activeArtworkUrl = heroArtworkUrl(items[pagerState.targetPage % items.size], effectiveArtworkSource)
            LaunchedEffect(onActiveArtworkChange, activeArtworkUrl) {
                onActiveArtworkChange?.invoke(activeArtworkUrl)
            }
            if (onActiveArtworkChange != null && rememberDynamicArtworkBackgroundEnabled()) {
                val heroArtworkUrls = remember(items, effectiveArtworkSource) {
                    items.mapNotNull { item -> heroArtworkUrl(item, effectiveArtworkSource) }
                }
                PrefetchArtworkDominantColors(heroArtworkUrls)
            }

            val platformContext = LocalPlatformContext.current
            LaunchedEffect(pagerState.settledPage, items, effectiveArtworkSource) {
                val settled = pagerState.settledPage
                val neighbours = listOf(settled + 1, settled - 1)
                    .filter { page -> page >= 0 && items.size > 1 }
                    .map { page -> items[page % items.size] }
                    .distinct()
                val imageLoader = SingletonImageLoader.get(platformContext)
                neighbours.forEach { item ->
                    listOfNotNull(heroArtworkUrl(item, effectiveArtworkSource), item.logo?.takeIf { it.isNotBlank() })
                        .forEach { url ->
                            imageLoader.enqueue(ImageRequest.Builder(platformContext).data(url).build())
                        }
                }
                neighbours.forEach { item ->
                    if (MetaDetailsRepository.peek(item.type, item.id) == null) {
                        runCatching { MetaDetailsRepository.fetch(item.type, item.id) }
                    }
                }
            }

            val heroTrailerItemKey = "${currentItem.type}:${currentItem.id}"
            var heroTrailerPlaybackSource by remember(currentItem.type, currentItem.id) {
                mutableStateOf<TrailerPlaybackSource?>(null)
            }
            var heroTrailerReady by remember(currentItem.type, currentItem.id) { mutableStateOf(false) }
            var heroTrailerFinished by remember(currentItem.type, currentItem.id) { mutableStateOf(false) }
            val heroTrailerMuted by HeroTrailerAudioState
                .muted(HeroTrailerSurface.Home)
                .collectAsStateWithLifecycle()

            LaunchedEffect(trailerStartUnmuted) {
                HeroTrailerAudioState.applyStartMuted(HeroTrailerSurface.Home, !trailerStartUnmuted)
            }

            val latestForceStopTrailer = rememberUpdatedState {
                if (heroTrailerPlaybackSource != null || !heroTrailerFinished) {
                    heroTrailerPlaybackSource = null
                    heroTrailerReady = false
                    heroTrailerFinished = true
                }
            }
            DisposableEffect(Unit) {
                val callback: () -> Unit = { latestForceStopTrailer.value.invoke() }
                HomeHeroTrailerPlaybackController.register(callback)
                onDispose { HomeHeroTrailerPlaybackController.unregister(callback) }
            }

            LaunchedEffect(
                effectiveTrailerPlaybackEnabled,
                currentItem.type,
                currentItem.id,
                trailerStartDelaySeconds,
            ) {
                heroTrailerPlaybackSource = null
                heroTrailerReady = false
                heroTrailerFinished = false
                if (!effectiveTrailerPlaybackEnabled) return@LaunchedEffect
                val resolvedSource = async {
                    val meta = runCatching {
                        MetaDetailsRepository.fetch(currentItem.type, currentItem.id)
                    }.getOrNull()
                    val trailer = meta?.trailers?.let(::selectHeroTrailer) ?: return@async null
                    runCatching {
                        TrailerPlaybackResolver.resolveFromYouTubeUrl(trailer.youtubePlaybackUrl())
                    }.getOrNull()
                }
                if (trailerStartDelaySeconds > 0) {
                    delay(trailerStartDelaySeconds.toLong() * 1000L)
                }
                val source = resolvedSource.await()
                if (source == null) {
                    heroTrailerFinished = true
                } else {
                    heroTrailerPlaybackSource = source
                }
            }

            // Card style bounces with a pure translation; bleed style stretches its height.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isCardStyle) {
                            Modifier.height(layout.heroHeight)
                        } else {
                            Modifier.heroStretchHeight(layout.heroHeight, effectiveStretchPx)
                        },
                    ),
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = false,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.01f },
                ) {
                    Box(modifier = Modifier.fillMaxSize())
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(layout.heroHeight)
                            .then(
                                if (isCardStyle) {
                                    Modifier
                                } else {
                                    Modifier.heroStretchZoom(effectiveStretchPx)
                                },
                            ),
                    ) {
                        visiblePages.forEach { layer ->
                            val item = items[layer.itemIndex]
                            val artworkUrl = heroArtworkUrl(item, effectiveArtworkSource)
                            val isCurrentLayer = layer.page == currentPage
                            key(effectiveArtworkSource, item.type, item.id, artworkUrl) {
                                val backdropFocus = rememberBackdropFocusState(artworkUrl)
                                AsyncImage(
                                    model = artworkUrl,
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            val offset = heroPageOffset(pagerState, layer.page)
                                            val visibility = (1f - abs(offset)).coerceIn(0f, 1f)
                                            alpha = backdropFocus.revealAlpha * when {
                                                isCurrentLayer -> visibility
                                                visibility > 0f -> 1f
                                                else -> 0f
                                            }
                                            translationX = -offset * heroWidthPx * artworkParallax
                                            translationY = if (isCardStyle) 0f else heroBackgroundScrollTranslationY(scrollOffsetPx)
                                            scaleX = artworkBaseScale *
                                                if (isCardStyle) 1f else heroBackgroundScrollScale(scrollOffsetPx)
                                            scaleY = artworkBaseScale *
                                                if (isCardStyle) 1f else heroBackgroundScrollScale(scrollOffsetPx)
                                        },
                                    alignment = backdropFocus.focusedAlignment(
                                        if (layout.isTablet) Alignment.TopCenter else Alignment.Center,
                                    ),
                                    contentScale = ContentScale.Crop,
                                    onSuccess = { state -> backdropFocus.onImageLoaded(state.result.image) },
                                    onError = { backdropFocus.onImageFailed() },
                                )
                            }
                        }

                        val heroTrailerSourceUrl = heroTrailerPlaybackSource
                            ?.videoUrl
                            ?.takeIf { it.isNotBlank() && effectiveTrailerPlaybackEnabled && !heroTrailerFinished }
                        if (heroTrailerSourceUrl != null) {
                            val trailerReadyAlpha by animateFloatAsState(
                                targetValue = if (heroTrailerReady) 1f else 0f,
                                animationSpec = tween(durationMillis = 300),
                                label = "home_hero_trailer_alpha",
                            )
                            HeroTrailerPlayerSurface(
                                sourceUrl = heroTrailerSourceUrl,
                                sourceAudioUrl = heroTrailerPlaybackSource?.audioUrl,
                                playWhenReady = !heroTrailerFinished && !isHeroScrolledAway,
                                muted = heroTrailerMuted,
                                startPositionMs = remember(heroTrailerItemKey) {
                                    HomeHeroTrailerPlaybackPositionStore.get(heroTrailerItemKey)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer {
                                        val currentPageOffset = pagerState.currentPageOffsetFraction
                                        val currentPageVisibility = (1f - abs(currentPageOffset)).coerceIn(0f, 1f)
                                        alpha = trailerReadyAlpha * currentPageVisibility
                                        translationX = -currentPageOffset * heroWidthPx * artworkParallax
                                        translationY = if (isCardStyle) 0f else heroBackgroundScrollTranslationY(scrollOffsetPx)
                                        scaleX = artworkBaseScale * if (isCardStyle) 1f else heroBackgroundScrollScale(scrollOffsetPx)
                                        scaleY = artworkBaseScale * if (isCardStyle) 1f else heroBackgroundScrollScale(scrollOffsetPx)
                                    },
                                onReady = {
                                    if (!heroTrailerFinished) heroTrailerReady = true
                                },
                                onEnded = {
                                    heroTrailerReady = false
                                    heroTrailerFinished = true
                                    HomeHeroTrailerPlaybackPositionStore.clear(heroTrailerItemKey)
                                },
                                onError = {
                                    heroTrailerReady = false
                                    heroTrailerFinished = true
                                    HomeHeroTrailerPlaybackPositionStore.clear(heroTrailerItemKey)
                                },
                                onPositionUpdate = { positionMs ->
                                    HomeHeroTrailerPlaybackPositionStore.set(heroTrailerItemKey, positionMs)
                                },
                            )
                        }
                    }

                    if (isCardStyle) {
                        // Strong internal shade: clear over the top 45%, then to 85% black.
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    drawRect(
                                        Brush.verticalGradient(
                                            0.45f to Color.Transparent,
                                            1f to Color.Black.copy(alpha = 0.85f),
                                        ),
                                    )
                                }
                                .clickable(
                                    interactionSource = cardClickInteraction,
                                    indication = null,
                                    enabled = onItemClick != null,
                                ) {
                                    onItemClick?.invoke(currentItem)
                                },
                        )

                        // Static chrome: the shared logo/tags/buttons overlay sits on top of the
                        // artwork layers and never fades or moves while pages slide beneath it.
                        val chromeWidth = if (layout.isTablet) layout.contentWidthFraction else 1f
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(
                                    horizontal = if (layout.isTablet) {
                                        layout.contentHorizontalPadding
                                    } else {
                                        HERO_CARD_CONTENT_HORIZONTAL_PADDING
                                    },
                                    vertical = HERO_CARD_CONTENT_VERTICAL_PADDING,
                                ),
                            verticalArrangement = Arrangement.spacedBy(HERO_CARD_CONTENT_SPACING),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(chromeWidth)
                                    .widthIn(max = layout.contentMaxWidth),
                            ) {
                                CardHeroTitleAndTags(
                                    item = currentItem,
                                    layout = layout,
                                    glowColor = heroFadeColorState?.value,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(chromeWidth)
                                    .widthIn(max = layout.contentMaxWidth),
                            ) {
                                HeroCardActionButtons(
                                    isSaved = isItemSaved(currentItem),
                                    onPlayClick = { (onPlayClick ?: onItemClick)?.invoke(currentItem) },
                                    onMyListClick = { onToggleSavedClick?.invoke(currentItem) },
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    val fade = heroFadeColorState?.value ?: themeBackground
                                    drawRect(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                fade.copy(alpha = 0.02f),
                                                fade.copy(alpha = 0.12f),
                                                fade.copy(alpha = 0.34f),
                                                fade.copy(alpha = 0.78f),
                                            ),
                                        ),
                                    )
                                },
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(layout.bottomFadeHeight)
                                .align(Alignment.BottomCenter)
                                .drawBehind {
                                    val fade = heroFadeColorState?.value ?: themeBackground
                                    drawRect(Brush.verticalGradient(colors = listOf(fade.copy(alpha = 0f), fade)))
                                },
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(enabled = onItemClick != null) {
                                    onItemClick?.invoke(currentItem)
                                },
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .padding(
                                    if (layout.isTablet) {
                                        PaddingValues(horizontal = layout.contentHorizontalPadding).withVerticalBar()
                                    } else {
                                        centeredVerticalBarPadding(layout.contentHorizontalPadding)
                                    },
                                )
                                .padding(vertical = layout.contentVerticalPadding),
                            horizontalAlignment = if (layout.isTablet) Alignment.Start else Alignment.CenterHorizontally,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(layout.contentWidthFraction)
                                    .widthIn(max = layout.contentMaxWidth),
                                contentAlignment = Alignment.BottomStart,
                            ) {
                                visiblePages.forEach { layer ->
                                    val layerItem = items[layer.itemIndex]
                                    key(layerItem.stableKey()) {
                                    Box(
                                        modifier = Modifier.graphicsLayer {
                                            val offset = heroPageOffset(pagerState, layer.page)
                                            val visibility = (1f - abs(offset)).coerceIn(0f, 1f)
                                            alpha = ((visibility - 0.5f) * 2f).coerceIn(0f, 1f)
                                            translationX = -offset * heroWidthPx * HERO_CONTENT_PARALLAX
                                        },
                                    ) {
                                        HeroContentBlock(
                                            item = layerItem,
                                            layout = layout,
                                            showImdbRating = showImdbRating,
                                            showMdbListRatings = showMdbListRatings,
                                            onItemClick = onItemClick,
                                        )
                                    }
                                    }
                                }
                            }

                            if (items.size > 1) {
                                Spacer(modifier = Modifier.height(if (layout.isTablet) 16.dp else 18.dp))
                                HeroPageIndicator(
                                    itemCount = items.size,
                                    pagerState = pagerState,
                                    coroutineScope = coroutineScope,
                                )
                            }
                        }
                    }

                    if (heroTrailerReady && heroTrailerPlaybackSource != null) {
                        val muteIconSize = 20.dp
                        val muteTopPadding = when {
                            isCardStyle -> 12.dp
                            useNativeNavigation -> platformPhysicalTopInset() + 8.dp
                            else -> statusBarTopPadding + 12.dp
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(
                                    top = muteTopPadding,
                                    end = if (layout.isTablet) 32.dp else 18.dp,
                                )
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.35f))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                ) {
                                    HeroTrailerAudioState.toggleMuted(HeroTrailerSurface.Home)
                                }
                                .padding(8.dp),
                        ) {
                            Icon(
                                imageVector = if (heroTrailerMuted) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(muteIconSize),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPageIndicator(
    itemCount: Int,
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(itemCount) { index ->
            val activeFraction = heroItemVisibility(pagerState, index, itemCount)
            Box(
                modifier = Modifier
                    .clickable {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                heroPageForItem(pagerState.currentPage, index, itemCount),
                            )
                        }
                    }
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onBackground)
                    .graphicsLayer {
                        alpha = 0.35f + (0.57f * activeFraction)
                    }
                    .width(8.dp + (24.dp * activeFraction))
                    .height(8.dp),
            )
        }
    }
}

private data class HeroPageLayer(
    val page: Int,
    val itemIndex: Int,
)

private fun heroArtworkUrl(item: MetaPreview, source: HomeHeroArtworkSource): String? = when (source) {
    HomeHeroArtworkSource.POSTER -> item.poster ?: item.banner
    HomeHeroArtworkSource.BACKDROP -> item.banner ?: item.poster
    HomeHeroArtworkSource.HERO_CARD_POSTER ->
        item.heroPoster ?: upgradeTmdbPosterUrl(item.poster, targetSize = "original") ?: item.banner
}

private fun heroPageOffset(
    pagerState: PagerState,
    page: Int,
): Float = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

private fun heroItemVisibility(pagerState: PagerState, itemIndex: Int, itemCount: Int): Float {
    val page = heroPageForItem(pagerState.currentPage, itemIndex, itemCount)
    return (1f - abs(heroPageOffset(pagerState, page))).coerceIn(0f, 1f)
}

internal fun heroPageForItem(currentPage: Int, itemIndex: Int, itemCount: Int): Int {
    val page = currentPage.toLong() - currentPage % itemCount + itemIndex
    return listOf(page - itemCount, page, page + itemCount)
        .filter { it in 0L until Int.MAX_VALUE.toLong() }
        .minBy { abs(it - currentPage) }
        .toInt()
}

@Composable
internal fun HomeHeroReservedSpace(
    modifier: Modifier = Modifier,
    viewportHeight: Dp? = null,
    mobileBelowSectionHeightHint: Dp? = null,
    heroStyle: HomeHeroStyle = HomeHeroStyle.FULL_BLEED,
) {
    val isCardStyle = heroStyle == HomeHeroStyle.CARD

    Column(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCardStyle) {
                        Modifier.padding(
                            top = HERO_CARD_TOP_PADDING,
                            start = HERO_CARD_HORIZONTAL_PADDING,
                            end = HERO_CARD_HORIZONTAL_PADDING,
                        )
                    } else {
                        Modifier
                    },
                )
                .clip(
                    if (isCardStyle) {
                        RoundedCornerShape(HERO_CARD_CORNER_RADIUS)
                    } else {
                        RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
                    },
                ),
        ) {
            val baseLayout = homeHeroLayout(
                maxWidthDp = maxWidth.value,
                viewportHeightDp = viewportHeight?.value,
                mobileBelowSectionHeightHintDp = mobileBelowSectionHeightHint?.value,
            )
            val heroHeight = if (isCardStyle) {
                cardHeroHeight(
                    maxWidth = maxWidth,
                    viewportHeight = viewportHeight,
                    isTablet = baseLayout.isTablet,
                )
            } else {
                baseLayout.heroHeight
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(heroHeight),
            )
        }
    }
}

@Composable
private fun HeroContentBlock(
    item: MetaPreview,
    layout: HomeHeroLayout,
    showImdbRating: Boolean,
    showMdbListRatings: Boolean,
    onItemClick: ((MetaPreview) -> Unit)?,
) {
    var logoLoadError by remember(item.type, item.id, item.logo) {
        mutableStateOf(false)
    }
    val logoUrl = item.logo?.takeIf { it.isNotBlank() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalAlignment = if (layout.isTablet) Alignment.Start else Alignment.CenterHorizontally,
    ) {
        if (logoUrl != null && !logoLoadError) {
            AsyncImage(
                model = logoUrl,
                contentDescription = item.name,
                modifier = Modifier
                    .fillMaxWidth(layout.logoWidthFraction)
                    .aspectRatio(2.6f)
                    .clickable(enabled = onItemClick != null) {
                        onItemClick?.invoke(item)
                    },
                alignment = if (layout.isTablet) Alignment.CenterStart else Alignment.Center,
                contentScale = ContentScale.Fit,
                onError = { logoLoadError = true },
            )
        } else {
            Text(
                text = item.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onItemClick != null) {
                        onItemClick?.invoke(item)
                    },
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Black,
                textAlign = if (layout.isTablet) TextAlign.Start else TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val detailsState = remember(item.type, item.id) {
            mutableStateOf(MetaDetailsRepository.peek(item.type, item.id))
        }
        LaunchedEffect(item.type, item.id) {
            if (detailsState.value == null) {
                detailsState.value = runCatching {
                    MetaDetailsRepository.fetch(item.type, item.id)
                }.getOrNull()
            }
        }
        val details = detailsState.value
        // Fill the gaps a catalog preview leaves (rating, age rating, genre, country) from
        // the cached details so the upstream meta rows stay populated on Enhanced.
        val metaItem = remember(item, details) {
            if (details == null) {
                item
            } else {
                item.copy(
                    imdbRating = item.imdbRating ?: details.imdbRating,
                    releaseInfo = item.releaseInfo ?: details.releaseInfo,
                    genres = item.genres.ifEmpty { details.genres },
                    runtime = item.runtime ?: details.runtime,
                    status = item.status ?: details.status,
                    ageRating = item.ageRating ?: details.ageRating,
                    language = item.language ?: details.language,
                    country = item.country ?: details.country,
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HomeHeroMetaRows(
            item = metaItem,
            isTablet = layout.isTablet,
            showImdbRating = showImdbRating,
            showMdbListRatings = showMdbListRatings,
        )

        val synopsis = (item.description ?: details?.description)?.trim()?.takeIf { it.isNotBlank() }
        if (synopsis != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = synopsis,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f),
                textAlign = if (layout.isTablet) TextAlign.Start else TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Logo (title) + tag line for the hero card, per the reference design: the logo sits
 * above the tags, both centered, with a graceful fade/scale entrance as they appear.
 * Buttons are intentionally excluded — they render once, statically, below this block.
 */
@Composable
private fun CardHeroTitleAndTags(
    item: MetaPreview,
    layout: HomeHeroLayout,
    glowColor: Color?,
) {
    // Fill genre gaps from cached details so the tag line stays populated.
    val detailsState = remember(item.type, item.id) {
        mutableStateOf(MetaDetailsRepository.peek(item.type, item.id))
    }
    LaunchedEffect(item.type, item.id) {
        if (detailsState.value == null) {
            detailsState.value = runCatching {
                MetaDetailsRepository.fetch(item.type, item.id)
            }.getOrNull()
        }
    }
    val tags = (item.genres.ifEmpty { detailsState.value?.genres.orEmpty() })
        .asSequence()
        .mapNotNull { genre -> genre.trim().takeIf(String::isNotBlank) }
        .distinct()
        .take(3)
        .joinToString(" • ")

    // Graceful entrance: the logo and tags fade/scale in when a new hero appears.
    val reveal = remember(item.stableKey()) { Animatable(0f) }
    LaunchedEffect(item.stableKey()) {
        reveal.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        )
    }

    var logoLoadError by remember(item.type, item.id, item.logo) {
        mutableStateOf(false)
    }
    val logoUrl = item.logo?.takeIf { it.isNotBlank() }
    val titleAlignment = if (layout.isTablet) TextAlign.Start else TextAlign.Center
    val density = LocalDensity.current
    val glow = (glowColor ?: Color.Black).copy(alpha = 0.9f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = reveal.value
                val scale = 0.96f + 0.04f * reveal.value
                scaleX = scale
                scaleY = scale
                translationY = 6.dp.toPx() * (1f - reveal.value)
            },
        horizontalAlignment = if (layout.isTablet) Alignment.Start else Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(HERO_CARD_CONTENT_SPACING),
    ) {
        if (logoUrl != null && !logoLoadError) {
            Box {
                // Reference drop-shadow: 0 2px 8px rgba(0,0,0,.6) behind the logo mark.
                AsyncImage(
                    model = logoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(HERO_CARD_LOGO_MAX_WIDTH_FRACTION)
                        .heightIn(max = HERO_CARD_LOGO_MAX_HEIGHT)
                        .blur(8.dp)
                        .graphicsLayer {
                            alpha = 0.6f
                            translationY = 2.dp.toPx()
                        },
                    contentScale = ContentScale.Fit,
                    colorFilter = ColorFilter.tint(Color.Black),
                )
                AsyncImage(
                    model = logoUrl,
                    contentDescription = item.name,
                    modifier = Modifier
                        .fillMaxWidth(HERO_CARD_LOGO_MAX_WIDTH_FRACTION)
                        .heightIn(max = HERO_CARD_LOGO_MAX_HEIGHT),
                    contentScale = ContentScale.Fit,
                    onError = { logoLoadError = true },
                )
            }
        } else {
            Text(
                text = item.name.uppercase(),
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 36.sp,
                    lineHeight = 40.sp,
                    letterSpacing = 0.06.em,
                    shadow = Shadow(
                        color = glow,
                        offset = Offset.Zero,
                        blurRadius = with(density) { 14.dp.toPx() },
                    ),
                ),
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = titleAlignment,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (tags.isNotBlank()) {
            Text(
                text = tags,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.6f),
                        offset = Offset(0f, with(density) { 1.dp.toPx() }),
                        blurRadius = with(density) { 3.dp.toPx() },
                    ),
                ),
                color = Color.White,
                fontWeight = FontWeight.Medium,
                textAlign = titleAlignment,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** Exact play/plus/check glyphs from the reference assets (24dp viewBox). */
private val HeroCardPlayIcon: ImageVector = ImageVector.Builder(
    name = "HeroCardPlay",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(
    fill = SolidColor(Color.Black),
) {
    moveTo(8f, 5f)
    verticalLineToRelative(14f)
    lineToRelative(11f, -7f)
    close()
}.build()

private val HeroCardAddIcon: ImageVector = ImageVector.Builder(
    name = "HeroCardAdd",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(
    fill = SolidColor(Color.White),
) {
    moveTo(11f, 5f)
    horizontalLineToRelative(2f)
    verticalLineToRelative(6f)
    horizontalLineToRelative(6f)
    verticalLineToRelative(2f)
    horizontalLineToRelative(-6f)
    verticalLineToRelative(6f)
    horizontalLineToRelative(-2f)
    verticalLineToRelative(-6f)
    horizontalLineTo(5f)
    verticalLineToRelative(-2f)
    horizontalLineToRelative(6f)
    close()
}.build()

private val HeroCardCheckIcon: ImageVector = ImageVector.Builder(
    name = "HeroCardCheck",
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f,
).path(
    fill = SolidColor(Color.White),
) {
    moveTo(9f, 16.2f)
    lineToRelative(-3.5f, -3.5f)
    lineTo(4f, 14.2f)
    lineTo(9f, 19.2f)
    lineTo(20f, 8.2f)
    lineToRelative(-1.4f, -1.4f)
    close()
}.build()

/** Reference "Play"/"My List" buttons: 44dp tall, 5dp radius, white vs. translucent grey. */
@Composable
private fun HeroCardActionButtons(
    isSaved: Boolean,
    onPlayClick: () -> Unit,
    onMyListClick: () -> Unit,
) {
    val buttonShape = RoundedCornerShape(5.dp)
    val buttonTextStyle = MaterialTheme.typography.labelLarge.copy(
        fontSize = 17.sp,
        fontWeight = FontWeight.SemiBold,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val playInteraction = remember { MutableInteractionSource() }
        val playPressed by playInteraction.collectIsPressedAsState()
        val playScale by animateFloatAsState(
            targetValue = if (playPressed) 0.96f else 1f,
            animationSpec = spring(stiffness = 600f),
            label = "hero_play_press_scale",
        )
        Row(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = playScale
                    scaleY = playScale
                    alpha = if (playPressed) 0.8f else 1f
                }
                .weight(1f)
                .height(44.dp)
                .clip(buttonShape)
                .background(Color.White)
                .clickable(
                    interactionSource = playInteraction,
                    indication = null,
                    onClick = onPlayClick,
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = HeroCardPlayIcon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.action_play),
                style = buttonTextStyle,
                color = Color.Black,
                maxLines = 1,
            )
        }

        val listInteraction = remember { MutableInteractionSource() }
        val listPressed by listInteraction.collectIsPressedAsState()
        val listScale by animateFloatAsState(
            targetValue = if (listPressed) 0.96f else 1f,
            animationSpec = spring(stiffness = 600f),
            label = "hero_list_press_scale",
        )
        Row(
            modifier = Modifier
                .graphicsLayer {
                    scaleX = listScale
                    scaleY = listScale
                    alpha = if (listPressed) 0.8f else 1f
                }
                .weight(1f)
                .height(44.dp)
                .clip(buttonShape)
                .background(Color(0xFF676767).copy(alpha = 0.8f))
                .clickable(
                    interactionSource = listInteraction,
                    indication = null,
                    onClick = onMyListClick,
                ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = if (isSaved) HeroCardCheckIcon else HeroCardAddIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(Res.string.hero_action_my_list),
                style = buttonTextStyle,
                color = Color.White,
                maxLines = 1,
            )
        }
    }
}

internal fun homeHeroLayout(
    maxWidthDp: Float,
    viewportHeightDp: Float? = null,
    mobileBelowSectionHeightHintDp: Float? = null,
): HomeHeroLayout {
    val isLandscapeViewport = viewportHeightDp?.let { maxWidthDp > it } == true
    val shortestViewportSideDp = viewportHeightDp?.let { minOf(maxWidthDp, it) } ?: maxWidthDp
    val shouldBoostTabletLandscapeHero = isLandscapeViewport && shortestViewportSideDp >= 600f

    return when {
        maxWidthDp >= 1200f -> HomeHeroLayout(
            isTablet = true,
            heroHeight = tabletHeroHeight(
                baseHeight = (maxWidthDp * 0.42f).dp.coerceIn(360.dp, 440.dp),
                boostForLandscape = shouldBoostTabletLandscapeHero,
            ),
            contentMaxWidth = 640.dp,
            contentWidthFraction = 0.56f,
            contentHorizontalPadding = 56.dp,
            contentVerticalPadding = 22.dp,
            bottomFadeHeight = 190.dp,
            logoWidthFraction = 0.58f,
        )
        maxWidthDp >= 840f -> HomeHeroLayout(
            isTablet = true,
            heroHeight = tabletHeroHeight(
                baseHeight = (maxWidthDp * 0.46f).dp.coerceIn(340.dp, 420.dp),
                boostForLandscape = shouldBoostTabletLandscapeHero,
            ),
            contentMaxWidth = 560.dp,
            contentWidthFraction = 0.62f,
            contentHorizontalPadding = 40.dp,
            contentVerticalPadding = 20.dp,
            bottomFadeHeight = 180.dp,
            logoWidthFraction = 0.56f,
        )
        maxWidthDp >= 600f -> HomeHeroLayout(
            isTablet = true,
            heroHeight = tabletHeroHeight(
                baseHeight = (maxWidthDp * 0.58f).dp.coerceIn(320.dp, 380.dp),
                boostForLandscape = shouldBoostTabletLandscapeHero,
            ),
            contentMaxWidth = 520.dp,
            contentWidthFraction = 0.72f,
            contentHorizontalPadding = 32.dp,
            contentVerticalPadding = 18.dp,
            bottomFadeHeight = 170.dp,
            logoWidthFraction = 0.54f,
        )
        else -> HomeHeroLayout(
            isTablet = false,
            heroHeight = mobileHeroHeight(
                maxWidthDp = maxWidthDp,
                viewportHeightDp = viewportHeightDp,
                mobileBelowSectionHeightHintDp = mobileBelowSectionHeightHintDp,
            ),
            contentMaxWidth = 480.dp,
            contentWidthFraction = 1f,
            contentHorizontalPadding = 24.dp,
            contentVerticalPadding = 16.dp,
            bottomFadeHeight = 220.dp,
            logoWidthFraction = 0.62f,
        )
    }
}

private fun tabletHeroHeight(
    baseHeight: Dp,
    boostForLandscape: Boolean,
): Dp {
    if (!boostForLandscape) return baseHeight

    return (baseHeight.value * TABLET_LANDSCAPE_HERO_HEIGHT_MULTIPLIER).dp
}

private fun mobileHeroHeight(
    maxWidthDp: Float,
    viewportHeightDp: Float?,
    mobileBelowSectionHeightHintDp: Float?,
): Dp {
    val isPortraitViewport = viewportHeightDp?.let { it >= maxWidthDp } != false

    if (isPortraitViewport) {
        return (maxWidthDp * MOBILE_PORTRAIT_HERO_WIDTH_RATIO).dp
    }

    val viewportDrivenHeight = viewportHeightDp?.let { (it * MOBILE_HERO_VIEWPORT_RATIO).dp }
    val widthFallbackHeight = (maxWidthDp * 1.16f).dp
    val baseHeight = viewportDrivenHeight ?: widthFallbackHeight

    val maxAllowedFromViewportDp = if (viewportHeightDp != null && mobileBelowSectionHeightHintDp != null) {
        viewportHeightDp - mobileBelowSectionHeightHintDp
    } else {
        null
    }
    val cappedHeight = if (maxAllowedFromViewportDp != null) {
        val maxAllowedFromViewport = maxAllowedFromViewportDp.dp
        baseHeight.coerceAtMost(maxAllowedFromViewport)
    } else {
        baseHeight
    }
    val minHeight = if (maxAllowedFromViewportDp != null) {
        minOf(MOBILE_HERO_MIN_HEIGHT_DP, maxAllowedFromViewportDp.coerceAtLeast(0f)).dp
    } else {
        MOBILE_HERO_MIN_HEIGHT_DP.dp
    }

    return cappedHeight.coerceIn(minHeight, MOBILE_HERO_MAX_HEIGHT_DP.dp)
}

private fun heroBackgroundScrollScale(scrollOffsetPx: Float): Float {
    val scaleIncrease = if (scrollOffsetPx < 0f) {
        abs(scrollOffsetPx) * HERO_SCROLL_UP_SCALE_MULTIPLIER
    } else {
        scrollOffsetPx * HERO_SCROLL_DOWN_SCALE_MULTIPLIER
    }
    return (1f + scaleIncrease).coerceAtMost(HERO_SCROLL_MAX_SCALE)
}

private fun heroBackgroundScrollTranslationY(scrollOffsetPx: Float): Float {
    return scrollOffsetPx * HERO_SCROLL_PARALLAX
}

private fun Modifier.homeHeroPagerGesture(
    pagerState: PagerState,
    itemCount: Int,
    coroutineScope: CoroutineScope,
): Modifier {
    if (itemCount <= 1) return this

    return pointerInput(pagerState, itemCount) {
        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial)
            val widthPx = size.width.toFloat().takeIf { it > 0f } ?: return@awaitEachGesture
            val velocityTracker = VelocityTracker().apply {
                addPosition(down.uptimeMillis, down.position)
            }
            val startPage = pagerState.currentPage
            var totalDx = 0f
            var totalDy = 0f
            var dragging = false

            while (true) {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                velocityTracker.addPosition(change.uptimeMillis, change.position)

                if (!change.pressed) {
                    if (dragging) {
                        val targetPage = resolveHeroTargetPage(
                            startPage = startPage,
                            pageCount = pagerState.pageCount,
                            totalDx = totalDx,
                            velocityX = velocityTracker.calculateVelocity().x,
                            widthPx = widthPx,
                        )
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(targetPage)
                        }
                    }
                    break
                }

                val delta = change.position - change.previousPosition
                totalDx += delta.x
                totalDy += delta.y

                if (!dragging) {
                    val horizontalDrag =
                        abs(totalDx) > viewConfiguration.touchSlop && abs(totalDx) > abs(totalDy)
                    val verticalDrag =
                        abs(totalDy) > viewConfiguration.touchSlop && abs(totalDy) > abs(totalDx)

                    when {
                        verticalDrag -> break
                        horizontalDrag -> dragging = true
                        else -> continue
                    }
                }

                pagerState.dispatchRawDelta(-delta.x)
                change.consume()
            }
        }
    }
}

private fun resolveHeroTargetPage(
    startPage: Int,
    pageCount: Int,
    totalDx: Float,
    velocityX: Float,
    widthPx: Float,
): Int {
    val thresholdPassed = abs(totalDx) > widthPx * HERO_SWIPE_THRESHOLD_FRACTION ||
        abs(velocityX) > HERO_SWIPE_VELOCITY_THRESHOLD
    if (!thresholdPassed) return startPage

    val currentPage = startPage.coerceIn(0, pageCount - 1)
    return when {
        totalDx > 0f -> (currentPage - 1).coerceAtLeast(0)
        totalDx < 0f -> (currentPage + 1).coerceAtMost(pageCount - 1)
        else -> currentPage
    }
}
