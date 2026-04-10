package com.smartkrishi.presentation.chat;

import androidx.lifecycle.ViewModel;
import kotlinx.coroutines.Dispatchers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J \u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u000f2\u000e\b\u0002\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014H\u0002J\u0018\u0010\u0016\u001a\u0004\u0018\u00010\u000f2\f\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\u0014H\u0002J\u000e\u0010\u0019\u001a\u00020\u00112\u0006\u0010\u001a\u001a\u00020\u0015J\u000e\u0010\u001b\u001a\u00020\u00112\u0006\u0010\u001c\u001a\u00020\u000fJ\u0006\u0010\u001d\u001a\u00020\u0011J\b\u0010\u001e\u001a\u00020\u0011H\u0002J\b\u0010\u001f\u001a\u00020\u0011H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R+\u0010\u0007\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u00068F@BX\u0086\u008e\u0002\u00a2\u0006\u0012\n\u0004\b\f\u0010\r\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006 "}, d2 = {"Lcom/smartkrishi/presentation/chat/KrishiMitriChatViewModel;", "Landroidx/lifecycle/ViewModel;", "()V", "client", "Lokhttp3/OkHttpClient;", "<set-?>", "Lcom/smartkrishi/presentation/chat/ChatUiState;", "uiState", "getUiState", "()Lcom/smartkrishi/presentation/chat/ChatUiState;", "setUiState", "(Lcom/smartkrishi/presentation/chat/ChatUiState;)V", "uiState$delegate", "Landroidx/compose/runtime/MutableState;", "userName", "", "addBotMessage", "", "text", "quickReplies", "", "Lcom/smartkrishi/presentation/chat/QuickReply;", "askGemini", "conversation", "Lcom/smartkrishi/presentation/chat/ChatMessage;", "onQuickReplyClick", "reply", "onUserSend", "message", "resetChat", "showOptionsMenu", "showWelcomeSequence", "app_debug"})
public final class KrishiMitriChatViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final androidx.compose.runtime.MutableState uiState$delegate = null;
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient client = null;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String userName = "";
    
    public KrishiMitriChatViewModel() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.smartkrishi.presentation.chat.ChatUiState getUiState() {
        return null;
    }
    
    private final void setUiState(com.smartkrishi.presentation.chat.ChatUiState p0) {
    }
    
    private final void showWelcomeSequence() {
    }
    
    private final void showOptionsMenu() {
    }
    
    private final void addBotMessage(java.lang.String text, java.util.List<com.smartkrishi.presentation.chat.QuickReply> quickReplies) {
    }
    
    public final void onQuickReplyClick(@org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.chat.QuickReply reply) {
    }
    
    public final void onUserSend(@org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    private final java.lang.String askGemini(java.util.List<com.smartkrishi.presentation.chat.ChatMessage> conversation) {
        return null;
    }
    
    public final void resetChat() {
    }
}