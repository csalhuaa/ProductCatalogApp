package app.productcatalog.data.repository

import app.productcatalog.data.model.Category

interface CategoryRepository {
    suspend fun getCategories(): List<Category>
    suspend fun getCategoryById(id: Int): Category?
    suspend fun insertCategory(category: Category): Category
    suspend fun updateCategory(category: Category): Boolean
    suspend fun deleteCategory(id: Int): Boolean
}
