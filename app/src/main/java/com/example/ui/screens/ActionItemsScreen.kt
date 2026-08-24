package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.example.data.model.ActionItemStatus
import com.example.data.model.MeetingPriority
import com.example.ui.components.ActionItemCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError
import com.example.ui.viewmodel.MeetingViewModel

@Composable
fun ActionItemsScreen(
    viewModel: MeetingViewModel
) {
    val actionItems by viewModel.allActionItems.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf<ActionItemStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newAssignee by remember { mutableStateOf("Rahul Sharma") }
    var newDueDate by remember { mutableStateOf("Aug 30, 2026") }

    val filteredItems = actionItems.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.assigneeName.contains(searchQuery, ignoreCase = true) ||
                item.meetingTitle.contains(searchQuery, ignoreCase = true)
        val matchesStatus = selectedStatusFilter == null || item.status == selectedStatusFilter
        matchesSearch && matchesStatus
    }

    val completedCount = actionItems.count { it.status == ActionItemStatus.COMPLETED }
    val overdueCount = actionItems.count { it.status == ActionItemStatus.OVERDUE }
    val inProgressCount = actionItems.count { it.status == ActionItemStatus.IN_PROGRESS }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("action_items_screen_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Action Items & Deliverables",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Track owners, deadlines & completion statuses across meetings",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = !showAddDialog },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.testTag("create_new_action_item_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Task", fontSize = 12.sp)
                }
            }
        }

        // Quick Stats Summary Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Active Tasks", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$inProgressCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$completedCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                    }
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Overdue", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$overdueCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RoseError)
                    }
                }
            }
        }

        // Expandable Quick Add Task Box
        if (showAddDialog) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Add New Action Item", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("Task description...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newAssignee,
                                onValueChange = { newAssignee = it },
                                label = { Text("Assignee") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = newDueDate,
                                onValueChange = { newDueDate = it },
                                label = { Text("Due Date") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        viewModel.createActionItem(
                                            meetingId = "meet_custom",
                                            meetingTitle = "Ad-hoc Meeting",
                                            title = newTitle,
                                            assigneeName = newAssignee,
                                            dueDate = newDueDate,
                                            priority = MeetingPriority.HIGH
                                        )
                                        newTitle = ""
                                        showAddDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                            ) {
                                Text("Save Action Item")
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search tasks, assignees, meetings...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
        }

        // Status Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val isSelected = selectedStatusFilter == null
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedStatusFilter = null }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("All (${actionItems.size})", fontSize = 12.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                items(ActionItemStatus.values()) { status ->
                    val isSelected = selectedStatusFilter == status
                    val count = actionItems.count { it.status == status }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedStatusFilter = status }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("${status.name.replace("_", " ")} ($count)", fontSize = 12.sp, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Action Items List
        if (filteredItems.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No action items found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(filteredItems) { item ->
                ActionItemCard(
                    item = item,
                    onStatusToggle = { newStatus ->
                        viewModel.updateActionItemStatus(item.id, newStatus)
                    }
                )
            }
        }
    }
}
