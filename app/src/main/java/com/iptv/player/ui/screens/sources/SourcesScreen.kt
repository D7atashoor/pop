package com.iptv.player.ui.screens.sources

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.iptv.player.data.model.Source
import com.iptv.player.data.model.SourceType
import com.iptv.player.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourcesScreen(
    navController: NavController,
    viewModel: SourcesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("المصادر") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddSource.route) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "إضافة مصدر")
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.sources.isEmpty()) {
            EmptySourcesContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onAddSourceClick = { navController.navigate(Screen.AddSource.route) }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.sources) { source ->
                    SourceCard(
                        source = source,
                        onToggleActive = { viewModel.toggleSourceStatus(source.id) },
                        onDeleteSource = { viewModel.deleteSource(source) }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptySourcesContent(
    modifier: Modifier = Modifier,
    onAddSourceClick: () -> Unit
) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Storage,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "لا توجد مصادر",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "قم بإضافة مصدر للبدء في مشاهدة القنوات",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onAddSourceClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("إضافة مصدر")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceCard(
    source: Source,
    onToggleActive: () -> Unit,
    onDeleteSource: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = getSourceTypeDisplayName(source.type),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Switch(
                    checked = source.isActive,
                    onCheckedChange = { onToggleActive() }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = source.url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "حذف",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("حذف")
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("تأكيد الحذف") },
            text = { Text("هل أنت متأكد من حذف هذا المصدر؟") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSource()
                        showDeleteDialog = false
                    }
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

private fun getSourceTypeDisplayName(type: SourceType): String {
    return when (type) {
        SourceType.M3U -> "M3U Playlist"
        SourceType.STALKER -> "Stalker Portal"
        SourceType.XTREAM -> "Xtream Codes"
        SourceType.MAC_PORTAL -> "MAC Portal"
    }
}