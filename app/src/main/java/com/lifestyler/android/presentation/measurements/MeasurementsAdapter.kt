package com.lifestyler.android.presentation.measurements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifestyler.android.data.model.JoiningStatus
import com.lifestyler.android.data.model.MeasurementEntry
import com.lifestyler.android.databinding.ItemMeasurementHistoryBinding

class MeasurementsAdapter : ListAdapter<MeasurementUiItem, MeasurementsAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemMeasurementHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemMeasurementHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MeasurementUiItem) {
            binding.dateHeader.text = item.date
            
            // Populate measurements
            binding.weightText.text = if (item.weight.isNullOrEmpty()) "--" else item.weight
            binding.chestText.text = if (item.chest.isNullOrEmpty()) "--" else item.chest
            binding.waistText.text = if (item.waist.isNullOrEmpty()) "--" else item.waist
            binding.hipsText.text = if (item.hips.isNullOrEmpty()) "--" else item.hips

            // Show Height and Target only for Joining Status
            if (item.isJoiningStatus) {
                binding.heightRow.visibility = View.VISIBLE
                binding.targetRow.visibility = View.VISIBLE
                binding.heightText.text = if (item.height.isNullOrEmpty()) "--" else item.height
                binding.targetWeightText.text = if (item.targetWeight.isNullOrEmpty()) "--" else item.targetWeight
            } else {
                binding.heightRow.visibility = View.GONE
                binding.targetRow.visibility = View.GONE
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<MeasurementUiItem>() {
        override fun areItemsTheSame(oldItem: MeasurementUiItem, newItem: MeasurementUiItem): Boolean {
            return oldItem.date == newItem.date
        }
        override fun areContentsTheSame(oldItem: MeasurementUiItem, newItem: MeasurementUiItem): Boolean {
            return oldItem == newItem
        }
    }
}

data class MeasurementUiItem(
    val date: String,
    val weight: String?,
    val chest: String?,
    val waist: String?,
    val hips: String?,
    val height: String? = null,
    val targetWeight: String? = null,
    val isJoiningStatus: Boolean = false
)
