package app.productcatalog.data.repository

import app.productcatalog.data.model.Product

interface ProductRepository {
    suspend fun getProducts(): List<Product>
    suspend fun getProductById(id: Int): Product?
    suspend fun insertProduct(product: Product): Product
    suspend fun updateProduct(product: Product): Boolean
    suspend fun deleteProduct(id: Int): Boolean
}
