package com.corecoders.gymbuddy.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    // dashboard
    object Dashboard : BottomNavItem("dashboard", Icons.Filled.Home, Icons.Outlined.Home)

    // statistici
    object Stats : BottomNavItem("stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)

    // catalog exercitii
    object Catalog : BottomNavItem("catalog", Icons.Filled.Search, Icons.Outlined.Search)

    // feed
    object Social : BottomNavItem("social", Icons.Filled.Groups, Icons.Outlined.Groups)

    // profil
    object Profile : BottomNavItem("profile", Icons.Filled.Person, Icons.Outlined.Person)
}
