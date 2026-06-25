package com.corecoders.gymbuddy.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.corecoders.gymbuddy.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentAge by viewModel.age.collectAsState()
    val currentGender by viewModel.gender.collectAsState()
    val currentWeight by viewModel.weight.collectAsState()
    val currentTargetWeight by viewModel.targetWeight.collectAsState()
    val currentHeight by viewModel.height.collectAsState()
    val currentGoal by viewModel.fitnessGoal.collectAsState()
    val currentExperience by viewModel.experienceLevel.collectAsState()
    val currentFrequency by viewModel.trainingFrequency.collectAsState()
    val isMetric by viewModel.unitSystemMetric.collectAsState()

    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var targetWeight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var experience by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }

    var genderExpanded by remember { mutableStateOf(false) }
    var goalExpanded by remember { mutableStateOf(false) }
    var experienceExpanded by remember { mutableStateOf(false) }

    val genders = listOf("Male", "Female", "Other")
    val goals = listOf("Lose Weight", "Build Muscle", "Keep Fit", "Increase Endurance")
    val experiences = listOf("Beginner", "Intermediate", "Advanced")

    LaunchedEffect(currentAge, currentGender, currentWeight, currentTargetWeight, currentHeight, currentGoal, currentExperience, currentFrequency, isMetric) {
        if (age.isEmpty()) age = if (currentAge > 0) currentAge.toString() else ""
        if (gender.isEmpty()) gender = currentGender
        if (weight.isEmpty()) {
            weight = if (currentWeight > 0f) {
                val displayW = if (isMetric) currentWeight else currentWeight * 2.20462f
                if (displayW % 1 == 0f) displayW.toInt().toString() else "%.1f".format(displayW)
            } else ""
        }
        if (targetWeight.isEmpty()) {
            targetWeight = if (currentTargetWeight > 0f) {
                val displayW = if (isMetric) currentTargetWeight else currentTargetWeight * 2.20462f
                if (displayW % 1 == 0f) displayW.toInt().toString() else "%.1f".format(displayW)
            } else ""
        }
        if (height.isEmpty()) height = if (currentHeight > 0) currentHeight.toString() else ""
        if (goal.isEmpty()) goal = currentGoal
        if (experience.isEmpty()) experience = currentExperience
        if (frequency.isEmpty()) frequency = if (currentFrequency > 0) currentFrequency.toString() else ""
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Age
            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter { c -> c.isDigit() } },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Gender Dropdown
            ExposedDropdownMenuBox(
                expanded = genderExpanded,
                onExpandedChange = { genderExpanded = it }
            ) {
                OutlinedTextField(
                    value = gender,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Gender") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = genderExpanded,
                    onDismissRequest = { genderExpanded = false }
                ) {
                    genders.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = {
                                gender = g
                                genderExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Current weight
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(if (isMetric) "Current Weight (kg)" else "Current Weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Target weight
            OutlinedTextField(
                value = targetWeight,
                onValueChange = { targetWeight = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(if (isMetric) "Target Weight (kg)" else "Target Weight (lbs)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Height
            OutlinedTextField(
                value = height,
                onValueChange = { height = it.filter { c -> c.isDigit() } },
                label = { Text("Height (cm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fitness goal dropdown
            ExposedDropdownMenuBox(
                expanded = goalExpanded,
                onExpandedChange = { goalExpanded = it }
            ) {
                OutlinedTextField(
                    value = goal,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Fitness Goal") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = goalExpanded,
                    onDismissRequest = { goalExpanded = false }
                ) {
                    goals.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = {
                                goal = g
                                goalExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Experience level dropdown
            ExposedDropdownMenuBox(
                expanded = experienceExpanded,
                onExpandedChange = { experienceExpanded = it }
            ) {
                OutlinedTextField(
                    value = experience,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Experience Level") },
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = experienceExpanded,
                    onDismissRequest = { experienceExpanded = false }
                ) {
                    experiences.forEach { exp ->
                        DropdownMenuItem(
                            text = { Text(exp) },
                            onClick = {
                                experience = exp
                                experienceExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Workout frequency
            OutlinedTextField(
                value = frequency,
                onValueChange = { frequency = it.filter { c -> c.isDigit() } },
                label = { Text("Workouts per Week") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Save changes button
            Button(
                onClick = {
                    val parsedAge = age.toIntOrNull() ?: 0
                    val parsedWeight = weight.toFloatOrNull() ?: 0f
                    val parsedTargetWeight = targetWeight.toFloatOrNull() ?: 0f
                    val parsedHeight = height.toIntOrNull() ?: 0
                    val parsedFrequency = frequency.toIntOrNull() ?: 0

                    val saveWeight = if (isMetric) parsedWeight else parsedWeight / 2.20462f
                    val saveTargetWeight = if (isMetric) parsedTargetWeight else parsedTargetWeight / 2.20462f

                    if (parsedAge <= 0 || saveWeight <= 0f || parsedHeight <= 0) {
                        Toast.makeText(context, "Please fill in valid physical metrics (Age, Weight, Height).", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.saveProfileData(
                            age = parsedAge,
                            gender = gender,
                            weight = saveWeight,
                            targetWeight = saveTargetWeight,
                            height = parsedHeight,
                            fitnessGoal = goal,
                            experienceLevel = experience,
                            trainingFrequency = parsedFrequency,
                            onSuccess = {
                                Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                                navController.popBackStack()
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("SAVE CHANGES", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
