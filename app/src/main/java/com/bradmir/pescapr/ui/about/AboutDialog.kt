package com.bradmir.pescapr.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.bradmir.pescapr.BuildConfig
import com.bradmir.pescapr.R
import androidx.compose.ui.res.stringResource

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painter = painterResource(id = R.drawable.logo_small), contentDescription = null, modifier = Modifier.size(40.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.about_title))
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.about_description))
                    Text(stringResource(R.string.about_developed_by), style = MaterialTheme.typography.labelSmall)
                    Text(stringResource(R.string.about_powered_by), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                HorizontalDivider()

                Text(stringResource(R.string.about_release_notes), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                // v2.3.5
                VersionNote(
                    version = stringResource(R.string.release_version_235),
                    changes = listOf(stringResource(R.string.release_235_change1))
                )

                // v2.3.4
                VersionNote(
                    version = stringResource(R.string.release_version_234),
                    changes = listOf(stringResource(R.string.release_234_change1))
                )

                // v2.1.5
                VersionNote(
                    version = stringResource(R.string.release_version_215),
                    changes = listOf(stringResource(R.string.release_215_change1), stringResource(R.string.release_215_change2))
                )

                // v2.1.4
                VersionNote(
                    version = stringResource(R.string.release_version_214),
                    changes = listOf(stringResource(R.string.release_214_change1))
                )

                // v2.1.3
                VersionNote(
                    version = stringResource(R.string.release_version_213),
                    changes = listOf(stringResource(R.string.release_213_change1), stringResource(R.string.release_213_change2))
                )

                // v2.1.2
                VersionNote(
                    version = stringResource(R.string.release_version_212),
                    changes = listOf(stringResource(R.string.release_212_change1))
                )

                // v2.1.1
                VersionNote(
                    version = stringResource(R.string.release_version_211),
                    changes = listOf(stringResource(R.string.release_211_change1), stringResource(R.string.release_211_change2))
                )

                // v2.1.0
                VersionNote(
                    version = stringResource(R.string.release_version_210),
                    changes = listOf(stringResource(R.string.release_210_change1), stringResource(R.string.release_210_change2))
                )

                // v2.0.0
                VersionNote(
                    version = stringResource(R.string.release_version_200),
                    changes = listOf(stringResource(R.string.release_200_change1), stringResource(R.string.release_200_change2), stringResource(R.string.release_200_change3))
                )

                // v1.9
                VersionNote(
                    version = stringResource(R.string.release_version_190),
                    changes = listOf(stringResource(R.string.release_190_change1), stringResource(R.string.release_190_change2), stringResource(R.string.release_190_change3))
                )

                // v1.8
                VersionNote(
                    version = stringResource(R.string.release_version_180),
                    changes = listOf(stringResource(R.string.release_180_change1), stringResource(R.string.release_180_change2))
                )
                // v1.7
                VersionNote(
                    version = stringResource(R.string.release_version_170),
                    changes = listOf(stringResource(R.string.release_170_change1), stringResource(R.string.release_170_change2))
                )
                // v1.6
                VersionNote(
                    version = stringResource(R.string.release_version_160),
                    changes = listOf(stringResource(R.string.release_160_change1), stringResource(R.string.release_160_change2))
                )
                // v1.5
                VersionNote(
                    version = stringResource(R.string.release_version_150),
                    changes = listOf(stringResource(R.string.release_150_change1), stringResource(R.string.release_150_change2))
                )
                // v1.4
                VersionNote(
                    version = stringResource(R.string.release_version_140),
                    changes = listOf(stringResource(R.string.release_140_change1), stringResource(R.string.release_140_change2), stringResource(R.string.release_140_change3))
                )

                // v1.3
                VersionNote(
                    version = stringResource(R.string.release_version_130),
                    changes = listOf(stringResource(R.string.release_130_change1), stringResource(R.string.release_130_change2), stringResource(R.string.release_130_change3), stringResource(R.string.release_130_change4))
                )

                // v1.2
                VersionNote(
                    version = stringResource(R.string.release_version_120),
                    changes = listOf(stringResource(R.string.release_120_change1), stringResource(R.string.release_120_change2), stringResource(R.string.release_120_change3), stringResource(R.string.release_120_change4))
                )

                // v1.1
                VersionNote(
                    version = stringResource(R.string.release_version_110),
                    changes = listOf(stringResource(R.string.release_110_change1), stringResource(R.string.release_110_change2), stringResource(R.string.release_110_change3))
                )

                // v1.0
                VersionNote(
                    version = stringResource(R.string.release_version_100),
                    changes = listOf(stringResource(R.string.release_100_change1), stringResource(R.string.release_100_change2), stringResource(R.string.release_100_change3))
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun VersionNote(version: String, changes: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(version, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        changes.forEach { change ->
            Row(modifier = Modifier.padding(start = 8.dp)) {
                Text("• ", fontWeight = FontWeight.Bold)
                Text(change, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
