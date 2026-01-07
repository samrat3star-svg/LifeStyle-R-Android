package com.lifestyler.android.presentation.main.clients

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lifestyler.android.databinding.ItemClientBinding
import com.lifestyler.android.domain.entity.Client

class ClientsAdapter(
    private val onClientClick: (Client) -> Unit
) : ListAdapter<Client, ClientsAdapter.ClientViewHolder>(ClientDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClientViewHolder {
        val binding = ItemClientBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ClientViewHolder(binding, onClientClick)
    }
    
    override fun onBindViewHolder(holder: ClientViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    class ClientViewHolder(
        private val binding: ItemClientBinding,
        private val onClientClick: (Client) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(client: Client) {
            binding.apply {
                textViewClientName.text = client.name
                textViewClientEmail.text = client.email
                textViewClientStatus.text = client.status
                textViewClientGoal.text = client.goal
                
                root.setOnClickListener {
                    onClientClick(client)
                }
            }
        }
    }
    
    private class ClientDiffCallback : DiffUtil.ItemCallback<Client>() {
        override fun areItemsTheSame(oldItem: Client, newItem: Client): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Client, newItem: Client): Boolean {
            return oldItem == newItem
        }
    }
} 