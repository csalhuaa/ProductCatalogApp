package app.productcatalog.data.repository

import app.productcatalog.data.datasource.FakeDataSource
import app.productcatalog.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl : CategoryRepository {

    override suspend fun getCategories(): List<Category> = withContext(Dispatchers.IO) {
        delay(400) // Simular latencia de red/base de datos
        FakeDataSource.getCategories()
    }

    override suspend fun getCategoryById(id: Int): Category? = withContext(Dispatchers.IO) {
        delay(300)
        FakeDataSource.getCategoryById(id)
    }

    override suspend fun insertCategory(category: Category): Category = withContext(Dispatchers.IO) {
        delay(300)
        FakeDataSource.insertCategory(category)
    }

    override suspend fun updateCategory(category: Category): Boolean = withContext(Dispatchers.IO) {
        delay(300)
        FakeDataSource.updateCategory(category)
    }

    override suspend fun deleteCategory(id: Int): Boolean = withContext(Dispatchers.IO) {
        delay(300)
        FakeDataSource.deleteCategory(id)
    }
}
