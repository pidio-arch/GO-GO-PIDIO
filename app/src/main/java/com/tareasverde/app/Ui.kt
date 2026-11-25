package com.tareasverde.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun App() {
    val ctx = LocalContext.current
    var onboarding by remember { mutableStateOf(!AppPrefs.onboardingDone(ctx)) }
    var tab by remember { mutableIntStateOf(0) }
    val tasks = remember { mutableStateListOf<Task>().apply { addAll(TaskStore.load(ctx)) } }

    // Al abrir la app busca si hay una versión nueva y, si la hay, muestra el aviso.
    LaunchedEffect(Unit) { checkForUpdate(ctx, manual = false) }
    UpdateDialog()

    if (onboarding) {
        OnboardingScreen {
            AppPrefs.setOnboardingDone(ctx, true)
            onboarding = false
        }
    } else {
        Scaffold(
            containerColor = BgBlack,
            bottomBar = {
                NavigationBar(containerColor = BgBlack) {
                    val items = listOf(
                        "Tareas" to Icons.Outlined.CheckCircle,
                        "Frases" to Icons.Outlined.FormatQuote,
                        "Ajustes" to Icons.Outlined.Settings,
                    )
                    items.forEachIndexed { i, (label, icon) ->
                        NavigationBarItem(
                            selected = tab == i,
                            onClick = { tab = i },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GreenAccent,
                                selectedTextColor = GreenAccent,
                                unselectedIconColor = TextDim,
                                unselectedTextColor = TextDim,
                                indicatorColor = GreenDeep,
                            ),
                        )
                    }
                }
            },
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) {
                when (tab) {
                    0 -> TasksScreen(tasks) { TaskStore.save(ctx, tasks.toList()) }
                    1 -> QuotesScreen()
                    else -> SettingsScreen()
                }
            }
        }
    }
}

/** Primera pantalla: explica y pide los permisos que necesitan las frases y los recordatorios. */
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(BgBlack)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Spacer(Modifier.height(24.dp))
        Brand(36.sp)
        Text(
            "Para avisarte a tiempo de tus tareas y mantener la app actualizada necesito algunos " +
                "permisos. Podés cambiarlos después en Ajustes.",
            color = TextDim,
            fontSize = 15.sp,
        )
        PermissionsPanel()
        Button(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = GreenAccent, contentColor = BgBlack),
        ) {
            Text("Continuar")
        }
    }
}
