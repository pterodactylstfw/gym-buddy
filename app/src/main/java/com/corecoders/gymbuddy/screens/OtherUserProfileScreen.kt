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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.corecoders.gymbuddy.data.dto.UserProfile
import com.corecoders.gymbuddy.viewmodel.OtherUserProfileViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileScreen(
    navController: NavController,
    userId: String,
    viewModel: OtherUserProfileViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val followers by viewModel.followers.collectAsState()
    val following by viewModel.following.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()

    var showSocialList by remember { mutableStateOf(false) }
    var selectedSocialTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(userId) {
        viewModel.loadUser(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userProfile?.username ?: "Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (isLoading || userProfile == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val user = userProfile!!
            val amIFollowingThem = currentUserProfile?.friends?.contains(userId) == true
            val areTheyFollowingMe = user.friends.contains(currentUserProfile?.userId)
            val isFriend = amIFollowingThem && areTheyFollowingMe

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Profile Header
                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.avatarUri.isNotEmpty() && File(user.avatarUri).exists()) {
                                AsyncImage(
                                    model = File(user.avatarUri),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Text(
                                    text = user.name.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                ProfileStat(count = posts.size.toString(), label = "Workouts")
                                ProfileStat(
                                    count = followers.size.toString(), 
                                    label = "Followers",
                                    modifier = Modifier.clickable { 
                                        selectedSocialTabIndex = 0
                                        showSocialList = true 
                                    }
                                )
                                ProfileStat(
                                    count = following.size.toString(), 
                                    label = "Following",
                                    modifier = Modifier.clickable { 
                                        selectedSocialTabIndex = 1
                                        showSocialList = true 
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Follow Button
                    if (isFriend) {
                        OutlinedButton(
                            onClick = { viewModel.toggleFollowStatus(userId, true) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Friend")
                        }
                    } else if (amIFollowingThem) {
                        OutlinedButton(
                            onClick = { viewModel.toggleFollowStatus(userId, true) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Following")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.toggleFollowStatus(userId, false) },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            val text = if (areTheyFollowingMe) "Follow Back" else "Follow"
                            Text(text, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.5.dp)
                
                // Posts List
                if (posts.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No public workouts yet.", color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(posts) { post ->
                            SocialActivityCard(post, onClapClick = { viewModel.toggleClap(post.postId) })
                        }
                    }
                }
            }

            // Social Bottom Sheet
            if (showSocialList) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { showSocialList = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface
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
                        val emptyMessage = if (selectedSocialTabIndex == 0) "No followers yet." else "Not following anyone yet."

                        if (currentList.isEmpty()) {
                            Text(
                                text = emptyMessage,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        } else {
                            LazyColumn {
                                items(currentList) { listUser ->
                                    ListItem(
                                        headlineContent = { Text(listUser.name, fontWeight = FontWeight.Bold) },
                                        supportingContent = { Text("@${listUser.username}") },
                                        leadingContent = {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = if (listUser.name.isNotEmpty()) listUser.name.take(1).uppercase() else "?",
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        },
                                        modifier = Modifier.clickable {
                                            showSocialList = false
                                            navController.navigate("other_user_profile/${listUser.userId}")
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

