@file:OptIn(ExperimentalLayoutApi::class)

package com.tareasverde.app

import android.Manifest
import android.app.WallpaperManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.material.icons.outlined.Refresh
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
import androidx.compose.ui.BiasAlignment
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import kotlin.math.roundToInt

// ───────────────────────────── Permisos ─────────────────────────────

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
    val battery = remember(tick) { Perms.batteryUnrestricted(ctx) }
    val exact = remember(tick) { Perms.exactAlarms(ctx) }
    val install = remember(tick) { ctx.packageManager.canRequestPackageInstalls() }

    Panel {
        PermRow(
            title = "Notificaciones",
            desc = "Para el recordatorio diario de tus tareas.",
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
        Spacer(Modifier.height(16.dp))
        PermRow(
            title = "Batería sin restricciones",
            desc = "Evita que el celular cierre la app y se pierdan avisos y cambios de frase.",
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

// ───────────────────── Frase fija: vista previa y aspecto ─────────────────────

/** Simula la pantalla de bloqueo (con reloj) para ver dónde queda la frase. */
@Composable
private fun LockPreview(quote: Quote, size: Int, color: Int, shadow: Boolean, yPercent: Int, bgMode: Int) {
    val textShadow = if (shadow) Shadow(Color.Black, Offset(0f, 2f), 8f) else null
    val bias = (yPercent / 100f) * 2f - 1f
    val backdrop = when (bgMode) {
        1 -> Brush.verticalGradient(listOf(Color(0xFF000000), Color(0xFF04150C), Color(0xFF0F5A33)))
        2 -> Brush.verticalGradient(listOf(Color(0xFF2B3A4A), Color(0xFF6C8EA4)))
        else -> Brush.verticalGradient(listOf(Color.Black, Color.Black))
    }
    Box(
        Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backdrop)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(Modifier.align(Alignment.TopCenter), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sábado 14", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            Text("14:27", color = Color.White, fontSize = 54.sp, fontWeight = FontWeight.Light)
        }
        Column(
            Modifier.align(BiasAlignment(0f, bias)).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                "“${quote.text}”",
                color = Color(color),
                fontSize = (size * 0.7f).sp,
                lineHeight = (size * 0.7f * 1.15f).sp,
                textAlign = TextAlign.Center,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis,
                style = TextStyle(shadow = textShadow),
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "— ${quote.author}",
                color = Color(color),
                fontSize = (size * 0.7f * 0.55f).coerceAtLeast(10f).sp,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = textShadow),
            )
        }
    }
}

@Composable
private fun LockQuotePanel(current: Quote, onCurrent: (Quote) -> Unit) {
    val ctx = LocalContext.current
    var size by remember { mutableIntStateOf(AppPrefs.quoteSize(ctx)) }
    var color by remember { mutableIntStateOf(AppPrefs.quoteColor(ctx)) }
    var shadow by remember { mutableStateOf(AppPrefs.quoteShadow(ctx)) }
    var y by remember { mutableIntStateOf(AppPrefs.quoteY(ctx)) }
    var bgMode by remember { mutableIntStateOf(AppPrefs.bgMode(ctx)) }

    val swatches = listOf(
        Color.White, GreenAccent, Color(0xFFFFEB3B), Color(0xFF40C4FF),
        Color(0xFFFF80AB), Color(0xFFFFAB40), Color.Black,
    )

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            Thread {
                val ok = WallpaperBg.saveFrom(ctx, uri)
                Handler(Looper.getMainLooper()).post {
                    if (ok) {
                        AppPrefs.setBgMode(ctx, 2)
                        bgMode = 2
                    } else {
                        Toast.makeText(ctx, "No se pudo usar esa foto, probá con otra", Toast.LENGTH_LONG).show()
                    }
                }
            }.start()
        }
    }

    val sliderColors = SliderDefaults.colors(
        thumbColor = GreenAccent,
        activeTrackColor = GreenAccent,
        inactiveTrackColor = CardHi,
    )

    Panel {
        Text("Tu frase en el bloqueo", color = TextMain, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Queda fija bajo el reloj, como un widget, y cambia sola. Así se verá (vista previa):",
            color = TextDim,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(14.dp))
        LockPreview(current, size, color, shadow, y, bgMode)
        Spacer(Modifier.height(6.dp))
        OutlinedButton(
            onClick = {
                onCurrent(QuoteRotator.rotate(ctx))
                QuoteWidget.refreshAll(ctx)
            },
            border = BorderStroke(1.dp, GreenAccent),
        ) {
            Icon(Icons.Outlined.Refresh, contentDescription = null, tint = GreenAccent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cambiar frase ahora", color = GreenAccent)
        }

        Spacer(Modifier.height(18.dp))
        Text("Tamaño del texto: $size", color = TextDim, fontSize = 12.sp)
        Slider(
            value = size.toFloat(),
            onValueChange = {
                size = it.roundToInt()
                AppPrefs.setQuoteSize(ctx, size)
            },
            onValueChangeFinished = { QuoteWidget.refreshAll(ctx) },
            valueRange = 16f..56f,
            colors = sliderColors,
        )

        Text("Altura en la pantalla: $y%", color = TextDim, fontSize = 12.sp)
        Slider(
            value = y.toFloat(),
            onValueChange = {
                y = it.roundToInt()
                AppPrefs.setQuoteY(ctx, y)
            },
            valueRange = 10f..90f,
            colors = sliderColors,
        )
        Text(
            "Bajala hasta que quede justo debajo del reloj de tu celular.",
            color = TextDim,
            fontSize = 11.sp,
        )

        Spacer(Modifier.height(14.dp))
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
                            QuoteWidget.refreshAll(ctx)
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
        Text("Fondo del fondo animado", color = TextDim, fontSize = 12.sp)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GreenChip("Negro", bgMode == 0) {
                bgMode = 0
                AppPrefs.setBgMode(ctx, 0)
            }
            GreenChip("Degradado verde", bgMode == 1) {
                bgMode = 1
                AppPrefs.setBgMode(ctx, 1)
            }
            GreenChip("Mi foto", bgMode == 2) {
                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        }
    }
}

// ───────────────────── Cómo mostrarla y cada cuánto cambia ─────────────────────

@Composable
private fun HowToShowPanel() {
    val ctx = LocalContext.current

    Panel {
        Text("1 · Fondo de pantalla con la frase", color = TextMain, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "La frase se dibuja fija en el fondo, justo como el reloj: no hay que cerrarla ni esperar. " +
                "Al activarlo, el celular te pregunta dónde usarlo: elegí \"Pantalla de bloqueo\" " +
                "(si tu celular no deja separarlo, se verá también en la de inicio).",
            color = TextDim,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(ctx, QuoteWallpaperService::class.java),
                )
                if (!Perms.open(ctx, intent)) {
                    if (!Perms.open(ctx, Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER))) {
                        Toast.makeText(
                            ctx,
                            "Tu celular no permite fondos animados desde acá",
                            Toast.LENGTH_LONG,
                        ).show()
                    }
                }
            },
            border = BorderStroke(1.dp, GreenAccent),
        ) { Text("Usar como fondo de pantalla", color = GreenAccent) }

        Spacer(Modifier.height(22.dp))
        Text("2 · Widget", color = TextMain, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "Frase sin fondo para poner donde quieras en tu pantalla de inicio. En los celulares con " +
                "Android 16 o más nuevo que permiten widgets en el bloqueo, también podés ponerlo ahí " +
                "desde \"Personalizar pantalla de bloqueo\". Tocarlo abre la app.",
            color = TextDim,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = {
                val mgr = AppWidgetManager.getInstance(ctx)
                if (mgr.isRequestPinAppWidgetSupported) {
                    mgr.requestPinAppWidget(ComponentName(ctx, QuoteWidget::class.java), null, null)
                } else {
                    Toast.makeText(
                        ctx,
                        "Mantené apretada la pantalla de inicio > Widgets > GO GO PIDIO",
                        Toast.LENGTH_LONG,
                    ).show()
                }
            },
            border = BorderStroke(1.dp, GreenAccent),
        ) { Text("Agregar widget", color = GreenAccent) }
    }
}

@Composable
private fun RotationPanel(onCurrent: (Quote) -> Unit) {
    val ctx = LocalContext.current
    var minutes by remember { mutableIntStateOf(AppPrefs.rotateMinutes(ctx)) }
    var filter by remember { mutableIntStateOf(AppPrefs.quoteFilter(ctx)) }

    val intervals = listOf(
        0 to "Al prender", 15 to "15 min", 30 to "30 min", 60 to "1 hora",
        180 to "3 horas", 360 to "6 horas", 1440 to "1 día",
    )
    val filters = listOf("Todas", "Motivacionales", "Reflexivas", "Religiosas")

    Panel {
        Text("Cada cuánto cambia", color = TextMain, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "\"Al prender\" cambia la frase cada vez que prendés la pantalla del celular.",
            color = TextDim,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            intervals.forEach { (m, label) ->
                GreenChip(label, minutes == m) {
                    minutes = m
                    AppPrefs.setRotateMinutes(ctx, m)
                    QuoteWidget.scheduleAlarm(ctx)
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Text("Qué frases mostrar", color = TextMain, fontSize = 16.sp)
        Spacer(Modifier.height(10.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.forEachIndexed { i, label ->
                GreenChip(label, filter == i) {
                    filter = i
                    AppPrefs.setQuoteFilter(ctx, i)
                    // si la frase actual no es del tipo elegido, elige otra que sí lo sea
                    onCurrent(QuoteRotator.current(ctx))
                    QuoteWidget.refreshAll(ctx)
                }
            }
        }
    }
}

// ───────────────────────────── Actualizaciones ─────────────────────────────

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

// ───────────────────────────── Pantalla de ajustes ─────────────────────────────

@Composable
fun SettingsScreen() {
    val ctx = LocalContext.current
    var hour by remember { mutableIntStateOf(AppPrefs.reminderHour(ctx)) }
    var current by remember { mutableStateOf(QuoteRotator.current(ctx)) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Brand(18.sp)
        Text("Ajustes", color = TextMain, fontSize = 32.sp, fontWeight = FontWeight.Bold)

        LockQuotePanel(current) { current = it }
        HowToShowPanel()
        RotationPanel { current = it }

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
            Spacer(Modifier.height(12.dp))
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
            ) { Text("Probar recordatorio", color = GreenAccent) }
        }

        Text("Permisos", color = TextDim, fontSize = 13.sp)
        PermissionsPanel()

        UpdatesPanel()
    }
}
