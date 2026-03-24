// File: presentation/chat/KrishiMitriChatScreen.kt
package com.smartkrishi.presentation.chat

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KrishiMitriChatScreen(
    modifier: Modifier = Modifier,
    viewModel: KrishiMitriChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    userName: String? = null // Pass from navigation or user session
) {
    // Set username if provided
    LaunchedEffect(userName) {
        if (!userName.isNullOrBlank()) {
            viewModel.setUserName(userName)
        }
    }

    // Start with light mode (isDarkTheme = false)
    var isDarkTheme by rememberSaveable { mutableStateOf(false) }

    ChatTheme(darkTheme = isDarkTheme) {
        KrishiMitriChatContent(
            modifier = modifier,
            viewModel = viewModel,
            isDarkTheme = isDarkTheme,
            onThemeToggle = { isDarkTheme = !isDarkTheme }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun KrishiMitriChatContent(
    modifier: Modifier,
    viewModel: KrishiMitriChatViewModel,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    val state = viewModel.uiState
    val scrollState = rememberLazyListState()
    var input by rememberSaveable { mutableStateOf("") }

    val chatColors = remember(isDarkTheme) {
        if (isDarkTheme) {
            ChatColorScheme(
                background = DarkChatColors.background,
                surface = DarkChatColors.surface,
                primary = DarkChatColors.primary,
                primaryContainer = DarkChatColors.primaryContainer,
                userBubble = DarkChatColors.userBubble,
                botBubble = DarkChatColors.botBubble,
                userText = DarkChatColors.userText,
                botText = DarkChatColors.botText,
                inputBackground = DarkChatColors.inputBackground,
                inputBorder = DarkChatColors.inputBorder,
                quickReplyBg = DarkChatColors.quickReplyBg,
                quickReplyBorder = DarkChatColors.quickReplyBorder
            )
        } else {
            ChatColorScheme(
                background = LightChatColors.background,
                surface = LightChatColors.surface,
                primary = LightChatColors.primary,
                primaryContainer = LightChatColors.primaryContainer,
                userBubble = LightChatColors.userBubble,
                botBubble = LightChatColors.botBubble,
                userText = LightChatColors.userText,
                botText = LightChatColors.botText,
                inputBackground = LightChatColors.inputBackground,
                inputBorder = LightChatColors.inputBorder,
                quickReplyBg = LightChatColors.quickReplyBg,
                quickReplyBorder = LightChatColors.quickReplyBorder
            )
        }
    }

    val voiceLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                if (!spoken.isNullOrEmpty()) {
                    input = spoken[0]
                }
            }
        }

    fun launchVoice() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your query...")
        voiceLauncher.launch(intent)
    }

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            scrollState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(chatColors.background),

        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = chatColors.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🌾", fontSize = 20.sp)
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                "KrishiMitri",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "Smart Farm Assistant",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            imageVector = if (isDarkTheme)
                                Icons.Default.LightMode
                            else
                                Icons.Default.DarkMode,
                            contentDescription = "Toggle theme"
                        )
                    }

                    IconButton(onClick = { viewModel.resetChat() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = chatColors.surface
                )
            )
        },

        bottomBar = {
            ChatInputBar(
                input = input,
                onInputChange = { input = it },
                onSend = {
                    if (input.isNotBlank() && !state.isLoading) {
                        viewModel.onUserSend(input)
                        input = ""
                    }
                },
                onVoiceClick = { launchVoice() },
                isLoading = state.isLoading,
                chatColors = chatColors
            )
        }

    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(chatColors.background)
                .padding(padding)
            // 🔥 KEY FIX (Keyboard safe)
        ) {

            LazyColumn(
                state = scrollState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp,
                    bottom = 80.dp // extra space above input
                )
            ) {

                items(state.messages) { msg ->
                    ChatMessageItem(
                        message = msg,
                        chatColors = chatColors,
                        onQuickReplyClick = { reply ->
                            viewModel.onQuickReplyClick(reply)
                        }
                    )
                }

                if (state.isLoading) {
                    item {
                        TypingIndicator(chatColors = chatColors)
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessageUi,
    chatColors: ChatColorScheme,
    onQuickReplyClick: (QuickReply) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 20 })
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .widthIn(max = 280.dp)
                    .clip(
                        RoundedCornerShape(
                            topStart = if (message.isUser) 18.dp else 4.dp,
                            topEnd = if (message.isUser) 4.dp else 18.dp,
                            bottomStart = 18.dp,
                            bottomEnd = 18.dp
                        )
                    )
                    .background(
                        if (message.isUser) chatColors.userBubble
                        else chatColors.botBubble
                    )
                    .then(
                        if (!message.isUser) {
                            Modifier.border(
                                width = 1.dp,
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(
                                    topStart = 4.dp,
                                    topEnd = 18.dp,
                                    bottomStart = 18.dp,
                                    bottomEnd = 18.dp
                                )
                            )
                        } else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (message.isUser) chatColors.userText
                    else chatColors.botText,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
            }
        }

        Text(
            text = message.timestamp.toTimeString(),
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(0.5f),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )

        if (message.quickReplies.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            QuickReplyButtons(
                replies = message.quickReplies,
                chatColors = chatColors,
                onClick = onQuickReplyClick
            )
        }
    }

    Spacer(Modifier.height(8.dp))
}

@Composable
fun QuickReplyButtons(
    replies: List<QuickReply>,
    chatColors: ChatColorScheme,
    onClick: (QuickReply) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(replies) { reply ->
            Surface(
                onClick = { onClick(reply) },
                modifier = Modifier
                    .height(36.dp)
                    .border(
                        width = 1.5.dp,
                        color = chatColors.quickReplyBorder,
                        shape = RoundedCornerShape(18.dp)
                    ),
                shape = RoundedCornerShape(18.dp),
                color = chatColors.quickReplyBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (reply.emoji.isNotEmpty()) {
                        Text(reply.emoji, fontSize = 16.sp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(
                        reply.text,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = chatColors.botText
                    )
                }
            }
        }
    }
}


@Composable
fun ChatInputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onVoiceClick: () -> Unit,
    isLoading: Boolean,
    chatColors: ChatColorScheme
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding(),   // 🔥 THIS FIXES KEYBOARD OVERLAP
        color = chatColors.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.Bottom
        ) {

            OutlinedTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        "Ask me anything...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.5f)
                    )
                },
                maxLines = 4,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = chatColors.quickReplyBorder,
                    unfocusedBorderColor = chatColors.inputBorder,
                    focusedContainerColor = chatColors.inputBackground,
                    unfocusedContainerColor = chatColors.inputBackground
                ),
                trailingIcon = {
                    IconButton(
                        onClick = onVoiceClick,
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = chatColors.primary
                        )
                    }
                }
            )

            Spacer(Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (input.isNotBlank() && !isLoading) {
                        onSend()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .alpha(
                        if (input.isNotBlank() && !isLoading) 1f else 0.5f
                    ),
                containerColor = chatColors.primary,
                contentColor = chatColors.userText,
                elevation = FloatingActionButtonDefaults.elevation(4.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = chatColors.userText
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
@Composable
fun TypingIndicator(chatColors: ChatColorScheme) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 120.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(chatColors.botBubble)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(18.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "typing")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600),
                            repeatMode = RepeatMode.Reverse,
                            initialStartOffset = StartOffset(index * 150)
                        ),
                        label = "dot_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(chatColors.botText.copy(0.6f))
                    )
                }
            }
        }
    }
}
