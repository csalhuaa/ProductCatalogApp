package app.productcatalog.data.repository

import app.productcatalog.data.model.Product
import app.productcatalog.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepositoryImpl : ProductRepository {

    private val api = RetrofitClient.productApi

    override suspend fun getProducts(): List<Product> = withContext(Dispatchers.IO) {
        api.getAllProducts()
    }

    override suspend fun addProduct(product: Product): Product = withContext(Dispatchers.IO) {
        api.addProduct(product)
    }

    override suspend fun updateProduct(id: Int, product: Product): Product = withContext(Dispatchers.IO) {
        api.updateProduct(id, product)
    }

    override suspend fun deleteProduct(id: Int): Boolean = withContext(Dispatchers.IO) {
        val response = api.deleteProduct(id)
        response.isSuccessful
    }
}
