package com.nextgen.iptv.ui.livetv

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
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

    var selectedId: String = ""

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
                if (pos >= 0) {
                    val channel = getItem(pos)
                    selectedId = channel.id
                    notifyDataSetChanged()
                    onChannelClick(channel)
                }
            }
            binding.root.setOnFocusChangeListener { _, hasFocus ->
                binding.channelPlayIcon.visibility = if (hasFocus) View.VISIBLE else View.INVISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemChannelRowBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val channel = getItem(position)
        val isSelected = channel.id == selectedId

        holder.binding.channelName.text = channel.name
        holder.binding.channelNowPlaying.text = channel.nowPlaying

        // Highlight selected channel
        if (isSelected) {
            holder.binding.root.setBackgroundColor(0x2248CAE4.toInt())
            holder.binding.channelName.setTextColor(0xFF48CAE4.toInt())
            holder.binding.channelPlayIcon.visibility = View.VISIBLE
        } else {
            holder.binding.root.setBackgroundColor(Color.TRANSPARENT)
            holder.binding.channelName.setTextColor(Color.WHITE)
            holder.binding.channelPlayIcon.visibility = View.INVISIBLE
        }

        // Load logo
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
