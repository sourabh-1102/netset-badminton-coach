package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CourtGreenDark
import com.example.ui.theme.TealContainer
import com.example.ui.theme.TextSlate
import com.example.ui.viewmodel.NavDestination

@Composable
fun BadmintonBottomNavBar(
    currentNav: NavDestination,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor = TealContainer,
        selectedIconColor = CourtGreenDark,
        selectedTextColor = CourtGreenDark,
        unselectedIconColor = TextSlate,
        unselectedTextColor = TextSlate
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        NavigationBarItem(
            selected = currentNav == NavDestination.HOME,
            onClick = { onNavigate(NavDestination.HOME) },
            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
            label = { Text("Home", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentNav == NavDestination.STUDENTS || currentNav == NavDestination.STUDENT_DETAIL,
            onClick = { onNavigate(NavDestination.STUDENTS) },
            icon = { Icon(Icons.Default.People, contentDescription = "Students") },
            label = { Text("Students", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentNav == NavDestination.DRILL_MODE,
            onClick = { onNavigate(NavDestination.DRILL_MODE) },
            icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Drills") },
            label = { Text("Drills", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentNav == NavDestination.MATCH_LOGS,
            onClick = { onNavigate(NavDestination.MATCH_LOGS) },
            icon = { Icon(Icons.Default.Sports, contentDescription = "Matches") },
            label = { Text("Matches", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            colors = itemColors
        )
        NavigationBarItem(
            selected = currentNav == NavDestination.SETTINGS,
            onClick = { onNavigate(NavDestination.SETTINGS) },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Coach") },
            label = { Text("Coach", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
            colors = itemColors
        )
    }
}
