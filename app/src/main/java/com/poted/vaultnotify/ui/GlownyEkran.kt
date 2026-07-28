package com.poted.vaultnotify.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.poted.vaultnotify.notifications.intentDoUstawienListenera
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val FORMAT_DATY = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

private fun formatujCzas(czas: Long?): String {
    if (czas == null) return "-"
    return Instant.ofEpochMilli(czas).atZone(ZoneId.systemDefault()).format(FORMAT_DATY)
}

@Composable
fun GlownyEkran(viewModel: GlownyViewModel) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val obserwator = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.odswiezUprawnienie()
        }
        lifecycleOwner.lifecycle.addObserver(obserwator)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obserwator) }
    }

    val uprawnienieWlaczone by viewModel.uprawnienieWlaczone.collectAsState()
    val diagnostykaWlaczona by viewModel.diagnostykaWlaczona.collectAsState()
    val licznikPowiadomien by viewModel.licznikPowiadomien.collectAsState()
    val ostatniePowiadomienieCzas by viewModel.ostatniePowiadomienieCzas.collectAsState()
    val ostatniaSynchronizacjaCzas by viewModel.ostatniaSynchronizacjaCzas.collectAsState()
    val liczbaOczekujacych by viewModel.liczbaOczekujacych.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("vault-notify", style = MaterialTheme.typography.headlineSmall)

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (uprawnienieWlaczone) "Uprawnienie do powiadomien: wlaczone"
                    else "Uprawnienie do powiadomien: WYLACZONE"
                )
                if (!uprawnienieWlaczone) {
                    Button(onClick = { context.startActivity(intentDoUstawienListenera()) }) {
                        Text("Otworz ustawienia")
                    }
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Odebrane powiadomienia z monitorowanych zrodel: $licznikPowiadomien")
                Text("Ostatnie: ${formatujCzas(ostatniePowiadomienieCzas)}")
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Oczekuje na wysylke: $liczbaOczekujacych")
                Text("Ostatnia udana synchronizacja: ${formatujCzas(ostatniaSynchronizacjaCzas)}")
                Button(onClick = { viewModel.uruchomSynchronizacje() }) {
                    Text("Synchronizuj teraz")
                }
            }
        }

        Card {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Tryb diagnostyczny")
                Switch(
                    checked = diagnostykaWlaczona,
                    onCheckedChange = { viewModel.przelaczDiagnostyke(it) }
                )
            }
        }
    }
}
