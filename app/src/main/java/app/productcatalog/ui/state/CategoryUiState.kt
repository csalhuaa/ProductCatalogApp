package app.productcatalog.ui.state

import app.productcatalog.data.model.Category

sealed interface CategoryUiState {
    object Loading : CategoryUiState
    data class Success(val categories: List<Category>) : CategoryUiState
    data class Error(val message: String) : CategoryUiState
}
