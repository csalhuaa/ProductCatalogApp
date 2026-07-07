# UNIVERSIDAD NACIONAL DE SAN AGUSTÍN DE AREQUIPA
**Facultad de Ingeniería de Producción y Servicios**  
**Escuela Profesional de Ingeniería de Sistemas / Ingeniería de Software**

**Curso:** Desarrollo Avanzado en Nuevas Plataformas  
**Título del Informe:** Evolución Arquitectónica hacia Sistemas Offline-First y Single Source of Truth (SSOT) en Ecosistemas Móviles  
**Aplicación:** Catálogo Pro  
**Docente:** [Nombre del Docente]  
**Autor:** [Tu Nombre / Equipo]  
**Fecha:** 7 de Julio de 2026  
**Ciudad:** Arequipa, Perú  

---

## RESUMEN EJECUTIVO (ABSTRACT)

El presente informe detalla exhaustivamente el proceso de reingeniería de software aplicado a la aplicación móvil "Catálogo Pro". La refactorización transiciona la arquitectura de una dependencia estricta de red (Online-Only) hacia un paradigma **Offline-First**, utilizando **Room Database**, **Kotlin Coroutines (Flows)** y **Jetpack Compose**. A través del patrón *Single Source of Truth (SSOT)*, se logró aislar la capa de presentación de las volatilidades de la red, garantizando resiliencia ante la pérdida de conectividad, optimización del ancho de banda y una experiencia de usuario (UX) sin bloqueos (ANRs). El documento abarca el marco teórico, modelado de componentes, fragmentos de código crítico y análisis de flujos reactivos.

---

## 1. MARCO TEÓRICO Y FUNDAMENTOS ARQUITECTÓNICOS

### 1.1. El Paradigma Offline-First y la Tolerancia a Fallos
Históricamente, las aplicaciones móviles actuaban como "clientes tontos" (thin clients), dependiendo del servidor para renderizar cualquier estado. El enfoque **Offline-First** asume que la red es, por defecto, inestable. Bajo este paradigma, la persistencia local no es un simple caché efímero, sino el estado primario de la aplicación. Inspirado en conceptos como el *Teorema CAP* (Consistencia, Disponibilidad, Tolerancia a particiones), el sistema móvil sacrifica la consistencia estricta en favor de la **Alta Disponibilidad** y la **Consistencia Eventual**, permitiendo al usuario interactuar con la app incluso en "particiones" de red (modo avión, túneles, etc.).

### 1.2. Patrón Single Source of Truth (SSOT)
En ecosistemas reactivos, múltiples fuentes de datos (Red, Memoria, Base de Datos local) provocan "condiciones de carrera" (Race Conditions) y estados inconsistentes. El SSOT establece que un dato debe existir y ser modificado en **un único lugar**. En "Catálogo Pro", este lugar es **Room Database**. La Interfaz de Usuario (UI) está funcionalmente "ciega" respecto a la API REST (FakeStoreAPI); su única labor es observar los cambios que ocurren en SQLite.

### 1.3. Ecosistema Reactivo: Kotlin Flows y Jetpack Compose
A diferencia de los *Callbacks* tradicionales que procesan datos de forma imperativa y puntual, `Flow` de Kotlin implementa el patrón Observador mediante "Streams" fríos asíncronos. Jetpack Compose, al ser un framework declarativo, está diseñado para "recomponerse" cada vez que el estado emitido por un `StateFlow` muta. Esta simbiosis permite una reactividad matemática: `UI = f(State)`.

---

## 2. DIAGRAMAS Y ARQUITECTURA DEL SISTEMA

### 2.1. Diagrama de Componentes (Arquitectura SSOT)

A continuación, se ilustra la dirección unidireccional de los datos (Unidirectional Data Flow) implementada en el proyecto:

```mermaid
graph TD
    subgraph Capa de Red
        API[FakeStoreAPI REST]
        Retrofit[Retrofit + GSON]
    end

    subgraph Capa de Datos (Repository & Local)
        Repo[ProductRepositoryImpl]
        DAO[ProductDao]
        Room[(Room SQLite DB)]
    end

    subgraph Capa de Presentación (UI)
        VM[ProductViewModel]
        UI[ProductListScreen - Compose]
    end

    API -->|JSON| Retrofit
    Retrofit -->|DTOs| Repo
    Repo -->|Transforma a Entities\n(Insert / Update)| DAO
    DAO -->|Escribe| Room
    
    Room -->|Emite Flow<List<ProductEntity>>| DAO
    DAO -->|Flujo Continuo| Repo
    Repo -->|Transforma a Domain Models| VM
    VM -->|StateFlow<UiState>| UI
```

*Figura 1: Flujo de Arquitectura Offline-First implementada en Catálogo Pro.*

---

## 3. DESARROLLO TÉCNICO Y ANÁLISIS DE IMPLEMENTACIÓN

El proceso técnico se subdivide en cuatro pilares fundamentales exigidos para la reingeniería:

### A) Funcionamiento sin Conexión a Internet (Resiliencia de Red)

**Objetivo Académico:**  
Diseñar un mecanismo de intercepción de fallos a nivel de *Socket* y resolución DNS, previniendo que una excepción detenga el ciclo de vida de la app o borre los datos en memoria, manteniendo la operatividad continua.

**Fundamento y Código:**  
La red se gestiona en un hilo secundario asilado gracias a `viewModelScope` y `Dispatchers.IO`. Cuando el dispositivo pierde conexión, el sistema operativo lanza excepciones nativas. `UnknownHostException` indica que el DNS no pudo resolver el dominio (sin internet en absoluto), mientras que `IOException` suele indicar un *Timeout* o caída del servidor. 

```kotlin
private fun refreshProducts() {
    viewModelScope.launch {
        _isLoading.value = true
        _isOffline.value = false
        try {
            // Operación asíncrona suspendida: orquesta la sincronización
            productRepository.refreshProducts()
        } catch (e: UnknownHostException) {
            // Interceptación: Imposible resolver la IP de FakeStoreAPI
            _isOffline.value = true
            _errorMessage.value = "Sin conexión a red local/móvil."
        } catch (e: IOException) {
            // Interceptación: Fallo de handshake o timeout
            _isOffline.value = true
            _errorMessage.value = "Servidor inalcanzable. Mostrando caché."
        } finally {
            // Se oculta el skeleton/spinner de carga general
            _isLoading.value = false
        }
    }
}
```

**Análisis de Flujo:**  
Al interceptar el fallo, la variable `_isOffline` emite `true`. Debido a la arquitectura SSOT, el flujo de datos principal (la lista de productos) **nunca entra en este bloque try-catch**. El `Flow` proveniente de Room es independiente, por lo que los datos previamente almacenados en SQLite siguen estando disponibles e inmutables en la pantalla, logrando disponibilidad total.

### B) Persistencia Local de Datos y Mapeo Relacional (Room ORM)

**Objetivo Académico:**  
Establecer un esquema de base de datos eficiente mediante ORM (Object-Relational Mapping). Configurar las políticas de resolución de colisiones y optimizar la compilación mediante KSP (Kotlin Symbol Processing).

**Fundamento y Código:**  
Para evitar la sobrecarga reflexiva en tiempo de ejecución, se utilizó **Room** compilado a través de **KSP**. Esto genera código Java/Kotlin nativo en tiempo de compilación. Las clases se decoraron con `@Entity`.

El componente crítico aquí es la estrategia `OnConflictStrategy.REPLACE` dentro del DAO:

```kotlin
@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int, // Llave primaria exigida por SQLite
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String
)

@Dao
interface ProductDao {
    // ROOM genera un InvalidationTracker para esta consulta.
    // Cualquier cambio en la tabla "products" re-emite los datos automáticamente.
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    // Operación UPSERT masiva: Si el ID existe, lo actualiza. Si no, lo inserta.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)
}
```

**Análisis de Flujo:**  
La operación de Inserción Masiva (`insertProducts`) encapsula una transacción atómica a nivel de SQLite. Al recibir el listado de productos de internet, Room analiza las llaves primarias (`id`). La estrategia `REPLACE` realiza un "Upsert" eficiente (Update or Insert), reemplazando registros antiguos y añadiendo los nuevos. Inmediatamente después del *Commit* en la base de datos, el `InvalidationTracker` de Room detecta la mutación y empuja la nueva lista hacia el `Flow` de lectura.

### C) Sincronización Automática al Detectar Red (Patrón Repositorio)

**Objetivo Académico:**  
Implementar el Patrón de Diseño Estructural *Repository*, aislando las reglas de negocio de la procedencia de los datos. El repositorio actuará como el orquestador absoluto de la sincronización.

**Fundamento y Código:**  
La sincronización debe separar los "DTOs" (Data Transfer Objects de Retrofit) de las Entidades de base de datos. El repositorio llama a la API, recibe los DTOs, los mapea a `ProductEntity` y los inyecta en el DAO.

```kotlin
class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {

    private val api = RetrofitClient.productApi

    // Exposición pasiva de la fuente de verdad (Lectura)
    override val products: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toDomainModel() } // Mapeo de Entidad a Modelo de Dominio
    }

    // Proceso activo de sincronización (Escritura)
    override suspend fun refreshProducts() = withContext(Dispatchers.IO) {
        // 1. Fetch bloqueante pero seguro (fuera del Main Thread)
        val remoteProducts = api.getAllProducts()
        
        // 2. Mapeo estructural y 3. Persistencia atómica
        productDao.insertProducts(remoteProducts.map { it.toEntity() })
    }
}
```

**Análisis de Flujo:**  
Al aislar esta lógica, la UI desconoce si los datos vienen de Retrofit o de SQLite. Cuando `refreshProducts()` culmina, su trabajo termina. No retorna la lista de vuelta a la UI. Es el observador pasivo (`val products`) el que se encarga de reaccionar a esta inserción, cerrando el ciclo reactivo Unidireccional.

### D) Consistencia entre UI, Caché Local y API REST (Jetpack Compose)

**Objetivo Académico:**  
Lograr una UI 100% determinista basada en el estado. Aprovechar los operadores de corrutinas para combinar múltiples fuentes de estado en un único objeto de estado inmutable (`ProductUiState`) que la interfaz gráfica pueda renderizar.

**Fundamento y Código:**  
La UI en Compose es una función del estado. Si manejáramos la carga, los errores y la lista en variables separadas, podríamos enfrentar desajustes visuales (ej. un spinner de carga superpuesto a una lista renderizada). En el ViewModel usamos el operador `combine` para fusionar el `Flow` constante de Room junto con estados transitorios (como la barra de búsqueda o filtros), compactándolos con `stateIn` para convertirlos en un `StateFlow` "caliente" (hot stream).

```kotlin
// Dentro del ViewModel: Combinación de Múltiples Flujos
val uiState: StateFlow<ProductUiState> = combine(
    productRepository.products, // [1] Flujo inagotable desde SQLite
    _searchQuery,               // [2] Flujo de la barra de búsqueda
    _selectedCategory,          // [3] Flujo de los filtros (Chips)
    _isLoading,                 // [4] Estado booleano de progreso
    _errorMessage               // [5] Posibles mensajes de error
) { products, query, category, loading, error ->
    
    // Árbol de decisiones de Estado
    when {
        loading && products.isEmpty() -> ProductUiState.Loading
        error != null && products.isEmpty() && !_isOffline.value -> ProductUiState.Error(error)
        else -> {
            // Filtrado local en memoria
            val filtered = products.filter { product ->
                val matchesQuery = product.title.contains(query, ignoreCase = true)
                val matchesCategory = category == null || product.category == category
                matchesQuery && matchesCategory
            }
            ProductUiState.Success(filtered)
        }
    }
}.stateIn(
    scope = viewModelScope,
    // Optimización: Cancela la suscripción si la UI pasa 5s en background
    started = SharingStarted.WhileSubscribed(5000), 
    initialValue = ProductUiState.Loading
)
```

**Análisis de Flujo:**  
1. **Inicio:** Compose invoca `uiState.collectAsState()`.
2. **Reacción:** Cualquier cambio en la Base de Datos (inserción, eliminación) empuja un evento a `products`.
3. **Evaluación:** `combine` recalcula inmediatamente la lógica de filtrado de búsqueda (ej. si el usuario escribió "Zapatos").
4. **Recomposición:** Compose, al detectar una nueva instancia de `ProductUiState.Success`, ejecuta una fase de "Recomposition", repintando la pantalla a 60 FPS sin parpadeos gracias a la comparación inteligente de nodos (Diffing algorítmico).

---

## 4. PRUEBAS DE VALIDACIÓN Y EVIDENCIAS SUGERIDAS

Para constatar empíricamente la efectividad de esta arquitectura ante el docente, el estudiante debe reproducir y adjuntar capturas de pantalla de los siguientes escenarios críticos:

1. **Escenario de Aislamiento de Red (Airplane Mode Test):**  
   - **Acción:** Apagar Wi-Fi y Datos Móviles, luego abrir la aplicación.
   - **Evidencia Esperada:** Captura de la pantalla principal mostrando todos los productos. En la parte superior debe figurar el Snackbar/Banner rojo con el icono de alerta y el texto *"⚠️ Sin conexión - Mostrando datos locales"*. La lista debe ser scrolleable y no debe cerrarse de forma abrupta (No Crash).
   
2. **Escenario de Inspección de Persistencia (Database Inspector):**  
   - **Acción:** En Android Studio, ejecutar la app y abrir el panel *App Inspection -> Database Inspector*.
   - **Evidencia Esperada:** Captura mostrando la tabla `products` seleccionada, evidenciando las filas importadas de FakeStoreAPI. Se debe apreciar que la estructura de la tabla coincide con el `@Entity` y que la columna `id` tiene la llave primaria.

3. **Escenario de Sincronización en Segundo Plano (Network Profiler):**  
   - **Acción:** Con la app abierta, borrar los datos (Clear Storage) y abrirla nuevamente mientras se graba la sesión en el *Profiler* de Android Studio.
   - **Evidencia Esperada:** Captura del panel de red demostrando un hilo HTTP `GET /products` que ocurre en paralelo a la renderización de la UI, confirmando la asincronía correcta.

4. **Escenario de Inserción y Recomposición Inmediata:**  
   - **Acción:** Usar el botón flotante (FAB) para agregar un nuevo producto, simular la respuesta de FakeStoreAPI.
   - **Evidencia Esperada:** Comprobar que al regresar a la lista, el producto nuevo aparece mágicamente sin necesidad de hacer un *"Pull to Refresh"*, validando la reactividad del InvalidationTracker de Room.

---

## 5. CONCLUSIONES ACADÉMICAS Y ARQUITECTÓNICAS

A partir del análisis profundo de esta reingeniería de software, se establecen las siguientes conclusiones formales:

1. **Robustez estructural mediante la Fuente Única de Verdad (SSOT):**  
   La implementación rigurosa del patrón SSOT elimina la ambigüedad del estado en la capa de presentación. Al prohibir que la Interfaz de Usuario consuma promesas de red directamente y forzarla a reaccionar únicamente a SQLite, se erradican los *"Estados Fantasma"* (ghost states) y *"Condiciones de Carrera"* (race conditions). Esto da como resultado un software predecible, determinista y matemáticamente testable, elementos cruciales en la ingeniería de software moderna.
   
2. **Mitigación de Latencia Perceptiva y Optimización de Ancho de Banda:**  
   Al almacenar la información en disco, el tiempo de respuesta percibido por el usuario desciende de los milisegundos que demora una petición HTTP a meros microsegundos de lectura en memoria NVMe/Flash del dispositivo móvil. Esta estrategia reduce exponencialmente el consumo repetitivo de APIs (ahorro de costes en infraestructura de la nube y de ancho de banda del usuario), aplazando las peticiones de red únicamente a escenarios de invalidación estricta de caché (Sincronizaciones pasivas en Background).
   
3. **Alta Cohesión y Bajo Acoplamiento (Principios SOLID y Clean Architecture):**  
   La inyección de abstracciones mediante el Patrón Repositorio y la separación clara de modelos (DTOs, Entidades de Dominio, Entidades Locales) demuestran un respeto riguroso por el Principio de Responsabilidad Única (SRP). La capa de interfaz (Jetpack Compose) y la capa de lógica de negocio (ViewModel) son ahora completamente agnósticas a las librerías de persistencia subyacentes, lo cual facilita enormemente la escalabilidad futura, el mantenimiento del código y la implementación de Pruebas Unitarias (Unit Testing) con dependencias falsificadas (Mocks).
   
4. **Resiliencia ante la Fragilidad del Entorno Móvil (Tolerancia a Fallos):**  
   En la ingeniería de aplicaciones distribuidas para entornos móviles, tratar a la infraestructura de red como un canal infalible es un error de diseño grave. Al incorporar el manejo de excepciones (`UnknownHostException`, `IOException`) como estados operativos esperados dentro del flujo normal de la aplicación, el sistema gana tolerancia a fallos. Esta madurez técnica diferencia a un simple prototipo estudiantil de una aplicación comercial de estándar profesional y "Production-Ready".

---

## 6. REFERENCIAS BIBLIOGRÁFICAS

1. **Android Developers (2024).** *Guide to app architecture*. Recuperado de https://developer.android.com/topic/architecture
2. **Android Developers (2024).** *Save data in a local database using Room*. Recuperado de https://developer.android.com/training/data-storage/room
3. **Elizarov, R. (2020).** *Kotlin Flows and Coroutines*. JetBrains Blog / Kotlin Documentation. Recuperado de https://kotlinlang.org/docs/flow.html
4. **Martin, R. C. (2017).** *Clean Architecture: A Craftsman's Guide to Software Structure and Design*. Prentice Hall.
5. **Google / Compose Team.** *State and Jetpack Compose*. Recuperado de https://developer.android.com/jetpack/compose/state

---
*Documento generado rigurosamente para evaluación académica técnica en el ámbito de Ingeniería de Software.*
