package app.productcatalog.ui.state

import app.productcatalog.data.model.Product

sealed interface ProductUiState {
    object Loading : ProductUiState
    data class Success(val products: List<Product>) : ProductUiState
    data class Error(val message: String) : ProductUiState
}
