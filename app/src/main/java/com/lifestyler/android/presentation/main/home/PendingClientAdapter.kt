package com.lifestyler.android.presentation.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifestyler.android.R
import com.lifestyler.android.domain.entity.PendingClient

class PendingClientAdapter :
    ListAdapter<PendingClient, PendingClientAdapter.ClientViewHolder>(ClientDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pending_client, parent, false)
        return ClientViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ClientViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.clientNameTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.clientStatusTextView)

        fun bind(client: PendingClient) {
            nameTextView.text = client.name
            statusTextView.text = client.status
        }
    }

    private class ClientDiffCallback : DiffUtil.ItemCallback<PendingClient>() {
        override fun areItemsTheSame(oldItem: PendingClient, newItem: PendingClient): Boolean {
    return oldItem.name == newItem.name // Correct: Use the 'name' property
}

        override fun areContentsTheSame(oldItem: PendingClient, newItem: PendingClient): Boolean {
            return oldItem == newItem
        }
    }
} 