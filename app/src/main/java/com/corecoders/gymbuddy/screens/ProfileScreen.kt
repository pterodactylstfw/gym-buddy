package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.ui.theme.OLEDBlack
import com.corecoders.gymbuddy.ui.theme.SlateSurface
import com.corecoders.gymbuddy.ui.theme.SurgicalRed
import com.corecoders.gymbuddy.ui.theme.SurgicalDivider
import com.corecoders.gymbuddy.viewmodel.ProfileViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSignOutSuccess: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Workouts", "Routines", "Progress")
    val user = Firebase.auth.currentUser
    val totalWorkouts by viewModel.totalWorkouts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.displayName?.lowercase() ?: "profile", style = MaterialTheme.typography.titleMedium, color = Color.White) },
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(Icons.Outlined.IosShare, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(onClick = { /* TODO: Edit */ }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = Color.White)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                    IconButton(onClick = {
                        Firebase.auth.signOut()
                        onSignOutSuccess()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Sign Out", tint = SurgicalRed)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = OLEDBlack)
            )
        },
        containerColor = OLEDBlack
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- HEADER PROFIL ---
            ProfileHeader(user?.displayName ?: "Raul Constantin", totalWorkouts)

            Spacer(modifier = Modifier.height(24.dp))

            // --- TAB-URI ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = OLEDBlack,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = SurgicalRed,
                            height = 2.dp
                        )
                    }
                },
                divider = { HorizontalDivider(color = SurgicalDivider, thickness = 0.5.dp) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedTabIndex == index) SurgicalRed else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CONTINUT TAB-URI ---
            when (selectedTabIndex) {
                0 -> WorkoutsTabContent()
                1 -> RoutinesTabContent()
                2 -> ProgressTabContent()
            }
        }
    }
}

@Composable
fun ProfileHeader(displayName: String, workoutCount: Int) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Poza de profil (Placeholder)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(SlateSurface)
                    .border(0.5.dp, SurgicalDivider, CircleShape)
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileStat(count = workoutCount.toString(), label = "Workouts")
                    ProfileStat(count = "0", label = "Followers")
                    ProfileStat(count = "0", label = "Following")
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Performance Athlete • gym-buddy",
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ProfileStat(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, style = MaterialTheme.typography.titleMedium, color = Color.White, fontSize = 16.sp)
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
}

@Composable
fun WorkoutsTabContent() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Your completed workouts will appear here.", color = Color.Gray, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun RoutinesTabContent() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Create routines to speed up your sessions.", color = Color.Gray, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun ProgressTabContent() {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Track your 1RM and volume progress over time.", color = Color.Gray, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}
