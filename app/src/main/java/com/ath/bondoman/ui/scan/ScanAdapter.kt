package com.ath.bondoman.ui.scan

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ath.bondoman.databinding.BillListItemBinding
import com.ath.bondoman.model.dto.Item

class ItemListAdapter : RecyclerView.Adapter<ItemListAdapter.ItemViewHolder>() {

    private var items: List<Item> = listOf()

    class ItemViewHolder(private val binding: BillListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val title: TextView = binding.title
        val qty: TextView=binding.qty
        val amount: TextView = binding.amount
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BillListItemBinding.inflate(inflater, parent, false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.name
        holder.qty.text = item.qty.toString()
        holder.amount.text = item.price.toString()
    }

    override fun getItemCount() = items.size

    fun setItems(items: List<Item>) {
        this.items = items
        notifyDataSetChanged()
    }
}
