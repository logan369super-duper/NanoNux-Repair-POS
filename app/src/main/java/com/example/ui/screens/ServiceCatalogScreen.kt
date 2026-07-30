package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.i18n.LocalStrings
import com.example.viewmodel.ServiceCatalogViewModel
import java.util.Locale

@Composable
fun ServiceCatalogScreen(viewModel: ServiceCatalogViewModel) {
    val state by viewModel.uiState.collectAsState()
    val strings = LocalStrings.current

    var showAddDialog by remember { mutableStateOf(false) }
    var serviceName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Screen") }
    var laborFee by remember { mutableStateOf("45.00") }
    var estTime by remember { mutableStateOf("45") }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Repair Service") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = serviceName,
                        onValueChange = { serviceName = it },
                        label = { Text("Service Title (e.g. OLED Screen Swap)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_service_name")
                    )
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category (e.g. Display, Power, Diagnostic)") },
                        modifier = Modifier.fillMaxWidth().testTag("add_service_category")
                    )
                    OutlinedTextField(
                        value = laborFee,
                        onValueChange = { laborFee = it },
                        label = { Text("Standard Labor Fee (Ks)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_service_fee")
                    )
                    /*
                    OutlinedTextField(
                        value = estTime,
                        onValueChange = { estTime = it },
                        label = { Text("Est. Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth().testTag("add_service_time")
                    )
                    */
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val feeDouble = laborFee.toDoubleOrNull() ?: 45.0
                        val minsInt = estTime.toIntOrNull() ?: 45
                        if (serviceName.isNotBlank()) {
                            viewModel.saveService(name = serviceName, category = category, fee = feeDouble, estMinutes = minsInt)
                            showAddDialog = false
                            serviceName = ""
                        }
                    },
                    modifier = Modifier.testTag("save_service_confirm_btn")
                ) {
                    Text("Save Service")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings.serviceCatalogTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            FloatingActionButton(
                onClick = { showAddDialog = true },
                modifier = Modifier.testTag("fab_add_service")
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Service")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.services) { service ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = service.serviceName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Category: ${service.category} | Est. Duration: ${service.estimatedTimeMinutes} mins",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ks ${String.format(Locale.US, "%.2f", service.baseLaborFee)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { viewModel.deleteService(service) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
