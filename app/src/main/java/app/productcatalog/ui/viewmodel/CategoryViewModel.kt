package app.productcatalog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.productcatalog.data.model.Category
import app.productcatalog.data.repository.CategoryRepository
import app.productcatalog.ui.state.CategoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryViewModel(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _rawCategories = MutableStateFlow<List<Category>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _errorMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CategoryUiState> = combine(
        _rawCategories,
        _isLoading,
        _errorMessage
    ) { categories, loading, error ->
        when {
            loading -> CategoryUiState.Loading
            error != null -> CategoryUiState.Error(error)
            else -> CategoryUiState.Success(categories)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CategoryUiState.Loading
    )

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                _rawCategories.value = categoryRepository.getCategories()
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar categorías: ${e.localizedMessage ?: "Desconocido"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveCategory(
        id: Int,
        nombre: String,
        descripcion: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val category = Category(id = id, nombre = nombre, descripcion = descripcion)
                if (id == 0) {
                    categoryRepository.insertCategory(category)
                } else {
                    categoryRepository.updateCategory(category)
                }
                loadCategories()
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Error al guardar categoría: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val success = categoryRepository.deleteCategory(id)
                if (success) {
                    _rawCategories.value = _rawCategories.value.filter { it.id != id }
                } else {
                    _errorMessage.value = "No se pudo eliminar la categoría."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar categoría: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
