package co.edu.uniquindio.pokeexplorer.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Franja de estado de red: verde "En línea" o roja "Sin conexión". */
@Composable
fun ConnectionBanner(isOnline: Boolean, modifier: Modifier = Modifier) {
    Surface(
        color = if (isOnline) Color(0xFF1F7A4D) else MaterialTheme.colorScheme.error,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = if (isOnline) "● En línea" else "● Sin conexión (offline)",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}