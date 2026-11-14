# GO GO PIDIO — checklist minimalista (negro y verde)

App Android nativa (Kotlin + Jetpack Compose). Sin cuentas ni servidores propios: las tareas se guardan en el celular.

## Qué hace
- **Checklist** con categorías (Diaria, Importante, Neutra, Innecesaria), fecha límite, editar, borrar y "Limpiar tachadas".
- **Recordatorio diario** a la hora que elijas, con las tareas con fecha límite y las diarias sin tachar.
- **Frase en la pantalla de bloqueo**: cada vez que prendés la pantalla aparece solo el texto, sin fondo, sobre tu fondo de pantalla. En **Ajustes** cambiás tamaño, color, sombra, posición y cuántos segundos queda. Se cierra tocándola.
- **Actualizaciones desde adentro de la app** (ver abajo).

## Permisos (la app los pide al inicio y se cambian en Ajustes)
Notificaciones · Pantalla completa en el bloqueo · Mostrar sobre otras apps · Batería sin restricciones · Alarmas exactas · Instalar actualizaciones.

## Actualizaciones: cómo funciona
1. El código vive en un repositorio **público** de GitHub.
2. Cada vez que se sube un cambio a la rama `main`, **GitHub compila el APK solo** (archivo `.github/workflows/build.yml`) y lo publica como "Release".
3. La app instalada, al abrirse (o con el botón de Ajustes), consulta ese Release. Si hay versión nueva muestra **"Actualización disponible"**; tocás *Actualizar*, descarga e instala encima **sin perder tus tareas**.

### Puesta en marcha (una sola vez)
1. Creá una cuenta en https://github.com y un repositorio **público** (ej. `gogo-pidio`).
2. Subí todo el contenido de esta carpeta al repositorio, en la rama `main` (incluida la carpeta oculta `.github`). En la web: *Add file > Upload files* y arrastrá todo.
3. En el repo, pestaña **Actions**: esperá a que termine "Compilar y publicar GO GO PIDIO" (unos 5–10 min). Si te pide habilitar los workflows, aceptá.
4. Entrá desde el **celular** a `https://github.com/TU_USUARIO/gogo-pidio/releases`, descargá `gogo-pidio.apk` e instalalo (Android pide permitir instalar desde el navegador).
5. Abrí la app, andá a **Ajustes > Actualizaciones** y escribí `TU_USUARIO/gogo-pidio`.

### Cada vez que quieras cambiar algo
Se modifica el código, se sube a `main` y en unos minutos aparece el aviso de actualización en el celular.

> **Importante:** la primera vez que instalás esta versión, **desinstalá la anterior** (estaba firmada con otra clave). Desde ahí todas las versiones usan la misma firma (`app/gogo.keystore`) y se actualizan encima sin borrar nada. Como el repositorio es público, esa clave también lo es; está bien para una app personal, pero no la uses para publicar en Play Store.

## Compilar en tu PC (opcional)
Android Studio: `File > Open` en esta carpeta, y `Build > Build APK(s)`. Para usar actualizaciones, instalá siempre las versiones que publica GitHub (las compiladas localmente tienen número de versión 1 y no pueden reemplazar a las de GitHub).

## Limitaciones
- La frase se muestra como una pantalla transparente sobre el bloqueo. Según la marca del celular puede tapar el reloj del sistema mientras está visible, y algunas marcas (Xiaomi, Huawei, Samsung) necesitan además permitir "pantalla completa" o "mostrar en pantalla de bloqueo" en los ajustes de la app. No se puede mostrar texto *dentro* del propio bloqueo del sistema sin ser una pantalla encima.
- Para que se vea al prender el celu, la app mantiene un aviso chico permanente ("frases activas") y conviene dejar la batería sin restricciones.

## Personalizar
- Frases: `app/src/main/java/com/tareasverde/app/Quotes.kt`
- Colores de la app: `Theme.kt`
- Ícono: `app/src/main/res/drawable-nodpi/ic_launcher_foreground.png`
