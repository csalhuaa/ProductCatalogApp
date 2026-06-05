package app.productcatalog.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.productcatalog.data.repository.CategoryRepository
import app.productcatalog.data.repository.ProductRepository

class ViewModelFactory(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProductViewModel::class.java) -> {
                ProductViewModel(productRepository, categoryRepository) as T
            }
            modelClass.isAssignableFrom(CategoryViewModel::class.java) -> {
                CategoryViewModel(categoryRepository) as T
            }
            else -> throw IllegalArgumentException("Clase ViewModel desconocida: ${modelClass.name}")
        }
    }
}
