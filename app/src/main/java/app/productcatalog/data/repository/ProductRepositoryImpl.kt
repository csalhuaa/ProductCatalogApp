package app.productcatalog.data.repository

import app.productcatalog.data.local.dao.ProductDao
import app.productcatalog.data.local.entity.toDomainModel
import app.productcatalog.data.local.entity.toEntity
import app.productcatalog.data.model.Product
import app.productcatalog.data.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ProductRepositoryImpl(
    private val productDao: ProductDao
) : ProductRepository {

    private val api = RetrofitClient.productApi

    // El flujo directo desde la base de datos local
    override val products: Flow<List<Product>> = productDao.getAllProducts().map { entities ->
        entities.map { it.toDomainModel() }
    }

    override suspend fun refreshProducts() = withContext(Dispatchers.IO) {
        // Intenta realizar la llamada a la API
        val remoteProducts = api.getAllProducts()
        // Si es exitosa, inserta los nuevos datos en el DAO
        productDao.insertProducts(remoteProducts.map { it.toEntity() })
    }

    override suspend fun addProduct(product: Product): Product = withContext(Dispatchers.IO) {
        val remoteProduct = api.addProduct(product)
        productDao.insertProduct(remoteProduct.toEntity())
        remoteProduct
    }

    override suspend fun updateProduct(id: Int, product: Product): Product = withContext(Dispatchers.IO) {
        val remoteProduct = api.updateProduct(id, product)
        productDao.insertProduct(remoteProduct.toEntity())
        remoteProduct
    }

    override suspend fun deleteProduct(id: Int): Boolean = withContext(Dispatchers.IO) {
        val response = api.deleteProduct(id)
        if (response.isSuccessful) {
            productDao.deleteProduct(id)
            true
        } else {
            false
        }
    }
}
