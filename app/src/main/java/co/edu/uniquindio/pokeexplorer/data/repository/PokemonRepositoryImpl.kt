package co.edu.uniquindio.pokeexplorer.data.repository

import co.edu.uniquindio.pokeexplorer.data.remote.NamedResourceDto
import co.edu.uniquindio.pokeexplorer.data.remote.PokeApi
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonDetail
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonPage
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonStat
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonSummary
import co.edu.uniquindio.pokeexplorer.domain.repository.PokemonRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

private const val SPRITES = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon"

private fun artwork(id: Int) = "$SPRITES/other/official-artwork/$id.png"

private fun NamedResourceDto.toSummary(): PokemonSummary {
    val id = url.trimEnd('/').substringAfterLast('/').toInt()
    return PokemonSummary(id, name, artwork(id))
}

private inline fun <T> safeCall(block: () -> T): Result<T> =
    try { Result.success(block()) }
    catch (e: CancellationException) { throw e }
    catch (e: Exception) { Result.failure(e) }


@Singleton
class PokemonRepositoryImpl @Inject constructor(private val api : PokeApi) : PokemonRepository {

    /** /type/{name} devuelve todos los Pokémon del tipo; se pagina en memoria. */
    private val typeCache = mutableMapOf<String, List<NamedResourceDto>>()

    /** PokeAPI no tiene búsqueda parcial: se descarga una vez la lista de nombres y se filtra aquí. */
    private var allNamesCache: List<NamedResourceDto>? = null

    private suspend fun allNames(): List<NamedResourceDto> =
        allNamesCache ?: api.getPokemonPage(limit = 10000, offset = 0).results.also { allNamesCache = it }

    override suspend fun getPage(offset: Int, limit: Int) = safeCall {
        val dto = api.getPokemonPage(limit, offset)
        PokemonPage(dto.results.map { it.toSummary() }, endReached = dto.next == null)
    }

    override suspend fun searchByName(prefix: String, offset: Int, limit: Int) = safeCall {
        val q = prefix.trim().lowercase()
        val matches = allNames().filter { it.name.startsWith(q) }
        val slice = matches.drop(offset).take(limit)
        PokemonPage(slice.map { it.toSummary() }, endReached = offset + limit >= matches.size)
    }

    override suspend fun getTypes() = safeCall {
        api.getTypes().results.map { it.name }.filterNot { it == "unknown" || it == "stellar" }
    }

    override suspend fun getByType(type: String, offset: Int, limit: Int) = safeCall {
        val all = typeCache.getOrPut(type) { api.getType(type).pokemon.map { it.pokemon } }
        val slice = all.drop(offset).take(limit)
        PokemonPage(slice.map { it.toSummary() }, endReached = offset + limit >= all.size)
    }

    override suspend fun getDetail(id: Int) = safeCall {
        val p = api.getPokemon(id.toString())
        // El id de la especie sale de species.url: /pokemon-species/{id} no existe para las formas (id >= 10000).
        val speciesId = p.species.url.trimEnd('/').substringAfterLast('/').toInt()
        PokemonDetail(
            id = p.id, name = p.name, imageUrl = artwork(p.id),
            heightMeters = p.height / 10.0, weightKg = p.weight / 10.0,
            types = p.types.sortedBy { it.slot }.map { it.type.name },
            abilities = p.abilities.map { if (it.isHidden) "${it.ability.name} (oculta)" else it.ability.name },
            stats = p.stats.map { PokemonStat(it.stat.name, it.baseStat) },
            description = loadDescription(speciesId)
        )
    }

    /** La descripción es opcional: si la especie falla, el detalle se muestra igual. */
    private suspend fun loadDescription(speciesId: Int): String =
        try {
            val entries = api.getSpecies(speciesId).entries
            (entries.firstOrNull { it.language.name == "es" } ?: entries.firstOrNull { it.language.name == "en" })
                ?.text?.replace(Regex("[\\n\\u000c]"), " ").orEmpty()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            ""
        }
}