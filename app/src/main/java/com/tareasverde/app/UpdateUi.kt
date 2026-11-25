package com.tareasverde.app

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Estado compartido entre el aviso al abrir la app y el botón de Ajustes. */
object UpdateUiState {
    var available by mutableStateOf<Update.Info?>(null)
}

/** Busca una actualización y avisa. Con manual = true también informa si no hay nada nuevo. */
fun checkForUpdate(ctx: android.content.Context, manual: Boolean) {
    Update.check(ctx) { r ->
        when (r) {
            is Update.Result.Available -> UpdateUiState.available = r.info
            Update.Result.UpToDate ->
                if (manual) Toast.makeText(ctx, "Ya tenés la última versión", Toast.LENGTH_LONG).show()
            Update.Result.NotConfigured ->
                if (manual) Toast.makeText(
                    ctx, "Primero escribí tu repositorio de GitHub (usuario/repo)", Toast.LENGTH_LONG
                ).show()
            is Update.Result.Error ->
                if (manual) Toast.makeText(ctx, "No se pudo buscar: ${r.message}", Toast.LENGTH_LONG).show()
        }
    }
}

@Composable
fun UpdateDialog() {
    val info = UpdateUiState.available ?: return
    val ctx = LocalContext.current
    var progress by remember { mutableStateOf<Int?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = { if (progress == null) UpdateUiState.available = null },
        containerColor = CardDark,
        title = { Text("Actualización disponible", color = TextMain) },
        text = {
            Column {
                Text("Versión ${info.versionName}", color = GreenAccent, fontSize = 16.sp)
                if (info.notes.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(info.notes, color = TextDim, fontSize = 14.sp)
                }
                progress?.let { p ->
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { p / 100f },
                        color = GreenAccent,
                        trackColor = CardHi,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text("Descargando… $p%", color = TextDim, fontSize = 12.sp)
                }
                error?.let {
                    Spacer(Modifier.height(12.dp))
                    Text(it, color = Danger, fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = progress == null,
                onClick = {
                    progress = 0
                    error = null
                    Update.download(ctx, info, onProgress = { progress = it }) { file ->
                        progress = null
                        if (file == null) {
                            error = "No se pudo descargar. Revisá la conexión y probá de nuevo."
                        } else if (!Update.install(ctx, file)) {
                            error = "Activá \"Permitir de esta fuente\" para GO GO PIDIO y tocá Actualizar otra vez."
                        } else {
                            UpdateUiState.available = null
                        }
                    }
                },
            ) { Text("Actualizar", color = if (progress == null) GreenAccent else TextDim) }
        },
        dismissButton = {
            TextButton(
                enabled = progress == null,
                onClick = { UpdateUiState.available = null },
            ) { Text("Después", color = TextDim) }
        },
    )
}
