import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.oikvpqya.compose.fastscroller.VerticalScrollbar
import io.github.oikvpqya.compose.fastscroller.material3.defaultMaterialScrollbarStyle
import io.github.oikvpqya.compose.fastscroller.rememberScrollbarAdapter

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

@Composable
fun StandardVerticalScrollbar(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {
    VerticalScrollbar(
        adapter = rememberScrollbarAdapter(listState),
        style = defaultMaterialScrollbarStyle(),
        enablePressToScroll = false,
        modifier = modifier
            .fillMaxHeight()
            .width(8.dp)
    )
}
