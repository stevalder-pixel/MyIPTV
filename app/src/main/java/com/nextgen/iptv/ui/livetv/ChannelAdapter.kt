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
    private val onChannelFocused: (Channel) -> Unit
) : ListAdapter<Channel, ChannelAdapter.VH>(DIFF) {

    private var selectedPosition = -1
    private var focusedPosition = -1

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(a: Channel, b: Channel) = a.id == b.id
            override fun areContentsTheSame(a: Channel, b: Channel) = a == b
        }
    }

    fun playSelected(onPlay: (Channel) -> Unit) {
        if (focusedPosition >= 0 && focusedPosition < currentList.size) {
            selectedPosition = focusedPosition
            notifyDataSetChanged()
            onPlay(currentList[focusedPosition])
        }
    }

    inner class VH(val binding: ItemChannelRowBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.isFocusable = true
            binding.root.isFocusableInTouchMode = true
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos >= 0 && pos < currentList.size) {
                    focusedPosition = pos
                    selectedPosition = pos
                    notifyDataSetChanged()
                    onChannelFocused(currentList[pos])
                }
            }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                val pos = adapterPosition
                if (hasFocus && pos >= 0 && pos < currentList.size) {
                    focusedPosition = pos
                    onChannelFocused(currentList[pos])
                }
                val isSelected = pos == selectedPosition
                binding.root.setBackgroundColor(when {
                    hasFocus -> 0x6648CAE4.toInt()
                    isSelected -> 0x3348CAE4.toInt()
                    else -> Color.TRANSPARENT
                })
                binding.channelName.setTextColor(if (hasFocus || isSelected) 0xFF48CAE4.toInt() else Color.WHITE)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemChannelRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val ch = currentList[position]
        val isSelected = position == selectedPosition
        holder.binding.channelName.text = ch.name
        holder.binding.channelNowPlaying.text = ch.nowPlaying
        holder.binding.root.setBackgroundColor(if (isSelected) 0x3348CAE4.toInt() else Color.TRANSPARENT)
        holder.binding.channelName.setTextColor(if (isSelected) 0xFF48CAE4.toInt() else Color.WHITE)
        if (ch.logo.isNotEmpty()) {
            Glide.with(holder.binding.channelLogo).load(ch.logo)
                .placeholder(android.R.color.transparent).error(android.R.color.transparent)
                .into(holder.binding.channelLogo)
        } else holder.binding.channelLogo.setImageDrawable(null)
    }
}
