package com.escom.examenfinal.ui.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.ViewModelProvider
import com.escom.examenfinal.R
import com.escom.examenfinal.databinding.ActivityMapBinding
import com.escom.examenfinal.service.LocationTrackingService
import com.escom.examenfinal.ui.history.HistoryActivity
import com.escom.examenfinal.utils.ThemeHelper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar

class MapActivity : AppCompatActivity(), OnMapReadyCallback, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMapBinding
    private lateinit var viewModel: MapViewModel
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    private var isTracking = false
    private var selectedInterval = 10 // segundos
    private var showNotification = true
    private var currentLocation: LatLng? = null
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true -> {
                enableMyLocation()
                getCurrentLocation()
            }
            else -> {
                Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Aplicar tema
        val theme = ThemeHelper.getTheme(this)
        setTheme(if (theme == ThemeHelper.THEME_IPN) R.style.Theme_ExamenFinal_IPN else R.style.Theme_ExamenFinal_ESCOM)
        
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        
        // Setup Navigation Drawer
        val toggle = ActionBarDrawerToggle(
            this, binding.drawerLayout, binding.toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        binding.navView.setNavigationItemSelectedListener(this)
        
        // ViewModel
        viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        
        // Location Client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Load settings
        showNotification = ThemeHelper.getShowNotification(this)
        updateNotificationIcon()
        
        // Setup Map
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        
        setupListeners()
        observeLocations()
        checkPermissions()
    }

    private fun setupListeners() {
        // Interval selector
        binding.chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            selectedInterval = when (checkedIds.firstOrNull()) {
                R.id.chip10s -> 10
                R.id.chip60s -> 60
                R.id.chip5min -> 300
                else -> 10
            }
        }
        
        // Start/Stop button
        binding.btnToggleTracking.setOnClickListener {
            toggleTracking()
        }
        
        // History button
        binding.btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    private fun observeLocations() {
        viewModel.allLocations.observe(this) { locations ->
            if (locations.isNotEmpty()) {
                val latLngList = locations.map { LatLng(it.latitude, it.longitude) }
                
                // Dibujar ruta
                googleMap.clear()
                if (latLngList.size > 1) {
                    googleMap.addPolyline(
                        PolylineOptions()
                            .addAll(latLngList)
                            .color(getColor(R.color.route_color))
                            .width(10f)
                    )
                }
                
                // Marcador actual
                val lastLocation = latLngList.last()
                googleMap.addMarker(
                    MarkerOptions()
                        .position(lastLocation)
                        .title("Ubicación Actual")
                )
                
                // Actualizar UI
                val last = locations.last()
                binding.tvLatitude.text = "Latitud: ${String.format("%.6f", last.latitude)}"
                binding.tvLongitude.text = "Longitud: ${String.format("%.6f", last.longitude)}"
                binding.tvAccuracy.text = "Precisión: ${String.format("%.2f", last.accuracy)} m"
            }
        }
    }

    private fun checkPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            enableMyLocation()
            getCurrentLocation()
        }
    }

    private fun enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    currentLocation = LatLng(it.latitude, it.longitude)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 15f))
                    
                    binding.tvLatitude.text = "Latitud: ${String.format("%.6f", it.latitude)}"
                    binding.tvLongitude.text = "Longitud: ${String.format("%.6f", it.longitude)}"
                    binding.tvAccuracy.text = "Precisión: ${String.format("%.2f", it.accuracy)} m"
                }
            }
        }
    }

    private fun toggleTracking() {
        if (isTracking) {
            stopTracking()
        } else {
            startTracking()
        }
    }

    private fun startTracking() {
        // Check background location permission for Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            AlertDialog.Builder(this)
                .setTitle("Permiso de ubicación en segundo plano")
                .setMessage("Para rastrear tu ubicación en segundo plano, necesitamos el permiso 'Permitir siempre'")
                .setPositiveButton("Conceder") { _, _ ->
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                        100
                    )
                }
                .setNegativeButton("Cancelar", null)
                .show()
            return
        }
        
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_START
            putExtra(LocationTrackingService.EXTRA_INTERVAL, selectedInterval.toLong())
            putExtra(LocationTrackingService.EXTRA_SHOW_NOTIFICATION, showNotification)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        
        isTracking = true
        updateTrackingUI()
        
        val message = if (showNotification) {
            "✅ Rastreo iniciado con notificación"
        } else {
            "⚠️ Rastreo iniciado sin notificación"
        }
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    private fun stopTracking() {
        val intent = Intent(this, LocationTrackingService::class.java).apply {
            action = LocationTrackingService.ACTION_STOP
        }
        startService(intent)
        
        isTracking = false
        updateTrackingUI()
        
        Snackbar.make(binding.root, "⏹️ Rastreo detenido", Snackbar.LENGTH_SHORT).show()
    }

    private fun updateTrackingUI() {
        if (isTracking) {
            binding.btnToggleTracking.text = "Detener"
            binding.btnToggleTracking.setBackgroundColor(getColor(R.color.stop_color))
            binding.tvTrackingStatus.text = "Rastreo Activo"
            binding.ivTrackingIcon.setImageResource(R.drawable.ic_gps_fixed)
        } else {
            binding.btnToggleTracking.text = "Iniciar"
            binding.btnToggleTracking.setBackgroundColor(getColor(R.color.start_color))
            binding.tvTrackingStatus.text = "Rastreo Inactivo"
            binding.ivTrackingIcon.setImageResource(R.drawable.ic_gps_off)
        }
    }

    private fun updateNotificationIcon() {
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        menu?.findItem(R.id.action_notification)?.setIcon(
            if (showNotification) R.drawable.ic_notifications_active
            else R.drawable.ic_notifications_off
        )
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_notification -> {
                if (isTracking) {
                    AlertDialog.Builder(this)
                        .setTitle("⚠️ Rastreo Activo")
                        .setMessage("Debes detener el rastreo antes de cambiar la configuración de notificación.")
                        .setPositiveButton("Entendido", null)
                        .show()
                } else {
                    showNotification = !showNotification
                    ThemeHelper.saveShowNotification(this, showNotification)
                    updateNotificationIcon()
                    
                    val message = if (showNotification) "🔔 Notificaciones activadas" else "🔕 Notificaciones desactivadas"
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_theme_ipn -> {
                ThemeHelper.saveTheme(this, ThemeHelper.THEME_IPN)
                recreate()
            }
            R.id.nav_theme_escom -> {
                ThemeHelper.saveTheme(this, ThemeHelper.THEME_ESCOM)
                recreate()
            }
            R.id.nav_clear_history -> {
                showClearHistoryDialog()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun showClearHistoryDialog() {
        AlertDialog.Builder(this)
            .setTitle("Confirmar")
            .setMessage("¿Deseas eliminar todo el historial?")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.clearHistory()
                googleMap.clear()
                Toast.makeText(this, "Historial eliminado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        enableMyLocation()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
