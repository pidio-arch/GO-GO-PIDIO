package com.tareasverde.app

enum class QuoteType { MOTIVACIONAL, RELIGIOSA }

data class Quote(val text: String, val author: String, val type: QuoteType)

/** Podés editar, borrar o agregar frases en esta lista. */
object Quotes {
    val all: List<Quote> = listOf(
        // --- Motivacionales ---
        Quote("No es que tengamos poco tiempo, sino que perdemos mucho.", "Séneca", QuoteType.MOTIVACIONAL),
        Quote("Un viaje de mil millas comienza con un solo paso.", "Lao Tsé", QuoteType.MOTIVACIONAL),
        Quote("Lo que se interpone en el camino se convierte en el camino.", "Marco Aurelio", QuoteType.MOTIVACIONAL),
        Quote("Empieza donde estás. Usa lo que tienes. Haz lo que puedas.", "Arthur Ashe", QuoteType.MOTIVACIONAL),
        Quote("El éxito es la suma de pequeños esfuerzos repetidos día tras día.", "Robert Collier", QuoteType.MOTIVACIONAL),
        Quote("Somos lo que hacemos repetidamente. La excelencia no es un acto, sino un hábito.", "Will Durant", QuoteType.MOTIVACIONAL),
        Quote("Quien tiene un porqué para vivir puede soportar casi cualquier cómo.", "Friedrich Nietzsche", QuoteType.MOTIVACIONAL),
        Quote("Caer siete veces, levantarse ocho.", "Proverbio japonés", QuoteType.MOTIVACIONAL),
        Quote("Quien quiere hacer algo encuentra un medio; quien no quiere, una excusa.", "Proverbio árabe", QuoteType.MOTIVACIONAL),
        Quote("No cuentes los días, haz que los días cuenten.", "Muhammad Ali", QuoteType.MOTIVACIONAL),
        Quote("La disciplina es el puente entre las metas y los logros.", "Jim Rohn", QuoteType.MOTIVACIONAL),
        Quote("Lo que haces hoy puede mejorar todos tus mañanas.", "Ralph Marston", QuoteType.MOTIVACIONAL),
        Quote("Hacer es la mejor forma de empezar a pensar con claridad: da el primer paso hoy.", "Anónimo", QuoteType.MOTIVACIONAL),
        Quote("Una tarea chica terminada vale más que una gran idea postergada.", "Anónimo", QuoteType.MOTIVACIONAL),
        Quote("Hoy no tiene que ser perfecto, solo tiene que ser avanzado.", "Anónimo", QuoteType.MOTIVACIONAL),

        // --- Religiosas ---
        Quote("Todo lo puedo en Cristo que me fortalece.", "Filipenses 4:13", QuoteType.RELIGIOSA),
        Quote("Esfuérzate y sé valiente; no temas ni desmayes.", "Josué 1:9", QuoteType.RELIGIOSA),
        Quote("El Señor es mi pastor; nada me faltará.", "Salmo 23:1", QuoteType.RELIGIOSA),
        Quote("El Señor es mi luz y mi salvación; ¿de quién temeré?", "Salmo 27:1", QuoteType.RELIGIOSA),
        Quote("Todo tiene su tiempo, y todo lo que se quiere debajo del cielo tiene su hora.", "Eclesiastés 3:1", QuoteType.RELIGIOSA),
        Quote("Confía en el Señor con todo tu corazón.", "Proverbios 3:5", QuoteType.RELIGIOSA),
        Quote("Si Dios es por nosotros, ¿quién contra nosotros?", "Romanos 8:31", QuoteType.RELIGIOSA),
        Quote("Pedid, y se os dará; buscad, y hallaréis.", "Mateo 7:7", QuoteType.RELIGIOSA),
        Quote("Basta a cada día su propio afán.", "Mateo 6:34", QuoteType.RELIGIOSA),
        Quote("Venid a mí todos los que estáis cansados y cargados, y yo os haré descansar.", "Mateo 11:28", QuoteType.RELIGIOSA),
        Quote("Todo lo que hagáis, hacedlo de corazón, como para el Señor.", "Colosenses 3:23", QuoteType.RELIGIOSA),
        Quote("Dios, concédeme serenidad para aceptar lo que no puedo cambiar, valor para cambiar lo que sí puedo y sabiduría para distinguir la diferencia.", "Oración de la serenidad", QuoteType.RELIGIOSA),
        Quote("No todos podemos hacer grandes cosas, pero sí pequeñas cosas con gran amor.", "Madre Teresa de Calcuta", QuoteType.RELIGIOSA),
        Quote("Empieza haciendo lo necesario, después lo posible, y de repente estarás haciendo lo imposible.", "Atribuida a San Francisco de Asís", QuoteType.RELIGIOSA),
    )

    fun random(type: QuoteType? = null): Quote {
        val pool = if (type == null) all else all.filter { it.type == type }
        return pool.random()
    }
}
