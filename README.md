# Catálogo Pro

Una aplicación móvil Android desarrollada con Jetpack Compose que implementa un catálogo de productos. Utiliza una arquitectura Offline-First impulsada por Room Database y Kotlin Flows para asegurar una experiencia de usuario fluida incluso sin conexión a internet.

## Características

- **Arquitectura Offline-First:** Los datos persisten localmente utilizando Room Database.
- **Single Source of Truth (SSOT):** La interfaz de usuario observa exclusivamente el estado de la base de datos local mediante Kotlin Flows.
- **Sincronización Automática:** La aplicación sincroniza los datos en segundo plano usando FakeStoreAPI (vía Retrofit).
- **Interfaz Reactiva:** Desarrollada completamente con Jetpack Compose.
- **Manejo de Errores de Red:** Interceptación segura de excepciones de conexión, notificando al usuario de manera no intrusiva mediante un banner persistente.

## Tecnologías Utilizadas

- **Kotlin**
- **Jetpack Compose** (Material 3, Navigation)
- **Coroutines & Flows** (Programación reactiva y asíncrona)
- **Room Database** (Persistencia local SQLite)
- **KSP** (Kotlin Symbol Processing)
- **Retrofit & GSON** (Cliente HTTP y serialización JSON)
- **Coil** (Carga de imágenes)

## Configuración del Entorno

1. Clona el repositorio en tu máquina local.
2. Abre el proyecto en Android Studio.
3. Sincroniza los archivos de Gradle.
4. Ejecuta la aplicación en un emulador o dispositivo físico.

## Arquitectura

El proyecto adopta el patrón de arquitectura recomendada por Android (Clean Architecture y Unidirectional Data Flow):
- **Capa de Datos:** Implementa Retrofit para el consumo de red y Room para la persistencia local. El repositorio coordina estas fuentes de manera centralizada.
- **Capa de Modelos:** Define la estructura de datos pura (`Product`) y la estructura de entidad de base de datos (`ProductEntity`).
- **Capa de Presentación:** Administra la lógica de vista en `ProductViewModel` fusionando el flujo de datos usando `StateFlow`, y reacciona de manera predecible en las pantallas de Jetpack Compose.
