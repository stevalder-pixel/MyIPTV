package com.nextgen.iptv.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nextgen.iptv.databinding.ItemPosterCardBinding

data class PosterItem(val id: Int, val title: String, val posterUrl: String, val rating: Float, val isTv: Boolean)

class PosterAdapter : ListAdapter<PosterItem, PosterAdapter.VH>(DIFF) {
    companion object {
        val DIFF = object : DiffUtil.ItemCallback<PosterItem>() {
            override fun areItemsTheSame(a: PosterItem, b: PosterItem) = a.id == b.id
            override fun areContentsTheSame(a: PosterItem, b: PosterItem) = a == b
        }
    }
    inner class VH(val binding: ItemPosterCardBinding) : RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemPosterCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.binding.posterTitle.text = item.title
        holder.binding.posterRating.text = "★ ${"%.1f".format(item.rating)}"
        Glide.with(holder.binding.posterImage).load(item.posterUrl)
            .placeholder(android.R.color.darker_gray).into(holder.binding.posterImage)
    }
}
