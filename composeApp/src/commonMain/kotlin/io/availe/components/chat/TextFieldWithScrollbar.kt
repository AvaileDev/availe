package io.availe.components.chat

import StandardVerticalScrollbar
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults.DecorationBox
import androidx.compose.material3.TextFieldDefaults.colors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextFieldWithScrollbar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    placeholder: @Composable (() -> Unit)? = null,
    label: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = colors(),
    shape: androidx.compose.ui.graphics.Shape = TextFieldDefaults.shape,
) {
    /* one-time line-height lookup for this TextStyle / density */
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val oneLineDp = remember(textStyle, density) {
        with(density) {
            textMeasurer.measure("Ag", textStyle).size.height.toDp()
        }
    }

    val minHeight = oneLineDp * minLines + 4.dp   // small vertical padding
    val maxHeight = oneLineDp * maxLines + 4.dp

    val scrollState = rememberScrollState()

    Box(
        modifier
            .fillMaxWidth()
            .heightIn(min = minHeight, max = maxHeight)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState),
            textStyle = textStyle,
            singleLine = singleLine,
            minLines = minLines,
            maxLines = Int.MAX_VALUE,
            enabled = enabled,
            readOnly = readOnly,
            cursorBrush = SolidColor(
                if (isError) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            ),
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            decorationBox = { inner ->
                DecorationBox(
                    value = value,
                    innerTextField = inner,
                    visualTransformation = visualTransformation,
                    placeholder = placeholder,
                    label = label,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    prefix = prefix,
                    suffix = suffix,
                    supportingText = supportingText,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = interactionSource,
                    colors = colors,
                    shape = shape
                )
            }
        )

        /* show scrollbar only when content overflows */
        if (scrollState.maxValue > 0) {
            StandardVerticalScrollbar(
                scrollState = scrollState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}
