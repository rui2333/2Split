package com.split.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.split.android.screens.HomeScreen
import com.split.android.screens.ReviewItemsScreen
import com.split.android.screens.SplitMethodScreen
import com.split.android.ui.theme.SplitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SplitTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(navController)
                        }
                        composable("review_items/{splitId}") { backStackEntry ->
                            val splitId = backStackEntry.arguments?.getString("splitId") ?: ""
                            ReviewItemsScreen(splitId, navController)
                        }
                        composable("split_method/{splitId}") { backStackEntry ->
                            val splitId = backStackEntry.arguments?.getString("splitId") ?: ""
                            SplitMethodScreen(splitId, navController)
                        }
                    }
                }
            }
        }
    }
}
