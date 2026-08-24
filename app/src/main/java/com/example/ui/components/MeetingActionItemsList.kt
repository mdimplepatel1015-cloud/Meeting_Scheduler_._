package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionItem
import com.example.data.model.ActionItemStatus
import com.example.data.model.MeetingPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary

@Composable
fun MeetingActionItemsList(
    meetingId: String,
    actionItems: List<ActionItem>,
    onToggleStatus: (String, ActionItemStatus) -> Unit,
    onAddActionItem: (String, String, String, MeetingPriority) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAddingNew by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newAssignee by remember { mutableStateOf("") }
    var newDueDate by remember { mutableStateOf("Aug 30, 2026") }
    var selectedFilter by remember { mutableStateOf("All") }

    val completedCount = actionItems.count { it.status == ActionItemStatus.COMPLETED }
    val progress = if (actionItems.isNotEmpty()) completedCount.toFloat() / actionItems.size else 0f

    val filteredItems = when (selectedFilter) {
        "Pending" -> actionItems.filter { it.status != ActionItemStatus.COMPLETED }
        "Completed" -> actionItems.filter { it.status == ActionItemStatus.COMPLETED }
        else -> actionItems
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meeting_action_items_section")
    ) {
        // Header & Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(IndigoPrimary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AssignmentTurnedIn,
                                contentDescription = null,
                                tint = IndigoLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Meeting Action Items",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "$completedCount of ${actionItems.size} tasks resolved",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { isAddingNew = !isAddingNew },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.testTag("btn_toggle_add_action_item")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isAddingNew) "Close" else "Add Item", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = EmeraldSuccess,
                    trackColor = MaterialTheme.colorScheme.surface
                )
            }
        }

        // Inline Add Action Item Form
        AnimatedVisibility(visible = isAddingNew) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "New Meeting Task",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        placeholder = { Text("What needs to be done?", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_action_title"),
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IndigoPrimary),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newAssignee,
                            onValueChange = { newAssignee = it },
                            placeholder = { Text("Assignee (e.g. Rahul S.)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("input_new_action_assignee"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newDueDate,
                            onValueChange = { newDueDate = it },
                            placeholder = { Text("Due Date", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).testTag("input_new_action_due_date"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (newTitle.isNotBlank()) {
                                onAddActionItem(
                                    newTitle.trim(),
                                    if (newAssignee.isBlank()) "Unassigned" else newAssignee.trim(),
                                    newDueDate.trim(),
                                    MeetingPriority.HIGH
                                )
                                newTitle = ""
                                newAssignee = ""
                                isAddingNew = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_save_action_item"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Save Action Item", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("All", "Pending", "Completed").forEach { filter ->
                val isSelected = selectedFilter == filter
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedFilter = filter }
                ) {
                    Text(
                        text = filter,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Action Items List
        if (filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No action items matching filter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredItems.forEach { item ->
                    ActionItemCard(
                        item = item,
                        onStatusToggle = { newStatus ->
                            onToggleStatus(item.id, newStatus)
                        }
                    )
                }
            }
        }
    }
}
