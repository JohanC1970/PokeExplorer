package co.edu.uniquindio.pokeexplorer.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Base URL: https://pokeapi.co/api/v2/  (todos los endpoints son GET y devuelven JSON). */
interface PokeApi {
    /** 1. Lista paginada de Pokémon. */
    @GET("pokemon")
    suspend fun getPokemonPage(@Query("limit") limit: Int, @Query("offset") offset: Int): PokemonPageDto

    /** 2. Detalle (o búsqueda exacta por nombre) de un Pokémon. Acepta nombre o id. */
    @GET("pokemon/{nameOrId}")
    suspend fun getPokemon(@Path("nameOrId") nameOrId: String): PokemonDetailDto

    /** 3. Datos de especie: descripción en varios idiomas. */
    @GET("pokemon-species/{id}")
    suspend fun getSpecies(@Path("id") id: Int): SpeciesDto

    /** 4. Catálogo de tipos (fuego, agua, ...). */
    @GET("type")
    suspend fun getTypes(): TypeListDto

    /** 5. Pokémon que pertenecen a un tipo. */
    @GET("type/{name}")
    suspend fun getType(@Path("name") name: String): TypeDetailDto
}

@Serializable data class NamedResourceDto(val name: String, val url: String)

@Serializable data class PokemonPageDto(
    val count: Int,
    val next: String? = null,
    val results: List<NamedResourceDto>
)

@Serializable data class PokemonDetailDto(
    val id: Int,
    val name: String,
    val height: Int,   // decímetros
    val weight: Int,   // hectogramos
    val types: List<TypeSlotDto>,
    val stats: List<StatDto>,
    val abilities: List<AbilitySlotDto>
)

@Serializable data class TypeSlotDto(val slot: Int, val type: NamedResourceDto)
@Serializable data class StatDto(@SerialName("base_stat") val baseStat: Int, val stat: NamedResourceDto)
@Serializable data class AbilitySlotDto(
    val ability: NamedResourceDto,
    @SerialName("is_hidden") val isHidden: Boolean
)

@Serializable data class SpeciesDto(
    @SerialName("flavor_text_entries") val entries: List<FlavorTextDto> = emptyList()
)
@Serializable data class FlavorTextDto(
    @SerialName("flavor_text") val text: String,
    val language: NamedResourceDto
)

@Serializable data class TypeListDto(val results: List<NamedResourceDto>)
@Serializable data class TypeDetailDto(val pokemon: List<TypePokemonDto>)
@Serializable data class TypePokemonDto(val pokemon: NamedResourceDto)