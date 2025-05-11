package io.availe.data.mock

val mockMessages: List<String> = listOf(
        "Hey, I'm trying to build a chat UI in Compose. I'd like it to have a modern app style.",
        """
For a modern chat layout, use a vertical `LazyColumn`, align each message to the left, and stack usernames and timestamps above messages. 
If you're simulating two speakers, use alternating alignments and distinct background shades per user type.
""",
        "I have that part working. What’s the best way to make the text selectable everywhere across a message row?",
        """
Jetpack Compose supports selection through `SelectionContainer`, but selection can only start from actual `Text`. 
To simulate full-row selection, wrap each message in a `SelectionContainer` and ensure the `Text` inside uses `.fillMaxWidth()`.
This lets you drag from whitespace, as long as it’s inside the Text’s box.
""",
        "Can you give me a small example?",
        """
@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    SelectionContainer {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = if (isUser) TextAlign.End else TextAlign.Start
            )
        }
    }
}
""",
        "Okay cool. Now I want to add a scrollbar. I’m using LazyColumn.",
        """
Use a vertical scrollbar composable like `VerticalScrollbar` from the FastScroller library. 
Pass your `LazyListState` to `rememberScrollbarAdapter` and align it to the end of your container.
Make sure to call `fillMaxHeight()` and set a consistent `width()` for best appearance.
""",
        "It scrolls now, but I’m seeing a weird purple background behind my cards.",
        """
The default background comes from Material 3's color scheme. 
`Card` uses `colorScheme.surface` and may apply tonal elevation if elevation is non-zero. 
Override both using:

colors = CardDefaults.cardColors(containerColor = Color.Transparent),
elevation = CardDefaults.cardElevation(0.dp)

To remove the root background, wrap your app in `Surface(color = Color.White)` and override your light color scheme to use white for `surface`, `background`, and `surfaceVariant`.
""",
        "I’ve done that but still seeing a hint of purple somewhere.",
        """
Check your theme’s root `Surface`. If not explicitly given a white background, it inherits from `colorScheme.surface`, which is slightly tinted in default M3. 
Also check if any child container like `Box` or `Column` uses `.background(MaterialTheme.colorScheme.surfaceVariant)` or similar.
""",
        "I see it now. My input field had surfaceVariant. Makes sense.",
        "Next question: how do I render code blocks inside messages like this chat does?",
        """
You can use a monospaced font and slightly different styling for code blocks.

@Composable
fun CodeBlock(text: String) {
    Surface(
        color = Color(0xFFEEEEEE),
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
        )
    }
}

Split your messages into paragraphs and code blocks, then render each part with the correct styling.
""",
        "What about a really long AI message? Like one that spans multiple screens?",
        """
Sure. Here's an example of a long AI response.

Jetpack Compose offers strong support for scrollable UIs, but you should consider how to break up long-form content visually.
A long message can include multiple paragraphs, bullet points, and code examples:

1. Use `LazyColumn` to render each paragraph separately.
2. Use `SelectionContainer` to preserve text copying.
3. Use `BoxWithConstraints` to react to screen size for wrapping and truncation.
4. Test edge cases like line wrapping in narrow layouts, soft line breaks, and high-DPI scaling.

Example:

val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }

Text(
    text = longMessage,
    onTextLayout = { layoutResult.value = it },
    softWrap = true,
    modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
)

Long-form messages should render cleanly, wrap naturally, and behave predictably with user input or gesture events.
Avoid clipping by verifying your parent containers don’t impose fixed height unless needed.
Use `rememberScrollState` or `LazyColumn` for scrolling behavior on long messages or overflowing conversations.
""",
        "Thanks. One more: how do I scroll to the bottom when a new message is added?",
        """
Call `state.animateScrollToItem(lastIndex)` after appending to your list.
If using a `LaunchedEffect`, tie the scroll to the message count:

LaunchedEffect(messages.size) {
    listState.animateScrollToItem(messages.lastIndex)
}

Ensure `LazyListState` is shared between the composable and the list.
Use `snapshotFlow` if reacting to scroll position updates.
""",
        "Great, that solves everything I needed. Thank you!"
)

val repeatedMockMessages: List<String> = List(10) { mockMessages }.flatten()