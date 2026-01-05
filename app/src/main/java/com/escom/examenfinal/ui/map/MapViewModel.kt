package com.escom.examenfinal.ui.map

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.escom.examenfinal.data.local.LocationDatabase
import com.escom.examenfinal.data.model.LocationRecord
import kotlinx.coroutines.launch

class MapViewModel(application: Application) : AndroidViewModel(application) {
    
    private val database = LocationDatabase.getDatabase(application)
    private val locationDao = database.locationDao()
    
    val allLocations: LiveData<List<LocationRecord>> = locationDao.getAllLocations()
    
    fun clearHistory() {
        viewModelScope.launch {
            locationDao.clearAll()
        }
    }
}
