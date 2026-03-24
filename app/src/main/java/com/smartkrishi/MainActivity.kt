package com.smartkrishi

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.smartkrishi.presentation.model.Farm
import com.smartkrishi.presentation.navigation.NavGraph
import com.smartkrishi.presentation.theme.SmartKrishiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var database: FirebaseDatabase? = null
    private var reference: DatabaseReference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        getMoistures()

        setContent {
            SmartKrishiTheme {

                val navController = rememberNavController()

                val drawerState = remember { DrawerState(initialValue = DrawerValue.Closed) }

                val selectedFarmState = remember { mutableStateOf<Farm?>(null) }

                NavGraph(
                    navController = navController,
                    drawerState = drawerState,
                    selectedFarmState = selectedFarmState
                )
            }
        }
    }

    private fun getMoistures(){
        database = FirebaseDatabase.getInstance()
        getNode1Moisture();
        getNode2Moisture();
    }

    private fun getNode1Moisture(){
        reference =
            database?.getReference("dashboard")?.child("farmer_SK001")?.child("farm_alpha01")
                ?.child("live")?.child("nodes")?.child("zone_1")?.child("moisture");
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val moisture = snapshot.getValue(Int::class.java)
                if (moisture != null && moisture <63 ) {
                    val title = if (moisture < 63) {
                        "🚨 Moisture Alert"
                    } else {
                        "⚠️ Moisture Alert"
                    }

                    val content = "Moisture level drop to  ${moisture}% in zone 1 .Please take corrective action.\n "

                    notificationSetting(title, content);
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        })
    }

    private fun getNode2Moisture() {
        reference =
            database?.getReference("dashboard")?.child("farmer_SK001")?.child("farm_alpha01")
                ?.child("live")?.child("nodes")?.child("zone_2")?.child("moisture");
        reference?.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val moisture = snapshot.getValue(Int::class.java)
                if (moisture != null && moisture < 25) {
                    val title = if (moisture < 25) {
                        "🚨 Critical Moisture Level Alert"
                    } else {
                        "⚠️ Moisture Alert"
                    }

                    val content = if (moisture < 25) {
                        "Moisture level is ${moisture}% zone 2. Crop health may be at risk. Please water immediately."
                    } else {
                        "Soil moisture in Zone 1 is ${moisture}%. It’s a good time to start irrigation."
                    }
                    notificationSetting(title, content);
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_LONG).show();
            }
        })
    }

    fun notificationSetting(title: String?, content: String?) {
        val CHANNEL_ID = "New Invitation"
        val CHANNEL_NAME = "New Invitation Notifications"
        val REQUEST_CODE = 100
        val drawable =
            ResourcesCompat.getDrawable(getResources(), R.drawable.app_logo11, null)
        val largeIcon = (drawable as BitmapDrawable).getBitmap()

        val intent: Intent = Intent(this@MainActivity, MainActivity::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("MOISTURE", title)
        val pendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val newProjectNotification: Notification?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            newProjectNotification =
                Notification.Builder(this).setSmallIcon(R.drawable.app_logo11) // app icon
                    .setLargeIcon(largeIcon) // notification icon
                    .setContentTitle(title)
                    .setContentText(content)
                    .setChannelId(CHANNEL_ID)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).build()
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
                )
            )
        } else {
            newProjectNotification =
                Notification.Builder(this).setSmallIcon(R.drawable.app_logo11) // app icon
                    .setLargeIcon(largeIcon)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL).build()
        }
        manager.notify(System.currentTimeMillis().toInt(), newProjectNotification)
    }

    fun BigPictureStyleNotification(
        title: String?,
        content: String?
    ): Notification.BigPictureStyle {
        val drawable =
            ResourcesCompat.getDrawable(getResources(), R.drawable.app_logo11, null)
        val MyBigPicture = (drawable as BitmapDrawable).getBitmap()
        val bigPictureStyle = Notification.BigPictureStyle().bigPicture(MyBigPicture)
            .setBigContentTitle(title)
            .setSummaryText(content)
        return bigPictureStyle
    }
}
