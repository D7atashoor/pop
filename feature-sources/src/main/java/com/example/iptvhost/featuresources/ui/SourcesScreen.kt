package com.example.iptvhost.featuresources.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.iptvhost.coredata.model.IptvSource
import com.example.iptvhost.featuresources.viewmodel.SourcesViewModel

@Composable
fun SourcesScreen(
    viewModel: SourcesViewModel = hiltViewModel()
) {
    val sources by viewModel.sources.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Source")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            if (sources.isEmpty()) {
                EmptyPlaceholder()
            } else {
                SourcesList(sources = sources, onDelete = { viewModel.deleteSource(it.id) })
            }
        }
    }

    if (showAddDialog) {
        AddSourceDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { viewModel.addSource(it) }
        )
    }
}

@Composable
private fun EmptyPlaceholder() {
    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("No sources added yet. Tap + to add one.")
    }
}

@Composable
private fun SourcesList(
    sources: List<IptvSource>,
    onDelete: (IptvSource) -> Unit,
) {
    LazyColumn {
        items(sources, key = { it.id }) { source ->
            SourceRow(source = source, onDelete = { onDelete(source) })
        }
    }
}

@Composable
private fun SourceRow(
    source: IptvSource,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text(source.name, style = MaterialTheme.typography.titleMedium)
                Text(source::class.simpleName ?: "", style = MaterialTheme.typography.bodySmall)
            }
            Row {
                /*Edit feature reserved*/
                //IconButton(onClick = { /*TODO edit*/ }) {
                //    Icon(Icons.Default.Edit, contentDescription = "Edit")
                //}
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}