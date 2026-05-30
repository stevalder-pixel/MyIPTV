package com.nextgen.iptv.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nextgen.iptv.databinding.ItemAddonBinding

class AddonAdapter(
    private val onRemove: (String) -> Unit
) : ListAdapter<String, AddonAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(a: String, b: String) = a == b
            override fun areContentsTheSame(a: String, b: String) = a == b
        }
    }

    inner class VH(val binding: ItemAddonBinding) : RecyclerView.ViewHolder(binding.root) {
        init { binding.removeBtn.setOnClickListener { onRemove(getItem(adapterPosition)) } }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemAddonBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val url = getItem(position)
        holder.binding.addonUrl.text = url.replace("/manifest.json", "").takeLast(40)
        holder.binding.addonUrl.text = url
    }
}
