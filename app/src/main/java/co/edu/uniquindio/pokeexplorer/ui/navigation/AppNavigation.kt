package co.edu.uniquindio.pokeexplorer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import co.edu.uniquindio.pokeexplorer.ui.detail.PokemonDetailScreen
import co.edu.uniquindio.pokeexplorer.ui.list.PokemonListScreen
import kotlinx.serialization.Serializable

@Serializable data object ListRoute
@Serializable data class DetailRoute(val id: Int)

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    NavHost(nav, startDestination = ListRoute) {
        composable<ListRoute> { PokemonListScreen(onPokemonClick = { nav.navigate(DetailRoute(it)) }) }
        composable<DetailRoute> { PokemonDetailScreen(onBack = { nav.popBackStack() }) }
    }
}