package com.split.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.split.android.screens.HomeScreen
import com.split.android.screens.ItemSplitScreen
import com.split.android.screens.ReviewItemsScreen
import com.split.android.screens.SettlementScreen
import com.split.android.screens.SplitMethodScreen
import com.split.android.ui.theme.SplitTheme
import com.split.shared.models.Item
import com.split.shared.models.Person

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplitTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    // Shared state for app
                    val currentItems = remember { mutableStateListOf<Item>() }
                    val currentPeople = remember { mutableStateListOf<Person>() }
                    val currentSplitId = remember { mutableStateOf("") }
                    val itemAssignments = remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(navController, currentItems, currentSplitId, currentPeople)
                        }
                        composable("review_items/{splitId}") { backStackEntry ->
                            val splitId = backStackEntry.arguments?.getString("splitId") ?: ""
                            ReviewItemsScreen(splitId, navController, currentItems)
                        }
                        composable("split_method/{splitId}") { backStackEntry ->
                            val splitId = backStackEntry.arguments?.getString("splitId") ?: ""
                            SplitMethodScreen(splitId, navController, currentItems, currentPeople)
                        }
                        composable("item_split/{splitId}") { backStackEntry ->
                            val splitId = backStackEntry.arguments?.getString("splitId") ?: ""
                            ItemSplitScreen(splitId, navController, currentItems, currentPeople, itemAssignments)
                        }
                        composable("settlement/{splitId}") { backStackEntry ->
                            val splitId = backStackEntry.arguments?.getString("splitId") ?: ""
                            SettlementScreen(splitId, navController, currentItems, currentPeople, itemAssignments)
                        }
                    }
                }
            }
        }
    }
}
