@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.tareasverde.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val esAR: Locale = Locale.forLanguageTag("es-AR")

private fun todayText(): String =
    LocalDate.now()
        .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", esAR))
        .replaceFirstChar { it.uppercase() }

private fun formatDate(epochDay: Long): String =
    LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern("EEE d MMM yyyy", esAR))

private fun Category.color(): Color = when (this) {
    Category.DIARIA -> GreenAccent
    Category.IMPORTANTE -> Danger
    Category.NEUTRA -> Color(0xFF9E9E9E)
    Category.INNECESARIA -> Color(0xFF4A4A4A)
}

@Composable
fun TasksScreen(tasks: SnapshotStateList<Task>, onChange: () -> Unit) {
    val today = LocalDate.now().toEpochDay()
    var filter by remember { mutableStateOf<Category?>(null) }
    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Task?>(null) }

    val visible = tasks
        .filter { filter == null || it.category == filter }
        .sortedWith(compareBy<Task> { it.done }.thenBy { it.dueEpochDay ?: Long.MAX_VALUE })
    val doneCount = tasks.count { it.done }
    val fraction = if (tasks.isEmpty()) 0f else doneCount.toFloat() / tasks.size
    val clearable = tasks.any { it.done && it.category != Category.DIARIA }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Column {
                    Brand(18.sp)
                    Spacer(Modifier.height(10.dp))
                    Text(todayText(), color = TextDim, fontSize = 14.sp)
                    Text("Mis tareas", color = TextMain, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "$doneCount de ${tasks.size} completadas",
                            color = GreenAccent,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                        )
                        if (clearable) {
                            TextButton(onClick = {
                                tasks.removeAll { it.done && it.category != Category.DIARIA }
                                onChange()
                            }) { Text("Limpiar tachadas", color = TextDim, fontSize = 12.sp) }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { fraction },
                        color = GreenAccent,
                        trackColor = CardHi,
                        modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    )
                    Spacer(Modifier.height(16.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GreenChip("Todas", filter == null) { filter = null }
                        Category.entries.forEach { c ->
                            GreenChip(c.label, filter == c) { filter = c }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }

            if (visible.isEmpty()) {
                item {
                    Text(
                        "No hay tareas acá. Tocá + para agregar una.",
                        color = TextDim,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 24.dp),
                    )
                }
            }

            items(visible, key = { it.id }) { t ->
                TaskRow(
                    task = t,
                    today = today,
                    onToggle = {
                        val i = tasks.indexOfFirst { it.id == t.id }
                        if (i >= 0) {
                            val nowDone = !t.done
                            tasks[i] = t.copy(done = nowDone, doneEpochDay = if (nowDone) today else null)
                            onChange()
                        }
                    },
                    onEdit = { editing = t },
                    onDelete = {
                        tasks.removeAll { it.id == t.id }
                        onChange()
                    },
                )
            }
        }

        FloatingActionButton(
            onClick = { showAdd = true },
            containerColor = GreenAccent,
            contentColor = BgBlack,
            modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Agregar tarea")
        }
    }

    val target = editing
    if (showAdd || target != null) {
        TaskDialog(
            initial = target,
            onDismiss = {
                showAdd = false
                editing = null
            },
            onSave = { title, cat, due ->
                if (target != null) {
                    val i = tasks.indexOfFirst { it.id == target.id }
                    if (i >= 0) {
                        tasks[i] = tasks[i].copy(title = title, category = cat, dueEpochDay = due)
                    }
                } else {
                    tasks.add(Task(title = title, category = cat, dueEpochDay = due))
                }
                onChange()
                showAdd = false
                editing = null
            },
        )
    }
}

@Composable
private fun CheckCircle(done: Boolean) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(if (done) GreenAccent else Color.Transparent)
            .border(2.dp, if (done) GreenAccent else Color(0xFF3A3A3A), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (done) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = BgBlack, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    today: Long,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val overdue = task.dueEpochDay != null && task.dueEpochDay < today && !task.done
    val due = dueLabel(task.dueEpochDay, today)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardDark)
            .clickable(onClick = onToggle)
            .padding(start = 14.dp, end = 0.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckCircle(task.done)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(
                task.title,
                color = if (task.done) TextDim else TextMain,
                fontSize = 16.sp,
                textDecoration = if (task.done) TextDecoration.LineThrough else null,
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(task.category.color()))
                Spacer(Modifier.width(6.dp))
                Text(task.category.label, color = TextDim, fontSize = 12.sp)
                if (due.isNotEmpty()) {
                    Text("  ·  ", color = TextDim, fontSize = 12.sp)
                    Text(due, color = if (overdue) Danger else TextDim, fontSize = 12.sp)
                }
            }
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Outlined.Edit, contentDescription = "Editar", tint = TextDim)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = "Eliminar", tint = TextDim)
        }
    }
}

/** Sirve para crear (initial == null) y para editar una tarea. */
@Composable
fun TaskDialog(
    initial: Task?,
    onDismiss: () -> Unit,
    onSave: (String, Category, Long?) -> Unit,
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var cat by remember { mutableStateOf(initial?.category ?: Category.NEUTRA) }
    var due by remember { mutableStateOf(initial?.dueEpochDay) }
    var showPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = { Text(if (initial == null) "Nueva tarea" else "Editar tarea", color = TextMain) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("¿Qué tenés que hacer?") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GreenAccent,
                        unfocusedBorderColor = LineDim,
                        cursorColor = GreenAccent,
                    ),
                )
                Text("Categoría", color = TextDim, fontSize = 12.sp)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { c ->
                        GreenChip(c.label, cat == c) { cat = c }
                    }
                }
                Text("Fecha límite", color = TextDim, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { showPicker = true },
                        border = BorderStroke(1.dp, LineDim),
                    ) {
                        Icon(
                            Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(due?.let { formatDate(it) } ?: "Sin fecha límite", color = TextMain)
                    }
                    if (due != null) {
                        IconButton(onClick = { due = null }) {
                            Icon(Icons.Outlined.Close, contentDescription = "Quitar fecha", tint = TextDim)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank(),
                onClick = { onSave(title.trim(), cat, due) },
            ) {
                Text(
                    if (initial == null) "Agregar" else "Guardar",
                    color = if (title.isNotBlank()) GreenAccent else TextDim,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = TextDim) }
        },
    )

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = (due ?: LocalDate.now().toEpochDay()) * 86_400_000L
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { due = Math.floorDiv(it, 86_400_000L) }
                    showPicker = false
                }) { Text("Aceptar", color = GreenAccent) }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancelar", color = TextDim) }
            },
            colors = DatePickerDefaults.colors(containerColor = CardHi),
        ) {
            DatePicker(state = state, colors = DatePickerDefaults.colors(containerColor = CardHi))
        }
    }
}
