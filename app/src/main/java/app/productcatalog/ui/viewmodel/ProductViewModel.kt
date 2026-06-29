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

    private val _rawProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Estado reactivo de la UI
    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = combine(
        _rawProducts,
        _searchQuery,
        _selectedCategory,
        _isLoading,
        _errorMessage
    ) { products, query, category, loading, error ->
        when {
            loading -> ProductUiState.Loading
            error != null -> ProductUiState.Error(error)
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

    // Lista de categorías únicas extraídas de los productos
    val categories: StateFlow<List<String>> = _rawProducts.map { products ->
        products.map { it.category }.distinct()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        getProducts()
    }

    fun getProductById(id: Int): Product? {
        return _rawProducts.value.find { it.id == id }
    }

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
                if (success) {
                    _rawProducts.value = _rawProducts.value.filter { it.id != id }
                }
            } catch (e: Exception) {
                _errorMessage.value = "No se pudo eliminar el producto."
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
                getProducts()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshAll() {
        getProducts()
    }
}
