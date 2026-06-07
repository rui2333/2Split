package com.split.android.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.split.shared.models.Item
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

data class PersonSplit(
    val name: String,
    val initials: String,
    val color: Color,
    val amount: Double = 0.0
)

@Composable
fun ItemSplitScreen(splitId: String, navController: NavController) {
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

    var currentItemIndex by remember { mutableStateOf(0) }
    var itemSplits by remember { mutableStateOf(items.associate { it.id to (it.total() / 2.0) }) }

    val currentItem = items.getOrNull(currentItemIndex)
    val dragOffsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val currentAmount = itemSplits[currentItem?.id] ?: 0.0
    val otherAmount = (currentItem?.total() ?: 0.0) - currentAmount

    fun navigateItem(offset: Float) {
        scope.launch {
            val direction = when {
                offset < -50 -> 1  // Swiped left, go to next
                offset > 50 -> -1  // Swiped right, go to previous
                else -> 0
            }

            if (direction != 0) {
                val newIndex = (currentItemIndex + direction).coerceIn(0, items.size - 1)
                if (newIndex != currentItemIndex) {
                    currentItemIndex = newIndex
                }
            }

            dragOffsetX.animateTo(0f, animationSpec = spring(stiffness = Spring.StiffnessHigh))
        }
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
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("< back", color = Color.Black)
            }
            Text("${currentItemIndex + 1} of ${items.size}", fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.weight(1f))
        }

        // Person indicators at top
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            people.forEach { person ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(person.color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        person.initials,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }

        // Swipeable item card
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset { IntOffset(dragOffsetX.value.roundToInt(), 0) }
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(2.dp, Color.LightGray, RoundedCornerShape(16.dp))
                .padding(24.dp)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                dragOffsetX.snapTo(dragOffsetX.value + dragAmount.x)
                            }
                        },
                        onDragEnd = {
                            navigateItem(dragOffsetX.value)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (currentItem != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        "${currentItem.quantity.toInt()}x",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        currentItem.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "$${String.format("%.2f", currentItem.total())}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Split controls
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Hold the card → split it", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (currentItem != null) {
                people.forEachIndexed { index, person ->
                    SplitBar(
                        person = person,
                        amount = if (index == 0) currentAmount else otherAmount,
                        total = currentItem.total(),
                        onAmountChange = { newAmount ->
                            itemSplits = itemSplits.toMutableMap().apply {
                                put(currentItem.id, newAmount.coerceIn(0.0, currentItem.total()))
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick split buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickSplitButton(
                        "50 / 50",
                        onClick = {
                            itemSplits = itemSplits.toMutableMap().apply {
                                put(currentItem.id, currentItem.total() / 2.0)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickSplitButton(
                        "60 / 40",
                        onClick = {
                            itemSplits = itemSplits.toMutableMap().apply {
                                put(currentItem.id, currentItem.total() * 0.6)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    QuickSplitButton(
                        "drag ↔",
                        onClick = {},
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SplitBar(
    person: PersonSplit,
    amount: Double,
    total: Double,
    onAmountChange: (Double) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(person.color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                person.initials,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .padding(horizontal = 12.dp)
                .background(Color.LightGray, RoundedCornerShape(6.dp))
        ) {
            val percentage = if (total > 0) (amount / total).toFloat() else 0f
            Box(
                modifier = Modifier
                    .fillMaxWidth(percentage)
                    .height(12.dp)
                    .background(person.color, RoundedCornerShape(6.dp))
            )
        }

        Text(
            "$${String.format("%.2f", amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(60.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun QuickSplitButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.height(36.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF0F0F0)
        )
    ) {
        Text(label, color = Color.Black, fontSize = 12.sp)
    }
}
