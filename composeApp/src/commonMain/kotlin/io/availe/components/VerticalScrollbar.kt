package io.availe.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.oikvpqya.compose.fastscroller.ScrollbarAdapter
import io.github.oikvpqya.compose.fastscroller.VerticalScrollbar
import io.github.oikvpqya.compose.fastscroller.material3.defaultMaterialScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter

/** Scrollbar for any ScrollState (e.g. multiline TextField) */
@Composable
fun StandardVerticalScrollbar(
    scrollState: ScrollState,
    modifier: Modifier = Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(scrollState),
        style = defaultMaterialScrollbarStyle(),
        enablePressToScroll = false,
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
    )
}

/** Scrollbar for LazyColumn driven by dynamic heights in HeightTracker */
@Composable
fun StandardVerticalScrollbar(
    listState: LazyListState,
    heights: HeightTracker,
    modifier: Modifier = Modifier
) {
    val adapter = remember(listState, heights) {
        FenwickScrollbarAdapter(listState, heights)
    }
    VerticalScrollbar(
        adapter = adapter,
        style = defaultMaterialScrollbarStyle(),
        enablePressToScroll = false,
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
    )
}

/** Tracks item heights and prefix sums via Fenwick tree for efficient updates */
class HeightTracker {
    private var fenwick = FenwickTree(0)
    private var itemHeights = IntArray(0)
    private var _total = 0
    val total: Int get() = _total

    fun resize(newSize: Int) {
        if (newSize <= itemHeights.size) return
        val old = itemHeights
        itemHeights = old.copyOf(newSize)
        fenwick = FenwickTree(newSize).also { tree ->
            for (i in old.indices) {
                tree.add(i, old[i])
            }
        }
    }

    fun updateHeight(index: Int, newHeight: Int) {
        if (index >= itemHeights.size) return
        val delta = newHeight - itemHeights[index]
        if (delta == 0) return
        itemHeights[index] = newHeight
        fenwick.add(index, delta)
        _total += delta
    }

    fun prefixSum(index: Int): Int =
        fenwick.prefixSum(index)
}

/** Fenwick tree supporting point updates and prefix sums in O(log n) */
private class FenwickTree(private val size: Int) {
    private val bit = IntArray(size + 1)

    fun add(index: Int, delta: Int) {
        var i = index + 1
        while (i <= size) {
            bit[i] += delta
            i += i and -i
        }
    }

    fun prefixSum(index: Int): Int {
        var i = index
        var sum = 0
        while (i > 0) {
            sum += bit[i]
            i -= i and -i
        }
        return sum
    }
}

/** Adapter connecting LazyListState and HeightTracker to scrollbar moves */
private class FenwickScrollbarAdapter(
    private val listState: LazyListState,
    private val heights: HeightTracker
) : ScrollbarAdapter {
    override val contentSize: Double
        get() = heights.total.toDouble()

    override val viewportSize: Double
        get() = listState.layoutInfo.viewportSize.height.toDouble()

    override val scrollOffset: Double
        get() = (heights.prefixSum(listState.firstVisibleItemIndex) +
                listState.firstVisibleItemScrollOffset).toDouble()

    override suspend fun scrollTo(scrollOffset: Double) {
        val target = scrollOffset.toInt()
        var low = 0
        var high = listState.layoutInfo.totalItemsCount
        while (low < high) {
            val mid = (low + high) / 2
            if (heights.prefixSum(mid) <= target) {
                low = mid + 1
            } else {
                high = mid
            }
        }
        val itemIndex = (low - 1).coerceAtLeast(0)
        val offsetWithinItem = target - heights.prefixSum(itemIndex)
        listState.scrollToItem(itemIndex, offsetWithinItem)
    }
}