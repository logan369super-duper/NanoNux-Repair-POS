package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.RepairItemBrandEntity
import com.example.viewmodel.ShopSetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrandManagementScreen(
    viewModel: ShopSetupViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var brandToEdit by remember { mutableStateOf<RepairItemBrandEntity?>(null) }
    var editNameInput by remember { mutableStateOf("") }
    var brandToDelete by remember { mutableStateOf<RepairItemBrandEntity?>(null) }

    LaunchedEffect(state.saveMessage) {
        state.saveMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSaveMessage()
        }
    }

    // Always ensure "Generic / Custom" is present at the top of the list
    val allBrands = remember(state.brandsList) {
        val list = state.brandsList.toMutableList()
        val hasGeneric = list.any { it.name == "Generic / Custom" }
        if (!hasGeneric) {
            list.add(0, RepairItemBrandEntity(id = -1, name = "Generic / Custom", isSystemDefault = true))
        }
        list
    }

    val filteredBrands = remember(allBrands, searchQuery) {
        if (searchQuery.isBlank()) allBrands
        else allBrands.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // Edit Brand Dialog
    if (brandToEdit != null) {
        AlertDialog(
            onDismissRequest = { brandToEdit = null },
            title = { Text("Edit Brand Name", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editNameInput,
                    onValueChange = { editNameInput = it },
                    label = { Text("Brand / Manufacturer Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_brand_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        brandToEdit?.let { b ->
                            viewModel.updateRepairBrand(b, editNameInput)
                        }
                        brandToEdit = null
                    },
                    modifier = Modifier.testTag("confirm_edit_brand_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { brandToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Brand Dialog
    if (brandToDelete != null) {
        AlertDialog(
            onDismissRequest = { brandToDelete = null },
            title = { Text("Confirm Brand Deletion", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete brand '${brandToDelete?.name}'? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        brandToDelete?.let { b ->
                            viewModel.deleteRepairBrand(b)
                        }
                        brandToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_brand_button")
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { brandToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("brand_management_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Brand / Manufacturer Setup",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Info Banner
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Manage brand names for repair tickets. 'Generic / Custom' is always built-in as default in Service Intake.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Create New Brand Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Add New Brand / Manufacturer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = state.newBrandInput,
                                onValueChange = { viewModel.updateNewBrandInput(it) },
                                label = { Text("Brand Name *") },
                                placeholder = { Text("e.g. Apple, Samsung, Sony, Makita") },
                                leadingIcon = { Icon(Icons.Filled.Category, contentDescription = null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("brand_crud_add_input"),
                                singleLine = true
                            )

                            Button(
                                onClick = { viewModel.addRepairBrand() },
                                modifier = Modifier
                                    .height(56.dp)
                                    .testTag("brand_crud_add_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = "Add")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add")
                            }
                        }
                    }
                }
            }

            // Search Bar & Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Configured Brands (${filteredBrands.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search brand name...") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("brand_crud_search_input"),
                    singleLine = true
                )
            }

            // List of Brands
            items(filteredBrands, key = { brand -> brand.id.toString() + "_" + brand.name }) { brand ->
                val isDefault = brand.name == "Generic / Custom" || brand.isSystemDefault
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDefault) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isDefault) MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isDefault) Icons.Filled.Star else Icons.Filled.Label,
                                contentDescription = null,
                                tint = if (isDefault) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = brand.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isDefault) FontWeight.Bold else FontWeight.SemiBold
                                )
                                if (isDefault) {
                                    Text(
                                        text = "System Default (Always selected by default in Service Intake)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        Row {
                            if (!isDefault) {
                                IconButton(
                                    onClick = {
                                        brandToEdit = brand
                                        editNameInput = brand.name
                                    },
                                    modifier = Modifier.testTag("edit_brand_${brand.id}")
                                ) {
                                    Icon(
                                        Icons.Filled.Edit,
                                        contentDescription = "Edit Brand",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = { brandToDelete = brand },
                                    modifier = Modifier.testTag("delete_brand_${brand.id}")
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete Brand",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("Default", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
