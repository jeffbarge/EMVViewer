package com.barger.emvviewer

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class EMVAdapter : RecyclerView.Adapter<EMVViewHolder>() {
    var data = listOf<EMVTag>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EMVViewHolder {
        return EMVViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_tag, parent, false))
    }

    override fun onBindViewHolder(holder: EMVViewHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class EMVViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private val header = itemView.findViewById<TextView>(R.id.header)
    private val value = itemView.findViewById<TextView>(R.id.value)
    fun bindData(tag: EMVTag) {
        header.text = "${tag.meaning} (${tag.tag.toUpperCase()})"
        value.text = tag.value
    }
}