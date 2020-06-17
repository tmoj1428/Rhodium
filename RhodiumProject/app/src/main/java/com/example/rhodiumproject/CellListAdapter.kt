package com.example.rhodiumproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CellListAdapter internal constructor(
    context: Context
) : RecyclerView.Adapter<CellListAdapter.CellViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private var cells = emptyList<LTE_Cell>() // Cached copy of words

    inner class CellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cellItemView: TextView = itemView.findViewById(R.id.ServingCellId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CellViewHolder {
        val itemView = inflater.inflate(R.layout.recyclerview_item, parent, false)
        return CellViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CellViewHolder, position: Int) {
        val current = cells[position]
        holder.cellItemView.text = current.cellId.toString()
    }

    internal fun setWords(words: List<LTE_Cell>) {
        this.cells = words
        notifyDataSetChanged()
    }

    override fun getItemCount() = cells.size
}