package com.example.iptvhost.featuresources.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.iptvhost.coredata.model.*
import java.util.UUID

@Composable
fun AddSourceDialog(
    onDismiss: () -> Unit,
    onConfirm: (IptvSource) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(SourceType.M3U) }

    val nameState = remember { mutableStateOf("") }
    val urlState = remember { mutableStateOf("") } // For playlist/base/portal
    val usernameState = remember { mutableStateOf("") }
    val passwordState = remember { mutableStateOf("") }
    val macState = remember { mutableStateOf("") }

    val errorState = remember { mutableStateOf<String?>(null) }

    fun validateAndCreate(): IptvSource? {
        val name = nameState.value.trim()
        if (name.isEmpty()) {
            errorState.value = "Name is required"
            return null
        }
        return when (selectedTab) {
            SourceType.M3U -> {
                val url = urlState.value.trim()
                if (url.isEmpty()) {
                    errorState.value = "Playlist URL is required"
                    null
                } else {
                    M3uSource(UUID.randomUUID().toString(), name, url)
                }
            }
            SourceType.XTREAM -> {
                val base = urlState.value.trim()
                val user = usernameState.value.trim()
                val pass = passwordState.value.trim()
                if (base.isEmpty() || user.isEmpty() || pass.isEmpty()) {
                    errorState.value = "All fields are required"
                    null
                } else {
                    XtreamSource(UUID.randomUUID().toString(), name, base, user, pass)
                }
            }
            SourceType.STALKER -> {
                val portal = urlState.value.trim()
                val mac = macState.value.trim()
                if (portal.isEmpty() || !mac.matches(MAC_REGEX)) {
                    errorState.value = "Portal URL and valid MAC are required"
                    null
                } else {
                    StalkerSource(UUID.randomUUID().toString(), name, portal, mac)
                }
            }
            SourceType.MAC_PORTAL -> {
                val portal = urlState.value.trim()
                val mac = macState.value.trim()
                if (portal.isEmpty() || !mac.matches(MAC_REGEX)) {
                    errorState.value = "Portal URL and valid MAC are required"
                    null
                } else {
                    MacPortalSource(UUID.randomUUID().toString(), name, portal, mac)
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val source = validateAndCreate()
                if (source != null) {
                    onConfirm(source)
                    onDismiss()
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Add IPTV Source") },
        text = {
            Column {
                SourceTypeTabRow(selected = selectedTab) { selectedTab = it }
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = nameState.value,
                    onValueChange = { nameState.value = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                when (selectedTab) {
                    SourceType.M3U -> {
                        UrlField(urlState, label = "Playlist URL")
                    }
                    SourceType.XTREAM -> {
                        UrlField(urlState, label = "Server Base URL")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = usernameState.value,
                            onValueChange = { usernameState.value = it },
                            label = { Text("Username") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordState.value,
                            onValueChange = { passwordState.value = it },
                            label = { Text("Password") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    SourceType.STALKER, SourceType.MAC_PORTAL -> {
                        UrlField(urlState, label = "Portal URL")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = macState.value,
                            onValueChange = { macState.value = it },
                            label = { Text("MAC Address (e.g., 00:1A:79:…)" )},
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                errorState.value?.let { msg ->
                    Spacer(Modifier.height(12.dp))
                    Text(msg, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@Composable
private fun SourceTypeTabRow(selected: SourceType, onSelect: (SourceType) -> Unit) {
    TabRow(selectedTabIndex = selected.ordinal) {
        SourceType.values().forEachIndexed { index, type ->
            Tab(
                selected = selected.ordinal == index,
                onClick = { onSelect(type) },
                text = { Text(type.title) }
            )
        }
    }
}

@Composable
private fun UrlField(state: MutableState<String>, label: String) {
    OutlinedTextField(
        value = state.value,
        onValueChange = { state.value = it },
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

private val MAC_REGEX = "^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$".toRegex()

private enum class SourceType(val title: String) {
    M3U("M3U"),
    XTREAM("Xtream"),
    STALKER("Stalker"),
    MAC_PORTAL("MAC Portal")
}