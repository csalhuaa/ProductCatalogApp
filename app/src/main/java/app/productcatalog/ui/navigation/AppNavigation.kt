package app.productcatalog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.productcatalog.data.local.AppDatabase
import app.productcatalog.data.repository.ProductRepositoryImpl
import app.productcatalog.ui.screens.ProductDetailScreen
import app.productcatalog.ui.screens.ProductFormScreen
import app.productcatalog.ui.screens.ProductListScreen
import app.productcatalog.ui.viewmodel.ProductViewModel
import app.productcatalog.ui.viewmodel.ViewModelFactory

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Inicialización de la capa de datos local
    val database = remember { AppDatabase.getDatabase(context) }
    val productRepository = remember { ProductRepositoryImpl(database.productDao()) }

    // Fábrica manual para inyección de dependencias
    val factory = remember { ViewModelFactory(productRepository) }

    // Instanciar el ViewModel
    val productViewModel: ProductViewModel = viewModel(factory = factory)

    NavHost(
        navController = navController,
        startDestination = Screen.ProductList.route,
        modifier = modifier
    ) {
        // Listado y Búsqueda de Productos
        composable(Screen.ProductList.route) {
            ProductListScreen(
                viewModel = productViewModel,
                onProductClick = { productId ->
                    navController.navigate(Screen.ProductDetail.createRoute(productId))
                },
                onAddProductClick = {
                    navController.navigate(Screen.ProductForm.createRoute(null))
                },
                onEditProductClick = { productId ->
                    navController.navigate(Screen.ProductForm.createRoute(productId))
                }
            )
        }

        // Detalle de Producto
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            ProductDetailScreen(
                productId = productId,
                viewModel = productViewModel,
                onBackClick = {
                    navController.popBackStack()
                },
                onEditClick = { id ->
                    navController.navigate(Screen.ProductForm.createRoute(id))
                }
            )
        }

        // Crear/Editar Producto
        composable(
            route = Screen.ProductForm.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val productIdArg = backStackEntry.arguments?.getInt("productId") ?: -1
            val productId = if (productIdArg == -1) null else productIdArg

            ProductFormScreen(
                productId = productId,
                viewModel = productViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
