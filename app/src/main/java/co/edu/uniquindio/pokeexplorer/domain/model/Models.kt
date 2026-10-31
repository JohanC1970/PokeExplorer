package co.edu.uniquindio.pokeexplorer.domain.model

data class PokemonSummary(val id: Int, val name: String, val imageUrl: String)

data class PokemonStat(val name: String, val value: Int)

data class PokemonDetail(
    val id: Int,
    val name: String,
    val imageUrl: String,
    val heightMeters: Double,
    val weightKg: Double,
    val types: List<String>,
    val abilities: List<String>,
    val stats: List<PokemonStat>,
    val description: String
)

data class PokemonPage(val items: List<PokemonSummary>, val endReached: Boolean)
