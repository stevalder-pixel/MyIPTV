package com.nextgen.iptv

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PosterAdapter(private val posterPaths: List<String>, private val onPosterClick: (String) -> Unit) :
    RecyclerView.Adapter<PosterAdapter.PosterViewHolder>() {

    class PosterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(android.R.id.icon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PosterViewHolder {
        // Use a built-in simple layout frame to hold the image view dynamically
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.activity_list_item, parent, false)
        
        // Quick tweak to ensure layout parameters match your TV row height
        view.layoutParams = ViewGroup.LayoutParams(260, ViewGroup.LayoutParams.MATCH_PARENT)
        return PosterViewHolder(view)
    }

    override fun onBindViewHolder(holder: PosterViewHolder, position: Int) {
        val fullUrl = "https://image.tmdb.org/t/p/w500" + posterPaths[position]
        
        Glide.with(holder.itemView.context)
            .load(fullUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .into(holder.imageView)

        holder.itemView.setOnClickListener { onPosterClick(fullUrl) }
    }

    override fun getItemCount(): Int = posterPaths.size
}
