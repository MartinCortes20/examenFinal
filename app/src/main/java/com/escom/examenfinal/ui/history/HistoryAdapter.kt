package com.escom.examenfinal.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.escom.examenfinal.data.model.LocationRecord
import com.escom.examenfinal.databinding.ItemLocationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter : ListAdapter<LocationRecord, HistoryAdapter.LocationViewHolder>(LocationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val binding = ItemLocationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class LocationViewHolder(private val binding: ItemLocationBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        
        fun bind(location: LocationRecord, number: Int) {
            binding.tvNumber.text = number.toString()
            binding.tvCoordinates.text = "Lat: ${String.format("%.6f", location.latitude)}\nLon: ${String.format("%.6f", location.longitude)}"
            binding.tvTimestamp.text = dateFormat.format(Date(location.timestamp))
            binding.tvAccuracy.text = "Precisión: ${String.format("%.2f", location.accuracy)} m"
        }
    }

    class LocationDiffCallback : DiffUtil.ItemCallback<LocationRecord>() {
        override fun areItemsTheSame(oldItem: LocationRecord, newItem: LocationRecord): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LocationRecord, newItem: LocationRecord): Boolean {
            return oldItem == newItem
        }
    }
}
