package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.EcoViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

enum class BottomTab {
    HOME, TRACK, INSIGHTS, COMMUNITY, PROFILE
}

enum class AuxiliaryScreen {
    NONE, COACH, SIMULATOR, LEARNING
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                EcoAppMain()
            }
        }
    }
}

@Composable
fun EcoAppMain() {
    val viewModel: EcoViewModel = viewModel()
    val profileState by viewModel.userProfile.collectAsState()

    val profile = profileState

    if (profile == null) {
        // Loading state
        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    } else if (!profile.isOnboarded) {
        // Run Onboarding Flow
        OnboardingScreen(
            viewModel = viewModel,
            onComplete = {
                viewModel.refreshHomeRecommendation()
            }
        )
    } else {
        // Main App Experience
        var currentTab by remember { mutableStateOf(BottomTab.HOME) }
        var activeAuxiliaryScreen by remember { mutableStateOf(AuxiliaryScreen.NONE) }

        // Transition views cleanly
        AnimatedContent(
            targetState = activeAuxiliaryScreen,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
            label = "ScreenTransition"
        ) { auxScreen ->
            when (auxScreen) {
                AuxiliaryScreen.COACH -> {
                    AICoachScreen(
                        viewModel = viewModel,
                        onBack = { activeAuxiliaryScreen = AuxiliaryScreen.NONE }
                    )
                }
                AuxiliaryScreen.SIMULATOR -> {
                    ScenarioSimulatorScreen(
                        viewModel = viewModel,
                        onBack = { activeAuxiliaryScreen = AuxiliaryScreen.NONE }
                    )
                }
                AuxiliaryScreen.LEARNING -> {
                    LearningCenterScreen(
                        onBack = { activeAuxiliaryScreen = AuxiliaryScreen.NONE }
                    )
                }
                AuxiliaryScreen.NONE -> {
                    // Regular bottom-scaffold tabs connected
                    Scaffold(
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .navigationBarsPadding()
                                    .testTag("app_bottom_bar")
                            ) {
                                NavigationBarItem(
                                    selected = currentTab == BottomTab.HOME,
                                    onClick = { currentTab = BottomTab.HOME },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == BottomTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("nav_home")
                                )

                                NavigationBarItem(
                                    selected = currentTab == BottomTab.TRACK,
                                    onClick = { currentTab = BottomTab.TRACK },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == BottomTab.TRACK) Icons.Filled.AddCircle else Icons.Outlined.AddCircle,
                                            contentDescription = "Track"
                                        )
                                    },
                                    label = { Text("Track", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("nav_track")
                                )

                                NavigationBarItem(
                                    selected = currentTab == BottomTab.INSIGHTS,
                                    onClick = { currentTab = BottomTab.INSIGHTS },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == BottomTab.INSIGHTS) Icons.Filled.Insights else Icons.Outlined.Insights,
                                            contentDescription = "Insights"
                                        )
                                    },
                                    label = { Text("Insights", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("nav_insights")
                                )

                                NavigationBarItem(
                                    selected = currentTab == BottomTab.COMMUNITY,
                                    onClick = { currentTab = BottomTab.COMMUNITY },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == BottomTab.COMMUNITY) Icons.Filled.People else Icons.Outlined.People,
                                            contentDescription = "Community"
                                        )
                                    },
                                    label = { Text("Community", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("nav_community")
                                )

                                NavigationBarItem(
                                    selected = currentTab == BottomTab.PROFILE,
                                    onClick = { currentTab = BottomTab.PROFILE },
                                    icon = {
                                        Icon(
                                            imageVector = if (currentTab == BottomTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                                            contentDescription = "Profile"
                                        )
                                    },
                                    label = { Text("Profile", style = MaterialTheme.typography.labelSmall) },
                                    modifier = Modifier.testTag("nav_profile")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentTab) {
                                BottomTab.HOME -> {
                                    HomeDashboardScreen(
                                        viewModel = viewModel,
                                        navigateToCoach = { activeAuxiliaryScreen = AuxiliaryScreen.COACH },
                                        navigateToSimulator = { activeAuxiliaryScreen = AuxiliaryScreen.SIMULATOR },
                                        navigateToLearning = { activeAuxiliaryScreen = AuxiliaryScreen.LEARNING }
                                    )
                                }
                                BottomTab.TRACK -> {
                                    TrackScreen(viewModel = viewModel)
                                }
                                BottomTab.INSIGHTS -> {
                                    InsightsScreen(viewModel = viewModel)
                                }
                                BottomTab.COMMUNITY -> {
                                    CommunityScreen(viewModel = viewModel)
                                }
                                BottomTab.PROFILE -> {
                                    ProfileScreen(
                                        viewModel = viewModel,
                                        navigateToLearning = { activeAuxiliaryScreen = AuxiliaryScreen.LEARNING },
                                        onReset = {
                                            currentTab = BottomTab.HOME
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
