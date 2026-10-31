package co.edu.uniquindio.pokeexplorer.domain.repository

import co.edu.uniquindio.pokeexplorer.domain.model.PokemonDetail
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonPage
import co.edu.uniquindio.pokeexplorer.domain.model.PokemonSummary

interface PokemonRepository {

    suspend fun getPage(offset: Int, limit: Int): Result<PokemonPage>
    suspend fun searchByName(name: String): Result<PokemonSummary>
    suspend fun getTypes(): Result<List<String>>
    suspend fun getByType(type: String, offset: Int, limit: Int): Result<PokemonPage>
    suspend fun getDetail(id: Int): Result<PokemonDetail>

}