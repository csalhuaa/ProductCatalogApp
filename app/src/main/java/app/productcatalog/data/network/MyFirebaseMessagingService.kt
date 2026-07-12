package app.productcatalog.data.network

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import app.productcatalog.MainActivity
import app.productcatalog.data.local.AppDatabase
import app.productcatalog.data.repository.ProductRepositoryImpl
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "products_updates_channel"
    }

    /**
     * Se llama cuando Firebase genera un nuevo token para este dispositivo.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo Token FCM: $token")
        // Aquí podrías enviar el token a tu backend si lo necesitas
    }

    /**
     * Se llama cuando se recibe un mensaje mientras la app está en PRIMER PLANO 
     * o si es un Data Message (se recibe siempre).
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        // 1. Manejo de Notification Message (Payload visual)
        message.notification?.let { notification ->
            val title = notification.title ?: "Nueva actualización"
            val body = notification.body ?: "Revisa nuestro catálogo"
            
            // Verificamos si trae un deep link oculto en la data
            val productId = message.data["productId"]
            
            showLocalNotification(title, body, productId)
        }

        // 2. Manejo de Data Message (Payload oculto de datos) e Integración Offline-First
        if (message.data.isNotEmpty()) {
            val syncAction = message.data["action"]
            
            // Si el backend nos dice "update_catalog", reaccionamos en segundo plano
            if (syncAction == "update_catalog") {
                syncCatalogBackground()
            }
        }
    }

    /**
     * INTEGRACIÓN MVVM: Sincronización en segundo plano (Offline-First)
     * Cuando llega un push silencioso, el servicio despierta y descarga 
     * la API hacia Room. La UI reaccionará sola al flujo.
     */
    private fun syncCatalogBackground() {
        // Obtenemos las dependencias de forma manual (Service Locator rudimentario)
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ProductRepositoryImpl(database.productDao())

        // Usamos un CoroutineScope atado al Dispatcher de IO
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Iniciando sincronización de catálogo por Push...")
                repository.refreshProducts()
                Log.d(TAG, "Catálogo sincronizado exitosamente en Room.")
            } catch (e: Exception) {
                Log.e(TAG, "Error sincronizando catálogo en background", e)
            }
        }
    }

    /**
     * NOTIFICACIONES LOCALES Y DEEP LINKS: Construye la notificación visual
     */
    private fun showLocalNotification(title: String, body: String, productId: String?) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal (Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Actualizaciones del Catálogo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre nuevos productos o cambios."
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 4. NAVEGACIÓN Y DEEP LINKS: Configurar el Intent que se abrirá al tocar
        val intent = Intent(
            Intent.ACTION_VIEW,
            // Si hay productId, generamos el Deep Link, si no, abrimos la app normal
            Uri.parse(if (productId != null) "catalogopro://app/producto/$productId" else "catalogopro://app/")
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Crear el PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Usa un ícono base de Android temporalmente
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        // Mostrar la notificación (usando un ID único basado en el tiempo)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
