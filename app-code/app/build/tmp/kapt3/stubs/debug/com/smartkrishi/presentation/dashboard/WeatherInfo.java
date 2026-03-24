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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b \n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B\u007f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0005\u0012\b\b\u0002\u0010\b\u001a\u00020\u0005\u0012\b\b\u0002\u0010\t\u001a\u00020\n\u0012\b\b\u0002\u0010\u000b\u001a\u00020\u0005\u0012\b\b\u0002\u0010\f\u001a\u00020\u0005\u0012\b\b\u0002\u0010\r\u001a\u00020\u0005\u0012\f\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f\u0012\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u000f\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\u0002\u0010\u0015J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\u000f\u0010(\u001a\b\u0012\u0004\u0012\u00020\u00100\u000fH\u00c6\u0003J\u000f\u0010)\u001a\b\u0012\u0004\u0012\u00020\u00120\u000fH\u00c6\u0003J\t\u0010*\u001a\u00020\u0014H\u00c6\u0003J\t\u0010+\u001a\u00020\u0005H\u00c6\u0003J\t\u0010,\u001a\u00020\u0005H\u00c6\u0003J\t\u0010-\u001a\u00020\u0005H\u00c6\u0003J\t\u0010.\u001a\u00020\u0005H\u00c6\u0003J\t\u0010/\u001a\u00020\nH\u00c6\u0003J\t\u00100\u001a\u00020\u0005H\u00c6\u0003J\t\u00101\u001a\u00020\u0005H\u00c6\u0003J\t\u00102\u001a\u00020\u0005H\u00c6\u0003J\u008d\u0001\u00103\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\b\b\u0002\u0010\b\u001a\u00020\u00052\b\b\u0002\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\u00052\b\b\u0002\u0010\f\u001a\u00020\u00052\b\b\u0002\u0010\r\u001a\u00020\u00052\u000e\b\u0002\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f2\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u000f2\b\b\u0002\u0010\u0013\u001a\u00020\u0014H\u00c6\u0001J\u0013\u00104\u001a\u0002052\b\u00106\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00107\u001a\u00020\u0005H\u00d6\u0001J\t\u00108\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017R\u0017\u0010\u000e\u001a\b\u0012\u0004\u0012\u00020\u00100\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0011\u0010\f\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\r\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001bR\u0017\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u000f\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0019R\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001bR\u0011\u0010\u0013\u001a\u00020\u0014\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010 R\u0011\u0010\b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001bR\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u001bR\u0011\u0010\u000b\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001bR\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010%R\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u001b\u00a8\u00069"}, d2 = {"Lcom/smartkrishi/presentation/dashboard/WeatherInfo;", "", "condition", "", "tempC", "", "humidity", "windKph", "pressure", "visibility", "", "uvIndex", "dewPoint", "feelsLike", "daily", "", "Lcom/smartkrishi/presentation/dashboard/DailyForecast;", "hourly", "Lcom/smartkrishi/presentation/dashboard/HourlyForecast;", "lastUpdated", "", "(Ljava/lang/String;IIIIDIIILjava/util/List;Ljava/util/List;J)V", "getCondition", "()Ljava/lang/String;", "getDaily", "()Ljava/util/List;", "getDewPoint", "()I", "getFeelsLike", "getHourly", "getHumidity", "getLastUpdated", "()J", "getPressure", "getTempC", "getUvIndex", "getVisibility", "()D", "getWindKph", "component1", "component10", "component11", "component12", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class WeatherInfo {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String condition = null;
    private final int tempC = 0;
    private final int humidity = 0;
    private final int windKph = 0;
    private final int pressure = 0;
    private final double visibility = 0.0;
    private final int uvIndex = 0;
    private final int dewPoint = 0;
    private final int feelsLike = 0;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.smartkrishi.presentation.dashboard.DailyForecast> daily = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.smartkrishi.presentation.dashboard.HourlyForecast> hourly = null;
    private final long lastUpdated = 0L;
    
    public WeatherInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String condition, int tempC, int humidity, int windKph, int pressure, double visibility, int uvIndex, int dewPoint, int feelsLike, @org.jetbrains.annotations.NotNull()
    java.util.List<com.smartkrishi.presentation.dashboard.DailyForecast> daily, @org.jetbrains.annotations.NotNull()
    java.util.List<com.smartkrishi.presentation.dashboard.HourlyForecast> hourly, long lastUpdated) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCondition() {
        return null;
    }
    
    public final int getTempC() {
        return 0;
    }
    
    public final int getHumidity() {
        return 0;
    }
    
    public final int getWindKph() {
        return 0;
    }
    
    public final int getPressure() {
        return 0;
    }
    
    public final double getVisibility() {
        return 0.0;
    }
    
    public final int getUvIndex() {
        return 0;
    }
    
    public final int getDewPoint() {
        return 0;
    }
    
    public final int getFeelsLike() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.smartkrishi.presentation.dashboard.DailyForecast> getDaily() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.smartkrishi.presentation.dashboard.HourlyForecast> getHourly() {
        return null;
    }
    
    public final long getLastUpdated() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.smartkrishi.presentation.dashboard.DailyForecast> component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.smartkrishi.presentation.dashboard.HourlyForecast> component11() {
        return null;
    }
    
    public final long component12() {
        return 0L;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int component3() {
        return 0;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int component5() {
        return 0;
    }
    
    public final double component6() {
        return 0.0;
    }
    
    public final int component7() {
        return 0;
    }
    
    public final int component8() {
        return 0;
    }
    
    public final int component9() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.smartkrishi.presentation.dashboard.WeatherInfo copy(@org.jetbrains.annotations.NotNull()
    java.lang.String condition, int tempC, int humidity, int windKph, int pressure, double visibility, int uvIndex, int dewPoint, int feelsLike, @org.jetbrains.annotations.NotNull()
    java.util.List<com.smartkrishi.presentation.dashboard.DailyForecast> daily, @org.jetbrains.annotations.NotNull()
    java.util.List<com.smartkrishi.presentation.dashboard.HourlyForecast> hourly, long lastUpdated) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}