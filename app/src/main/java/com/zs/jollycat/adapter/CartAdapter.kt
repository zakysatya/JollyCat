package com.zs.jollycat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zs.jollycat.model.CartData
import com.zs.jollycat.R
import com.zs.jollycat.database.DatabaseHelper
import com.zs.jollycat.fragment.TotalPriceListener
import java.text.NumberFormat
import java.util.Locale


class CartAdapter(private val userCartList: ArrayList<CartData>, private val listener: TotalPriceListener) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private lateinit var dbHelper : DatabaseHelper

    fun onQuantityChanged() {
        listener.onTotalPriceChanged()
    }

    //private val cartList = Database.cartList
    //private val catList = Database.catList

    class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val catNameTextView = itemView.findViewById<TextView>(R.id.tvCatName)
        val catPriceTextView = itemView.findViewById<TextView>(R.id.tvCatPrice)
        var catImageView = itemView.findViewById<ImageView>(R.id.ivCatImage)
        val quantityEditText = itemView.findViewById<EditText>(R.id.tvQuantity)

        val subtotalTextView = itemView.findViewById<TextView>(R.id.tvSubtotalCatPrice)

        val updateButton = itemView.findViewById<Button>(R.id.btn_updateQty)
        val deleteButton = itemView.findViewById<Button>(R.id.btn_delete)
    }

    override fun getItemCount(): Int {
        return userCartList.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = userCartList[position]

        val theCat = dbHelper.getCatFromDatabase(cartItem.CatID)
        //val theCat = catList.find { it.CatID == cartItem.CatID.toString() }
        if (theCat != null) {
            Glide.with(holder.itemView.context)
                .load(theCat.CatImage)
                .into(holder.catImageView)

            holder.catNameTextView.text = theCat.CatName

            val locale = Locale("id", "ID")
            val formatter = NumberFormat.getCurrencyInstance(locale)

            val formattedCatPrice = formatter.format(theCat.CatPrice).replace("Rp", "Rp ")
            holder.catPriceTextView.text = formattedCatPrice.toString()

            holder.quantityEditText.setText(cartItem.Quantity.toString())

            val subTotal = (theCat.CatPrice * cartItem.Quantity)
            val formattedCatSubPrice = formatter.format(subTotal).replace("Rp", "Rp ")
            holder.subtotalTextView.text = formattedCatSubPrice.toString()

        }
        holder.updateButton.setOnClickListener {
            if (holder.updateButton.text == "Update") {

                // Ubah EditText menjadi mode edit
                holder.quantityEditText.isFocusableInTouchMode = true
                holder.quantityEditText.isClickable = true


                holder.quantityEditText.setBackground(
                    ResourcesCompat.getDrawable(
                        holder.itemView.context.resources,
                        R.drawable.rounded_corner,
                        null
                    )
                )

                holder.quantityEditText.setTextColor(
                    ResourcesCompat.getColor(
                        holder.itemView.context.resources,
                        R.color.my_primary_variant,
                        holder.itemView.context.theme
                    )
                )

                holder.updateButton.text = "Done"

            } else if (holder.updateButton.text == "Done") {
                val quantityString = holder.quantityEditText.text.toString()
                val quantity = quantityString.toIntOrNull()
                if (quantity == null || quantity <= 0) {
                    Toast.makeText(
                        holder.itemView.context,
                        "Quantity must be a number and more than 0",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                } else {
                    cartItem.Quantity = quantity
                    val subtotal = theCat?.CatPrice?.times(quantity)
                    val locale = Locale("id", "ID")
                    val formatter = NumberFormat.getCurrencyInstance(locale)

                    val formattedTotalPrice = formatter.format(subtotal).replace("Rp", "Rp ")

                    dbHelper.addOrUpdateCartItem(cartItem)

                    holder.subtotalTextView.text = formattedTotalPrice.toString()

                    onQuantityChanged()

                    Toast.makeText(
                        holder.itemView.context,
                        "Success change quantity",
                        Toast.LENGTH_SHORT
                    ).show()

                }

                holder.quantityEditText.isFocusableInTouchMode = false
                holder.quantityEditText.isClickable = false

                holder.quantityEditText.setBackgroundColor(
                    ResourcesCompat.getColor(
                        holder.itemView.context.resources,
                        R.color.my_primary_variant,
                        holder.itemView.context.theme
                    )
                )

                holder.quantityEditText.setTextColor(
                    ResourcesCompat.getColor(
                        holder.itemView.context.resources,
                        R.color.my_secondary,
                        holder.itemView.context.theme
                    )
                )
                holder.updateButton.text = "Update"
            }
        }

        holder.deleteButton.setOnClickListener {
            val catID = userCartList[position].CatID.toString()

            dbHelper.deleteCatItemInCart(catID)
           // val index = cartList.indexOfFirst { it.CatID.toString() == catID }
            //if (index != -1) {
            //    cartList.removeAt(index)
            //}
            userCartList.removeAt(position)
            notifyDataSetChanged()
            onQuantityChanged()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        dbHelper = DatabaseHelper(parent.context)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cart_item_layout, parent, false)

        return CartViewHolder(view)
    }




}

