package app.productcatalog.data.model

data class Product(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val descripcion: String,
    val imagen: String,
    val idCategoria: Int
)
