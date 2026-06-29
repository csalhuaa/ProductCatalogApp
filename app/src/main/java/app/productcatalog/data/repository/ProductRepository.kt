package app.productcatalog.data.repository

import app.productcatalog.data.model.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun addProduct(product: Product): Product
    suspend fun updateProduct(id: Int, product: Product): Product
    suspend fun deleteProduct(id: Int): Boolean
}
