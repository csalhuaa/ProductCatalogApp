package app.productcatalog.data.repository

import app.productcatalog.data.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    // Fuente única de verdad local (Room)
    val products: Flow<List<Product>>

    // Sincroniza datos de la red hacia la base de datos local
    suspend fun refreshProducts()

    suspend fun addProduct(product: Product): Product
    suspend fun updateProduct(id: Int, product: Product): Product
    suspend fun deleteProduct(id: Int): Boolean
}
