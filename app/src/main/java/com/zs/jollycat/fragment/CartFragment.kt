package com.zs.jollycat.fragment

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.telephony.SmsManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zs.jollycat.R
import com.zs.jollycat.model.CheckoutHistory
import com.zs.jollycat.adapter.CartAdapter
import com.zs.jollycat.database.DatabaseHelper
import com.zs.jollycat.model.CartData
import java.text.NumberFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface TotalPriceListener {
    fun onTotalPriceChanged()
}

class CartFragment : Fragment(), TotalPriceListener {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var userCartList: ArrayList<CartData>

    override fun onTotalPriceChanged() {
        calculateTotalPrice()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        dbHelper = DatabaseHelper(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_cart, container, false)
        dbHelper = DatabaseHelper(requireContext())

        val userID = arguments?.getString("userID") as String

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.SEND_SMS), 1)
        }

        //val user = userList.find { it.UserID == userID }
        val user = dbHelper.getUserFromDatabase(userID)
        val username = user?.Username
        val titleName = view.findViewById<TextView>(R.id.tvUsernameCart)
        titleName.text = username.toString()

       // userCartList = cartList.filter { it.UserID == userID } as ArrayList<CartData>
        userCartList = dbHelper.getUserCartList(userID)

        val count = userCartList.size
        val cartCount = view.findViewById<TextView>(R.id.tvAvailableCat)
        if (count == 1 )
            cartCount.text= "'s Cart: ${count} cat"
        else cartCount.text= "'s Cart: ${count} cats"

        cartRecyclerView = view.findViewById<RecyclerView>(R.id.rvCartList)
        cartAdapter = CartAdapter(userCartList, this)
        cartRecyclerView.adapter = cartAdapter
        cartRecyclerView.layoutManager = LinearLayoutManager(activity)

        val locale = Locale("id", "ID")
        val formatter = NumberFormat.getCurrencyInstance(locale)
        val total = calculateTotalPrice()
        val totalPriceTextView = view.findViewById<TextView>(R.id.tvPrice)
        if (total <= 0){
            totalPriceTextView?.text = "Cart Empty"
        } else {
            val formattedTotalPrice = formatter.format(total).replace("Rp", "Rp ")
            totalPriceTextView?.text = formattedTotalPrice.toString()
        }

        val checkoutButton = view.findViewById<Button>(R.id.btn_checkout)
        checkoutButton.setOnClickListener {
            if (userCartList.isEmpty()) {
                Toast.makeText(context, "Your cart is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val checkoutID = UUID.randomUUID().toString()

            for (cartItem in userCartList) {
                if(cartItem.CheckoutID.isEmpty()){
                    cartItem.CheckoutID = checkoutID
                }
            }

            val checkoutDate = Date()
            //val checkoutHistory = CheckoutHistory(checkoutID, userID , checkoutDate, userCartList, calculateTotalPrice())
            //checkoutList.add(checkoutHistory)
            val totalPrice = calculateTotalPrice();
            val checkoutHistory = CheckoutHistory(checkoutID, userID, checkoutDate, userCartList, totalPrice)
            dbHelper.addCheckoutHistory(checkoutHistory)


            val user = dbHelper.getUserFromDatabase(userID)
            val phoneNumber = user?.PhoneNum
            if (phoneNumber != null) {
                sendSMS(phoneNumber, "Your checkout success ${user.Username}! \n\n" +
                        "Cart ID: ${userCartList[0].CartID} \n" +
                        "Checkout ID: $checkoutID \n" +
                        "Total Price: Rp $totalPrice")
                Toast.makeText(activity, "Checkout success!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "Checkout failed. Cart is empty or phone number is not available.", Toast.LENGTH_SHORT).show()
            }

            val openMessagingAppIntent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$phoneNumber"))
            try {
                startActivity(openMessagingAppIntent)
            } catch (e: Exception) {
                Toast.makeText(activity, "Failed to open messaging app", Toast.LENGTH_SHORT).show()
            }

            val formattedTotalPrice = formatter.format(totalPrice).replace("Rp", "Rp ")
            val builder = AlertDialog.Builder(view.context)
            builder.setTitle("Checkout success!")
            builder.setMessage("\nUsername: " + username + "\nYour total price: "+ formattedTotalPrice + "\n\n~Your data has been stored in history~\n Click OK to get SMS")

            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                Toast.makeText(activity, "Checkout success!", Toast.LENGTH_SHORT).show()
                dbHelper.deleteUserCart(userID)
                userCartList.clear()
                cartAdapter.notifyDataSetChanged()

                val bundle = Bundle()
                bundle.putString("userID", userID)
                val homeFragment = HomeFragment()
                homeFragment.arguments = bundle

                val fragmentManager = requireActivity().supportFragmentManager
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainer, homeFragment)
                fragmentTransaction.commit()
            }
            builder.show()

        }

        return view
    }

    private fun sendSMS(phoneNumber: String, message: String) {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(android.Manifest.permission.SEND_SMS), 1)
        } else {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                Toast.makeText(activity, "SMS failed to send", Toast.LENGTH_SHORT).show()
            }
        }
    }


    fun calculateTotalPrice():Int {
        var totalPrice = 0
        for (cartItem in userCartList) {
            //val theCat = catList.find { it.CatID == cartItem.CatID }
            val theCat = dbHelper.getCatFromDatabase(cartItem.CatID)
            if (theCat != null) {
                totalPrice += theCat.CatPrice * cartItem.Quantity
            }
        }
        val totalPriceTextView = view?.findViewById<TextView>(R.id.tvPrice)

        val locale = Locale("id", "ID")
        val formatter = NumberFormat.getCurrencyInstance(locale)
        val formattedTotalPrice = formatter.format(totalPrice).replace("Rp", "Rp ")

        totalPriceTextView?.text = formattedTotalPrice.toString()

        return totalPrice

    }

}