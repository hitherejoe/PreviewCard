package co.joebirch.previewcard.sample

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import androidx.tv.material3.*
import co.joebirch.previewcard.PreviewCard
import co.joebirch.previewcard.sample.DataFactory.makeCarouselItem

@UnstableApi
@ExperimentalAnimationApi
@ExperimentalTvMaterial3Api
@Composable
fun HomeList(
    modifier: Modifier = Modifier
) {
    val items = makeCarouselItem()
    val immersiveListHeight = 420.dp
    val cardSpacing = 10.dp
    val cardHeight = 120.dp

    ImmersiveList(
        modifier = modifier
            .background(Color.Black)
            .height(immersiveListHeight + (cardHeight.times(1.2f)) / 2)
            .fillMaxWidth(),
        background = { index, hasFocus ->

        },
        list = {
            var focusedIndex by remember { mutableStateOf(0) }
            LazyRow(
                contentPadding = PaddingValues(32.dp),
                horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    item(key = item.url) {
                        RowItem(
                            item = item,
                            height = cardHeight,
                            index = index,
                            focused = focusedIndex == index
                        ) {
                            focusedIndex = it
                        }
                    }
                }
            }
        }
    )
}

@UnstableApi
@ExperimentalTvMaterial3Api
@Composable
fun ImmersiveListScope.RowItem(
    item: TvItem,
    height: Dp,
    index: Int,
    focused: Boolean,
    onSelected: (index: Int) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    PreviewCard(modifier = Modifier
        .onFocusChanged { onSelected(index) }
        .clickable(
            interactionSource = interactionSource,
            indication = rememberRipple(),
            onClick = { onSelected(index) }
        ), cardWidth = 240.dp, cardHeight = height, videoUrl = item.url,
        hasFocus = focused, thumbnailUrl = item.image)
}