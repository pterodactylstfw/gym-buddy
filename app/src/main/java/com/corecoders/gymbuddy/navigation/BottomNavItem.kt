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
    // 1. Acasă (Dashboard)
    object Dashboard : BottomNavItem("dashboard", Icons.Filled.Home, Icons.Outlined.Home)

    // 2. Statistici/Istoric (Tab-ul cu bare din poză)
    object Stats : BottomNavItem("stats", Icons.Filled.BarChart, Icons.Outlined.BarChart)

    // 3. Catalog Exerciții (Lupa din poză)
    object Catalog : BottomNavItem("catalog", Icons.Filled.Search, Icons.Outlined.Search)

    // 4. Magazin / Premium (Plasa de cumpărături din poză)
    object Store : BottomNavItem("store", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag)

    // 5. Profil (Folosim iconița standard momentan, dar o putem schimba cu poza userului)
    object Profile : BottomNavItem("profile", Icons.Filled.Person, Icons.Outlined.Person)
}