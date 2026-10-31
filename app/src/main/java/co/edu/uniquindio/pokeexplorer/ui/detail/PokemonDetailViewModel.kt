package co.edu.uniquindio.pokeexplorer.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import co.edu.uniquindio.pokeexplorer.core.toUserMessage
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonDetail
import co.edu.uniquindio.pokeexplorer.domain.repository.PokemonRepository
import co.edu.uniquindio.pokeexplorer.ui.navigation.DetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val pokemon: PokemonDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class PokemonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PokemonRepository
) : ViewModel() {

    private val id = savedStateHandle.toRoute<DetailRoute>().id
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getDetail(id).fold(
                onSuccess = { p -> _uiState.update { DetailUiState(pokemon = p, isLoading = false) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.toUserMessage()) } }
            )
        }
    }
}