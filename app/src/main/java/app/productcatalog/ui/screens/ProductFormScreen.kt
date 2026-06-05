package app.productcatalog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import app.productcatalog.ui.viewmodel.ProductViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: Int?,
    viewModel: ProductViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = productId != null
    val categories by viewModel.categories.collectAsState()

    // Estados de los campos del formulario
    var nombre by remember { mutableStateOf("") }
    var precioStr by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var imagenUrl by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }

    // Estados de error para validación
    var nombreError by remember { mutableStateOf<String?>(null) }
    var precioError by remember { mutableStateOf<String?>(null) }
    var descripcionError by remember { mutableStateOf<String?>(null) }
    var categoriaError by remember { mutableStateOf<String?>(null) }

    // Si es modo edición, cargar datos iniciales del producto
    LaunchedEffect(productId) {
        if (isEditMode && productId != null) {
            val product = viewModel.getProductById(productId)
            if (product != null) {
                nombre = product.nombre
                precioStr = product.precio.toString()
                descripcion = product.descripcion
                imagenUrl = product.imagen
                selectedCategoryId = product.idCategoria
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "Editar Producto" else "Nuevo Producto",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Campo Nombre
            OutlinedTextField(
                value = nombre,
                onValueChange = {
                    nombre = it
                    if (it.isNotBlank()) nombreError = null
                },
                label = { Text("Nombre del Producto") },
                isError = nombreError != null,
                supportingText = { nombreError?.let { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Precio
            OutlinedTextField(
                value = precioStr,
                onValueChange = {
                    precioStr = it
                    if (it.toDoubleOrNull() != null && it.toDouble() > 0) precioError = null
                },
                label = { Text("Precio ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = precioError != null,
                supportingText = { precioError?.let { Text(it) } },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown de Categoría
            var dropdownExpanded by remember { mutableStateOf(false) }
            val selectedCategoryName = categories.find { it.id == selectedCategoryId }?.nombre ?: ""

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Categoría") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    isError = categoriaError != null,
                    supportingText = { categoriaError?.let { Text(it) } },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    if (categories.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No hay categorías disponibles") },
                            onClick = { dropdownExpanded = false }
                        )
                    } else {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.nombre) },
                                onClick = {
                                    selectedCategoryId = category.id
                                    categoriaError = null
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Campo Imagen URL
            OutlinedTextField(
                value = imagenUrl,
                onValueChange = { imagenUrl = it },
                label = { Text("URL de Imagen (Opcional)") },
                placeholder = { Text("https://example.com/imagen.jpg") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo Descripción
            OutlinedTextField(
                value = descripcion,
                onValueChange = {
                    descripcion = it
                    if (it.isNotBlank()) descripcionError = null
                },
                label = { Text("Descripción") },
                isError = descripcionError != null,
                supportingText = { descripcionError?.let { Text(it) } },
                maxLines = 5,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botón de Guardar
            Button(
                onClick = {
                    // Validar los campos del formulario
                    var isValid = true
                    
                    if (nombre.isBlank()) {
                        nombreError = "El nombre es obligatorio"
                        isValid = false
                    }
                    
                    val precio = precioStr.toDoubleOrNull()
                    if (precio == null || precio <= 0) {
                        precioError = "Ingresa un precio válido mayor a 0"
                        isValid = false
                    }
                    
                    if (selectedCategoryId == null) {
                        categoriaError = "Debes seleccionar una categoría"
                        isValid = false
                    }
                    
                    if (descripcion.isBlank()) {
                        descripcionError = "La descripción es obligatoria"
                        isValid = false
                    }

                    if (isValid && precio != null && selectedCategoryId != null) {
                        viewModel.saveProduct(
                            id = productId ?: 0,
                            nombre = nombre,
                            precio = precio,
                            descripcion = descripcion,
                            imagen = imagenUrl,
                            idCategoria = selectedCategoryId!!,
                            onSuccess = onNavigateBack
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Guardar",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = if (isEditMode) "Guardar Cambios" else "Crear Producto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
