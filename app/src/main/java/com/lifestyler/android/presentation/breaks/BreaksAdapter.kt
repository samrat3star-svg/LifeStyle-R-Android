package com.lifestyler.android.presentation.breaks

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifestyler.android.data.model.BreakEntry
import com.lifestyler.android.databinding.ItemBreakHistoryBinding

class BreaksAdapter : ListAdapter<BreakEntry, BreaksAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemBreakHistoryBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemBreakHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BreakEntry) {
            binding.dateHeader.text = item.date ?: "Unknown Date"
            // Reason is hardcoded or from API
            // Use the reason from the API, which now contains the real duration (e.g., "Break after 16 Hrs")
            binding.reasonText.text = item.reason ?: "Manual Break"
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<BreakEntry>() {
        override fun areItemsTheSame(oldItem: BreakEntry, newItem: BreakEntry): Boolean {
            return oldItem.date == newItem.date
        }
        override fun areContentsTheSame(oldItem: BreakEntry, newItem: BreakEntry): Boolean {
            return oldItem == newItem
        }
    }
}
