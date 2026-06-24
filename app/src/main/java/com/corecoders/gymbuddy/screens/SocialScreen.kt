package com.corecoders.gymbuddy.screens

import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.corecoders.gymbuddy.data.dto.SocialPostDto
import com.corecoders.gymbuddy.viewmodel.SocialViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.corecoders.gymbuddy.utils.getAvatarModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    navController: NavController,
    viewModel: SocialViewModel = viewModel()
) {
    val feedPosts by viewModel.feedPosts.collectAsState()
    var activeCommentsPostId by remember { mutableStateOf<String?>(null) }
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFeed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feed", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = { navController.navigate("find_friends") }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add Friends", tint = MaterialTheme.colorScheme.primary)
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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (feedPosts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No posts yet.", color = MaterialTheme.colorScheme.secondary)
                    TextButton(onClick = { navController.navigate("find_friends") }) {
                        Text("Find friends to see their workouts!")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(feedPosts) { post ->
                    SocialActivityCard(
                        post = post, 
                        onClapClick = { viewModel.toggleClap(post.postId) },
                        onUserClick = { navController.navigate("other_user_profile/${post.userId}") },
                        onCommentsClick = { activeCommentsPostId = post.postId }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    activeCommentsPostId?.let { postId ->
        CommentsBottomSheet(
            postId = postId,
            socialViewModel = viewModel,
            onDismissRequest = { activeCommentsPostId = null }
        )
    }
}

@Composable
fun SocialActivityCard(
    post: SocialPostDto,
    onClapClick: () -> Unit,
    onUserClick: () -> Unit = {},
    onCommentsClick: () -> Unit = {}
) {
    val timeAgo = DateUtils.getRelativeTimeSpanString(post.timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS).toString()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    val hasClapped = post.clappedBy.contains(currentUserId)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: User Info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onUserClick() }
            ) {
                val avatarModel = remember(post.userAvatar) { getAvatarModel(post.userAvatar) }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarModel != null) {
                        AsyncImage(
                            model = avatarModel,
                            contentDescription = "User Avatar",
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val textToShow = post.userAvatar.takeIf { it.length == 1 } ?: post.username.take(1).uppercase()
                        Text(textToShow, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(post.username, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(timeAgo, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workout Content
            Text(
                text = post.workoutName,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.stats,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Exercises snippet
            if (post.exercises.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    post.exercises.forEach { exercise ->
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(
                                imageVector = Icons.Default.Check, 
                                contentDescription = null, 
                                modifier = Modifier.size(14.dp), 
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(exercise, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Interaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InteractionButton(
                    icon = Icons.Default.ThumbUp, 
                    count = "${post.claps}", 
                    isActive = hasClapped,
                    onClick = onClapClick
                )
                Spacer(modifier = Modifier.width(24.dp))
                InteractionButton(
                    icon = Icons.Outlined.ChatBubbleOutline, 
                    count = "${post.comments}",
                    isActive = false,
                    onClick = onCommentsClick
                )
            }
        }
    }
}

@Composable
fun InteractionButton(icon: ImageVector, count: String, isActive: Boolean, onClick: () -> Unit) {
    val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            modifier = Modifier.size(20.dp), 
            tint = color
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(count, fontSize = 14.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
