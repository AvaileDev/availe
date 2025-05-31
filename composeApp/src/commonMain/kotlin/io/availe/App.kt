package io.availe

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import io.availe.components.chat.ChatInputFieldContainer
import io.availe.components.chat.ChatSidebar
import io.availe.components.chat.ChatThread
import io.availe.config.HttpClientProvider
import io.availe.repositories.KtorChatRepository
import io.availe.util.getScreenWidthDp
import io.availe.viewmodels.ChatViewModel
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import io.ktor.http.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    val httpClient = HttpClientProvider.client
    val listState = rememberLazyListState()
    val screenWidth: Dp = getScreenWidthDp()
    val responsiveWidth: Float = when {
        screenWidth < 600.dp -> .9f
        screenWidth < 840.dp -> .7f
        else -> .63f
    }
    var textContent: String by remember { mutableStateOf("") }
    val chatRepository = remember { KtorChatRepository(httpClient) }
    val chatViewModel = remember { ChatViewModel(chatRepository) }
    var targetUrl: String by remember { mutableStateOf("http://localhost:8080/nlip") }
    var uploadedFiles by remember { mutableStateOf(listOf<PlatformFile>()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val screenWidthDp = getScreenWidthDp()
    val compact = screenWidthDp < 600.dp
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var isSidebarOpen by remember { mutableStateOf(true) }

    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                addPlatformFileSupport()
            }
            .build()
    }

    MaterialTheme(colorScheme = lightColorScheme()) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.zIndex(1f).fillMaxWidth()
                )
            }
        ) { innerPadding ->
            if (compact) {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ChatSidebar(
                            viewModel = chatViewModel,
                            closeDrawer = { scope.launch { drawerState.close() } }
                        )
                    },
                    scrimColor = Color.Black.copy(alpha = .45f)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MainChatArea(
                            innerPadding = innerPadding,
                            responsiveWidth = responsiveWidth,
                            listState = listState,
                            viewModel = chatViewModel,
                            textContent = textContent,
                            onTextChange = { textContent = it },
                            targetUrl = targetUrl,
                            onTargetUrlChange = { text, _ -> targetUrl = text },
                            snackbarHostState = snackbarHostState,
                            onSend = { message, url ->
                                chatViewModel.send(message, url)
                                textContent = ""
                            },
                            uploadedFiles = uploadedFiles,
                            onFileUploaded = { file ->
                                uploadedFiles = uploadedFiles + file
                            }
                        )

                        Text(
                            "≡",
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 12.dp, y = 12.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .clickable { scope.launch { drawerState.open() } }
                                .zIndex(2f)
                        )
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        val sidebarWidth by animateDpAsState(if (isSidebarOpen) 250.dp else 0.dp)
                        AnimatedVisibility(
                            visible = sidebarWidth > 0.dp,
                            enter = expandHorizontally(),
                            exit = shrinkHorizontally()
                        ) {
                            ChatSidebar(
                                viewModel = chatViewModel,
                                closeDrawer = { isSidebarOpen = false }
                            )
                        }

                        MainChatArea(
                            innerPadding = innerPadding,
                            responsiveWidth = responsiveWidth,
                            listState = listState,
                            viewModel = chatViewModel,
                            textContent = textContent,
                            onTextChange = { textContent = it },
                            targetUrl = targetUrl,
                            onTargetUrlChange = { text, _ -> targetUrl = text },
                            snackbarHostState = snackbarHostState,
                            onSend = { message, url ->
                                chatViewModel.send(message, url)
                                textContent = ""
                            },
                            uploadedFiles = uploadedFiles,
                            onFileUploaded = { file ->
                                uploadedFiles = uploadedFiles + file
                            }
                        )
                    }

                    Text(
                        if (isSidebarOpen) "<" else ">",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = if (isSidebarOpen) 250.dp else 0.dp, y = 12.dp)
                            .clickable { isSidebarOpen = !isSidebarOpen }
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                            .zIndex(2f)
                    )
                }
            }
        }
    }
}

@Composable
private fun MainChatArea(
    innerPadding: PaddingValues,
    responsiveWidth: Float,
    listState: androidx.compose.foundation.lazy.LazyListState,
    viewModel: ChatViewModel,
    textContent: String,
    onTextChange: (String) -> Unit,
    targetUrl: String,
    onTargetUrlChange: (String, Url?) -> Unit,
    snackbarHostState: SnackbarHostState,
    onSend: (String, Url) -> Unit,
    uploadedFiles: List<PlatformFile>,
    onFileUploaded: (PlatformFile) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ChatThread(
            state = listState,
            responsiveWidth = responsiveWidth,
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Spacer(Modifier.height(8.dp))
        ChatInputFieldContainer(
            modifier = Modifier.fillMaxWidth(responsiveWidth),
            textContent = textContent,
            onTextChange = onTextChange,
            targetUrl = targetUrl,
            onTargetUrlChange = onTargetUrlChange,
            snackbarHostState = snackbarHostState,
            onSend = onSend,
            uploadedFiles = uploadedFiles,
            onFileUploaded = onFileUploaded
        )
    }
}
