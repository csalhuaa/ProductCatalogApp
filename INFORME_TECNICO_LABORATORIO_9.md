# UNIVERSIDAD NACIONAL DE SAN AGUSTÍN DE AREQUIPA
## FACULTAD DE INGENIERÍA DE PRODUCCIÓN Y SERVICIOS
## ESCUELA PROFESIONAL DE INGENIERÍA DE SISTEMAS

---

# LABORATORIO 9: APIs REST en Compose - Consumo de Datos
## Informe Técnico Académico

### Curso: Desarrollo Avanzado en Nuevas Plataformas
### Docente: Dr. [Nombre del Docente]
### Estudiante: [Nombre del Estudiante]
### Fecha: 29 de Junio de 2026
### Ciudad: Arequipa, Perú

---

## ÍNDICE

1. [Introducción y Contexto Tecnológico](#1-introducción-y-contexto-tecnológico)
2. [Desarrollo del Proyecto por Secciones](#2-desarrollo-del-proyecto-por-secciones)
   - [a) Integración de Retrofit en un Proyecto Existente](#a-integración-de-retrofit-en-un-proyecto-existente)
   - [b) Consumo de Datos desde una API REST Pública](#b-consumo-de-datos-desde-una-api-rest-pública)
   - [c) Aplicación del Patrón Arquitectónico MVVM](#c-aplicación-del-patrón-arquitectónico-mvvm)
   - [d) Gestión de Estados de Red (Loading, Success y Error)](#d-gestión-de-estados-de-red-loading-success-y-error)
   - [e) Renderizado y Visualización de Datos Dinámicos en Compose](#e-renderizado-y-visualización-de-datos-dinámicos-en-compose)
3. [Sección de Evidencias Sugeridas](#3-sección-de-evidencias-sugeridas)
4. [Conclusiones Académicas](#4-conclusiones-académicas)

---

## 1. INTRODUCCIÓN Y CONTEXTO TECNOLÓGICO

El desarrollo de aplicaciones móviles modernas requiere una arquitectura robusta que permita el consumo eficiente de servicios web RESTful mientras se mantiene una experiencia de usuario fluida y responsiva. En el contexto del ecosistema Android, Jetpack Compose ha revolucionado el paradigma de construcción de interfaces de usuario mediante la programación declarativa, reemplazando el sistema imperativo tradicional basado en XML. Este cambio fundamental exige una reestructuración de cómo se gestionan los datos asíncronos y los estados de la interfaz.

La arquitectura de consumo de APIs en Jetpack Compose se fundamenta en el principio de unidireccionalidad del flujo de datos, donde la información viaja desde fuentes remotas hasta la interfaz de usuario a través de capas bien definidas. Esta separación de responsabilidades es crítica para mantener la escalabilidad, testabilidad y mantenibilidad del código. La capa de UI se encarga exclusivamente de la presentación visual, delegando la lógica de negocio al ViewModel, el cual interactúa con el Repository para obtener datos, ya sea de fuentes remotas mediante APIs o locales mediante bases de datos.

El patrón MVVM (Model-View-ViewModel) en el contexto de Compose se adapta naturalmente debido a la naturaleza reactiva de los componentes Composable. El ViewModel expone estados mediante flujos de datos reactivos como StateFlow o LiveData, los cuales son recolectados por la UI mediante funciones como `collectAsState()`, garantizando que la interfaz se reconstruya automáticamente ante cualquier cambio en el estado subyacente. Este mecanismo elimina la necesidad de manipulación manual del ciclo de vida y sincronización de hilos.

Las tecnologías fundamentales implementadas en este proyecto incluyen:

- **Kotlin Coroutines**: Framework de concurrencia que permite ejecutar operaciones asíncronas de manera secuencial y no bloqueante, evitando el bloqueo del hilo principal (Main Thread) durante operaciones de red. Las corrutinas utilizan el concepto de `suspend functions` para marcar operaciones que pueden suspender su ejecución sin bloquear el hilo, permitiendo que el sistema operativo pueda realizar otras tareas mientras se espera la respuesta de la red.

- **StateFlow**: Flujo de datos reactivo emitido por el ViewModel que representa el estado actual de la UI. A diferencia de LiveData, StateFlow es parte de las Coroutines y garantiza que siempre tenga un valor inicial, siendo ideal para representar estados que deben estar siempre disponibles. Su integración con Compose mediante `collectAsState()` permite una recolección eficiente y segura para el ciclo de vida.

- **Retrofit**: Biblioteca de cliente HTTP tipo-safe que simplifica el consumo de APIs REST mediante la conversión de interfaces Java/Kotlin en implementaciones HTTP. Utiliza anotaciones para describir las solicitudes HTTP, soporta conversores como GSON para la serialización/deserialización automática de JSON, y proporciona integración nativa con Coroutines mediante funciones suspendidas.

- **GSON (Google Gson)**: Biblioteca de serialización/deserialización JSON que convierte objetos Java/Kotlin a su representación JSON y viceversa. En el contexto de Retrofit, actúa como ConverterFactory, transformando automáticamente las respuestas JSON de la API en objetos de datos tipados, eliminando la necesidad de parsing manual.

- **FakeStoreAPI**: API REST pública gratuita (https://fakestoreapi.com) diseñada específicamente para prototipado y testing de aplicaciones de comercio electrónico. Proporciona endpoints para productos, carritos, usuarios y autenticación, con datos simulados que permiten simular un entorno de producción real sin necesidad de infraestructura backend propia.

La aplicación "Catálogo Pro" desarrollada en este laboratorio implementa todos estos conceptos para crear un catálogo de productos que consume datos en tiempo real desde una API remota, gestionando adecuadamente los estados de carga, éxito y error, y presentando la información de manera dinámica y responsiva mediante Jetpack Compose.

---

## 2. DESARROLLO DEL PROYECTO POR SECCIONES

### a) Integración de Retrofit en un Proyecto Existente

#### Objetivo
Configurar e integrar la biblioteca Retrofit en un proyecto Android existente con Jetpack Compose, estableciendo la infraestructura necesaria para realizar solicitudes HTTP asíncronas hacia servicios web RESTful. Esta integración incluye la configuración de dependencias en el sistema de gestión de paquetes Gradle y la implementación del patrón Singleton para la instancia de Retrofit con el conversor GSON.

#### Fundamento Teórico
Retrofit requiere una configuración inicial que define la URL base del servicio web y el convertidor de datos que transformará las respuestas JSON en objetos Kotlin. El patrón Singleton es utilizado para garantizar que exista una única instancia de Retrofit durante todo el ciclo de vida de la aplicación, optimizando el consumo de recursos y evitando la creación innecesaria de múltiples instancias que podrían sobrecargar el sistema. El `GsonConverterFactory` es responsable de la serialización y deserialización automática entre objetos Kotlin y JSON, utilizando reflexión para mapear los campos de las clases de datos con las propiedades del JSON.

La configuración de dependencias en Gradle (versión Kotlin DSL) requiere la especificación de las bibliotecas de Retrofit core, el convertidor GSON, y opcionalmente, el interceptor de logging para depuración. Estas dependencias se resuelven mediante el catálogo de versiones de Gradle (libs.versions.toml) o directamente en el archivo build.gradle.kts, garantizando versiones compatibles y actualizadas.

#### Código y Explicación

**Fragmento 1: Dependencias de Retrofit en build.gradle.kts**
```kotlin
dependencies {
    // Dependencias de Retrofit y GSON
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
}
```

**Explicación:** En el bloque `dependencies` del archivo de configuración de Gradle, se agregan las dos dependencias esenciales de Retrofit. La dependencia `retrofit` proporciona el core de la biblioteca para realizar solicitudes HTTP, mientras que `retrofit.gson` incluye el convertidor GSON que transformará automáticamente las respuestas JSON en objetos Kotlin. Se utiliza el catálogo de versiones (libs.*) para garantizar consistencia en las versiones entre módulos del proyecto.

**Fragmento 2: Declaración del Singleton RetrofitClient**
```kotlin
object RetrofitClient {
    private const val BASE_URL = "https://fakestoreapi.com/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
```

**Explicación:** La clase `RetrofitClient` implementa el patrón Singleton mediante la declaración `object` de Kotlin, garantizando una única instancia durante todo el ciclo de vida de la aplicación. La constante `BASE_URL` define la URL base de la API FakeStore. La propiedad `retrofit` se inicializa de manera diferida (`lazy`) para optimizar el rendimiento, creando la instancia solo cuando se accede por primera vez. El `Retrofit.Builder` configura la URL base y registra el `GsonConverterFactory` como conversor de datos.

**Fragmento 3: Exposición de la interfaz ProductApi**
```kotlin
val productApi: ProductApi by lazy {
    retrofit.create(ProductApi::class.java)
}
```

**Explicación:** Esta propiedad expone la implementación de la interfaz `ProductApi`, generada dinámicamente por Retrofit mediante proxies dinámicos. También utiliza inicialización diferida para evitar la creación prematura de la instancia. El método `retrofit.create()` genera una implementación concreta de la interfaz que intercepta las llamadas a los métodos y las convierte en solicitudes HTTP reales.

---

### b) Consumo de Datos desde una API REST Pública

#### Objetivo
Implementar el modelado de datos basado en el esquema OpenAPI de FakeStoreAPI y definir los endpoints asíncronos necesarios para consumir los datos de productos mediante funciones suspendidas de Kotlin Coroutines, garantizando tipado fuerte y seguridad en tiempo de compilación.

#### Fundamento Teórico
El modelado de datos en Kotlin se realiza mediante `data classes`, que son clases inmutables diseñadas específicamente para almacenar datos. Estas clases generan automáticamente métodos útiles como `equals()`, `hashCode()`, `toString()`, `copy()`, y `componentN()` para destructuring. Cuando se consume una API REST, es esencial que las propiedades de la data class coincidan exactamente con los campos del JSON response. GSON utiliza el nombre de las propiedades para realizar el mapeo, aunque puede personalizarse mediante anotaciones como `@SerializedName` cuando los nombres difieren.

La definición de endpoints en Retrofit se realiza mediante interfaces con métodos anotados que describen las solicitudes HTTP. Las anotaciones `@GET`, `@POST`, `@PUT`, `@DELETE` especifican el método HTTP, mientras que el valor de la anotación indica la ruta relativa a la URL base. Los parámetros de la ruta se especifican con `@Path`, los parámetros de consulta con `@Query`, y el cuerpo de la solicitud con `@Body`. El uso de funciones `suspend` permite que estas operaciones se ejecuten de manera asíncrona sin bloquear el hilo principal, integrándose perfectamente con el flujo de corrutinas.

#### Código y Explicación

**Fragmento 1: Data class Product**
```kotlin
data class Product(
    val id: Int = 0,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String
)
```

**Explicación:** La data class `Product` representa la entidad de dominio que mapea el esquema JSON de la API FakeStore. Cada propiedad corresponde a un campo del response JSON: `id` (identificador único), `title` (nombre), `price` (precio), `description` (descripción), `category` (categoría), e `image` (URL de imagen). El valor por defecto `id = 0` facilita la creación de nuevos productos cuando el servidor genera IDs automáticamente.

**Fragmento 2: Endpoint GET para obtener todos los productos**
```kotlin
@GET("products")
suspend fun getAllProducts(): List<Product>
```

**Explicación:** Este método define el endpoint para recuperar todos los productos. La anotación `@GET("products")` indica una solicitud HTTP GET a la ruta `/products`. Es una función `suspend` que permite ejecución asíncrona sin bloquear el hilo principal. Retorna una `List<Product>`, y GSON deserializa automáticamente el array JSON del response en una lista de objetos Product.

**Fragmento 3: Endpoint POST para crear un producto**
```kotlin
@POST("products")
suspend fun addProduct(@Body product: Product): Product
```

**Explicación:** Este método define el endpoint para crear un nuevo producto. La anotación `@POST("products")` especifica el método HTTP POST. El parámetro `@Body product: Product` indica que el objeto será serializado a JSON y enviado en el cuerpo de la solicitud. Retorna el objeto Product creado por el servidor, incluyendo el ID generado.

**Fragmento 4: Endpoint PUT para actualizar un producto**
```kotlin
@PUT("products/{id}")
suspend fun updateProduct(@Path("id") id: Int, @Body product: Product): Product
```

**Explicación:** Este método define el endpoint para actualizar un producto existente. La anotación `@PUT("products/{id}")` incluye un segmento de ruta dinámico `{id}`. El parámetro `@Path("id") id: Int` inserta el valor en la URL (ej: `/products/5`). El cuerpo contiene los datos actualizados del producto.

**Fragmento 5: Endpoint DELETE para eliminar un producto**
```kotlin
@DELETE("products/{id}")
suspend fun deleteProduct(@Path("id") id: Int): Response<Unit>
```

**Explicación:** Este método define el endpoint para eliminar un producto. Utiliza `@Path` para especificar el ID del producto a eliminar. Retorna `Response<Unit>` en lugar de Unit directamente porque Retrofit requiere un tipo de retorno para manejar códigos de estado HTTP. `Unit` es equivalente a `void` en Java.

---

### c) Aplicación del Patrón Arquitectónico MVVM

#### Objetivo
Implementar el patrón arquitectónico Model-View-ViewModel para desacoplar completamente la lógica de negocio y las operaciones de red de la interfaz de usuario, estableciendo una separación clara de responsabilidades mediante las capas Repository y ViewModel, mejorando la testabilidad, mantenibilidad y escalabilidad del código.

#### Fundamento Teórico
El patrón MVVM es una variante del patrón MVC (Model-View-Controller) diseñada específicamente para plataformas con soporte para data binding y programación reactiva. En el contexto de Android moderno con Jetpack Compose, las responsabilidades se distribuyen de la siguiente manera:

- **Model**: Representa los datos de la aplicación y la lógica de negocio. Incluye las entidades de dominio (data classes), el acceso a datos (Repository), y la configuración de servicios externos (API clients). El Model no tiene conocimiento de la UI.

- **View**: En Compose, la View está compuesta por funciones `@Composable` que describen la interfaz de usuario de manera declarativa. La View observa los estados expuestos por el ViewModel y reacciona a los cambios reconstruyéndose automáticamente. No contiene lógica de negocio, solo lógica de presentación.

- **ViewModel**: Actúa como intermediario entre el Model y la View. Contiene la lógica de presentación de la UI, gestiona los estados de la interfaz, y coordina las operaciones asíncronas. El ViewModel sobrevive a los cambios de configuración (como rotaciones de pantalla) y es responsable de limpiar recursos cuando ya no es necesario.

El Repository Pattern se utiliza como una capa de abstracción adicional entre el ViewModel y las fuentes de datos. El Repository proporciona una API unificada para acceder a datos, independientemente de su origen (API remota, base de datos local, caché en memoria). Esto permite cambiar la implementación de acceso a datos sin afectar el ViewModel, facilita el testing mediante mocks, y permite implementar estrategias de caché y sincronización complejas.

La inyección de dependencias se utiliza para proporcionar las instancias del Repository al ViewModel. En este proyecto, se implementa manualmente mediante una `ViewModelFactory`, aunque en proyectos más grandes se recomienda el uso de frameworks como Dagger Hilt o Koin.

#### Código y Explicación

**Fragmento 1: Interfaz del Repository**
```kotlin
interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun addProduct(product: Product): Product
    suspend fun updateProduct(id: Int, product: Product): Product
    suspend fun deleteProduct(id: Int): Boolean
}
```

**Explicación:** La interfaz `ProductRepository` define el contrato que cualquier implementación debe cumplir. Sigue el principio de inversión de dependencias de SOLID, permitiendo que el ViewModel dependa de abstracciones. Todos los métodos son `suspend` porque las operaciones de acceso a datos son asíncronas por naturaleza.

**Fragmento 2: Implementación del Repository con withContext**
```kotlin
class ProductRepositoryImpl : ProductRepository {
    private val api = RetrofitClient.productApi

    override suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        api.getAllProducts()
    }
}
```

**Explicación:** `ProductRepositoryImpl` utiliza Retrofit para acceder a la API. El uso de `withContext(Dispatchers.IO)` garantiza que las operaciones de red se ejecuten en el hilo de I/O en lugar del hilo principal, evitando bloqueos de la UI.

**Fragmento 3: Declaración de StateFlows en el ViewModel**
```kotlin
class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _rawProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProductUiState> = combine(
        _rawProducts, _isLoading, _errorMessage
    ) { products, loading, error ->
        when {
            loading -> ProductUiState.Loading
            error != null -> ProductUiState.Error(error)
            else -> ProductUiState.Success(products)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProductUiState.Loading)
}
```

**Explicación:** El ViewModel utiliza `MutableStateFlow` para mantener el estado interno. El estado expuesto a la UI (`uiState`) se calcula reactivamente mediante el operador `combine`, que combina múltiples flujos y produce un nuevo flujo que se actualiza cuando cualquiera de los flujos de origen cambia. `stateIn` convierte el flujo en un `StateFlow` ideal para representar el estado de la UI.

**Fragmento 4: Método getProducts con manejo de excepciones**
```kotlin
fun getProducts() {
    viewModelScope.launch {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val products = productRepository.getProducts()
            _rawProducts.value = products
        } catch (e: UnknownHostException) {
            _errorMessage.value = "Sin conexión a internet. Verifica tu red."
        } catch (e: IOException) {
            _errorMessage.value = "Error de red al intentar conectar con el servidor."
        } catch (e: Exception) {
            _errorMessage.value = "Error inesperado: ${e.localizedMessage}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

**Explicación:** Este método demuestra el patrón típico de carga de datos: establece `_isLoading = true`, ejecuta la operación del repositorio dentro de un bloque `try-catch` capturando excepciones específicas, y en el bloque `finally` establece `_isLoading = false` independientemente del resultado. `viewModelScope` garantiza que las corrutinas se cancelen automáticamente cuando el ViewModel se destruye.

---

### d) Gestión de Estados de Red (Loading, Success y Error)

#### Objetivo
Implementar un sistema robusto de gestión de estados mediante clases selladas (Sealed Classes) para representar explícitamente los diferentes estados de la interfaz de usuario durante operaciones de red, y manejar excepciones de manera segura en el ciclo de vida del ViewModel para proporcionar retroalimentación adecuada al usuario.

#### Fundamento Teórico
El manejo de estados en aplicaciones modernas se basa en el principio de que la UI debe ser una función pura del estado. Esto significa que dado un estado específico, la UI siempre se renderizará de la misma manera. Para representar estos estados de manera type-safe, Kotlin proporciona las `sealed classes` (clases selladas).

Una sealed class es una clase abstracta que restringe la jerarquía de herencia: todas las subclases deben estar declaradas en el mismo archivo. Esto permite que los compiladores y herramientas de análisis de código realicen verificaciones exhaustivas de exhaustividad en expresiones `when`, garantizando que todos los casos posibles sean manejados. A diferencia de las clases regulares o enums, las sealed classes pueden mantener estado (propiedades) y pueden tener múltiples instancias con diferentes valores.

Los estados típicos en una operación de red asíncrona son:
- **Loading**: Indica que la operación está en progreso. La UI debe mostrar un indicador de carga y deshabilitar interacciones del usuario.
- **Success**: Indica que la operación se completó exitosamente. La UI debe mostrar los datos obtenidos.
- **Error**: Indica que la operación falló. La UI debe mostrar un mensaje de error y proporcionar una opción para reintentar.

El manejo de excepciones en corrutinas se realiza mediante bloques `try-catch`. Es importante capturar excepciones específicas para proporcionar mensajes de error apropiados:
- `UnknownHostException`: Se lanza cuando no se puede resolver el nombre de host, indicando falta de conexión a internet.
- `IOException`: Clase base para excepciones de I/O, incluyendo timeouts de conexión y errores de lectura/escritura.
- `HttpException` (de Retrofit): Se lanza cuando el servidor retorna un código de estado HTTP no exitoso (4xx, 5xx).
- `Exception`: Clase base para capturar cualquier otra excepción no prevista.

El uso de `viewModelScope.launch` garantiza que las corrutinas se cancelen automáticamente cuando el ViewModel se destruye, evitando que las operaciones de red continúen en segundo plano después de que el usuario haya navegado fuera de la pantalla.

#### Código y Explicación

**Archivo: ui/state/ProductUiState.kt**
```kotlin
package app.productcatalog.ui.state

import app.productcatalog.data.model.Product

sealed interface ProductUiState {
    object Loading : ProductUiState
    data class Success(val products: List<Product>) : ProductUiState
    data class Error(val message: String) : ProductUiState
}
```

**Explicación:** `ProductUiState` es una sealed interface que define los tres estados posibles de la UI de productos. El uso de `sealed interface` en lugar de `sealed class` es una característica más moderna de Kotlin que permite implementaciones más flexibles mientras mantiene las restricciones de herencia.

- **`Loading`**: Es un `object` (singleton) porque no necesita mantener estado adicional. Representa simplemente el estado de carga.
- **`Success`**: Es una `data class` que contiene la lista de productos obtenidos exitosamente. Al ser una data class, proporciona automáticamente `equals()`, `hashCode()`, `copy()`, etc., lo que facilita la comparación de estados.
- **`Error`**: Es una `data class` que contiene el mensaje de error a mostrar al usuario. El mensaje puede provenir de la excepción capturada o ser un mensaje predefinido.

La naturaleza sealed de esta interfaz permite que el compilador verifique la exhaustividad en expresiones `when`, garantizando que todos los casos sean manejados. Si se agrega un nuevo estado en el futuro, el compilador marcará todos los `when` que no manejen el nuevo caso, previniendo errores en tiempo de ejecución.

**Fragmento del ViewModel (manejo de estados):**
```kotlin
fun getProducts() {
    viewModelScope.launch {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val products = productRepository.getProducts()
            _rawProducts.value = products
        } catch (e: UnknownHostException) {
            _errorMessage.value = "Sin conexión a internet. Verifica tu red."
        } catch (e: IOException) {
            _errorMessage.value = "Error de red al intentar conectar con el servidor."
        } catch (e: Exception) {
            _errorMessage.value = "Error inesperado: ${e.localizedMessage}"
        } finally {
            _isLoading.value = false
        }
    }
}
```

**Explicación:** Este método demuestra el patrón completo de gestión de estados en una operación de red:

1. **Inicialización del estado de carga**: `_isLoading.value = true` indica a la UI que debe mostrar un indicador de carga. Esto se hace antes de iniciar la operación asíncrona para garantizar que la UI responda inmediatamente.

2. **Limpieza de errores previos**: `_errorMessage.value = null` elimina cualquier mensaje de error de operaciones anteriores, proporcionando una experiencia de usuario limpia.

3. **Ejecución asíncrona**: `viewModelScope.launch` inicia una corrutina en el scope del ViewModel. La operación `productRepository.getProducts()` es una función suspend que se ejecuta de manera asíncrona.

4. **Manejo de excepciones específicas**:
   - `UnknownHostException`: Capturada específicamente para proporcionar un mensaje claro sobre falta de conexión a internet. Esta excepción se lanza cuando el dispositivo no puede resolver el nombre de dominio de la API.
   - `IOException`: Captura errores genéricos de I/O, incluyendo timeouts, conexiones rechazadas, y problemas de lectura/escritura. El mensaje indica un problema de red general.
   - `Exception`: Captura cualquier otra excepción no prevista como medida de seguridad. El mensaje incluye `e.localizedMessage` para proporcionar información de depuración.

5. **Limpieza garantizada**: El bloque `finally` siempre se ejecuta, independientemente de si la operación tuvo éxito o falló. `_isLoading.value = false` garantiza que el indicador de carga se oculte en todos los casos.

El estado `uiState` se calcula automáticamente mediante el operador `combine` basándose en `_isLoading`, `_errorMessage`, y `_rawProducts`. Cuando `_isLoading` es true, el estado es `Loading`. Cuando `_errorMessage` no es null, el estado es `Error`. De lo contrario, el estado es `Success` con los productos filtrados.

Este enfoque reactivo significa que la UI no necesita saber cómo se obtienen los datos; simplemente observa el estado y reacciona accordingly. Esto separa completamente la lógica de negocio de la lógica de presentación.

---

### e) Renderizado y Visualización de Datos Dinámicos en Compose

#### Objetivo
Implementar la interfaz de usuario mediante Jetpack Compose utilizando programación declarativa y reactiva, recolectando estados de manera segura para el ciclo de vida mediante `collectAsState`, y controlando el flujo visual con estructuras condicionales para mostrar diferentes interfaces según el estado de la aplicación (Loading, Success, Error).

#### Fundamento Teórico
Jetpack Compose introduce un paradigma de programación declarativa para la UI en Android. A diferencia del enfoque imperativo tradicional donde se manipulan vistas directamente (findViewById, setText, etc.), en Compose se describe cómo debería verse la UI en función de su estado, y el framework se encarga de actualizar la vista cuando el estado cambia.

Los componentes fundamentales de Compose son las funciones `@Composable`, que describen una parte de la UI. Estas funciones pueden aceptar parámetros que representan el estado, y cuando esos parámetros cambian, Compose recompondrá (reconstruirá) automáticamente la función para reflejar el nuevo estado. Este proceso de recomposición es eficiente porque Compose utiliza un sistema de "smart recomposition" que solo recompone los componentes que realmente necesitan actualizarse.

La recolección de estados en Compose se realiza mediante funciones como `collectAsState()`, que convierten flujos reactivos (StateFlow, Flow, LiveData) en estados observables por Compose. `collectAsState()` es seguro para el ciclo de vida porque automáticamente inicia y detiene la recolección cuando el composable entra y sale de la composición, evitando fugas de memoria y actualizaciones innecesarias.

El control de flujo visual en Compose se realiza mediante estructuras condicionales de Kotlin (`if`, `when`) en lugar de vistas ocultas/visibles como en el sistema imperativo. Esto permite describir diferentes UIs para diferentes estados de manera natural y legible.

`LaunchedEffect` es un efecto secundario (side effect) en Compose que se ejecuta cuando el composable entra en la composición. Es ideal para iniciar operaciones que deben ejecutarse una sola vez, como cargar datos iniciales. Acepta una clave (key) que determina cuándo reiniciar el efecto; si la clave cambia, el efecto se cancela y se reinicia.

`LazyColumn` es el equivalente en Compose de RecyclerView, optimizado para mostrar listas largas de manera eficiente. Solo renderiza los elementos visibles en pantalla, reciclando las vistas cuando el usuario hace scroll. Acepta un parámetro `items` que acepta una lista y una función que genera el composable para cada elemento.

#### Código y Explicación

**Fragmento 1: Recolección de estados con collectAsState**
```kotlin
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    onProductClick: (Int) -> Unit,
    onAddProductClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.getProducts()
    }
}
```

**Explicación:** `collectAsState()` convierte los StateFlow del ViewModel en estados observables por Compose. El uso de `by` (delegación de propiedad) simplifica la sintaxis. `LaunchedEffect(Unit)` se ejecuta cuando el composable entra en la composición, garantizando que los productos se carguen automáticamente.

**Fragmento 2: Estado Loading con CircularProgressIndicator**
```kotlin
when (val state = uiState) {
    is ProductUiState.Loading -> {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
```

**Explicación:** Cuando el estado es Loading, se muestra un indicador de carga circular centrado. `CircularProgressIndicator` es el componente estándar de Material Design para indicar progreso indeterminado.

**Fragmento 3: Estado Error con botón de reintento**
```kotlin
is ProductUiState.Error -> {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = state.message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { viewModel.refreshAll() }) {
            Text("Reintentar")
        }
    }
}
```

**Explicación:** Cuando el estado es Error, se muestra una interfaz con icono de advertencia, mensaje de error específico, y botón de reintento. Esto proporciona retroalimentación clara y una ruta de recuperación al usuario.

**Fragmento 4: Estado Success con LazyColumn**
```kotlin
is ProductUiState.Success -> {
    val products = state.products
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = products,
            key = { it.id }
        ) { product ->
            ProductItem(
                product = product,
                onProductClick = onProductClick,
                onEditClick = onEditProductClick,
                onDeleteClick = { viewModel.deleteProduct(it) }
            )
        }
    }
}
```

**Explicación:** Cuando el estado es Success, se renderiza la lista con `LazyColumn`, el equivalente en Compose de RecyclerView. Solo renderiza los elementos visibles, optimizando el rendimiento. El parámetro `key = { it.id }` es crucial para el rendimiento y el mantenimiento del estado del scroll.

---

## 3. SECCIÓN DE EVIDENCIAS SUGERIDAS

Para demostrar de manera irrefutable el correcto funcionamiento de la aplicación "Catálogo Pro" y el cumplimiento de todos los requerimientos del Laboratorio 9, se sugiere incluir las siguientes capturas de pantalla en el informe:

### Evidencia 1: Configuración del Proyecto y Dependencias
**Descripción:** Captura de pantalla del archivo `app/build.gradle.kts` mostrando las dependencias de Retrofit y GSON correctamente configuradas. Debe resaltar las líneas:
```kotlin
implementation(libs.retrofit)
implementation(libs.retrofit.gson)
```
**Propósito:** Demostrar la integración correcta de Retrofit en el proyecto existente (Literal a).

### Evidencia 2: Modelado de Datos y Definición de API
**Descripción:** Captura de pantalla del archivo `data/model/Product.kt` mostrando la data class con todos los campos mapeados del esquema JSON de FakeStoreAPI. Adicionalmente, captura de `data/network/ProductApi.kt` mostrando la interfaz con las anotaciones `@GET`, `@POST`, `@PUT`, `@DELETE`.
**Propósito:** Demostrar el modelado de datos basado en OpenAPI y la definición de endpoints asíncronos (Literal b).

### Evidencia 3: Implementación del Repository
**Descripción:** Captura de pantalla del archivo `data/repository/ProductRepositoryImpl.kt` mostrando la implementación de los métodos con `withContext(Dispatchers.IO)` y las llamadas a la API de Retrofit.
**Propósito:** Demostrar la implementación de la capa Repository y el uso de corrutinas para operaciones de red (Literal c).

### Evidencia 4: Implementación del ViewModel y Gestión de Estados
**Descripción:** Captura de pantalla del archivo `ui/viewmodel/ProductViewModel.kt` mostrando:
- La inicialización de StateFlows para el estado
- El método `getProducts()` con el bloque `try-catch` para manejo de excepciones
- El operador `combine` para calcular el estado reactivo
**Propósito:** Demostrar la implementación del ViewModel con MVVM y la gestión de estados Loading/Success/Error (Literales c y d).

### Evidencia 5: Definición de Estados con Sealed Class
**Descripción:** Captura de pantalla del archivo `ui/state/ProductUiState.kt` mostrando la sealed interface con los tres estados: Loading, Success, y Error.
**Propósito:** Demostrar el modelado explícito de estados mediante sealed classes (Literal d).

### Evidencia 6: Pantalla de Loading (Estado de Carga)
**Descripción:** Captura de pantalla de la aplicación ejecutándose en el emulador o dispositivo físico mostrando el indicador de carga (`CircularProgressIndicator`) mientras se obtienen los datos de la API. Debe ser visible el texto "Catálogo Pro" en el TopAppBar.
**Propósito:** Demostrar el correcto manejo del estado Loading y la retroalimentación visual al usuario (Literal d y e).

### Evidencia 7: Pantalla de Success (Lista de Productos Cargada)
**Descripción:** Captura de pantalla de la aplicación mostrando la lista de productos desplegada con datos reales obtenidos de la API FakeStore. Deben ser visibles:
- Múltiples productos con sus imágenes, títulos, categorías y precios
- La barra de búsqueda
- El filtro de categorías horizontal
- El FloatingActionButton para agregar productos
**Propósito:** Demostrar el consumo exitoso de datos desde la API REST pública y el renderizado dinámico en Compose (Literales b y e).

### Evidencia 8: Pantalla de Error (Simulación de Fallo de Red)
**Descripción:** Captura de pantalla de la aplicación mostrando el estado de error. Para obtener esta captura, se sugiere:
- Activar el modo avión en el dispositivo/emulador
- O desconectar la conexión a internet
- Debe ser visible el icono de advertencia, el mensaje de error ("Sin conexión a internet. Verifica tu red."), y el botón "Reintentar"
**Propósito:** Demostrar el correcto manejo de excepciones de red y la UI de error con opción de reintento (Literal d y e).

### Evidencia 9: Pantalla de Detalle de Producto
**Descripción:** Captura de pantalla de la pantalla `ProductDetailScreen` mostrando los detalles completos de un producto:
- Imagen grande del producto
- Chip de categoría
- Título del producto
- Precio destacado
- Descripción completa
- Botón de edición en el TopAppBar
**Propósito:** Demostrar la navegación entre pantallas y el paso de parámetros (productId) en Compose Navigation (Literal e).

### Evidencia 10: Pantalla de Formulario de Producto
**Descripción:** Captura de pantalla de la pantalla `ProductFormScreen` mostrando:
- Campos para nombre, precio, categoría (dropdown), URL de imagen, y descripción
- Validación de campos (mostrar mensajes de error si los campos están vacíos)
- Botón de guardar
**Propósito:** Demostrar la implementación de formularios en Compose con validación y el uso de dropdowns para selección de categorías (Literal e).

### Evidencia 11: Filtro por Categoría Funcional
**Descripción:** Captura de pantalla mostrando la lista de productos filtrada por una categoría específica (por ejemplo, "electronics"). Debe ser visible el chip de categoría seleccionado en el filtro horizontal y la lista mostrando solo productos de esa categoría.
**Propósito:** Demostrar el filtrado reactivo de datos basado en el estado seleccionado (Literal e).

### Evidencia 12: Búsqueda de Productos Funcional
**Descripción:** Captura de pantalla mostrando la barra de búsqueda con texto ingresado y la lista de productos filtrada según el criterio de búsqueda. Debe ser visible el texto en el campo de búsqueda y los resultados filtrados.
**Propósito:** Demostrar la implementación de búsqueda en tiempo real con StateFlow reactivo (Literal e).

### Evidencia 13: Eliminación de Producto
**Descripción:** Captura en secuencia (o collage) mostrando:
1. La lista de productos antes de la eliminación
2. El clic en el botón de eliminar de un producto
3. La lista después de la eliminación (el producto ya no aparece)
**Propósito:** Demostrar la implementación de operaciones DELETE y la actualización reactiva de la UI (Literal b y e).

### Evidencia 14: Agregar Nuevo Producto
**Descripción:** Captura en secuencia mostrando:
1. El formulario vacío para agregar un nuevo producto
2. El formulario completado con datos de prueba
3. La lista de productos después de agregar el nuevo producto (el nuevo producto aparece en la lista)
**Propósito:** Demostrar la implementación de operaciones POST y la actualización reactiva de la UI (Literal b y e).

### Evidencia 15: Logs de Red (Opcional pero Recomendado)
**Descripción:** Captura de pantalla del Logcat de Android Studio mostrando las solicitudes HTTP realizadas por Retrofit. Debe ser visible el request GET a `https://fakestoreapi.com/products` y el response con los datos JSON.
**Propósito:** Demostrar que Retrofit está realizando correctamente las solicitudes HTTP y recibiendo las respuestas de la API (Literal b).

---

## 4. CONCLUSIONES ACADÉMICAS

### Conclusión 1: Beneficios Arquitectónicos del Patrón MVVM en Compose

La implementación del patrón arquitectónico Model-View-ViewModel en el contexto de Jetpack Compose ha demostrado ser altamente efectiva para lograr una separación clara de responsabilidades y mejorar la mantenibilidad del código. A diferencia del enfoque tradicional donde la lógica de presentación y la lógica de negocio estaban entrelazadas en Activities y Fragments, MVVM permite que el ViewModel actúe como un intermediario que gestiona el estado de la UI y coordina las operaciones asíncronas, mientras que la View (Composables) se limita a describir cómo se visualiza ese estado. Esta separación facilita significativamente el testing unitario: los ViewModels pueden probarse de manera aislada sin necesidad de una UI, y los Composables pueden probarse con estados mockeados sin necesidad de un ViewModel real. Además, el ViewModel sobrevive a los cambios de configuración como las rotaciones de pantalla, eliminando la necesidad de manejar manualmente la restauración del estado y reduciendo la posibilidad de errores de pérdida de datos. La combinación de MVVM con el sistema de inyección de dependencias implementado mediante ViewModelFactory proporciona una flexibilidad adicional para cambiar implementaciones sin afectar el código de la UI, siguiendo el principio de inversión de dependencias de SOLID.

### Conclusión 2: Ventajas del Tipado Fuerte de Retrofit y GSON

La integración de Retrofit con GSON proporciona un nivel de seguridad tipográfica que es fundamental para el desarrollo robusto de aplicaciones Android. A diferencia de enfoques basados en parsing manual de JSON o bibliotecas dinámicas, Retrofit + GSON garantiza que los errores de mapeo de datos se detecten en tiempo de compilación en lugar de tiempo de ejecución. La definición de interfaces fuertemente tipadas para los endpoints de la API permite que el compilador verifique la corrección de los tipos de retorno y parámetros, previniendo errores comunes como intentar asignar un string a un campo numérico. GSON realiza la deserialización automática mediante reflexión, pero el uso de data classes de Kotlin con propiedades inmutables garantiza que los datos no puedan ser modificados accidentalmente después de su deserialización, lo que es crucial para mantener la consistencia del estado en una arquitectura reactiva. Además, el tipado fuerte facilita la refactorización: si la API cambia y se modifica el esquema JSON, el compilador marcará todos los lugares donde el código necesita actualización, reduciendo significativamente el riesgo de regresiones. La integración de Retrofit con Coroutines mediante funciones suspendidas elimina la necesidad de callbacks o listeners, resultando en un código más lineal y fácil de leer que se comporta de manera síncrona aunque se ejecute de manera asíncrona.

### Conclusión 3: Eficiencia de las Corrutinas frente al Bloqueo del Hilo Principal

El uso de Kotlin Coroutines para operaciones de red representa una mejora significativa respecto a enfoques tradicionales basados en callbacks o Threads. Las corrutinas permiten escribir código asíncrono de manera secuencial y legible, similar a código síncrono, pero sin bloquear el hilo principal. Esto es crítico en Android porque el bloqueo del hilo principal por más de unos pocos segundos resulta en una ANR (Application Not Responding) y una mala experiencia de usuario. Con Coroutines, las operaciones de red se ejecutan en el dispatcher IO, que está optimizado para operaciones de I/O, mientras que el hilo principal permanece libre para responder a interacciones del usuario y renderizar la UI. El operador `withContext(Dispatchers.IO)` permite cambiar explícitamente de contexto cuando es necesario, garantizando que las operaciones de red no se ejecuten accidentalmente en el hilo principal. Además, el `viewModelScope` proporciona integración automática con el ciclo de vida del ViewModel: todas las corrutinas iniciadas en este scope se cancelan automáticamente cuando el ViewModel se destruye, evitando fugas de memoria y operaciones innecesarias en segundo plano. Esta gestión automática del ciclo de vida es superior a enfoques manuales donde el desarrollador debe recordar cancelar callbacks o Threads, reduciendo la posibilidad de errores y mejorando la eficiencia del uso de recursos.

### Conclusión 4: Impacto del Manejo de Estados en la Experiencia de Usuario (UX)

La implementación de un sistema explícito de gestión de estados mediante sealed classes y StateFlow tiene un impacto directo y positivo en la experiencia de usuario. Al modelar explícitamente los estados Loading, Success, y Error, la aplicación puede proporcionar retroalimentación visual inmediata y apropiada en cada etapa del ciclo de vida de una operación asíncrona. El estado Loading con un indicador de carga circular informa al usuario que la aplicación está trabajando, reduciendo la percepción de lentitud y evitando que el usuario realice acciones prematuras. El estado Error con un mensaje específico y un botón de reintento transforma una situación potencialmente frustrante en una experiencia controlada donde el usuario entiende qué salió mal y cómo recuperarse. El estado Success presenta los datos de manera clara y organizada, permitiendo al usuario interactuar con la información de manera eficiente. La naturaleza reactiva de StateFlow garantiza que la UI siempre refleje el estado actual del sistema, eliminando inconsistencias visuales que pueden confundir al usuario. Además, el filtrado y búsqueda reactivos implementados mediante el operador `combine` permiten que la UI responda instantáneamente a las interacciones del usuario sin necesidad de recargas manuales, creando una experiencia fluida y moderna. Este enfoque centrado en el estado, combinado con la programación declarativa de Compose, resulta en aplicaciones que son más robustas, más fáciles de depurar, y que proporcionan una experiencia de usuario superior en comparación con enfoques imperativos tradicionales.

---

## REFERENCIAS BIBLIOGRÁFICAS

1. Google. (2024). *Jetpack Compose Documentation*. Android Developers. https://developer.android.com/jetpack/compose

2. Square, Inc. (2024). *Retrofit Documentation*. https://square.github.io/retrofit/

3. Google. (2024). *Kotlin Coroutines Guide*. https://developer.android.com/kotlin/coroutines

4. Google. (2024). *Guide to App Architecture*. Android Developers. https://developer.android.com/topic/architecture

5. FakeStoreAPI. (2024). *FakeStoreAPI Documentation*. https://fakestoreapi.com/docs

6. Kotlin. (2024). *Sealed Classes Documentation*. https://kotlinlang.org/docs/sealed-classes.html

7. Google. (2024). *StateFlow and SharedFlow Documentation*. https://developer.android.com/kotlin/flow/stateflow-and-sharedflow

8. Google. (2024). *Material Design 3 for Android*. https://m3.material.io/

---

**FIN DEL INFORME TÉCNICO**
