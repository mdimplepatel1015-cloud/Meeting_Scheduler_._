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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.ui.components.DecisionCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.MeetingViewModel

@Composable
fun DecisionLogScreen(
    viewModel: MeetingViewModel
) {
    val decisions by viewModel.allDecisions.collectAsState()
    var selectedProjectFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    var newTitle by remember { mutableStateOf("") }
    var newOwner by remember { mutableStateOf("Alex Morgan") }
    var newProject by remember { mutableStateOf("Core Platform v3") }
    var newContext by remember { mutableStateOf("") }

    val projects = listOf("All", "Core Platform v3", "Streaming Engine", "Infrastructure", "Marketing")

    val filteredDecisions = decisions.filter { d ->
        val matchesSearch = searchQuery.isBlank() ||
                d.title.contains(searchQuery, ignoreCase = true) ||
                d.ownerName.contains(searchQuery, ignoreCase = true) ||
                d.relatedProject.contains(searchQuery, ignoreCase = true)
        val matchesProject = selectedProjectFilter == "All" || d.relatedProject.equals(selectedProjectFilter, ignoreCase = true)
        matchesSearch && matchesProject
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("decision_log_screen_root"),
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
                        text = "Decision Registry & Corporate Memory",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Audit trail of strategic agreements, approvals & architecture commitments",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddDialog = !showAddDialog },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                    modifier = Modifier.testTag("log_new_decision_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Decision", fontSize = 12.sp)
                }
            }
        }

        // Expandable Quick Add Decision Form
        if (showAddDialog) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("Log Executive Decision", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("Decision title / outcome...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = newOwner,
                                onValueChange = { newOwner = it },
                                label = { Text("Decision Owner") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            OutlinedTextField(
                                value = newProject,
                                onValueChange = { newProject = it },
                                label = { Text("Project / Stream") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = newContext,
                            onValueChange = { newContext = it },
                            placeholder = { Text("Rationale & context notes...") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        viewModel.createDecision(
                                            meetingId = "meet_custom",
                                            meetingTitle = "Strategic Review",
                                            title = newTitle,
                                            ownerName = newOwner,
                                            relatedProject = newProject,
                                            context = newContext
                                        )
                                        newTitle = ""
                                        newContext = ""
                                        showAddDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = VioletAccent)
                            ) {
                                Text("Save Decision")
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
                placeholder = { Text("Search decisions, owners, projects...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
        }

        // Project Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(projects) { p ->
                    val isSelected = selectedProjectFilter == p
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) VioletAccent else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedProjectFilter = p }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = p,
                            fontSize = 12.sp,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Decisions List
        if (filteredDecisions.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    Text("No decisions found matching your filter.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            items(filteredDecisions) { decision ->
                DecisionCard(decision = decision)
            }
        }
    }
}
