package com.chaitany.carbonview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class EmissionAdapter(private var emissionList: List<EmissionData>) :
    RecyclerView.Adapter<EmissionAdapter.EmissionViewHolder>() {

    class EmissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val activity: TextView = itemView.findViewById(R.id.tvActivity)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val emission: TextView = itemView.findViewById(R.id.tvEmission)
        val scope: TextView = itemView.findViewById(R.id.tvScope)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmissionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_emission, parent, false)
        return EmissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: EmissionViewHolder, position: Int) {
        val emissionData = emissionList[position]
        holder.activity.text = "Activity: ${emissionData.Activity}"
        holder.date.text = "Date: ${emissionData.Date}"
        holder.emission.text = "Emission: ${emissionData.Emission} kg CO2e"
        holder.scope.text = "Scope: ${emissionData.Scope}"
    }

    override fun getItemCount(): Int = emissionList.size

    fun updateList(newList: List<EmissionData>) {
        emissionList = newList
        notifyDataSetChanged()
    }
}
