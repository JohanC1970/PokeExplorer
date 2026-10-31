package co.edu.uniquindio.pokeexplorer.core

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import retrofit2.HttpException
import java.io.IOException

/** Emite true/false cada vez que cambia el estado de la red. Requiere ACCESS_NETWORK_STATE. */
@Singleton
class ConnectivityObserver @Inject constructor(@ApplicationContext context: Context) {
    private val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun currentlyOnline(): Boolean =
        manager.getNetworkCapabilities(manager.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) { trySend(true) }
            override fun onLost(network: Network) { trySend(currentlyOnline()) }
        }
        trySend(currentlyOnline())
        manager.registerDefaultNetworkCallback(callback)
        awaitClose { manager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

}

fun Throwable.toUserMessage(): String = when {

    this is HttpException && code() == 404 -> "No se encontró el Pokémon"
    this is IOException -> "Sin conexión o servidor inaccesible. Inténtalo de nuevo"
    else -> "Ocurrió un error inesperado"

}