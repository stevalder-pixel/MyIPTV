package com.nextgen.iptv.ui.livetv

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nextgen.iptv.data.models.Channel
import com.nextgen.iptv.databinding.ItemChannelRowBinding

class ChannelAdapter(
    private val onChannelClick: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.VH>(DIFF) {

    private var selectedPosition = 0

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(a: Channel, b: Channel) = a.id == b.id
            override fun areContentsTheSame(a: Channel, b: Channel) = a == b
        }
    }

    inner class VH(val binding: ItemChannelRowBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_ID.toInt()) {
                    selectedPosition = pos
                    notifyDataSetChanged()
                    onChannelClick(getItem(pos))
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemChannelRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val channel = getItem(position)
        holder.binding.channelName.text = channel.name
        holder.binding.channelNowPlaying.text = channel.nowPlaying.ifEmpty { "" }
        holder.binding.root.isSelected = position == selectedPosition
        holder.binding.root.setBackgroundColor(
            if (position == selectedPosition) 0x2248CAE4.toInt() else android.graphics.Color.TRANSPARENT
        )
        if (channel.logo.isNotEmpty()) {
            Glide.with(holder.binding.channelLogo)
                .load(channel.logo)
                .placeholder(android.R.color.transparent)
                .error(android.R.color.transparent)
                .into(holder.binding.channelLogo)
        }
    }
}
