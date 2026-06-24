package com.corecoders.gymbuddy.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.corecoders.gymbuddy.utils.getAvatarModel
import com.corecoders.gymbuddy.viewmodel.SocialViewModel
import android.text.format.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    postId: String,
    socialViewModel: SocialViewModel,
    onDismissRequest: () -> Unit
) {
    val comments by socialViewModel.comments.collectAsState()
    var commentText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(postId) {
        socialViewModel.loadComments(postId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
                .imePadding()
        ) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)

            // Comments List
            Box(modifier = Modifier.weight(1f)) {
                if (comments.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No comments yet. Be the first to comment!", color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(comments) { comment ->
                            Row(verticalAlignment = Alignment.Top) {
                                val avatarModel = remember(comment.userAvatar) { getAvatarModel(comment.userAvatar) }
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
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
                                        Text(
                                            text = comment.username.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = comment.username,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        val timeStr = DateUtils.getRelativeTimeSpanString(
                                            comment.timestamp,
                                            System.currentTimeMillis(),
                                            DateUtils.MINUTE_IN_MILLIS
                                        ).toString()
                                        Text(
                                            text = timeStr,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = comment.text,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Write a comment...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (commentText.isNotBlank()) {
                                socialViewModel.addComment(postId, commentText.trim())
                                commentText = ""
                                keyboardController?.hide()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            socialViewModel.addComment(postId, commentText.trim())
                            commentText = ""
                            keyboardController?.hide()
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    socialViewModel: SocialViewModel,
    onDismissRequest: () -> Unit
) {
    val notifications by socialViewModel.notifications.collectAsState()

    LaunchedEffect(Unit) {
        socialViewModel.loadNotifications()
        socialViewModel.markNotificationsAsRead()
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), thickness = 0.5.dp)

            Box(modifier = Modifier.weight(1f)) {
                if (notifications.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No notifications yet.", color = MaterialTheme.colorScheme.secondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(notifications) { notif ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val avatarModel = remember(notif.senderAvatar) { getAvatarModel(notif.senderAvatar) }
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
                                            contentDescription = "Sender Avatar",
                                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = notif.senderUsername.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val text = when (notif.type) {
                                        "CLAP" -> "clapped your workout: \"${notif.postWorkoutName}\""
                                        "FOLLOW" -> "started following you"
                                        "COMMENT" -> "commented on your workout: \"${notif.postWorkoutName}\""
                                        else -> "interacted with you"
                                    }
                                    
                                    val formattedText = remember(notif.senderUsername, text) {
                                        androidx.compose.ui.text.buildAnnotatedString {
                                            pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                                            append(notif.senderUsername)
                                            pop()
                                            append(" ")
                                            append(text)
                                        }
                                    }
                                    
                                    Text(
                                        text = formattedText,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val timeStr = DateUtils.getRelativeTimeSpanString(
                                        notif.timestamp,
                                        System.currentTimeMillis(),
                                        DateUtils.MINUTE_IN_MILLIS
                                    ).toString()
                                    Text(
                                        text = timeStr,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                
                                // Red dot for unread
                                if (!notif.read) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.error)
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
