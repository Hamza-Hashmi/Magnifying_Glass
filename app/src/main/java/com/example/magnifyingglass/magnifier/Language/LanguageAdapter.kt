package com.example.magnifyingglass.magnifier.Language

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.magnifyingglass.magnifier.R
import com.example.magnifyingglass.magnifier.databinding.LanguageCardDesignBinding

class LanguageAdapter(
    private val mList: ArrayList<LanguageModel>,
    private val listener: OnItemClickListener,
) : RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LanguageCardDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val itemsViewModel = mList[position]

        holder.binding.cname.text = itemsViewModel.name

        if (position==0){
            holder.binding.image.setImageResource(R.drawable.ic_checked)

        }
        holder.itemView.setOnClickListener {
            listener.onItemClick(position)

            holder.binding.image.setImageResource(R.drawable.ic_checked)


        }
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    class ViewHolder(val binding : LanguageCardDesignBinding,) : RecyclerView.ViewHolder(binding.root) {


    }
}


interface OnItemClickListener {
    fun onItemClick(position: Int)
}
