package com.toprunner.imagestory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState

// BottomNavItem data class to hold navigation item properties
data class BottomNavItemData(
    val route: String,
    val iconResId: Int,
    val label: String
)

@Composable
fun BottomNavItem(
    navController: NavController,
    route: String,
    iconResId: Int,
    label: String,
    tint: Color = Color(0xFFAA8866)
) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry.value?.destination

    // Check if this item is selected (current route matches this item's route)
    val isSelected = currentDestination?.hierarchy?.any { it.route == route } == true

    val textColor = if (isSelected) Color.Black else Color(0xFF666666)
    val iconTint = if (isSelected) Color.Black else tint
    val fontWeight = if(isSelected) FontWeight.Bold else FontWeight.Normal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .clickable {
                navController.navigate(route) {
                    // Pop up to the start destination of the graph to
                    // avoid building up a large stack of destinations
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    // Avoid multiple copies of the same destination when
                    // re-selecting the same item
                    launchSingleTop = true
                    // Restore state when re-selecting a previously selected item
                    restoreState = true
                }
            }
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = iconTint
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = textColor,
            textAlign = TextAlign.Center,
            fontWeight = fontWeight
        )
    }
}