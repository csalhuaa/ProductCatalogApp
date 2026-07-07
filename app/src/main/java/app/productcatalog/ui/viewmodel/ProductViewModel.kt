package app.productcatalog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.productcatalog.data.model.Product
import app.productcatalog.data.repository.ProductRepository
import app.productcatalog.ui.state.ProductUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException

class ProductViewModel(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)
    
    // Estado para indicar si estamos offline
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Mantener la lista actual de productos para el acceso síncrono
    private val _currentProducts = productRepository.products.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    // Estado reactivo de la UI combinando el Flow del repositorio y los estados locales
    val uiState: StateFlow<ProductUiState> = combine(
        productRepository.products,
        _searchQuery,
        _selectedCategory,
        _isLoading,
        _errorMessage
    ) { products, query, category, loading, error ->
        when {
            // Si está cargando y no hay datos, muestra Loading
            loading && products.isEmpty() -> ProductUiState.Loading
            // Si hay error y no hay datos, muestra Error
            error != null && products.isEmpty() && !_isOffline.value -> ProductUiState.Error(error)
            // Si hay datos, los mostramos independientemente del estado de carga/error (Offline-First)
            else -> {
                val filtered = products.filter { product ->
                    val matchesQuery = product.title.contains(query, ignoreCase = true) ||
                            product.description.contains(query, ignoreCase = true)
                    val matchesCategory = category == null || product.category == category
                    matchesQuery && matchesCategory
                }
                ProductUiState.Success(filtered)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductUiState.Loading
    )

    // Lista de categorías únicas extraídas dinámicamente de Room
    val categories: StateFlow<List<String>> = productRepository.products.map { products ->
        products.map { it.category }.distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshProducts()
    }

    fun getProductById(id: Int): Product? {
        return _currentProducts.value.find { it.id == id }
    }

    private fun refreshProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _isOffline.value = false
            try {
                productRepository.refreshProducts()
            } catch (e: UnknownHostException) {
                _isOffline.value = true
                _errorMessage.value = "Sin conexión a internet. Mostrando datos locales."
            } catch (e: IOException) {
                _isOffline.value = true
                _errorMessage.value = "Error de red al intentar sincronizar. Mostrando datos locales."
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProducts(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            try {
                val success = productRepository.deleteProduct(id)
                if (!success) {
                    _errorMessage.value = "No se pudo eliminar el producto remotamente."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar el producto: ${e.localizedMessage}"
            }
        }
    }

    fun saveProduct(
        id: Int,
        title: String,
        price: Double,
        description: String,
        category: String,
        image: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val product = Product(
                    id = id,
                    title = title,
                    price = price,
                    description = description,
                    category = category,
                    image = image
                )

                if (id == 0) {
                    productRepository.addProduct(product)
                } else {
                    productRepository.updateProduct(id, product)
                }
                // No es necesario llamar a refreshProducts(), Room emitirá los cambios automáticamente
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Mantenemos el método para recargar manualmente desde la UI
    fun refreshAll() {
        refreshProducts()
    }
}
