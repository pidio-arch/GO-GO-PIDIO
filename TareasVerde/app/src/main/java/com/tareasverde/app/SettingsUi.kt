@file:OptIn(ExperimentalLayoutApi::class)

package com.tareasverde.app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.roundToInt

@Composable
private fun PermRow(title: String, desc: String, granted: Boolean, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = TextMain, fontSize = 15.sp)
            Spacer(Modifier.height(2.dp))
            Text(desc, color = TextDim, fontSize = 12.sp)
        }
        Spacer(Modifier.width(12.dp))
        if (granted) {
            Icon(Icons.Filled.Check, contentDescription = "Activado", tint = GreenAccent)
        } else {
            OutlinedButton(onClick = onClick, border = BorderStroke(1.dp, GreenAccent)) {
                Text("Activar", color = GreenAccent)
            }
        }
    }
}

/** Lista de permisos con su estado; se actualiza sola al volver desde los ajustes del sistema. */
@Composable
fun PermissionsPanel() {
    val ctx = LocalContext.current
    var tick by remember { mutableIntStateOf(0) }

    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                tick++
                ReminderScheduler.schedule(ctx) // por si se concedió el permiso de alarmas exactas
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }

    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        // Si el sistema ya no deja preguntar, llevamos al usuario a los ajustes de notificaciones.
        if (!granted) Perms.open(ctx, Perms.notificationSettingsIntent(ctx))
        tick++
    }

    val notif = remember(tick) { Perms.notifications(ctx) }
    val fullScreen = remember(tick) { Perms.fullScreenIntent(ctx) }
    val overlay = remember(tick) { Perms.overlay(ctx) }
    val battery = remember(tick) { Perms.batteryUnrestricted(ctx) }
    val exact = remember(tick) { Perms.exactAlarms(ctx) }
    val install = remember(tick) { ctx.packageManager.canRequestPackageInstalls() }

    Panel {
        PermRow(
            title = "Notificaciones",
            desc = "Recordatorios de tareas y respaldo de las frases.",
            granted = notif,
        ) {
            val needsRuntime = Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            if (needsRuntime) {
                notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                Perms.open(ctx, Perms.notificationSettingsIntent(ctx))
            }
        }
        if (Build.VERSION.SDK_INT >= 34) {
            Spacer(Modifier.height(16.dp))
            PermRow(
                title = "Pantalla completa en el bloqueo",
                desc = "Permite que la frase aparezca sobre la pantalla de bloqueo.",
                granted = fullScreen,
            ) { Perms.open(ctx, Perms.fullScreenIntentSettings(ctx)) }
        }
        Spacer(Modifier.height(16.dp))
        PermRow(
            title = "Mostrar sobre otras apps",
            desc = "Ayuda a lanzar la frase apenas prendés la pantalla.",
            granted = overlay,
        ) { Perms.open(ctx, Perms.overlayIntent(ctx)) }
        Spacer(Modifier.height(16.dp))
        PermRow(
            title = "Batería sin restricciones",
            desc = "Evita que el celular cierre la app y se pierdan frases y avisos.",
            granted = battery,
        ) {
            if (!Perms.open(ctx, Perms.batteryIntent(ctx))) {
                Perms.open(ctx, Perms.batteryFallbackIntent())
            }
        }
        if (Build.VERSION.SDK_INT >= 31) {
            Spacer(Modifier.height(16.dp))
            PermRow(
                title = "Alarmas exactas",
                desc = "Para que el recordatorio llegue justo a la hora elegida.",
                granted = exact,
            ) { Perms.open(ctx, Perms.exactAlarmIntent(ctx)) }
        }
        Spacer(Modifier.height(16.dp))
        PermRow(
            title = "Instalar actualizaciones",
            desc = "Para que la app se pueda actualizar sola desde adentro.",
            granted = install,
        ) { Perms.open(ctx, Perms.installUnknownAppsIntent(ctx)) }
    }
}

/** Simula la pantalla de bloqueo para ver cómo queda la frase con los ajustes actuales. */
@Composable
private fun QuotePreview(size: Int, color: Int, shadow: Boolean, pos: Int) {
    val align = when (pos) {
        0 -> Alignment.TopCenter
        2 -> Alignment.BottomCenter
        else -> Alignment.Center
    }
    val textShadow = if (shadow) Shadow(Color.Black, Offset(0f, 2f), 8f) else null
    Box(
        Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF1F3B4D), Color(0xFF6C8EA4))))
            .padding(16.dp),
        contentAlignment = align,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "“Hoy no tiene que ser perfecto, solo tiene que ser avanzado.”",
                color = Color(color),
                fontSize = (size * 0.55f).sp,
                lineHeight = (size * 0.55f * 1.15f).sp,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = textShadow),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "— Anónimo",
                color = Color(color),
                fontSize = (size * 0.55f * 0.55f).coerceAtLeast(10f).sp,
                style = TextStyle(shadow = textShadow),
            )
        }
    }
}

@Composable
private fun QuoteAppearancePanel() {
    val ctx = LocalContext.current
    var size by remember { mutableIntStateOf(AppPrefs.quoteSize(ctx)) }
    var color by remember { mutableIntStateOf(AppPrefs.quoteColor(ctx)) }
    var shadow by remember { mutableStateOf(AppPrefs.quoteShadow(ctx)) }
    var pos by remember { mutableIntStateOf(AppPrefs.quotePosition(ctx)) }
    var secs by remember { mutableIntStateOf(AppPrefs.quoteSeconds(ctx)) }

    val swatches = listOf(
        Color.White, GreenAccent, Color(0xFFFFEB3B), Color(0xFF40C4FF),
        Color(0xFFFF80AB), Color(0xFFFFAB40), Color.Black,
    )

    Panel {
        Text("Frase en la pantalla de bloqueo", color = TextMain, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Se muestra sin fondo sobre tu pantalla de bloqueo. Elegí cómo se ve (vista previa reducida).",
            color = TextDim,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(14.dp))
        QuotePreview(size, color, shadow, pos)

        Spacer(Modifier.height(16.dp))
        Text("Tamaño: $size", color = TextDim, fontSize = 12.sp)
        Slider(
            value = size.toFloat(),
            onValueChange = {
                size = it.roundToInt()
                AppPrefs.setQuoteSize(ctx, size)
            },
            valueRange = 16f..56f,
            colors = SliderDefaults.colors(
                thumbColor = GreenAccent,
                activeTrackColor = GreenAccent,
                inactiveTrackColor = CardHi,
            ),
        )

        Spacer(Modifier.height(6.dp))
        Text("Color", color = TextDim, fontSize = 12.sp)
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            swatches.forEach { c ->
                val selected = color == c.toArgb()
                Box(
                    Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(c)
                        .border(
                            if (selected) 3.dp else 1.dp,
                            if (selected) GreenAccent else Color(0xFF555555),
                            CircleShape,
                        )
                        .clickable {
                            color = c.toArgb()
                            AppPrefs.setQuoteColor(ctx, color)
                        }
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Sombra", color = TextMain, fontSize = 15.sp)
                Text("Ayuda a leerla sobre fondos claros.", color = TextDim, fontSize = 12.sp)
            }
            Switch(
                checked = shadow,
                onCheckedChange = {
                    shadow = it
                    AppPrefs.setQuoteShadow(ctx, it)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BgBlack,
                    checkedTrackColor = GreenAccent,
                    uncheckedThumbColor = TextDim,
                    uncheckedTrackColor = CardHi,
                    uncheckedBorderColor = LineDim,
                ),
            )
        }

        Spacer(Modifier.height(16.dp))
        Text("Posición", color = TextDim, fontSize = 12.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Arriba", "Centro", "Abajo").forEachIndexed { i, label ->
                GreenChip(label, pos == i) {
                    pos = i
                    AppPrefs.setQuotePosition(ctx, i)
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        Text("Se queda en pantalla", color = TextDim, fontSize = 12.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(5, 10, 20, 30).forEach { s ->
                GreenChip("$s s", secs == s) {
                    secs = s
                    AppPrefs.setQuoteSeconds(ctx, s)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Tocá la frase para cerrarla antes.", color = TextDim, fontSize = 12.sp)
    }
}

@Composable
private fun UpdatesPanel() {
    val ctx = LocalContext.current
    var repo by remember { mutableStateOf(AppPrefs.updateRepo(ctx)) }

    Panel {
        Text("Actualizaciones", color = TextMain, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Versión instalada: ${Update.currentVersionName(ctx)} (${Update.currentVersionCode(ctx)})",
            color = GreenAccent,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = repo,
            onValueChange = {
                repo = it
                AppPrefs.setUpdateRepo(ctx, it)
            },
            label = { Text("Repositorio de GitHub (usuario/repo)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenAccent,
                unfocusedBorderColor = LineDim,
                cursorColor = GreenAccent,
                focusedLabelColor = GreenAccent,
            ),
        )
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { checkForUpdate(ctx, manual = true) },
            border = BorderStroke(1.dp, GreenAccent),
        ) { Text("Buscar actualización", color = GreenAccent) }
        Spacer(Modifier.height(8.dp))
        Text(
            "Al abrir la app también se busca sola y te avisa si hay una versión nueva.",
            color = TextDim,
            fontSize = 12.sp,
        )
    }
}

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    var unlock by remember { mutableStateOf(AppPrefs.unlockQuotes(ctx)) }
    var hour by remember { mutableIntStateOf(AppPrefs.reminderHour(ctx)) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Brand(18.sp)
        Text("Ajustes", color = TextMain, fontSize = 32.sp, fontWeight = FontWeight.Bold)

        Panel {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Frase al prender el celular", color = TextMain, fontSize = 16.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Una frase sobre la pantalla de bloqueo cada vez que se prende la pantalla.",
                        color = TextDim,
                        fontSize = 13.sp,
                    )
                }
                Spacer(Modifier.width(12.dp))
                Switch(
                    checked = unlock,
                    onCheckedChange = { on ->
                        unlock = on
                        AppPrefs.setUnlockQuotes(ctx, on)
                        if (on) UnlockService.start(ctx) else UnlockService.stop(ctx)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BgBlack,
                        checkedTrackColor = GreenAccent,
                        uncheckedThumbColor = TextDim,
                        uncheckedTrackColor = CardHi,
                        uncheckedBorderColor = LineDim,
                    ),
                )
            }
        }

        QuoteAppearancePanel()

        Text("Permisos", color = TextDim, fontSize = 13.sp)
        PermissionsPanel()

        Panel {
            Text("Recordatorio diario", color = TextMain, fontSize = 16.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                "Cada día a esta hora te aviso de las tareas con fecha límite y las diarias sin tachar.",
                color = TextDim,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(12.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(7, 9, 12, 15, 18, 21).forEach { h ->
                    GreenChip("$h:00", hour == h) {
                        hour = h
                        AppPrefs.setReminderHour(ctx, h)
                        ReminderScheduler.schedule(ctx)
                    }
                }
            }
        }

        UpdatesPanel()

        Panel {
            Text("Probar", color = TextMain, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = {
                        if (!Notifier.canNotify(ctx)) {
                            Toast.makeText(ctx, "Activá las notificaciones primero", Toast.LENGTH_LONG).show()
                        } else if (!Notifier.showReminder(ctx)) {
                            Toast.makeText(
                                ctx,
                                "No hay tareas pendientes con fecha límite ni diarias",
                                Toast.LENGTH_LONG,
                            ).show()
                        }
                    },
                    border = BorderStroke(1.dp, GreenAccent),
                ) { Text("Recordatorio", color = GreenAccent) }

                OutlinedButton(
                    onClick = {
                        ctx.startActivity(
                            Intent(ctx, QuoteActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    border = BorderStroke(1.dp, GreenAccent),
                ) { Text("Frase", color = GreenAccent) }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "Para ver la frase de verdad sobre el bloqueo, apagá la pantalla y volvé a prenderla.",
                color = TextDim,
                fontSize = 12.sp,
            )
        }
    }
}
