package com.zs.jollycat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.zs.jollycat.database.DatabaseHelper
import com.zs.jollycat.model.CartData
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

class CatDetailActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var catQuantityEditText: EditText
    private lateinit var buyButton: Button

    //private val catList = Database.catList
    //private val cartList = Database.cartList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cat_detail)

        dbHelper = DatabaseHelper(this)
        val catID = intent.getStringExtra("catID") as String
        val userID = intent.getStringExtra("userID") as String

        //cari data dari id kucing
        //val theCat = catList.find { it.CatID == catID.toString() }
        val theCat = dbHelper.getCatFromDatabase(catID)

        val catImageView = findViewById<ImageView>(R.id.ivCatImage)
        val catTextView = findViewById<TextView>(R.id.tvCatName)
        val catDescTextView = findViewById<TextView>(R.id.tvCatDesc)
        val catPriceTextView = findViewById<TextView>(R.id.tvPrice)

        if (theCat != null) {
            Glide .with(this)
                .load(theCat.CatImage)
                .into(catImageView);

            catTextView.text = theCat.CatName
            catDescTextView.text = theCat.CatDescription
            val locale = Locale("id", "ID")
            val formatter = NumberFormat.getCurrencyInstance(locale)
            val formattedCatPrice = formatter.format(theCat.CatPrice).replace("Rp","Rp ")
            catPriceTextView.text = formattedCatPrice.toString()
        } else {
            Toast.makeText(this,"Cat not found", Toast.LENGTH_LONG).show()
            finish()
        }

        catQuantityEditText = findViewById<EditText>(R.id.etQuantity)
        buyButton = findViewById<Button>(R.id.btn_buy)

        buyButton.setOnClickListener {
            val quantityString = catQuantityEditText.text.toString()
            if (quantityString.isEmpty()) {
                Toast.makeText(this, "Quantity must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val quantity = quantityString.toIntOrNull()
            if (quantity == null || quantity <= 0) {
                Toast.makeText(this, "Quantity must be a number and more than 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val cartItem = CartData(
                CartID = UUID.randomUUID().toString(),
                CatID = catID,
                CheckoutID = "",
                UserID = userID,
                Quantity = quantity
            )

            //cartList.add(cartItem)
            dbHelper.addOrUpdateCartItem(cartItem)
            Toast.makeText(this, "Add to cart success", Toast.LENGTH_SHORT).show()
            onBackPressed()
        }

        val backButton: Button = findViewById(R.id.btn_back)
        backButton.setOnClickListener {
            onBackPressed()
        }
    }

}