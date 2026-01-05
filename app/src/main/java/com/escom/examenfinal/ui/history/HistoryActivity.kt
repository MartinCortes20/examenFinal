package com.escom.examenfinal.ui.history

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.escom.examenfinal.databinding.ActivityHistoryBinding
import com.escom.examenfinal.ui.map.MapViewModel
import com.escom.examenfinal.utils.ThemeHelper

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: MapViewModel
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Aplicar tema
        val theme = ThemeHelper.getTheme(this)
        setTheme(if (theme == ThemeHelper.THEME_IPN) 
            com.escom.examenfinal.R.style.Theme_ExamenFinal_IPN 
            else com.escom.examenfinal.R.style.Theme_ExamenFinal_ESCOM)
        
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Historial de Ubicaciones"
        
        viewModel = ViewModelProvider(this)[MapViewModel::class.java]
        
        adapter = HistoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
        
        viewModel.allLocations.observe(this) { locations ->
            adapter.submitList(locations)
            binding.tvEmpty.visibility = if (locations.isEmpty()) 
                android.view.View.VISIBLE else android.view.View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
