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
                Text("Acerca de PescaPR")
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Versión ${BuildConfig.VERSION_NAME}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Tu compañero de pesca en Puerto Rico. Identificación de especies con IA, mapa de spots y registro de capturas.")
                    Text("Desarrollado por: Bradmir Consulting / Edgar Rivera", style = MaterialTheme.typography.labelSmall)
                    Text("Potenciado por Google Gemini AI.", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }

                HorizontalDivider()

                Text("Notas de Versión", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)

                // v2.3.5
                VersionNote(
                    version = "v2.3.5 - 8/10/2026",
                    changes = listOf(
                        "Morfología Costera Actualizable: Recibe nuevas estructuras sin actualizar la app y conserva los datos disponibles sin conexión."
                    )
                )

                // v2.3.4
                VersionNote(
                    version = "v2.3.4 - 8/10/2026",
                    changes = listOf(
                        "Morfología Costera Interactiva: Toca una estructura para consultar sus detalles sin bloquear la interacción con los spots de pesca."
                    )
                )

                // v2.1.5
                VersionNote(
                    version = "v2.1.5 - 7/26/2026",
                    changes = listOf(
                        "Publicación Estática de Spots: Guardado en Firestore con atribución userId explícita y cero rastreo en tiempo real.",
                        "Terminología UI: Actualización global de 'Swell' a 'Marejada'."
                    )
                )

                // v2.1.4
                VersionNote(
                    version = "v2.1.4 - 7/26/2026",
                    changes = listOf(
                        "Pantalla Paywall Pro: Interfaz de usuario para suscripciones y beneficios de PescaPR Pro."
                    )
                )

                // v2.1.3
                VersionNote(
                    version = "v2.1.3 - 7/26/2026",
                    changes = listOf(
                        "SubscriptionManager: Control de estado Pro en tiempo real y sincronización de derechos con Firestore.",
                        "Manejo de Compras: Soporte para verificación asíncrona de suscripciones y flujo de facturación."
                    )
                )

                // v2.1.2
                VersionNote(
                    version = "v2.1.2 - 7/26/2026",
                    changes = listOf(
                        "Google Play Billing v7: Integración de dependencias ktx de facturación oficial de Google Play."
                    )
                )

                // v2.1.1
                VersionNote(
                    version = "v2.1.1 - 7/25/2026",
                    changes = listOf(
                        "Red de Pines de la Comunidad: Filtrado por rol según estado de suscripción Pro.",
                        "Consultas Optimizadas: Lectura bajo demanda con botón de refresco manual para ahorrar lecturas de Firestore."
                    )
                )

                // v2.1.0
                VersionNote(
                    version = "v2.1.0 - 7/25/2026",
                    changes = listOf(
                        "Modelo PuntoPesca: Asociación de spots al ID de usuario en Firestore y Room.",
                        "Migración Room: Actualización no destructiva de base de datos de v1 a v2."
                    )
                )

                // v2.0.0
                VersionNote(
                    version = "v2.0.0 - 7/24/2026",
                    changes = listOf(
                        "Diario Privado Pro: Sincronización automática en la nube.",
                        "AI Pattern Matcher: Análisis inteligente de tus capturas para encontrar patrones de éxito.",
                        "Métricas Pro Marejada (v1.9): Optimizadas para costa."
                    )
                )

                // v1.9
                VersionNote(
                    version = "v1.9.0 - 7/24/2026",
                    changes = listOf(
                        "Pro Feature: Métricas de Marejada y Oleaje en tiempo real.",
                        "Optimización: Carga paralela de datos meteorológicos y marinos.",
                        "Arquitectura: Refactorización estructural para módulos Pro."
                    )
                )

                // v1.8
                VersionNote(
                    version = "v1.8 - 7/23/2026 ",
                    changes = listOf(
                        "Nueva Función: Capacidad de búsqueda en Guia Oficial.",
                        "Manómetro de mareas se actualiza al pulsarlo."
                    )
                )
                // v1.7
                VersionNote(
                    version = "v1.7 - 7/22/2026",
                    changes = listOf(
                        "Mejoras de Guia Oficial: Ordenado por nombre científico.",
                        "Identificación de Picos de Oro: Mejor dia de pesca en los próximos 30 días."
                    )
                )
                // v1.6
                VersionNote(
                    version = "v1.6 - 7/21/2026",
                    changes = listOf(
                        "Mejoras de Mapa: Vistas de alta resolución al hacer zoom, corregida.",
                        "Mejoras de Signos Vitales del Spot: Manómetro de mareas más preciso."
                    )
                )
                // v1.5
                VersionNote(
                    version = "v1.5 - 7/17/2026",
                    changes = listOf(
                        "Mejoras de Guía Oficial: Tamaños de imágenes mejoradas para una mejor vista.",
                        "Pines de Comunidad: Sección de pines privados y pines púbicos en el mapa."
                    )
                )
                // v1.4
                VersionNote(
                    version = "v1.4 - 7/16/2026",
                    changes = listOf(
                        "Mejoras de Estabilidad: Protecciones añadidas para evitar cierres inesperados al guardar datos.",
                        "Recuperación Automática: El app ahora puede recuperarse de errores en la base de datos sin quedar bloqueada.",
                        "Seguridad de Datos: Manejo mejorado de información interna para mayor fluidez."
                    )
                )

                // v1.3
                VersionNote(
                    version = "v1.3 - 7/15/2026",
                    changes = listOf(
                        "Privacidad Total: Spots y récords ahora se guardan localmente (Room).",
                        "Bitácora Personal: Cada usuario tiene sus propios datos privados.",
                        "Fotos Locales: Las fotos se guardan en el dispositivo (sin internet).",
                        "Persistencia: Datos protegidos durante actualizaciones del app."
                    )
                )

                // v1.2
                VersionNote(
                    version = "v1.2 - 7/4/2026",
                    changes = listOf(
                        "Integración avanzada de capturas con el Mapa.",
                        "Registro de clima automático (Temp, Viento, Presión, Marea).",
                        "Gestión de fotos del spot (límite de 4 fotos).",
                        "Navegación directa de Récords a ubicación en Mapa."
                    )
                )

                // v1.1
                VersionNote(
                    version = "v1.1 - 6/26/2026",
                    changes = listOf(
                        "Nueva sección 'Acerca de' con notas de versión.",
                        "Mejoras en la fluidez de la interfaz de usuario.",
                        "Corrección de errores en la base de datos Firestore."
                    )
                )

                // v1.0
                VersionNote(
                    version = "v1.0 - 5/8/2026",
                    changes = listOf(
                        "Identificación de peces con Inteligencia Artificial.",
                        "Mapa de spots de pesca compartidos.",
                        "Guía oficial de regulaciones autogestionada."
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar")
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
