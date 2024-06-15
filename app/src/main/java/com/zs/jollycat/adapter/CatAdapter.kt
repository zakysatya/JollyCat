package com.zs.jollycat.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zs.jollycat.CatDetailActivity
import com.zs.jollycat.R
import com.zs.jollycat.model.Cats
import java.text.NumberFormat
import java.util.Locale

class CatAdapter(private val catList : ArrayList<Cats>, private val userID: String): RecyclerView.Adapter<CatAdapter.viewHolder>() {

    class viewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cvCatList)

        var catImageView = itemView.findViewById<ImageView>(R.id.ivCatImage)
        val catNameTextView = itemView.findViewById<TextView>(R.id.tvCatName)
        val catDescTextView = itemView.findViewById<TextView>(R.id.tvDescription)
        var catPriceTextView = itemView.findViewById<TextView>(R.id.tvPrice)
    }

    fun updateData(newCatList: ArrayList<Cats>) {
        catList.clear()
        catList.addAll(newCatList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
            return catList.size
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        val currentCat = catList[position]
        //holder.catImageView.setImageResource(currentCat.CatImage)
        Glide.with(holder.itemView.context)
            .load(currentCat.CatImage)
            .into(holder.catImageView)
        holder.catNameTextView.text = currentCat.CatName
        holder.catDescTextView.text = currentCat.CatDescription

        val locale = Locale("id", "ID")
        val formatter = NumberFormat.getCurrencyInstance(locale)
        val formattedCatPrice = formatter.format(currentCat.CatPrice).replace("Rp", "Rp ")

        holder.catPriceTextView.text = formattedCatPrice.toString()
        //holder.catPriceTextView.text = currentCat.CatPrice.toString()

        holder.cardView.setOnClickListener {
            // buka cat detail page
            val intent = Intent(it.context, CatDetailActivity::class.java)
            intent.putExtra("catID", currentCat.CatID)
            intent.putExtra("userID", userID)
            it.context.startActivity(intent)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.cat_item_layout, parent,false)
        return viewHolder(itemView)
    }

}