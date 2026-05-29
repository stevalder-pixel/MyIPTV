package com.nextgen.iptv.ui.livetv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nextgen.iptv.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val categories: List<Pair<String, String>>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.VH>() {

    private var selectedPosition = 0

    inner class VH(val binding: ItemCategoryBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                selectedPosition = adapterPosition
                notifyDataSetChanged()
                onCategoryClick(categories[adapterPosition].first)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.binding.categoryName.text = categories[position].second
        val selected = position == selectedPosition
        holder.binding.categoryName.setTextColor(
            if (selected) 0xFF48CAE4.toInt() else 0xFFB0BEC5.toInt()
        )
        holder.binding.root.setBackgroundColor(
            if (selected) 0x2248CAE4.toInt() else android.graphics.Color.TRANSPARENT
        )
    }

    override fun getItemCount() = categories.size
}
