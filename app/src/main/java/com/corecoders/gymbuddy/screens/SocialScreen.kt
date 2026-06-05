package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

data class SocialPost(
    val userName: String,
    val userAvatar: String,
    val workoutName: String,
    val timeAgo: String,
    val stats: String,
    val exercises: List<String>,
    val claps: Int,
    val comments: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(navController: NavController) {
    val dummyPosts = listOf(
        SocialPost("Alex Ionescu", "A", "Push Day - Hypertrophy", "2h ago", "12.4k kg • 75 min", listOf("Bench Press", "Incline DB Press", "Tricep Pushdowns"), 24, 5),
        SocialPost("Maria Popa", "M", "Morning Yoga & Core", "5h ago", "0 kg • 45 min", listOf("Sun Salutation", "Plank Circuit", "Deadbug"), 15, 2),
        SocialPost("Robert Dan", "R", "Heavy Squat Session", "Yesterday", "18.2k kg • 90 min", listOf("Back Squat", "Leg Press", "Leg Extensions"), 42, 12),
        SocialPost("Elena Radu", "E", "Quick HIIT Blast", "Yesterday", "500 kg • 20 min", listOf("Burpees", "Mountain Climbers", "Kettlebell Swings"), 18, 3)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Feed", fontWeight = FontWeight.ExtraBold) },
                actions = {
                    IconButton(onClick = { /* TODO */ }) {
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dummyPosts) { post ->
                SocialActivityCard(post)
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun SocialActivityCard(post: SocialPost) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: User Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(post.userAvatar, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(post.userName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
                    Text(post.timeAgo, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
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

            // Interaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                InteractionButton(Icons.Default.ThumbUp, "${post.claps}")
                Spacer(modifier = Modifier.width(24.dp))
                InteractionButton(Icons.Outlined.ChatBubbleOutline, "${post.comments}")
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* TODO */ }) {
                    Icon(
                        imageVector = Icons.Outlined.Share, 
                        contentDescription = "Share", 
                        tint = MaterialTheme.colorScheme.secondary, 
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InteractionButton(icon: ImageVector, count: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon, 
            contentDescription = null, 
            modifier = Modifier.size(20.dp), 
            tint = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(count, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
    }
}
