package com.nextgen.iptv.ui.livetv

import android.graphics.Color
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

    private var selectedPosition = RecyclerView.NO_ID.toInt()

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
                if (pos >= 0 && pos < currentList.size) {
                    val prev = selectedPosition
                    selectedPosition = pos
                    if (prev >= 0) notifyItemChanged(prev)
                    notifyItemChanged(pos)
                    onChannelClick(currentList[pos])
                }
            }
            binding.root.setOnFocusChangeListener { v, hasFocus ->
                v.setBackgroundColor(
                    if (hasFocus) 0x4448CAE4.toInt() else {
                        if (adapterPosition == selectedPosition) 0x2248CAE4.toInt()
                        else Color.TRANSPARENT
                    }
                )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemChannelRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val channel = currentList[position]
        val isSelected = position == selectedPosition
        holder.binding.channelName.text = channel.name
        holder.binding.channelNowPlaying.text = channel.nowPlaying
        holder.binding.root.setBackgroundColor(
            if (isSelected) 0x2248CAE4.toInt() else Color.TRANSPARENT
        )
        holder.binding.channelName.setTextColor(
            if (isSelected) 0xFF48CAE4.toInt() else Color.WHITE
        )
        if (channel.logo.isNotEmpty()) {
            Glide.with(holder.binding.channelLogo)
                .load(channel.logo)
                .placeholder(android.R.color.transparent)
                .error(android.R.color.transparent)
                .into(holder.binding.channelLogo)
        } else {
            holder.binding.channelLogo.setImageDrawable(null)
        }
    }
}
