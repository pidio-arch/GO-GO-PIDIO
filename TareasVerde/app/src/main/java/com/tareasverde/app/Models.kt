package com.tareasverde.app

import java.util.UUID

enum class Category(val label: String) {
    DIARIA("Diaria"),
    IMPORTANTE("Importante"),
    NEUTRA("Neutra"),
    INNECESARIA("Innecesaria")
}

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val category: Category = Category.NEUTRA,
    /** Fecha límite como "epoch day" (días desde 1970-01-01), o null si no tiene. */
    val dueEpochDay: Long? = null,
    val done: Boolean = false,
    /** Día en que se tachó; sirve para reiniciar las tareas "Diaria" cada día. */
    val doneEpochDay: Long? = null,
)

/** Texto corto sobre el vencimiento: "vence hoy", "vence en 3 días", "venció ayer"... */
fun dueLabel(due: Long?, today: Long): String {
    if (due == null) return ""
    val diff = due - today
    return when {
        diff < -1L -> "venció hace ${-diff} días"
        diff == -1L -> "venció ayer"
        diff == 0L -> "vence hoy"
        diff == 1L -> "vence mañana"
        else -> "vence en $diff días"
    }
}
