package com.corecoders.gymbuddy.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadProfileData()
    }

    val pagerState = rememberPagerState(pageCount = { 7 })
    val coroutineScope = rememberCoroutineScope()

    val age by viewModel.age.collectAsState()
    val gender by viewModel.gender.collectAsState()
    val weight by viewModel.weight.collectAsState()
    val targetWeight by viewModel.targetWeight.collectAsState()
    val height by viewModel.height.collectAsState()
    val goal by viewModel.fitnessGoal.collectAsState()
    val experience by viewModel.experienceLevel.collectAsState()
    val frequency by viewModel.trainingFrequency.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            BottomNavigationBar(
                currentPage = pagerState.currentPage,
                totalPages = 7,
                onNext = {
                    if (pagerState.currentPage < 6) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        viewModel.saveOnboardingData { onFinish() }
                    }
                },
                onBack = {
                    if (pagerState.currentPage > 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    }
                },
                onSkip = {
                    viewModel.saveOnboardingData { onFinish() }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> AgePage(age = age?.toString() ?: "", onAgeChange = {
                        it.toIntOrNull()?.let { v -> viewModel.updateAge(v) }
                    })
                    1 -> GenderPage(gender = gender, onGenderChange = { viewModel.updateGender(it) })
                    2 -> WeightPage(
                        weight = weight?.toString() ?: "",
                        targetWeight = targetWeight?.toString() ?: "",
                        onWeightChange = { it.toFloatOrNull()?.let { v -> viewModel.updateWeight(v) } },
                        onTargetWeightChange = { it.toFloatOrNull()?.let { v -> viewModel.updateTargetWeight(v) } }
                    )
                    3 -> HeightPage(height = height?.toString() ?: "", onHeightChange = {
                        it.toIntOrNull()?.let { v -> viewModel.updateHeight(v) }
                    })
                    4 -> GoalPage(goal = goal, onGoalChange = { viewModel.updateFitnessGoal(it) })
                    5 -> ExperiencePage(experience = experience, onExperienceChange = { viewModel.updateExperienceLevel(it) })
                    6 -> FrequencyPage(frequency = frequency?.toString() ?: "", onFrequencyChange = {
                        it.toIntOrNull()?.let { v -> viewModel.updateTrainingFrequency(v) }
                    })
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    currentPage: Int,
    totalPages: Int,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(totalPages) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (currentPage == index) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(if (currentPage == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onSkip) {
                Text("Skip", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
            }
            
            Row {
                AnimatedVisibility(visible = currentPage > 0, enter = fadeIn(), exit = fadeOut()) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Button(
                    onClick = onNext,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    if (currentPage == totalPages - 1) {
                        Text("Finish", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.Check, contentDescription = null)
                    } else {
                        Text("Next", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageContainer(title: String, subtitle: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        content()
    }
}

@Composable
fun AgePage(age: String, onAgeChange: (String) -> Unit) {
    OnboardingPageContainer(
        title = "What's your age?",
        subtitle = "This helps us calculate your calorie burn and tailor routines."
    ) {
        OutlinedTextField(
            value = age,
            onValueChange = onAgeChange,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.width(120.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
    }
}

@Composable
fun GenderPage(gender: String, onGenderChange: (String) -> Unit) {
    val options = listOf("Male", "Female", "Other")
    OnboardingPageContainer(
        title = "What's your gender?",
        subtitle = "Used for precise body metric calculations."
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            options.forEach { option ->
                SelectionButton(
                    text = option,
                    isSelected = gender == option,
                    onClick = { onGenderChange(option) }
                )
            }
        }
    }
}

@Composable
fun WeightPage(weight: String, targetWeight: String, onWeightChange: (String) -> Unit, onTargetWeightChange: (String) -> Unit) {
    OnboardingPageContainer(
        title = "Weight details",
        subtitle = "Track your current and target weight."
    ) {
        OutlinedTextField(
            value = weight,
            onValueChange = onWeightChange,
            label = { Text("Current Weight (kg)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = targetWeight,
            onValueChange = onTargetWeightChange,
            label = { Text("Target Weight (kg) - Optional") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun HeightPage(height: String, onHeightChange: (String) -> Unit) {
    OnboardingPageContainer(
        title = "What's your height?",
        subtitle = "Important for BMI and other fitness metrics."
    ) {
        OutlinedTextField(
            value = height,
            onValueChange = onHeightChange,
            label = { Text("Height (cm)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.width(180.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}

@Composable
fun GoalPage(goal: String, onGoalChange: (String) -> Unit) {
    val options = listOf("Lose Weight", "Build Muscle", "Keep Fit", "Increase Endurance")
    OnboardingPageContainer(
        title = "What's your main goal?",
        subtitle = "We'll help you focus your workouts on what matters."
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            options.forEach { option ->
                SelectionButton(
                    text = option,
                    isSelected = goal == option,
                    onClick = { onGoalChange(option) }
                )
            }
        }
    }
}

@Composable
fun ExperiencePage(experience: String, onExperienceChange: (String) -> Unit) {
    val options = listOf("Beginner", "Intermediate", "Advanced")
    OnboardingPageContainer(
        title = "Experience Level",
        subtitle = "How familiar are you with working out?"
    ) {
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            options.forEach { option ->
                SelectionButton(
                    text = option,
                    isSelected = experience == option,
                    onClick = { onExperienceChange(option) }
                )
            }
        }
    }
}

@Composable
fun FrequencyPage(frequency: String, onFrequencyChange: (String) -> Unit) {
    OnboardingPageContainer(
        title = "Workout Frequency",
        subtitle = "How many times a week do you plan to train?"
    ) {
        OutlinedTextField(
            value = frequency,
            onValueChange = onFrequencyChange,
            label = { Text("Workouts / Week") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontSize = 24.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.width(180.dp),
            shape = RoundedCornerShape(16.dp),
            singleLine = true
        )
    }
}

@Composable
fun SelectionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(1.dp) // Border thickness essentially via background
            .background(backgroundColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 18.sp
        )
    }
}
