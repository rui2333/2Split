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
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController
import com.split.android.utils.BackButton
import com.split.shared.models.Item
import com.split.shared.models.Person

@Composable
fun SettlementScreen(
    splitId: String,
    navController: NavController,
    items: List<Item>,
    people: List<Person>,
    itemAssignments: MutableState<Map<String, Int>>
) {
    // Create PersonSplit list with colors for display
    val personSplits = remember {
        val colors = listOf(Color(0xFFE8845E), Color(0xFF4DB8A8))
        people.mapIndexed { index, person ->
            PersonSplit(
                name = person.name,
                initials = person.name.firstOrNull()?.toString() ?: "?",
                color = colors.getOrNull(index) ?: Color.Gray
            )
        }
    }

    // Calculate totals
    val personTotals = people.indices.map { index ->
        items.filter { item ->
            itemAssignments.value[item.id] == index
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
            items(personSplits.indices.toList()) { index ->
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
                                    .background(personSplits[index].color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    personSplits[index].initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(personSplits[index].name, fontWeight = FontWeight.SemiBold)
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