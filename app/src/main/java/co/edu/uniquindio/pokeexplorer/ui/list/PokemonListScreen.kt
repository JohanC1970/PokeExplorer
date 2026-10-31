package co.edu.uniquindio.pokeexplorer.ui.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonSummary
import co.edu.uniquindio.pokeexplorer.ui.components.ConnectionBanner
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun PokemonListScreen(
    onPokemonClick: (Int) -> Unit,
    viewModel: PokemonListViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Scroll infinito: al acercarse a los últimos 3 elementos se pide la siguiente página.
    LaunchedEffect(listState) {
        snapshotFlow {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 3
        }.distinctUntilChanged().collect { nearEnd -> if (nearEnd) viewModel.loadMore() }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding()) {
        ConnectionBanner(state.isOnline)

        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::onQueryChange,
            placeholder = { Text("Buscar por nombre (ej. pi)") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { viewModel.onSearch() }),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(selected = state.selectedType == null && state.query.isBlank(),
                    onClick = { viewModel.onTypeSelected(null) }, label = { Text("Todos") })
            }
            items(state.types) { type ->
                FilterChip(selected = state.selectedType == type,
                    onClick = { viewModel.onTypeSelected(type) }, label = { Text(type.replaceFirstChar { it.uppercase() }) })
            }
        }

        Box(Modifier.fillMaxSize()) {
            val error = state.error
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.items.isEmpty() && error != null -> Column(
                    Modifier.align(Alignment.Center).padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::retry) { Text("Reintentar") }
                }
                state.items.isEmpty() && state.endReached -> Text(
                    "No hay Pokémon que empiecen por \"${state.query}\"",
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.items, key = { it.id }) { PokemonRow(it) { onPokemonClick(it.id) } }
                    if (state.isLoadingMore) item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) { CircularProgressIndicator() }
                    }
                    if (error != null) item {
                        TextButton(onClick = viewModel::retry, modifier = Modifier.fillMaxWidth()) {
                            Text("$error · Reintentar")
                        }
                    }
                }
            }
        }
    }
}



@Composable
private fun PokemonRow(pokemon: PokemonSummary, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(model = pokemon.imageUrl, contentDescription = pokemon.name, modifier = Modifier.size(72.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text("#${pokemon.id.toString().padStart(4, '0')}", style = MaterialTheme.typography.labelMedium)
                Text(pokemon.name.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
