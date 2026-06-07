package com.split.android.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

enum class SplitOption {
    EVEN, BY_ITEM, CUSTOM
}

@Composable
fun SplitMethodScreen(splitId: String, navController: NavController) {
    var selectedOption by remember { mutableStateOf(SplitOption.EVEN) }

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
            Button(
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("< back", color = Color.Black)
            }
            Text("How to split?", fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.weight(1f))
        }

        // Title
        Text(
            text = "Pick a way to split",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "$77.40 total - between 2 people",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Split options
        Column(modifier = Modifier.weight(1f)) {
            SplitOptionItem(
                title = "Split evenly",
                description = "½ (50/50 - skip the items)",
                details = "You\n$38.70\n\nSam\n$38.70",
                isSelected = selectedOption == SplitOption.EVEN,
                onClick = { selectedOption = SplitOption.EVEN }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SplitOptionItem(
                title = "Split by item",
                description = "assign each line to a person",
                details = null,
                isSelected = selectedOption == SplitOption.BY_ITEM,
                onClick = { selectedOption = SplitOption.BY_ITEM }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SplitOptionItem(
                title = "Custom share",
                description = "drag the split, e.g. 60/40",
                details = null,
                isSelected = selectedOption == SplitOption.CUSTOM,
                onClick = { selectedOption = SplitOption.CUSTOM }
            )
        }

        Button(
            onClick = {
                when (selectedOption) {
                    SplitOption.BY_ITEM -> {
                        navController.navigate("item_split/$splitId")
                    }
                    else -> {
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Continue →", fontSize = 16.sp)
        }
    }
}

@Composable
fun SplitOptionItem(
    title: String,
    description: String,
    details: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = isSelected,
                        onClick = onClick,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = description,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (details != null && isSelected) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = details,
                            fontSize = 12.sp,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
