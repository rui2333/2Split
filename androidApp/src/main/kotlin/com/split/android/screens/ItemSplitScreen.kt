package com.split.android.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.MutableState
import androidx.navigation.NavController
import com.split.android.utils.BackButton
import com.split.shared.models.Item
import com.split.shared.models.Person
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
fun ItemSplitScreen(
    splitId: String,
    navController: NavController,
    items: List<Item>,
    people: List<Person>,
    itemAssignments: MutableState<Map<String, Int>>
) {

    var currentItemIndex by remember { mutableStateOf(0) }

    // Convert Person to PersonSplit with colors
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

    val currentItem = items.getOrNull(currentItemIndex)
    val dragOffsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val assignedPersonIndex = itemAssignments.value[currentItem?.id] ?: 0
    val currentAmount = if (assignedPersonIndex == 0) currentItem?.total() ?: 0.0 else 0.0
    val otherAmount = if (assignedPersonIndex == 1) currentItem?.total() ?: 0.0 else 0.0

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
            BackButton(onClick = { navController.popBackStack() })
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
            personSplits.forEachIndexed { index, person ->
                val isSelected = assignedPersonIndex == index
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(person.color, CircleShape)
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = Color.Black,
                            shape = CircleShape
                        )
                        .clickable {
                            if (currentItem != null) {
                                itemAssignments.value = itemAssignments.value.toMutableMap().apply {
                                    put(currentItem.id, index)
                                }
                            }
                        },
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
            Text("Tap above → assign to person", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))

            if (currentItem != null) {
                personSplits.forEachIndexed { index, person ->
                    SplitBar(
                        person = person,
                        amount = if (index == 0) currentAmount else otherAmount,
                        total = currentItem.total()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("settlement/$splitId")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Finish & Calculate →", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun SplitBar(
    person: PersonSplit,
    amount: Double,
    total: Double,
    onAmountChange: (Double) -> Unit = {}
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

