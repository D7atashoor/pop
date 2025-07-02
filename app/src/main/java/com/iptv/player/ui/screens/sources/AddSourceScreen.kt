package com.iptv.player.ui.screens.sources

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.iptv.player.data.model.SourceType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceScreen(
    navController: NavController,
    viewModel: AddSourceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isSourceAdded) {
        if (uiState.isSourceAdded) {
            navController.popBackStack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("إضافة مصدر جديد") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Source name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("اسم المصدر") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Source type selection
            Text(
                text = "نوع المصدر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            SourceType.values().forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (uiState.selectedType == type),
                            onClick = { viewModel.updateSourceType(type) }
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (uiState.selectedType == type),
                        onClick = { viewModel.updateSourceType(type) }
                    )
                    Text(
                        text = getSourceTypeDisplayName(type),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
            
            Divider()
            
            // Dynamic fields based on source type
            when (uiState.selectedType) {
                SourceType.M3U -> {
                    M3USourceFields(
                        url = uiState.url,
                        onUrlChange = viewModel::updateUrl
                    )
                }
                SourceType.STALKER -> {
                    StalkerSourceFields(
                        portalUrl = uiState.url,
                        macAddress = uiState.macAddress,
                        username = uiState.username,
                        password = uiState.password,
                        onPortalUrlChange = viewModel::updateUrl,
                        onMacAddressChange = viewModel::updateMacAddress,
                        onUsernameChange = viewModel::updateUsername,
                        onPasswordChange = viewModel::updatePassword
                    )
                }
                SourceType.XTREAM -> {
                    XtreamSourceFields(
                        serverUrl = uiState.url,
                        username = uiState.username,
                        password = uiState.password,
                        onServerUrlChange = viewModel::updateUrl,
                        onUsernameChange = viewModel::updateUsername,
                        onPasswordChange = viewModel::updatePassword
                    )
                }
                SourceType.MAC_PORTAL -> {
                    MacPortalSourceFields(
                        portalUrl = uiState.url,
                        macAddress = uiState.macAddress,
                        serialNumber = uiState.serialNumber,
                        onPortalUrlChange = viewModel::updateUrl,
                        onMacAddressChange = viewModel::updateMacAddress,
                        onSerialNumberChange = viewModel::updateSerialNumber
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Error message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            
            // Add button
            Button(
                onClick = viewModel::addSource,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && uiState.name.isNotBlank() && uiState.url.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("إضافة المصدر")
            }
        }
    }
}

@Composable
fun M3USourceFields(
    url: String,
    onUrlChange: (String) -> Unit
) {
    OutlinedTextField(
        value = url,
        onValueChange = onUrlChange,
        label = { Text("رابط M3U") },
        placeholder = { Text("http://example.com/playlist.m3u8") },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true
    )
}

@Composable
fun StalkerSourceFields(
    portalUrl: String,
    macAddress: String,
    username: String,
    password: String,
    onPortalUrlChange: (String) -> Unit,
    onMacAddressChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = portalUrl,
            onValueChange = onPortalUrlChange,
            label = { Text("رابط البوابة") },
            placeholder = { Text("http://portal.example.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = macAddress,
            onValueChange = onMacAddressChange,
            label = { Text("عنوان MAC") },
            placeholder = { Text("00:1A:79:XX:XX:XX") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("اسم المستخدم (اختياري)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("كلمة المرور (اختياري)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
    }
}

@Composable
fun XtreamSourceFields(
    serverUrl: String,
    username: String,
    password: String,
    onServerUrlChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = serverUrl,
            onValueChange = onServerUrlChange,
            label = { Text("رابط الخادم") },
            placeholder = { Text("http://server.example.com:8080") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("اسم المستخدم") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("كلمة المرور") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )
    }
}

@Composable
fun MacPortalSourceFields(
    portalUrl: String,
    macAddress: String,
    serialNumber: String,
    onPortalUrlChange: (String) -> Unit,
    onMacAddressChange: (String) -> Unit,
    onSerialNumberChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = portalUrl,
            onValueChange = onPortalUrlChange,
            label = { Text("رابط البوابة") },
            placeholder = { Text("http://portal.example.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = macAddress,
            onValueChange = onMacAddressChange,
            label = { Text("عنوان MAC") },
            placeholder = { Text("00:1A:79:XX:XX:XX") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        
        OutlinedTextField(
            value = serialNumber,
            onValueChange = onSerialNumberChange,
            label = { Text("الرقم التسلسلي (اختياري)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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