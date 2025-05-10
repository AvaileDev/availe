package io.availe

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.availe.components.ChatInputField
import io.availe.components.ChatMessageThread
import io.availe.util.getScreenWidthDp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val screenWidth: Dp = getScreenWidthDp()

    val responsiveWidth: Float = when {
        screenWidth < 600.dp -> .9f
        screenWidth < 840.dp -> .7f
        else -> .55f
    }

    MaterialTheme(colorScheme = lightColorScheme()) {
        Surface {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 12.dp, top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ChatMessageThread(
                    modifier = Modifier
                        .fillMaxWidth(responsiveWidth)
                        .weight(1f)
                )
                Spacer(Modifier.height(8.dp))
                ChatInputField(
                    modifier = Modifier.fillMaxWidth(responsiveWidth)
                )
            }
        }
    }
}