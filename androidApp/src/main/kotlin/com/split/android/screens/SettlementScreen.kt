package com.split.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.split.android.utils.BackButton
import com.split.shared.models.Item
import java.util.UUID

@Composable
fun SettlementScreen(splitId: String, navController: NavController) {
    val items = remember {
        listOf(
            Item(UUID.randomUUID().toString(), "", "Margherita pizza", 1.0, 18.00),
            Item(UUID.randomUUID().toString(), "", "Caesar salad", 2.0, 12.50),
            Item(UUID.randomUUID().toString(), "", "Garlic bread", 1.0, 7.00),
            Item(UUID.randomUUID().toString(), "", "Sparkling water", 2.0, 6.00),
            Item(UUID.randomUUID().toString(), "", "Tiramisu", 1.0, 9.50),
            Item(UUID.randomUUID().toString(), "", "House red (glass)", 2.0, 11.00)
        )
    }

    val people = remember {
        listOf(
            PersonSplit("You", "Y", Color(0xFFE8845E)),
            PersonSplit("Sam", "S", Color(0xFF4DB8A8))
        )
    }

    // Dummy itemAssignments - in real app this would be passed in
    val itemAssignments by remember {
        mutableStateOf(mapOf(
            items[0].id to 0,  // You
            items[1].id to 1,  // Sam
            items[2].id to 0,  // You
            items[3].id to 1,  // Sam
            items[4].id to 0,  // You
            items[5].id to 1   // Sam
        ))
    }

    // Calculate totals
    val personTotals = people.indices.map { index ->
        items.filter { item ->
            itemAssignments[item.id] == index
        }.sumOf { it.total() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BackButton(onClick = { navController.popBackStack() })
            Text("Settlement", fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.weight(1f))
        }

        // Person summary
        Text("Payment Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(people.indices.toList()) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(people[index].color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    people[index].initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(people[index].name, fontWeight = FontWeight.SemiBold)
                        }
                        Text(
                            "$${String.format("%.2f", personTotals[index])}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Button(
            onClick = {
                navController.popBackStack(route = "home", inclusive = false)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Done", fontSize = 16.sp)
        }
    }
}