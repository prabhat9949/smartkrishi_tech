package com.smartkrishi.presentation.chat;

import androidx.lifecycle.ViewModel;
import kotlinx.coroutines.Dispatchers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/smartkrishi/presentation/chat/GeminiConfig;", "", "()V", "GEMINI_API_KEY", "", "GEMINI_ENDPOINT", "app_debug"})
public final class GeminiConfig {
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GEMINI_API_KEY = "AIzaSyCRL2SZCh1fDwRb6S9Il_KuZWTO7a29r9U";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GEMINI_ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=AIzaSyCRL2SZCh1fDwRb6S9Il_KuZWTO7a29r9U";
    @org.jetbrains.annotations.NotNull()
    public static final com.smartkrishi.presentation.chat.GeminiConfig INSTANCE = null;
    
    private GeminiConfig() {
        super();
    }
}