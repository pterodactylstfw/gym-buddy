package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.viewmodel.WorkoutViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.util.Calendar

@Composable
fun DashboardScreen(
    navController: NavController,
    viewModel: WorkoutViewModel,
    onStartWorkout: () -> Unit
) {
    val auth = Firebase.auth
    val scrollState = rememberScrollState()

    // Obținem ora curentă pentru salutul dinamic
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (hour) {
        in 5..11 -> "Good morning,"
        in 12..17 -> "Good afternoon,"
        else -> "Good evening,"
    }

    // Luăm numele din Firebase (sau lăsăm un fallback dacă nu e setat)
    val userName = auth.currentUser?.displayName ?: "Raul"

    Scaffold(
        containerColor = Color(0xFFF5F5F5) // Fundalul gri deschis din poză
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        color = Color.Gray,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = userName,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Clopoțel cu badge roșu (mock)
                    Box(modifier = Modifier.clickable { /* TODO: Notificări */ }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notificări", modifier = Modifier.size(28.dp))
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color.Red)
                                .align(Alignment.TopEnd)
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Chat",
                        modifier = Modifier.size(28.dp).clickable { /* TODO: Chat */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- SPLIT SECTION ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFFF28F79), Color(0xFFE8B28B))
                            )
                        )
                        .clickable { /* TODO: Deschide Split */ }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Untitled Split", fontSize = 14.sp, color = Color.Gray)
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
            Spacer(modifier = Modifier.height(24.dp))

            // --- READY TO TRAIN CARD ---
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().clickable { onStartWorkout() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "READY TO TRAIN",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Untitled Workout",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )
                    }

                    // Butonul de Play
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Start Workout",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- QUICK ACTIONS ---
            Text(
                text = "Quick Actions",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Grid cu 2 coloane
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickActionCard(
                        icon = Icons.Default.FitnessCenter,
                        title = "Routines",
                        subtitle = "Your splits",
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ }
                    )
                    QuickActionCard(
                        icon = Icons.Default.Schedule,
                        title = "History",
                        subtitle = "Past sessions",
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO: Navighează spre noul ecran History */ }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickActionCard(
                        icon = Icons.Default.LocalFireDepartment,
                        title = "Nutrition",
                        subtitle = "Calories & macros",
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ }
                    )
                    QuickActionCard(
                        icon = Icons.Default.CameraAlt,
                        title = "Physique",
                        subtitle = "Track progress",
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ }
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    QuickActionCard(
                        icon = Icons.Default.GridView,
                        title = "Exercises",
                        subtitle = "Full library",
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate("catalog") } // Am legat deja asta!
                    )
                    QuickActionCard(
                        icon = Icons.Default.Download,
                        title = "Import",
                        subtitle = "From other apps",
                        modifier = Modifier.weight(1f),
                        onClick = { /* TODO */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp)) // Spațiu pentru bottom bar
        }
    }
}

@Composable
fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(28.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Text(text = subtitle, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}