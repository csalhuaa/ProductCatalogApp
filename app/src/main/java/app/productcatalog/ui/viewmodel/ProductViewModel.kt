package app.productcatalog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.productcatalog.data.model.Category
import app.productcatalog.data.model.Product
import app.productcatalog.data.repository.CategoryRepository
import app.productcatalog.data.repository.ProductRepository
import app.productcatalog.ui.state.ProductUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductViewModel(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _rawProducts = MutableStateFlow<List<Product>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Int?>(null)
    val selectedCategoryId: StateFlow<Int?> = _selectedCategoryId.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Estado reactivo de la UI combinando la lista de productos, la búsqueda y el filtro por categoría
    val uiState: StateFlow<ProductUiState> = combine(
        _rawProducts,
        _searchQuery,
        _selectedCategoryId,
        _isLoading,
        _errorMessage
    ) { products, query, catId, loading, error ->
        when {
            loading -> ProductUiState.Loading
            error != null -> ProductUiState.Error(error)
            else -> {
                val filtered = products.filter { product ->
                    val matchesQuery = product.nombre.contains(query, ignoreCase = true) ||
                            product.descripcion.contains(query, ignoreCase = true)
                    val matchesCategory = catId == null || product.idCategoria == catId
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

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // Cargar categorías primero para asegurar consistencia
                val cats = categoryRepository.getCategories()
                _categories.value = cats

                val prods = productRepository.getProducts()
                _rawProducts.value = prods
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar los datos: ${e.localizedMessage ?: "Desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchProducts(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: Int?) {
        _selectedCategoryId.value = categoryId
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = productRepository.deleteProduct(id)
                if (success) {
                    // Actualizar el estado local sin necesidad de recargar completamente
                    _rawProducts.value = _rawProducts.value.filter { it.id != id }
                } else {
                    _errorMessage.value = "No se pudo eliminar el producto."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveProduct(
        id: Int,
        nombre: String,
        precio: Double,
        descripcion: String,
        imagen: String,
        idCategoria: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val imageUrl = imagen.ifBlank { "https://images.unsplash.com/photo-1531403009284-440f080d1e12?q=80&w=600&auto=format&fit=crop" }
                val product = Product(
                    id = id,
                    nombre = nombre,
                    precio = precio,
                    descripcion = descripcion,
                    imagen = imageUrl,
                    idCategoria = idCategoria
                )

                if (id == 0) {
                    productRepository.insertProduct(product)
                } else {
                    productRepository.updateProduct(product)
                }
                refreshAll()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getProductById(id: Int): Product? {
        return _rawProducts.value.find { it.id == id }
    }
}
