package com.bradmir.pescapr.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bradmir.pescapr.utils.BugReportLogger

@Composable
fun AdminScreen() {
    var showReportDialog by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Herramientas de Administración",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Button(onClick = { showReportDialog = true }) {
            Icon(Icons.Default.BugReport, contentDescription = null)
            Text("Reportar Error", modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(onClick = { showLogDialog = true }) {
            Icon(Icons.Default.ListAlt, contentDescription = null)
            Text("Ver Registro de Errores", modifier = Modifier.padding(start = 8.dp))
        }
    }

    if (showReportDialog) ReportDialog(onDismiss = { showReportDialog = false })
    if (showLogDialog) LogDialog(onDismiss = { showLogDialog = false })
}

@Composable
private fun ReportDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reportar Error") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título (opcional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = description.isNotBlank(),
                onClick = {
                    if (BugReportLogger.append(context, title, description)) {
                        title = ""
                        description = ""
                        onDismiss()
                        Toast.makeText(context, "Error guardado", Toast.LENGTH_SHORT).show()
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun LogDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var logText by remember { mutableStateOf(BugReportLogger.read(context)) }
    var confirmClear by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registro de Errores") },
        text = {
            Text(
                logText.ifBlank { "No hay errores reportados." },
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(
                enabled = logText.isNotBlank(),
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Registro de errores de PescaPR")
                        putExtra(Intent.EXTRA_TEXT, logText)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Compartir registro"))
                }
            ) { Text("Compartir") }
        },
        dismissButton = {
            Column {
                TextButton(enabled = logText.isNotBlank(), onClick = { confirmClear = true }) {
                    Text("Borrar registro")
                }
                TextButton(onClick = onDismiss) { Text("Cerrar") }
            }
        }
    )

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Borrar registro") },
            text = { Text("¿Seguro que deseas borrar todos los errores reportados?") },
            confirmButton = {
                TextButton(onClick = {
                    if (BugReportLogger.clear(context)) logText = ""
                    confirmClear = false
                }) { Text("Borrar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancelar") }
            }
        )
    }
}
