package com.zs.jollycat.model

import java.util.Date

data class CheckoutHistory(
    val checkoutID: String,
    val userID: String,
    val checkoutDate: Date,
    val checkedOutItems: ArrayList<CartData>,
    val totalPrice: Int
)
