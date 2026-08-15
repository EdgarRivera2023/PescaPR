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
import com.bradmir.pescapr.R
import androidx.compose.ui.res.stringResource

@Composable
fun AdminScreen() {
    var showReportDialog by remember { mutableStateOf(false) }
    var showLogDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            stringResource(R.string.admin_tools_title),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Button(onClick = { showReportDialog = true }) {
            Icon(Icons.Default.BugReport, contentDescription = null)
            Text(stringResource(R.string.admin_report_error), modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedButton(onClick = { showLogDialog = true }) {
            Icon(Icons.Default.ListAlt, contentDescription = null)
            Text(stringResource(R.string.admin_view_error_log), modifier = Modifier.padding(start = 8.dp))
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
        title = { Text(stringResource(R.string.admin_report_error)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.admin_title_optional)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(R.string.admin_description)) },
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
                        Toast.makeText(context, context.getString(R.string.admin_error_saved), Toast.LENGTH_SHORT).show()
                    }
                }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) } }
    )
}

@Composable
private fun LogDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val noErrorsText = stringResource(R.string.admin_no_errors)
    var logText by remember { mutableStateOf(BugReportLogger.read(context)) }
    var confirmClear by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.admin_error_log)) },
        text = {
            Text(
                logText.ifBlank { noErrorsText },
                modifier = Modifier.heightIn(max = 480.dp).verticalScroll(rememberScrollState())
            )
        },
        confirmButton = {
            TextButton(
                enabled = logText.isNotBlank(),
                onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.admin_error_log_subject))
                        putExtra(Intent.EXTRA_TEXT, logText)
                    }
                        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.admin_share_log)))
                }
            ) { Text(stringResource(R.string.admin_share)) }
        },
        dismissButton = {
            Column {
                TextButton(enabled = logText.isNotBlank(), onClick = { confirmClear = true }) {
                    Text(stringResource(R.string.admin_clear_log))
                }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_close)) }
            }
        }
    )

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(stringResource(R.string.admin_clear_log)) },
            text = { Text(stringResource(R.string.admin_confirm_clear_log)) },
            confirmButton = {
                TextButton(onClick = {
                    if (BugReportLogger.clear(context)) logText = ""
                    confirmClear = false
                }) { Text(stringResource(R.string.admin_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.action_cancel)) }
            }
        )
    }
}
