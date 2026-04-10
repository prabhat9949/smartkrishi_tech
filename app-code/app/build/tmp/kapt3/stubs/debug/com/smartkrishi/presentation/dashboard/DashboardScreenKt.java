package com.smartkrishi.presentation.dashboard;

import androidx.compose.animation.core.RepeatMode;
import androidx.compose.foundation.ExperimentalFoundationApi;
import androidx.compose.foundation.layout.*;
import androidx.compose.foundation.lazy.grid.GridCells;
import androidx.compose.material.icons.Icons;
import androidx.compose.material.icons.filled.*;
import androidx.compose.material.icons.outlined.*;
import androidx.compose.material3.*;
import androidx.compose.runtime.*;
import androidx.compose.runtime.snapshots.SnapshotStateMap;
import androidx.compose.ui.Alignment;
import androidx.compose.ui.Modifier;
import androidx.compose.ui.graphics.Brush;
import androidx.compose.ui.graphics.drawscope.Stroke;
import androidx.compose.ui.graphics.vector.ImageVector;
import androidx.compose.ui.layout.ContentScale;
import androidx.compose.ui.text.font.FontWeight;
import androidx.compose.ui.text.style.TextAlign;
import androidx.compose.ui.text.style.TextOverflow;
import androidx.compose.ui.window.DialogProperties;
import androidx.navigation.NavController;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.smartkrishi.R;
import com.smartkrishi.presentation.home.FarmViewModel;
import com.smartkrishi.presentation.model.Farm;
import com.smartkrishi.presentation.model.SensorNode;
import com.smartkrishi.presentation.navigation.Screen;
import com.smartkrishi.presentation.theme.ThemeState;
import kotlinx.coroutines.Dispatchers;
import org.json.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\u009c\u0001\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b*\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0005\n\u0002\u0010\u0007\n\u0002\b\u0005\u001a\u0012\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\rH\u0007\u001a\u0016\u0010\u000e\u001a\u00020\u000b2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010H\u0007\u001a\u001e\u0010\u0011\u001a\u00020\u000b2\u0006\u0010\u0012\u001a\u00020\u00132\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010H\u0007\u001a*\u0010\u0015\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\b2\u0006\u0010\u0019\u001a\u00020\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001a\u0010\u001b\u001a\\\u0010\u001c\u001a\u00020\u000b2\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010 \u001a\u00020\b2\b\u0010!\u001a\u0004\u0018\u00010\"2\u0006\u0010#\u001a\u00020$2\b\u0010%\u001a\u0004\u0018\u00010\b2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b&\u0010\'\u001a2\u0010(\u001a\u00020\u000b2\u0006\u0010)\u001a\u00020*2\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b+\u0010,\u001a \u0010-\u001a\u00020\u000b2\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010.\u001a\u00020/2\u0006\u00100\u001a\u000201H\u0007\u001a\u0010\u00102\u001a\u00020\u000b2\u0006\u0010\u0012\u001a\u00020\u0013H\u0007\u001a&\u00103\u001a\u00020\u000b2\u0006\u00104\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\u00172\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010H\u0007\u001aD\u00105\u001a\u00020\u000b2\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u00106\u001a\u0002072\u0006\u0010\u0012\u001a\u00020\u00132\b\u00108\u001a\u0004\u0018\u00010\"H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b9\u0010:\u001aj\u0010;\u001a\u00020\u000b2\f\u0010<\u001a\b\u0012\u0004\u0012\u0002010=2\u0006\u0010>\u001a\u0002012\f\u0010?\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00102\u0012\u0010@\u001a\u000e\u0012\u0004\u0012\u000201\u0012\u0004\u0012\u00020\u000b0A2\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\bB\u0010C\u001a2\u0010D\u001a\u00020\u000b2\u0006\u0010E\u001a\u00020\b2\u0006\u0010F\u001a\u00020\b2\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u0001H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\bG\u0010H\u001a2\u0010I\u001a\u00020\u000b2\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010\u0012\u001a\u00020\u0013H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\bJ\u0010K\u001aV\u0010L\u001a\u00020\u000b2\u0006\u0010M\u001a\u00020\b2\f\u0010N\u001a\b\u0012\u0004\u0012\u00020O0=2\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010P\u001a\u00020$2\f\u0010Q\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\bR\u0010S\u001aZ\u0010T\u001a\u00020\u000b2\f\u0010U\u001a\b\u0012\u0004\u0012\u00020V0=2\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\f\u0010W\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00102\u0012\u0010X\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\u000b0AH\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\bY\u0010Z\u001a@\u0010[\u001a\u00020\u000b2\u0006\u0010>\u001a\u0002012\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00102\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b\\\u0010]\u001a`\u0010^\u001a\u00020\u000b2\u0006\u0010M\u001a\u00020\b2\u0006\u0010_\u001a\u00020$2\u0006\u0010`\u001a\u00020$2\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\u0012\u0010a\u001a\u000e\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020\u000b0A2\b\b\u0002\u0010\f\u001a\u00020\rH\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\bb\u0010c\u001a8\u0010d\u001a\u00020\u000b2\u0006\u00104\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0019\u001a\u00020\u00012\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u000b0\u0010H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\be\u0010f\u001a*\u0010g\u001a\u00020\u000b2\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u0001H\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\bh\u0010i\u001a\u0010\u0010j\u001a\u00020\u000b2\u0006\u0010k\u001a\u00020OH\u0007\u001aN\u0010l\u001a\u00020\u000b2\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\u0006\u0010m\u001a\u00020$2\u0006\u0010n\u001a\u00020$2\u0012\u0010o\u001a\u000e\u0012\u0004\u0012\u00020$\u0012\u0004\u0012\u00020\u000b0AH\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\bp\u0010q\u001a\\\u0010r\u001a\u00020\u000b2\u0006\u0010M\u001a\u00020\b2\u0006\u0010s\u001a\u00020\b2\u0006\u0010t\u001a\u00020\b2\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010u\u001a\u00020\u00012\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u00012\b\b\u0002\u0010\f\u001a\u00020\rH\u0007\u00f8\u0001\u0000\u00a2\u0006\u0004\bv\u0010w\u001aL\u0010x\u001a\u00020\u000b2\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010M\u001a\u00020\b2\u0006\u0010\u0018\u001a\u00020\b2\b\b\u0002\u0010\f\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\by\u0010z\u001aH\u0010{\u001a\u00020\u000b2\u0006\u0010!\u001a\u00020\"2\u0006\u0010|\u001a\u00020\b2\f\u0010?\u001a\b\u0012\u0004\u0012\u00020\u000b0\u00102\u0006\u0010\u001d\u001a\u00020\u00012\u0006\u0010\u001e\u001a\u00020\u00012\u0006\u0010\u001f\u001a\u00020\u0001H\u0003\u00f8\u0001\u0000\u00a2\u0006\u0004\b}\u0010~\u001a \u0010\u007f\u001a\u0002072\f\u0010U\u001a\b\u0012\u0004\u0012\u00020V0=2\b\u0010!\u001a\u0004\u0018\u00010\"H\u0002\u001a\u001e\u0010\u0080\u0001\u001a\b\u0012\u0004\u0012\u00020O0=2\r\u0010U\u001a\t\u0012\u0005\u0012\u00030\u0081\u00010=H\u0002\u001a\n\u0010\u0082\u0001\u001a\u00030\u0081\u0001H\u0002\u001a/\u0010\u0083\u0001\u001a\u0004\u0018\u00010\"2\u0007\u0010\u0084\u0001\u001a\u00020\b2\b\u0010\u0085\u0001\u001a\u00030\u0086\u00012\b\u0010\u0087\u0001\u001a\u00030\u0086\u0001H\u0082@\u00a2\u0006\u0003\u0010\u0088\u0001\u001a\u0017\u0010\u0089\u0001\u001a\b\u0012\u0004\u0012\u00020\b0=2\u0006\u0010!\u001a\u00020\"H\u0002\u001a!\u0010\u008a\u0001\u001a\u00020\u00012\u0007\u0010\u008b\u0001\u001a\u00020\b2\u0007\u0010\u0018\u001a\u00030\u008c\u0001H\u0002\u00a2\u0006\u0003\u0010\u008d\u0001\u001a\u0012\u0010\u008e\u0001\u001a\u00020\b2\u0007\u0010\u008f\u0001\u001a\u00020\bH\u0002\u001a\u000e\u0010\u0090\u0001\u001a\u00030\u0081\u0001*\u00020VH\u0002\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0006\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u000e\u0010\u0007\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000\"\u0010\u0010\t\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\u0082\u0002\u0007\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0091\u0001"}, d2 = {"AccentGreenSoft", "Landroidx/compose/ui/graphics/Color;", "J", "CardDark", "CardLight", "DarkBackground", "LightBackground", "OPEN_WEATHER_API_KEY", "", "PrimaryGreen", "AnimatedChatbotAvatar", "", "modifier", "Landroidx/compose/ui/Modifier;", "AnimatedChatbotFab", "onClick", "Lkotlin/Function0;", "ChatPopup", "navController", "Landroidx/navigation/NavController;", "onClosePopup", "CompactMetric", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "value", "textColor", "CompactMetric-mxwnekA", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;J)V", "CompactWeatherCard", "cardColor", "textPrimary", "textSecondary", "farmLocation", "weather", "Lcom/smartkrishi/presentation/dashboard/WeatherInfo;", "isLoading", "", "error", "CompactWeatherCard-KC2tld4", "(JJJLjava/lang/String;Lcom/smartkrishi/presentation/dashboard/WeatherInfo;ZLjava/lang/String;Lkotlin/jvm/functions/Function0;)V", "DailyForecastRow", "day", "Lcom/smartkrishi/presentation/dashboard/DailyForecast;", "DailyForecastRow-zSO0fhY", "(Lcom/smartkrishi/presentation/dashboard/DailyForecast;JJJ)V", "DashboardScreen", "drawerState", "Landroidx/compose/material3/DrawerState;", "farm", "Lcom/smartkrishi/presentation/model/Farm;", "DrawerContent", "DrawerItem", "label", "EnhancedAiRecommendations", "recommendations", "Lcom/smartkrishi/presentation/dashboard/AiRecommendations;", "weatherInfo", "EnhancedAiRecommendations-RGew2ao", "(JJJLcom/smartkrishi/presentation/dashboard/AiRecommendations;Landroidx/navigation/NavController;Lcom/smartkrishi/presentation/dashboard/WeatherInfo;)V", "FarmSelectionDialog", "farms", "", "selectedFarm", "onDismiss", "onFarmSelected", "Lkotlin/Function1;", "FarmSelectionDialog-xKDoepM", "(Ljava/util/List;Lcom/smartkrishi/presentation/model/Farm;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;Landroidx/navigation/NavController;JJJ)V", "GreetingSection", "userName", "farmName", "GreetingSection-0YGnOg8", "(Ljava/lang/String;Ljava/lang/String;JJ)V", "LogHistoryBarGraph", "LogHistoryBarGraph-qwTeutE", "(JJJLandroidx/navigation/NavController;)V", "NodeSensorCardWithCircles", "title", "params", "Lcom/smartkrishi/presentation/dashboard/SoilParam;", "isSyncing", "onSync", "NodeSensorCardWithCircles--ANb2zM", "(Ljava/lang/String;Ljava/util/List;JJJZLkotlin/jvm/functions/Function0;)V", "NodeSensorsSection", "nodes", "Lcom/smartkrishi/presentation/model/SensorNode;", "onSyncAll", "onSyncNode", "NodeSensorsSection-EtIuwbw", "(Ljava/util/List;JJJLkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;)V", "ProfessionalFarmSelector", "ProfessionalFarmSelector-FLEW7EY", "(Lcom/smartkrishi/presentation/model/Farm;Lkotlin/jvm/functions/Function0;JJJ)V", "PumpStatusTile", "isOn", "isUpdating", "onToggle", "PumpStatusTile-Oe2N1DA", "(Ljava/lang/String;ZZJJJLkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;)V", "QuickButton", "QuickButton-9LQNqLg", "(Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;JLkotlin/jvm/functions/Function0;)V", "QuickModuleButtons", "QuickModuleButtons-WkMS-hQ", "(Landroidx/navigation/NavController;JJ)V", "SingleSensorCircle", "param", "SystemStatusGrid", "isPumpOn", "isPumpUpdating", "onPumpToggleRequest", "SystemStatusGrid-RGew2ao", "(JJJZZLkotlin/jvm/functions/Function1;)V", "SystemTile", "subtitle", "status", "tint", "SystemTile-jGAnO84", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/graphics/vector/ImageVector;JJJJLandroidx/compose/ui/Modifier;)V", "WeatherDetailCard", "WeatherDetailCard-Uzs9YN0", "(Landroidx/compose/ui/graphics/vector/ImageVector;Ljava/lang/String;Ljava/lang/String;Landroidx/compose/ui/Modifier;JJJ)V", "WeatherDetailDialog", "location", "WeatherDetailDialog-15-ZiRo", "(Lcom/smartkrishi/presentation/dashboard/WeatherInfo;Ljava/lang/String;Lkotlin/jvm/functions/Function0;JJJ)V", "computeAiRecommendations", "computeAverageParams", "Lcom/smartkrishi/presentation/dashboard/SoilNodeUi;", "demoNodeUi", "fetchWeatherForLocation", "locationName", "lat", "", "lon", "(Ljava/lang/String;DDLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getWeatherBasedTips", "soilStatusColor", "name", "", "(Ljava/lang/String;F)J", "weatherEmojiFor", "condition", "toSoilNodeUi", "app_debug"})
public final class DashboardScreenKt {
    private static final long PrimaryGreen = 0L;
    private static final long AccentGreenSoft = 0L;
    private static final long LightBackground = 0L;
    private static final long DarkBackground = 0L;
    private static final long CardLight = 0L;
    private static final long CardDark = 0L;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String OPEN_WEATHER_API_KEY = "64961347ba9d05d6b1a486037c5142c1";
    
    private static final com.smartkrishi.presentation.dashboard.SoilNodeUi demoNodeUi() {
        return null;
    }
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class, androidx.compose.foundation.ExperimentalFoundationApi.class})
    @androidx.compose.runtime.Composable()
    public static final void DashboardScreen(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    androidx.compose.material3.DrawerState drawerState, @org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.model.Farm farm) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DrawerContent(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void DrawerItem(@org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.graphics.vector.ImageVector icon, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    private static final java.lang.String weatherEmojiFor(java.lang.String condition) {
        return null;
    }
    
    private static final com.smartkrishi.presentation.dashboard.SoilNodeUi toSoilNodeUi(com.smartkrishi.presentation.model.SensorNode $this$toSoilNodeUi) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    public static final void SingleSensorCircle(@org.jetbrains.annotations.NotNull()
    com.smartkrishi.presentation.dashboard.SoilParam param) {
    }
    
    private static final java.util.List<com.smartkrishi.presentation.dashboard.SoilParam> computeAverageParams(java.util.List<com.smartkrishi.presentation.dashboard.SoilNodeUi> nodes) {
        return null;
    }
    
    private static final long soilStatusColor(java.lang.String name, float value) {
        return 0L;
    }
    
    private static final java.util.List<java.lang.String> getWeatherBasedTips(com.smartkrishi.presentation.dashboard.WeatherInfo weather) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ChatPopup(@org.jetbrains.annotations.NotNull()
    androidx.navigation.NavController navController, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClosePopup) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AnimatedChatbotFab(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AnimatedChatbotAvatar(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    private static final java.lang.Object fetchWeatherForLocation(java.lang.String locationName, double lat, double lon, kotlin.coroutines.Continuation<? super com.smartkrishi.presentation.dashboard.WeatherInfo> $completion) {
        return null;
    }
    
    private static final com.smartkrishi.presentation.dashboard.AiRecommendations computeAiRecommendations(java.util.List<com.smartkrishi.presentation.model.SensorNode> nodes, com.smartkrishi.presentation.dashboard.WeatherInfo weather) {
        return null;
    }
}