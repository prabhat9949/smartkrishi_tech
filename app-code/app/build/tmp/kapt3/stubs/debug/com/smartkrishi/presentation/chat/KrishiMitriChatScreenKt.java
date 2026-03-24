package com.smartkrishi.presentation.chat;

import android.app.Activity;
import android.content.Intent;
import android.speech.RecognizerIntent;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.compose.animation.*;
import androidx.compose.animation.core.*;
import androidx.compose.foundation.layout.*;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextOverflow;
import com.smartkrishi.presentation.chat.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000N\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0003\u001aP\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00052\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0006\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0007\u001a,\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\f2\u0012\u0010\u0010\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a.\u0010\u0012\u001a\u00020\u00012\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\n2\f\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0003\u001a\u001c\u0010\u0019\u001a\u00020\u00012\b\b\u0002\u0010\u0013\u001a\u00020\u00142\b\b\u0002\u0010\u0015\u001a\u00020\u0016H\u0007\u001a2\u0010\u001a\u001a\u00020\u00012\f\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00110\u001c2\u0006\u0010\u000b\u001a\u00020\f2\u0012\u0010\u001d\u001a\u000e\u0012\u0004\u0012\u00020\u0011\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u0010\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\fH\u0007\u00a8\u0006\u001f"}, d2 = {"ChatInputBar", "", "input", "", "onInputChange", "Lkotlin/Function1;", "onSend", "Lkotlin/Function0;", "onVoiceClick", "isLoading", "", "chatColors", "Lcom/smartkrishi/presentation/chat/ChatColorScheme;", "ChatMessageItem", "message", "Lcom/smartkrishi/presentation/chat/ChatMessage;", "onQuickReplyClick", "Lcom/smartkrishi/presentation/chat/QuickReply;", "KrishiMitriChatContent", "modifier", "Landroidx/compose/ui/Modifier;", "viewModel", "Lcom/smartkrishi/presentation/chat/KrishiMitriChatViewModel;", "isDarkTheme", "onThemeToggle", "KrishiMitriChatScreen", "QuickReplyButtons", "replies", "", "onClick", "TypingIndicator", "app_debug"})
public final class KrishiMitriChatScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void KrishiMitriChatScreen(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.chat.KrishiMitriChatViewModel viewModel) {
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    private static final void KrishiMitriChatContent(androidx.compose.ui.Modifier modifier, com.smartkrishi.presentation.chat.KrishiMitriChatViewModel viewModel, boolean isDarkTheme, kotlin.jvm.functions.Function0<kotlin.Unit> onThemeToggle) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ChatMessageItem(@org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.chat.ChatMessage message, @org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.chat.ChatColorScheme chatColors, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.smartkrishi.presentation.chat.QuickReply, kotlin.Unit> onQuickReplyClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void QuickReplyButtons(@org.jetbrains.annotations.NotNull()
    java.util.List<com.smartkrishi.presentation.chat.QuickReply> replies, @org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.chat.ChatColorScheme chatColors, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.smartkrishi.presentation.chat.QuickReply, kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ChatInputBar(@org.jetbrains.annotations.NotNull()
    java.lang.String input, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onInputChange, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSend, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onVoiceClick, boolean isLoading, @org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.chat.ChatColorScheme chatColors) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TypingIndicator(@org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.chat.ChatColorScheme chatColors) {
    }
}