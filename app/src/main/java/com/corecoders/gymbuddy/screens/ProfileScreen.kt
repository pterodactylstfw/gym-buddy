package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.data.Routine
import com.corecoders.gymbuddy.data.Workout
import com.corecoders.gymbuddy.viewmodel.ProfileViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onSignOutSuccess: () -> Unit
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val tabs = listOf("Workouts", "Routines", "Progress")
    val user = Firebase.auth.currentUser
    
    val workouts by viewModel.allWorkouts.collectAsState()
    val routines by viewModel.allRoutines.collectAsState()
    val totalVolume by viewModel.totalVolume.collectAsState()
    val totalWorkouts by viewModel.totalWorkouts.collectAsState()

    val age by viewModel.age.collectAsState()
    val weight by viewModel.weight.collectAsState()
    val experience by viewModel.experienceLevel.collectAsState()
    val profilePictureUri by viewModel.profilePictureUri.collectAsState()

    val followers by viewModel.followers.collectAsState()
    val following by viewModel.following.collectAsState()
    var showFollowersList by remember { mutableStateOf(false) }
    var selectedSocialTabIndex by remember { mutableIntStateOf(0) }

    var showPhotoMenu by remember { mutableStateOf(false) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.saveProfilePicture(context, uri)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.displayName?.lowercase() ?: "profile", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground) },
                actions = {
                    IconButton(onClick = { /* TODO: Share */ }) {
                        Icon(Icons.Outlined.IosShare, contentDescription = "Share", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { navController.navigate("edit_profile") }) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // --- HEADER PROFIL ---
            val experienceStr = experience.takeIf { it.isNotEmpty() } ?: "Athlete"
            val ageStr = if (age > 0) "$age yrs" else ""
            val weightStr = if (weight > 0f) "${weight.toInt()} kg" else ""
            val subtitleParts = listOf(experienceStr, ageStr, weightStr).filter { it.isNotEmpty() }
            val subtitle = if (subtitleParts.isNotEmpty()) subtitleParts.joinToString(" • ") else "Welcome to GymBuddy!"

            ProfileHeader(
                displayName = user?.displayName ?: "User", 
                subtitle = subtitle, 
                workoutCount = totalWorkouts, 
                routineCount = routines.size,
                followersCount = followers.size,
                profilePictureUri = profilePictureUri,
                onPhotoClick = { showPhotoMenu = true },
                onFollowersClick = { showFollowersList = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- TAB-URI ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 2.dp
                        )
                    }
                },
                divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                            )
                        }
                    )
                }
            }

            // --- CONTINUT TAB-URI ---
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTabIndex) {
                    0 -> WorkoutsTabContent(workouts, navController)
                    1 -> RoutinesTabContent(routines, navController)
                    2 -> ProgressTabContent(totalVolume, totalWorkouts)
                }
            }
        }



        if (showFollowersList) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showFollowersList = false },
                containerColor = MaterialTheme.colorScheme.surface,
                sheetState = sheetState
            ) {
                Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(bottom = 32.dp)) {
                    TabRow(
                        selectedTabIndex = selectedSocialTabIndex,
                        containerColor = MaterialTheme.colorScheme.surface,
                        indicator = { tabPositions ->
                            if (selectedSocialTabIndex < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[selectedSocialTabIndex]),
                                    color = MaterialTheme.colorScheme.primary,
                                    height = 2.dp
                                )
                            }
                        },
                        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp) }
                    ) {
                        Tab(
                            selected = selectedSocialTabIndex == 0,
                            onClick = { selectedSocialTabIndex = 0 },
                            text = { Text("Followers", color = if (selectedSocialTabIndex == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary) }
                        )
                        Tab(
                            selected = selectedSocialTabIndex == 1,
                            onClick = { selectedSocialTabIndex = 1 },
                            text = { Text("Following", color = if (selectedSocialTabIndex == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary) }
                        )
                    }
                    
                    val currentList = if (selectedSocialTabIndex == 0) followers else following
                    val emptyMessage = if (selectedSocialTabIndex == 0) "No followers yet." else "You are not following anyone yet."

                    if (currentList.isEmpty()) {
                        Text(
                            text = emptyMessage,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        LazyColumn {
                            items(currentList) { user ->
                                ListItem(
                                    headlineContent = { Text(user.name, fontWeight = FontWeight.Bold) },
                                    supportingContent = { Text("@${user.username}") },
                                    leadingContent = {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = if (user.name.isNotEmpty()) user.name.take(1).uppercase() else "?",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showPhotoMenu) {
            ModalBottomSheet(
                onDismissRequest = { showPhotoMenu = false },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                ) {
                    Text(
                        text = "Profile Photo",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                    ListItem(
                        headlineContent = { Text("Choose from Library") },
                        modifier = Modifier.clickable {
                            showPhotoMenu = false
                            photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
                    if (profilePictureUri.isNotEmpty() && File(profilePictureUri).exists()) {
                        ListItem(
                            headlineContent = { Text("Remove Photo", color = MaterialTheme.colorScheme.error) },
                            modifier = Modifier.clickable {
                                showPhotoMenu = false
                                viewModel.removeProfilePicture()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(
    displayName: String, 
    subtitle: String, 
    workoutCount: Int, 
    routineCount: Int,
    followersCount: Int,
    profilePictureUri: String,
    onPhotoClick: () -> Unit,
    onFollowersClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Poza de profil
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    .clickable { onPhotoClick() },
                contentAlignment = Alignment.Center
            ) {
                if (profilePictureUri.isNotEmpty() && File(profilePictureUri).exists()) {
                    AsyncImage(
                        model = File(profilePictureUri),
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = displayName.take(1).uppercase(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ProfileStat(count = workoutCount.toString(), label = "Workouts")
                    ProfileStat(count = routineCount.toString(), label = "Routines")
                    ProfileStat(
                        count = followersCount.toString(), 
                        label = "Followers",
                        modifier = Modifier.clickable { onFollowersClick() }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 13.sp
        )
    }
}

@Composable
fun ProfileStat(count: String, label: String, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(text = count, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        Text(text = label, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
}

@Composable
fun WorkoutsTabContent(workouts: List<Workout>, navController: NavController) {
    if (workouts.isEmpty()) {
        EmptyTabPlaceholder(Icons.Default.History, "No workouts logged yet.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(workouts.take(10)) { workout ->
                WorkoutHistoryItem(
                    workout = workout,
                    onClick = { navController.navigate("workout_details/${workout.id}") }
                )
            }
            if (workouts.size > 10) {
                item {
                    TextButton(
                        onClick = { navController.navigate("history") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("See All History", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun RoutinesTabContent(routines: List<Routine>, navController: NavController) {
    if (routines.isEmpty()) {
        EmptyTabPlaceholder(Icons.Default.FitnessCenter, "No routines created yet.")
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(routines) { routine ->
                RoutineItem(
                    routine = routine,
                    onDelete = { /* Nu ștergem de aici pentru siguranță */ },
                    onClick = { navController.navigate("routine_details/${routine.id}") }
                )
            }
        }
    }
}

@Composable
fun ProgressTabContent(totalVolume: Double, totalWorkouts: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Lifetime Stats", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("TOTAL VOLUME", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${"%,.0f".format(totalVolume)} kg",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("lifted across all time", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("CONSISTENCY", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$totalWorkouts sessions",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("logged in GymBuddy", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun EmptyTabPlaceholder(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(16.dp))
        Text(text, color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}
