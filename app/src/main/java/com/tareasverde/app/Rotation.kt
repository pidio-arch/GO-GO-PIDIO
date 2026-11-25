package com.tareasverde.app

import android.content.Context

/**
 * Decide qué frase se muestra y cuándo cambia. La comparten el fondo de bloqueo y el widget,
 * así los dos muestran siempre la misma.
 */
object QuoteRotator {
    private fun state(ctx: Context) =
        ctx.getSharedPreferences("tareas_quote_state", Context.MODE_PRIVATE)

    /** Posiciones (en Quotes.all) de las frases permitidas según el tipo elegido en Ajustes. */
    private fun pool(ctx: Context): List<Int> {
        val type = when (AppPrefs.quoteFilter(ctx)) {
            1 -> QuoteType.MOTIVACIONAL
            2 -> QuoteType.REFLEXIVA
            3 -> QuoteType.RELIGIOSA
            else -> null
        }
        val idx = Quotes.all.indices.filter { type == null || Quotes.all[it].type == type }
        return idx.ifEmpty { Quotes.all.indices.toList() }
    }

    fun current(ctx: Context): Quote {
        val idx = state(ctx).getInt("idx", -1)
        return if (idx in pool(ctx)) Quotes.all[idx] else rotate(ctx)
    }

    /** Elige otra frase distinta de la actual. */
    fun rotate(ctx: Context): Quote {
        val pool = pool(ctx)
        val old = state(ctx).getInt("idx", -1)
        val candidates = if (pool.size > 1) pool.filter { it != old } else pool
        val n = candidates.random()
        state(ctx).edit().putInt("idx", n).putLong("at", System.currentTimeMillis()).apply()
        return Quotes.all[n]
    }

    /**
     * Cambia la frase solo si ya pasó el tiempo elegido (o, con "Al prender", cuando se
     * prende la pantalla). grace < 1 adelanta un poco el límite para alarmas inexactas.
     */
    fun rotateIfDue(ctx: Context, onScreenOn: Boolean = false, grace: Double = 1.0): Quote {
        val mins = AppPrefs.rotateMinutes(ctx)
        val last = state(ctx).getLong("at", 0L)
        val due = if (mins == 0) {
            onScreenOn
        } else {
            System.currentTimeMillis() - last >= (mins * 60_000L * grace).toLong()
        }
        return if (due || last == 0L) rotate(ctx) else current(ctx)
    }
}
