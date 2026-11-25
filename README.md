# GO GO PIDIO — checklist minimalista (negro y verde)

App Android nativa (Kotlin + Jetpack Compose). Las tareas se guardan en el celular, sin cuentas.

## Qué hace
- **Checklist** con categorías (Diaria, Importante, Neutra, Innecesaria), fecha límite, editar, borrar y "Limpiar tachadas".
- **Recordatorio diario** a la hora que elijas, con las tareas con fecha límite y las diarias sin tachar.
- **Frase fija bajo el reloj**, como un widget: no aparece ni hay que cerrarla; queda ahí y **cambia sola**. Más de 140 frases motivacionales, reflexivas y religiosas.
- **Actualizaciones desde adentro de la app** (ver abajo).

## Frase fija: tres formas de usarla (Ajustes)
1. **Fondo de pantalla con la frase** (la que más se parece a un widget de bloqueo): "Usar como fondo de pantalla" y el celular pregunta dónde ponerlo; elegí *Pantalla de bloqueo*. Fondo a elección: negro, degradado verde o **tu propia foto**.
   - Si tu celular no permite un fondo animado solo en el bloqueo (Android de Google, por ejemplo, suele aplicarlo a ambos), la frase se verá también en la pantalla de inicio.
2. **Widget** sin fondo: botón "Agregar widget" (o mantené apretada la pantalla de inicio > Widgets > GO GO PIDIO). En celulares con Android 16 o más nuevo que traen widgets de bloqueo, también se puede poner en "Personalizar pantalla de bloqueo".
3. Las dos usan la misma frase.

Se personaliza: **tamaño, color, sombra, altura en pantalla** (para ponerla justo debajo del reloj de tu celular) y la **vista previa** te muestra cómo queda.

**Cada cuánto cambia:** al prender la pantalla, 15 min, 30 min, 1 h, 3 h, 6 h o 1 día. También elegís qué tipo de frases: todas, motivacionales, reflexivas o religiosas.

## Permisos (la app los pide al inicio y se cambian en Ajustes)
Notificaciones (recordatorio) · Batería sin restricciones · Alarmas exactas · Instalar actualizaciones.
La frase fija ya **no necesita** permisos especiales.

## Actualizaciones
1. El código vive en un repositorio **público** de GitHub.
2. Cada cambio subido a la rama `main` hace que GitHub compile el APK solo (`.github/workflows/build.yml`) y lo publique como "Release".
3. La app, al abrirse (o con el botón de Ajustes), consulta ese Release y avisa si hay versión nueva. Tocás *Actualizar* y se instala encima sin perder tareas.

En **Ajustes > Actualizaciones** hay que escribir `tuusuario/tu-repo`.

> Todas las versiones se firman con la misma clave (`app/gogo.keystore`), por eso se actualizan encima. Como el repositorio es público, esa clave también: sirve para una app personal, no para publicar en Play Store.

## Compilar en tu PC (opcional)
Android Studio: `File > Open` en esta carpeta, y `Build > Build APK(s)`.

## Personalizar
- Frases: `app/src/main/java/com/tareasverde/app/Quotes.kt`
- Colores de la app: `Theme.kt`
- Ícono: `app/src/main/res/drawable-nodpi/ic_launcher_foreground.png`
