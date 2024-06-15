package com.zs.jollycat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zs.jollycat.model.CheckoutHistory
import com.zs.jollycat.R
import com.zs.jollycat.database.DatabaseHelper
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CheckoutListAdapter(private val checkoutHistoryList: ArrayList<CheckoutHistory>, private val dbHelper: DatabaseHelper) : RecyclerView.Adapter<CheckoutListAdapter.CheckoutViewHolder>() {

    //private val catList = Database.catList
    //private val userList = Database.userList

    class CheckoutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkoutIDTextView = itemView.findViewById<TextView>(R.id.checkoutIDTextView)
        val userTextView = itemView.findViewById<TextView>(R.id.userTextView)
        val checkoutDateTextView= itemView.findViewById<TextView>(R.id.checkoutDateTextView)
        val itemsTextView = itemView.findViewById<TextView>(R.id.itemCountTextView)
        val totalTextView = itemView.findViewById<TextView>(R.id.totalTextView)
    }

    override fun onBindViewHolder(holder: CheckoutViewHolder, position: Int) {
        val checkoutHistory = checkoutHistoryList[position]
        holder.checkoutIDTextView.text = "Checkout ID: ${checkoutHistory.checkoutID}"

        //val user = userList.find { it.UserID == checkoutHistory.userID }
        val user = dbHelper.getUserFromDatabase(checkoutHistory.userID)
        val username = user?.Username
        holder.userTextView.text = "Username: $username"

        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        val checkoutDateStr = dateFormat.format(checkoutHistory.checkoutDate)
        holder.checkoutDateTextView.text = "Checkout Date: $checkoutDateStr"

        var itemsText = ""
        var totalPrice: Int = 0

        val stringBuilder = StringBuilder()
        for (cartItem in checkoutHistory.checkedOutItems) {
            val cat = dbHelper.getCatFromDatabase(cartItem.CatID)
            if (cat != null) {
                stringBuilder.append("-> ${cat.CatName} (Quantity: ${cartItem.Quantity})\n")
                totalPrice += cat.CatPrice * cartItem.Quantity
            }

        }
        itemsText = stringBuilder.toString()
        holder.itemsTextView.text = "Items: \n$itemsText"

        val locale = Locale("id", "ID")
        val formatter = NumberFormat.getCurrencyInstance(locale)
        val formattedTotalPrice = formatter.format(totalPrice).replace("Rp", "Rp ")
        holder.totalTextView.text = "Total Price: $formattedTotalPrice"

//        for (cartItem in checkoutHistory.checkedOutItems) {
//            val cat = dbHelper.getCatFromDatabase(cartItem.CatID)
//            if (cat != null) {
//                itemsText += "${cat.CatName} (Quantity: ${cartItem.Quantity}), "
//                totalPrice += cat.CatPrice * cartItem.Quantity
//            }
//        }
//
//        holder.itemsTextView.text = "Items: $itemsText"
//        holder.totalTextView.text = "Total Price: Rp. $totalPrice"

//        for (cartItem in checkoutHistory.checkedOutItems) {
//            val catName = catList.find { it.CatID == cartItem.CatID }?.CatName
//            catName?.let {
//                itemsText += "$it (Quantity: ${cartItem.Quantity}), "
//            }
//        }
//        holder.itemsTextView.text = "Items: $itemsText"

//        var totalPrice: Int = 0
//        for (cartItem in checkoutHistory.checkedOutItems) {
//            val theCat = catList.find { it.CatID == cartItem.CatID }
//            if (theCat != null) {
//                totalPrice += theCat.CatPrice * cartItem.Quantity
//            }
//        }
//        holder.totalTextView.text = "Total Price: Rp. $totalPrice"

    }

    override fun getItemCount(): Int {
        return checkoutHistoryList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.checkout_item_layout, parent, false)
        return CheckoutViewHolder(view)
    }

}
