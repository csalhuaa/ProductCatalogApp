package app.productcatalog.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.productcatalog.data.local.entity.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    // Consulta todos los productos retornando un Flow constante
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<ProductEntity>>

    // Insertar/Actualizar una lista de productos en lote
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    // Insertar/Actualizar un solo producto
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    // Eliminar un producto específico
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Int)

    // Limpiar toda la tabla local
    @Query("DELETE FROM products")
    suspend fun deleteAllProducts()
}
