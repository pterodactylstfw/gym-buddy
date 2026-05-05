package com.corecoders.gymbuddy.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Stats,
        BottomNavItem.Catalog,
        BottomNavItem.Store,
        BottomNavItem.Profile
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = Color(0xFFF5F5F5), // Același gri deschis ca fundalul aplicației din poză
        tonalElevation = 0.dp, // Fără umbră urâtă
        modifier = Modifier.height(80.dp) // O facem puțin mai înaltă pentru aspect premium
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.route,
                        modifier = Modifier.size(28.dp) // Iconițe un pic mai mari
                    )
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                // ASCUNDEM TEXTUL complet pentru a replica poza
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black, // Iconița selectată e neagră
                    unselectedIconColor = Color.Gray, // Celelalte sunt gri
                    indicatorColor = Color.Transparent // Scoatem "bula" urâtă din spatele iconiței din Material 3
                )
            )
        }
    }
}