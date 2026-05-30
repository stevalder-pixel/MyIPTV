package com.nextgen.iptv.ui.movies

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nextgen.iptv.databinding.ItemPosterCardBinding
import com.nextgen.iptv.ui.common.MediaItem

class MediaRowAdapter(
    private val onItemClick: (MediaItem) -> Unit
) : ListAdapter<MediaItem, MediaRowAdapter.VH>(DIFF) {

    var onItemFocused: ((MediaItem) -> Unit)? = null

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(a: MediaItem, b: MediaItem) = a.id == b.id
            override fun areContentsTheSame(a: MediaItem, b: MediaItem) = a == b
        }
    }

    inner class VH(val binding: ItemPosterCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos >= 0 && pos < currentList.size) onItemClick(currentList[pos])
            }
            binding.root.setOnFocusChangeListener { v, hasFocus ->
                v.animate().scaleX(if (hasFocus) 1.08f else 1f)
                    .scaleY(if (hasFocus) 1.08f else 1f)
                    .setDuration(150).start()
                if (hasFocus) {
                    val pos = adapterPosition
                    if (pos >= 0 && pos < currentList.size) {
                        onItemFocused?.invoke(currentList[pos])
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPosterCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = currentList[position]
        holder.binding.posterTitle.text = item.title
        holder.binding.posterRating.text = if (item.rating > 0) "★ " + "%.1f".format(item.rating) else ""
        if (item.posterUrl.isNotEmpty()) {
            Glide.with(holder.binding.posterImage)
                .load(item.posterUrl)
                .placeholder(android.R.color.darker_gray)
                .into(holder.binding.posterImage)
        }
    }
}
