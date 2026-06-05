package app.productcatalog.data.datasource

import app.productcatalog.data.model.Category
import app.productcatalog.data.model.Product
import java.util.concurrent.atomic.AtomicInteger

object FakeDataSource {
    private val categories = mutableListOf(
        Category(
            id = 1,
            nombre = "Electrónica",
            descripcion = "Smartphones, laptops, accesorios y gadgets tecnológicos de última generación."
        ),
        Category(
            id = 2,
            nombre = "Hogar y Cocina",
            descripcion = "Muebles, electrodomésticos y decoración para hacer de tu hogar un espacio ideal."
        ),
        Category(
            id = 3,
            nombre = "Moda y Calzado",
            descripcion = "Ropa, zapatos y accesorios de tendencia para todas las temporadas."
        ),
        Category(
            id = 4,
            nombre = "Deportes",
            descripcion = "Equipamiento de entrenamiento, calzado deportivo y accesorios de aire libre."
        )
    )

    private val products = mutableListOf(
        // Categoria 1: Electronica
        Product(
            id = 1,
            nombre = "iPhone 15 Pro Max",
            precio = 1199.99,
            descripcion = "Diseño de titanio de grado aeroespacial, chip A17 Pro superpotente y sistema de cámaras de nivel profesional con zoom de 5x.",
            imagen = "https://images.unsplash.com/photo-1695048133142-1a20484d2569?q=80&w=600&auto=format&fit=crop",
            idCategoria = 1
        ),
        Product(
            id = 2,
            nombre = "Auriculares Over-Ear ANC",
            precio = 299.50,
            descripcion = "Cancelación activa de ruido inteligente de primer nivel, sonido de alta resolución y hasta 40 horas de reproducción continua.",
            imagen = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?q=80&w=600&auto=format&fit=crop",
            idCategoria = 1
        ),
        Product(
            id = 3,
            nombre = "Teclado Mecánico RGB",
            precio = 129.99,
            descripcion = "Teclado mecánico con interruptores táctiles de alta velocidad, iluminación RGB por tecla configurable y reposamuñecas ergonómico.",
            imagen = "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?q=80&w=600&auto=format&fit=crop",
            idCategoria = 1
        ),
        Product(
            id = 4,
            nombre = "Smartwatch Deportivo GPS",
            precio = 249.99,
            descripcion = "Monitoreo continuo de salud, GPS integrado de alta precisión, múltiples modos de deporte y batería con autonomía de hasta 7 días.",
            imagen = "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?q=80&w=600&auto=format&fit=crop",
            idCategoria = 1
        ),

        // Categoria 2: Hogar y Cocina
        Product(
            id = 5,
            nombre = "Cafetera Superautomática",
            precio = 450.00,
            descripcion = "Cafetera espresso con molinillo integrado de muelas cónicas. Prepara espressos, capuchinos y lattes con solo pulsar un botón.",
            imagen = "https://images.unsplash.com/photo-1517256064527-09c53b2d0bc6?q=80&w=600&auto=format&fit=crop",
            idCategoria = 2
        ),
        Product(
            id = 6,
            nombre = "Lámpara LED de Escritorio",
            precio = 45.99,
            descripcion = "Lámpara con control táctil, regulación de intensidad y temperatura de color. Incluye puerto USB para carga de dispositivos.",
            imagen = "https://images.unsplash.com/photo-1507473885765-e6ed057f782c?q=80&w=600&auto=format&fit=crop",
            idCategoria = 2
        ),
        Product(
            id = 7,
            nombre = "Licuadora de Alta Velocidad",
            precio = 119.90,
            descripcion = "Motor de 1200W capaz de triturar hielo y alimentos duros en segundos. Jarra de vidrio templado de 1.8 litros libre de BPA.",
            imagen = "https://images.unsplash.com/photo-1578643463396-0997cb5328c1?q=80&w=600&auto=format&fit=crop",
            idCategoria = 2
        ),
        Product(
            id = 8,
            nombre = "Set de Sartenes Antiadherentes",
            precio = 79.99,
            descripcion = "Juego de 3 sartenes de aluminio forjado con revestimiento antiadherente de alta resistencia. Aptos para todo tipo de cocinas, incluida inducción.",
            imagen = "https://images.unsplash.com/photo-1584269600464-37b1b58a9fe7?q=80&w=600&auto=format&fit=crop",
            idCategoria = 2
        ),

        // Categoria 3: Moda y Calzado
        Product(
            id = 9,
            nombre = "Zapatillas UltraBoost",
            precio = 180.00,
            descripcion = "Amortiguación receptiva de máximo retorno de energía, exterior de tejido transpirable Primeknit y suela antideslizante Continental para todo clima.",
            imagen = "https://images.unsplash.com/photo-1542291026-7eec264c27ff?q=80&w=600&auto=format&fit=crop",
            idCategoria = 3
        ),
        Product(
            id = 10,
            nombre = "Chaqueta Impermeable Cortaviento",
            precio = 149.90,
            descripcion = "Chaqueta con costuras selladas térmicamente y tecnología transpirable. Ideal para protegerte de la lluvia y el viento en tus viajes.",
            imagen = "https://images.unsplash.com/photo-1544022613-e87ca75a784a?q=80&w=600&auto=format&fit=crop",
            idCategoria = 3
        ),
        Product(
            id = 11,
            nombre = "Mochila Urbana para Laptop",
            precio = 59.99,
            descripcion = "Compartimento acolchado para laptops de hasta 15.6 pulgadas. Material repelente al agua y diseño ergonómico de correas transpirables.",
            imagen = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?q=80&w=600&auto=format&fit=crop",
            idCategoria = 3
        ),
        Product(
            id = 12,
            nombre = "Gafas de Sol Polarizadas Classic",
            precio = 85.00,
            descripcion = "Protección 100% UV400 contra rayos solares dañinos. Montura ligera de acetato de alta calidad y diseño clásico atemporal.",
            imagen = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?q=80&w=600&auto=format&fit=crop",
            idCategoria = 3
        ),

        // Categoria 4: Deportes
        Product(
            id = 13,
            nombre = "Bicicleta de Montaña Trail",
            precio = 649.99,
            descripcion = "Cuadro de aluminio reforzado, suspensión delantera de 100mm, transmisión Shimano de 24 velocidades y frenos de disco hidráulicos.",
            imagen = "https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=600&auto=format&fit=crop",
            idCategoria = 4
        ),
        Product(
            id = 14,
            nombre = "Esterilla de Yoga Antideslizante",
            precio = 29.99,
            descripcion = "Elaborada con material TPE ecológico de alta densidad. Grosor de 6mm con textura de agarre óptimo en ambas caras para mayor estabilidad.",
            imagen = "https://images.unsplash.com/photo-1592432678016-e910b452f9a2?q=80&w=600&auto=format&fit=crop",
            idCategoria = 4
        ),
        Product(
            id = 15,
            nombre = "Set de Mancuernas Ajustables",
            precio = 199.99,
            descripcion = "Par de mancuernas de peso ajustable de hasta 20kg por mancuerna. Selector rápido de placas de acero macizo y base protectora ergonómica.",
            imagen = "https://images.unsplash.com/photo-1638536532686-d610adfc8e5c?q=80&w=600&auto=format&fit=crop",
            idCategoria = 4
        ),
        Product(
            id = 16,
            nombre = "Balón de Fútbol Premium",
            precio = 34.90,
            descripcion = "Diseño de costuras termoselladas para una trayectoria uniforme y mínima absorción de agua. Cámara de látex para retención óptima de aire.",
            imagen = "https://images.unsplash.com/photo-1508098682722-e99c43a406b2?q=80&w=600&auto=format&fit=crop",
            idCategoria = 4
        )
    )

    private val productIdCounter = AtomicInteger(products.maxOf { it.id })
    private val categoryIdCounter = AtomicInteger(categories.maxOf { it.id })

    @Synchronized
    fun getProducts(): List<Product> = products.toList()

    @Synchronized
    fun getProductById(id: Int): Product? = products.find { it.id == id }

    @Synchronized
    fun insertProduct(product: Product): Product {
        val newId = productIdCounter.incrementAndGet()
        val newProduct = product.copy(id = newId)
        products.add(newProduct)
        return newProduct
    }

    @Synchronized
    fun updateProduct(product: Product): Boolean {
        val index = products.indexOfFirst { it.id == product.id }
        return if (index != -1) {
            products[index] = product
            true
        } else {
            false
        }
    }

    @Synchronized
    fun deleteProduct(id: Int): Boolean {
        return products.removeAll { it.id == id }
    }

    @Synchronized
    fun getCategories(): List<Category> = categories.toList()

    @Synchronized
    fun getCategoryById(id: Int): Category? = categories.find { it.id == id }

    @Synchronized
    fun insertCategory(category: Category): Category {
        val newId = categoryIdCounter.incrementAndGet()
        val newCategory = category.copy(id = newId)
        categories.add(newCategory)
        return newCategory
    }

    @Synchronized
    fun updateCategory(category: Category): Boolean {
        val index = categories.indexOfFirst { it.id == category.id }
        return if (index != -1) {
            categories[index] = category
            true
        } else {
            false
        }
    }

    @Synchronized
    fun deleteCategory(id: Int): Boolean {
        // Al eliminar una categoría, eliminamos también los productos asociados para mantener la consistencia del catálogo
        products.removeAll { it.idCategoria == id }
        return categories.removeAll { it.id == id }
    }
}
