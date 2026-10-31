package co.edu.uniquindio.pokeexplorer.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.edu.uniquindio.pokeexplorer.core.ConnectivityObserver
import co.edu.uniquindio.pokeexplorer.core.toUserMessage
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonPage
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonSummary
import co.edu.uniquindio.pokeexplorer.domain.repository.PokemonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


private const val PAGE_SIZE = 20

data class PokemonListUiState(
    val items: List<PokemonSummary> = emptyList(),
    val query: String = "",
    val types: List<String> = emptyList(),
    val selectedType: String? = null,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val endReached: Boolean = false,
    val isOnline: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PokemonListViewModel @Inject constructor(
    private val repository: PokemonRepository,
    connectivity: ConnectivityObserver
) : ViewModel() {

    private val _uiState = MutableStateFlow(PokemonListUiState())
    val uiState: StateFlow<PokemonListUiState> = _uiState.asStateFlow()
    private var job: Job? = null

    init {
        loadTypes()
        fetch(reset = true)
        viewModelScope.launch {
            connectivity.isOnline.collect { online ->
                val wasOffline = !_uiState.value.isOnline
                _uiState.update { it.copy(isOnline = online) }
                // Al recuperar la conexión se reintenta lo que haya fallado.
                if (online && wasOffline) {
                    if (_uiState.value.types.isEmpty()) loadTypes()
                    if (_uiState.value.items.isEmpty()) fetch(reset = true)
                }
            }
        }
    }

    private fun loadTypes() {
        viewModelScope.launch {
            repository.getTypes().onSuccess { types -> _uiState.update { it.copy(types = types) } }
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    /** Búsqueda por nombre (endpoint /pokemon/{name}). Un texto vacío vuelve a la lista. */
    fun onSearch() {
        _uiState.update { it.copy(selectedType = null) }
        fetch(reset = true)
    }

    /** Filtro por tipo (endpoint /type/{name}). */
    fun onTypeSelected(type: String?) {
        _uiState.update { it.copy(selectedType = type, query = "") }
        fetch(reset = true)
    }

    fun loadMore() = fetch(reset = false)
    fun retry() = fetch(reset = _uiState.value.items.isEmpty())

    private fun fetch(reset: Boolean) {
        val s = _uiState.value
        if (!reset && (s.isLoading || s.isLoadingMore || s.endReached)) return
        job?.cancel()
        job = viewModelScope.launch {
            val offset = if (reset) 0 else s.items.size
            _uiState.update {
                it.copy(
                    items = if (reset) emptyList() else it.items,
                    isLoading = reset, isLoadingMore = !reset,
                    endReached = if (reset) false else it.endReached, error = null
                )
            }
            val type = s.selectedType
            val result = when {
                s.query.isNotBlank() -> repository.searchByName(s.query, offset, PAGE_SIZE)
                type != null -> repository.getByType(type, offset, PAGE_SIZE)
                else -> repository.getPage(offset, PAGE_SIZE)
            }
            result.fold(
                onSuccess = { page ->
                    _uiState.update {
                        it.copy(items = it.items + page.items, endReached = page.endReached,
                            isLoading = false, isLoadingMore = false)
                    }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, isLoadingMore = false, error = e.toUserMessage()) }
                }
            )
        }
    }
}
