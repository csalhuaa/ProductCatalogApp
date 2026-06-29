package app.productcatalog.data.model

import com.google.gson.annotations.SerializedName

data class Product(
    val id: Int = 0,
    val title: String,
    val price: Double,
    val description: String,
    val category: String,
    val image: String
)
