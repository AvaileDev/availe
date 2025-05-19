package io.availe.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import io.availe.utils.normaliseUrl
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.securelyAccessFile
import io.github.vinceglb.filekit.dialogs.FileKitMode
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import io.ktor.http.*
import kotlinx.coroutines.launch

@Composable
fun ChatInputField(
    modifier: Modifier = Modifier,
    text: String,
    onTextChange: (String) -> Unit,
) {
    val maxLines = 5

    TextFieldWithScrollbar(
        value = text,
        onValueChange = onTextChange,
        modifier = modifier.fillMaxWidth(),
        minLines = 1,
        maxLines = maxLines,
        placeholder = { Text("Type a message...") }
    )
}

@Composable
fun UrlInputBox(
    targetUrl: String,
    onTargetUrlChange: (String, Url?) -> Unit,
    error: String? = null
) {
    TextField(
        value = targetUrl,
        onValueChange = { input ->
            val cleaned = input.trim()
            val parsedUrl = try {
                Url(cleaned)
            } catch (e: Exception) {
                null
            }
            onTargetUrlChange(input, parsedUrl)
        },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text("Enter URL") },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Uri
        ),
        isError = error != null
    )
    if (error != null) {
        Text(text = error, color = Color.Red)
    }
}

@Composable
fun ChatInputFieldContainer(
    modifier: Modifier = Modifier,
    textContent: String,
    onTextChange: (String) -> Unit,
    targetUrl: String,
    onTargetUrlChange: (String, Url?) -> Unit,
    snackbarHostState: SnackbarHostState,
    onSend: (String, Url) -> Unit,
    uploadedFiles: List<PlatformFile>,
    onFileUploaded: (PlatformFile) -> Unit,
) {
    var urlError by remember { mutableStateOf<String?>(null) }
    var previewFile by remember { mutableStateOf<PlatformFile?>(null) }

    FilePreviewDialog(
        file = previewFile,
        onDismiss = { previewFile = null }
    )

    Column(modifier = modifier.imePadding()) {
        FileUploadThumbnails(
            files = uploadedFiles,
            onFileClick = { file -> previewFile = file }
        )
        ChatInputField(text = textContent, onTextChange = onTextChange)

        Spacer(Modifier.padding(10.dp))

        UrlInputBox(targetUrl, onTargetUrlChange, error = urlError)

        Spacer(Modifier.padding(10.dp))

        Row {
            UploadButton { files ->
                files.forEach(onFileUploaded)
            }

            Spacer(Modifier.padding(10.dp))

            SubmitButton(
                textContent = textContent,
                targetUrl = targetUrl,
                snackbarHostState = snackbarHostState,
                onSend = onSend,
                setUrlError = { urlError = it }
            )
        }
    }
}

@Composable
fun UploadButton(
    onFilesPicked: (List<PlatformFile>) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    Button(onClick = {
        coroutineScope.launch {
            FileKit.openFilePicker(
                type = FileKitType.File(),
                mode = FileKitMode.Multiple()
            )?.let(onFilesPicked)
        }
    }) {
        Text("Upload files")
    }
}


@Composable
fun SubmitButton(
    textContent: String,
    targetUrl: String,
    snackbarHostState: SnackbarHostState,
    onSend: (String, Url) -> Unit,
    setUrlError: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    Button(
        onClick = {
            val parsed = try {
                normaliseUrl(targetUrl, ensureTrailingSlash = true)
            } catch (e: Exception) {
                null
            }
            if (parsed != null) {
                setUrlError(null)
                onSend(textContent, parsed)
            } else {
                setUrlError("Invalid URL")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Invalid URL!")
                }
            }
        },
        enabled = textContent.isNotBlank(),
        modifier = modifier
    ) {
        Text("Send")
    }
}

@Composable
fun FileUploadThumbnails(
    files: List<PlatformFile>,
    onFileClick: (PlatformFile) -> Unit,
    modifier: Modifier = Modifier
) {
    if (files.isEmpty()) return

    LazyRow(
        modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        items(files) { file ->
            Card(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(80.dp)
                    .clickable { onFileClick(file) },
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                SubcomposeAsyncImage(
                    model = file,
                    contentDescription = file.name,
                    modifier = Modifier.fillMaxSize(),
                    onState = { state -> state.securelyAccessFile(file) }
                ) {
                    val state by painter.state.collectAsState()
                    when (state) {
                        is AsyncImagePainter.State.Loading -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }

                        is AsyncImagePainter.State.Error -> Box(
                            Modifier
                                .fillMaxSize()
                                .background(Color(0xFFD0D0D0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Black,
                                maxLines = 2
                            )
                        }

                        is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun FilePreviewDialog(
    file: PlatformFile?,
    onDismiss: () -> Unit
) {
    if (file == null) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.8f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = file,
                contentDescription = file.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(16.dp),
                onState = { state -> state.securelyAccessFile(file) },
                contentScale = ContentScale.Fit
            )
        }
    }
}