package com.hackathon2025.deviationwizard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.hackathon2025.deviationwizard.api.ApiConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    onGoBack: () -> Unit,
    onUpdateBaseUrl: (String) -> Unit,
) {
    var selectedUrl by remember { mutableStateOf(ApiConfig.baseUrl) }
    var customUrl by remember { mutableStateOf("") }
    var showCustomUrlInput by remember { mutableStateOf(false) }
    var apiKey by remember { mutableStateOf(ApiConfig.apiKey) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Server Configuration") },
                navigationIcon = {
                    IconButton(onClick = onGoBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Default URLs
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Default URLs",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Google Cloud URL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Google Cloud",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Remote test server",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        RadioButton(
                            selected = selectedUrl == ApiConfig.GOOGLE_CLOUD_URL && !showCustomUrlInput,
                            onClick = {
                                selectedUrl = ApiConfig.GOOGLE_CLOUD_URL
                                showCustomUrlInput = false
                                onUpdateBaseUrl(selectedUrl)
                            }
                        )
                    }

                    HorizontalDivider()

                    // Android Emulator URL
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Android Emulator",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = ApiConfig.DEFAULT_EMULATOR_URL,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        RadioButton(
                            selected = selectedUrl == ApiConfig.DEFAULT_EMULATOR_URL && !showCustomUrlInput,
                            onClick = {
                                selectedUrl = ApiConfig.DEFAULT_EMULATOR_URL
                                showCustomUrlInput = false
                                onUpdateBaseUrl(selectedUrl)
                            }
                        )
                    }
                }
            }

            // Custom URL option
            Card(
                modifier = Modifier.fillMaxWidth()
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
                        Text(
                            text = "Custom URL",
                            style = MaterialTheme.typography.titleMedium
                        )
                        RadioButton(
                            selected = showCustomUrlInput,
                            onClick = { 
                                showCustomUrlInput = true
                                if (selectedUrl != ApiConfig.GOOGLE_CLOUD_URL && selectedUrl != ApiConfig.DEFAULT_EMULATOR_URL) {
                                    customUrl = selectedUrl
                                }
                            }
                        )
                    }

                    if (showCustomUrlInput) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customUrl,
                            onValueChange = { customUrl = it },
                            label = { Text("Enter custom URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("http://your-server:port") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (customUrl.isNotEmpty()) {
                                    selectedUrl = customUrl
                                    onUpdateBaseUrl(selectedUrl)
                                }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Apply")
                        }
                    }
                }
            }

            // API Key
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "API Key",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = apiKey,
                        onValueChange = { 
                            apiKey = it
                            ApiConfig.setApiKey(it)
                        },
                        label = { Text("Enter API Key") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Current URL display
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Current API URL",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = ApiConfig.baseUrl,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}