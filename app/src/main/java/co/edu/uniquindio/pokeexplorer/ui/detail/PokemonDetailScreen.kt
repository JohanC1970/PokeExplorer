package co.edu.uniquindio.pokeexplorer.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonDetail
import co.edu.uniquindio.pokeexplorer.ui.components.ConnectionBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonDetailScreen(onBack: () -> Unit, viewModel: PokemonDetailViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(topBar = {
        Column {
            TopAppBar(
                title = { Text(state.pokemon?.name?.replaceFirstChar { it.uppercase() } ?: "Detalle") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") }
                }
            )
            ConnectionBanner(state.isOnline)
        }
    }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val pokemon = state.pokemon
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                pokemon == null -> Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error ?: "Error")
                    Button(onClick = viewModel::load) { Text("Reintentar") }
                }
                else -> DetailContent(pokemon)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(p: PokemonDetail) {
    Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AsyncImage(model = p.imageUrl, contentDescription = p.name, modifier = Modifier.fillMaxWidth().height(220.dp))
        Text("#${p.id.toString().padStart(4, '0')}", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            p.types.forEach { SuggestionChip(onClick = {}, label = { Text(it) }) }
        }
        if (p.description.isNotBlank()) Text(p.description, style = MaterialTheme.typography.bodyLarge)
        Text("Altura: ${p.heightMeters} m  ·  Peso: ${p.weightKg} kg", style = MaterialTheme.typography.bodyMedium)
        Text("Habilidades", style = MaterialTheme.typography.titleMedium)
        Text(p.abilities.joinToString(", "))
        Text("Estadísticas base", style = MaterialTheme.typography.titleMedium)
        p.stats.forEach { s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(s.name, modifier = Modifier.width(120.dp), style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(progress = { (s.value / 255f).coerceIn(0f, 1f) }, modifier = Modifier.weight(1f).height(8.dp))
                Text("  ${s.value}", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}