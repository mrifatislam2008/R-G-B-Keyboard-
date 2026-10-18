package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundHapticManager
import com.example.data.KeyboardPreferences
import com.example.rgb.RgbAnimationEngine
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class MainTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    HOME("Home", Icons.Default.Home),
    APPEARANCE("RGB & Style", Icons.Default.Palette),
    SOUND("Sound", Icons.Default.VolumeUp),
    ABOUT("Settings", Icons.Default.Tune)
}

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    private lateinit var preferences: KeyboardPreferences
    private lateinit var soundHapticManager: SoundHapticManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = KeyboardPreferences.getInstance(this)
        soundHapticManager = SoundHapticManager(this)

        setContent {
            MyApplicationTheme {
                val settings by preferences.settings.collectAsState()
                var currentTab by remember { mutableStateOf(MainTab.HOME) }
                val context = LocalContext.current
                val isEnabled = remember(currentTab) { checkIsImeEnabled(context) }
                val isSelected = remember(currentTab) { checkIsImeSelected(context) }

                // Animated RGB Phase for Top Bar Glow
                val phaseState = RgbAnimationEngine.rememberAnimatedRgbPhase(settings)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        Column {
                            TopAppBar(
                                title = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0D1117))
                                                .border(1.5.dp, Color(settings.rgbAccentColorHex), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Image(
                                                painter = painterResource(id = R.drawable.rimu_logo),
                                                contentDescription = "Rimu Keyboard Logo",
                                                modifier = Modifier.size(30.dp).clip(CircleShape)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = "Rimu Keyboard",
                                                fontSize = 17.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "RGB Gaming IME",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(settings.rgbAccentColorHex)
                                            )
                                        }
                                    }
                                },
                                actions = {
                                    // Status Badge
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isEnabled && isSelected) {
                                            Color(0xFF00E676).copy(alpha = 0.2f)
                                        } else {
                                            Color(0xFFFF9100).copy(alpha = 0.2f)
                                        },
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isEnabled && isSelected) Color(0xFF00E676) else Color(0xFFFF9100)
                                        ),
                                        modifier = Modifier.padding(end = 12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isEnabled && isSelected) Color(0xFF00E676) else Color(0xFFFF9100))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isEnabled && isSelected) "ACTIVE" else "SETUP NEEDED",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isEnabled && isSelected) Color(0xFF00E676) else Color(0xFFFF9100)
                                            )
                                        }
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            // Subtle RGB illuminated line under top bar
                            if (settings.rainbowBorder) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(2.dp)
                                        .background(RgbAnimationEngine.getBorderBrush(settings, phaseState.value))
                                )
                            }
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            MainTab.values().forEach { tab ->
                                val isSelectedTab = currentTab == tab
                                NavigationBarItem(
                                    selected = isSelectedTab,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelectedTab) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.Black,
                                        indicatorColor = Color(settings.rgbAccentColorHex),
                                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                        selectedTextColor = Color(settings.rgbAccentColorHex),
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f)
                                    ),
                                    modifier = Modifier.testTag("tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                            when (tab) {
                                MainTab.HOME -> HomeScreen(
                                    settings = settings,
                                    preferences = preferences,
                                    soundHapticManager = soundHapticManager,
                                    onNavigateToRgb = { currentTab = MainTab.APPEARANCE }
                                )
                                MainTab.APPEARANCE -> RgbAppearanceScreen(
                                    settings = settings,
                                    preferences = preferences,
                                    soundHapticManager = soundHapticManager
                                )
                                MainTab.SOUND -> SoundHapticsScreen(
                                    settings = settings,
                                    preferences = preferences,
                                    soundHapticManager = soundHapticManager
                                )
                                MainTab.ABOUT -> InputAboutScreen(
                                    settings = settings,
                                    preferences = preferences
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundHapticManager.release()
    }
}
