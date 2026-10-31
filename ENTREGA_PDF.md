# Actividad – Consumo de APIs REST en Android

**Repositorio:** https://github.com/JohanC1970/PokeExplorer

## 1. Descripción de la API
PokeAPI (https://pokeapi.co/api/v2/) es una API REST pública y gratuita, sin autenticación, que expone datos de la franquicia Pokémon. Todas las peticiones usan el método **HTTP GET** y devuelven **JSON**. La app la consume con Retrofit y mapea cada respuesta a DTOs de kotlinx-serialization, que luego se convierten a modelos de dominio.

| # | Endpoint | Parámetros | Respuesta | Uso en la app |
|---|----------|-----------|-----------|---------------|
| 1 | `GET /pokemon` | `limit`, `offset` (query) | `count`, `next`, `results[{name,url}]` | Lista principal con scroll infinito |
| 2 | `GET /pokemon/{nameOrId}` | nombre o id (path) | id, nombre, altura, peso, tipos, estadísticas, habilidades | Búsqueda por nombre y datos del detalle |
| 3 | `GET /type` | — | `results[{name,url}]` | Chips de filtro |
| 4 | `GET /type/{name}` | nombre del tipo (path) | `pokemon[{pokemon:{name,url}}]` | Filtro por tipo (paginado en memoria) |
| 5 | `GET /pokemon-species/{id}` | id (path) | `flavor_text_entries[{flavor_text,language}]` | Descripción en el detalle |

## 2. Capturas de pantalla (tómalas en el emulador)
1. Lista inicial con indicador "En línea".
2. Scroll infinito cargando más elementos.
3. Búsqueda por nombre (ej. "pikachu").
4. Filtro por tipo (ej. "fire").
5. Pantalla de detalle.
6. Modo avión: banner "Sin conexión (offline)" y mensaje de error con Reintentar.

## 3. Uso de inteligencia artificial
Herramientas: <!-- completa con lo que realmente usaste -->
- Claude (chatbot): elección de la API según los requisitos, diseño de la arquitectura en capas (UI / ViewModel / Repository / Domain / Retrofit) con Hilt y generación del código base.
- Se le dio como contexto la arquitectura y convenciones del proyecto Entorno para mantener el mismo estilo (StateFlow, UiState inmutable, Result en repositorios).
- El código generado se revisó, compiló y probó en emulador; los errores de compilación y de versiones se corrigieron iterando con el asistente.
