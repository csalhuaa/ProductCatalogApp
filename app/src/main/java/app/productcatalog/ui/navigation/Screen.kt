package app.productcatalog.ui.navigation

sealed class Screen(val route: String) {
    object ProductList : Screen("productList")

    object ProductDetail : Screen("productDetail/{productId}") {
        fun createRoute(productId: Int) = "productDetail/$productId"
    }

    object ProductForm : Screen("productForm?productId={productId}") {
        fun createRoute(productId: Int?) = if (productId != null) "productForm?productId=$productId" else "productForm"
    }
}
