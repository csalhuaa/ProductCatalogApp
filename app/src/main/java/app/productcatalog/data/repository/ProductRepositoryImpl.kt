package app.productcatalog.data.repository

import app.productcatalog.data.datasource.FakeDataSource
import app.productcatalog.data.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ProductRepositoryImpl : ProductRepository {

    override suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        delay(500) // Simular latencia de red/base de datos
        FakeDataSource.getProducts()
    }

    override suspend fun getProductById(id: Int): Product? = withContext(Dispatchers.IO) {
        delay(300)
        FakeDataSource.getProductById(id)
    }

    override suspend fun insertProduct(product: Product): Product = withContext(Dispatchers.IO) {
        delay(400)
        FakeDataSource.insertProduct(product)
    }

    override suspend fun updateProduct(product: Product): Boolean = withContext(Dispatchers.IO) {
        delay(400)
        FakeDataSource.updateProduct(product)
    }

    override suspend fun deleteProduct(id: Int): Boolean = withContext(Dispatchers.IO) {
        delay(300)
        FakeDataSource.deleteProduct(id)
    }
}
