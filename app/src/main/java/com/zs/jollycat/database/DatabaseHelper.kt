package com.zs.jollycat.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.zs.jollycat.model.CartData
import com.zs.jollycat.model.Cats
import com.zs.jollycat.model.CheckoutHistory
import com.zs.jollycat.model.Users
import java.util.Date
import java.util.UUID

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "dbCats.db", null, 2) {
    override fun onCreate(db: SQLiteDatabase?) {

        val queryUsers = """
            CREATE TABLE IF NOT EXISTS Users (
                UserID CHAR(36) PRIMARY KEY,
                Username VARCHAR(100),
                PhoneNumber VARCHAR(20),
                Password VARCHAR(64)
            )
        """
        db?.execSQL(queryUsers)

        val queryCats = """
            CREATE TABLE IF NOT EXISTS Cats (
                CatID CHAR(36) PRIMARY KEY,
                CatName VARCHAR(100),
                CatDescription VARCHAR(255),
                CatImage VARCHAR(255),
                CatPrice INTEGER
            )
        """
        db?.execSQL(queryCats)

        val queryCartData = """
            CREATE TABLE IF NOT EXISTS CartData (
                CartID CHAR(36) PRIMARY KEY,
                CatID CHAR(36),
                CheckoutID CHAR(36),
                UserID CHAR(36),
                Quantity INTEGER(10),
                FOREIGN KEY (CatID) REFERENCES Cats(CatID),
                FOREIGN KEY (UserID) REFERENCES Users(UserID)
            )
        """
        db?.execSQL(queryCartData)

        val queryCheckoutHistory = """
            CREATE TABLE IF NOT EXISTS CheckoutHistory (
                checkoutID CHAR(36) PRIMARY KEY,
                userID CHAR(36),
                checkoutDate INTEGER,
                totalPrice INTEGER
            )
        """
        db?.execSQL(queryCheckoutHistory)

        val queryCheckedOutItems = """
            CREATE TABLE IF NOT EXISTS CheckedOutItems (
                CheckedOutItemID CHAR(36) PRIMARY KEY,
                CheckoutID CHAR(36),
                CatID CHAR(36),
                Quantity INTEGER,
                FOREIGN KEY (CheckoutID) REFERENCES CheckoutHistory(CheckoutID),
                FOREIGN KEY (CatID) REFERENCES Cats(CatID)
            )
        """
        db?.execSQL(queryCheckedOutItems)

    }

    fun deleteCatItemInCart(catID: String) {
        val db = writableDatabase
        db.delete("CartData", "CatID = ?", arrayOf(catID))
    }

     fun getUserFromDatabase(userID: String?): Users? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Users WHERE UserID = ?", arrayOf(userID))
        if (cursor.moveToFirst()) {
            val userID = cursor.getString(cursor.getColumnIndexOrThrow("UserID"))
            val username = cursor.getString(cursor.getColumnIndexOrThrow("Username"))
            val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("PhoneNumber"))
            val password = cursor.getString(cursor.getColumnIndexOrThrow("Password"))
            cursor.close()
            return Users(userID, username, password, phoneNumber)
        }
        cursor.close()
        return null
    }

    fun getUserFromDatabaseByUserPhone(username: String? ,phoneNum: String?): Users? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Users WHERE Username = ? AND PhoneNumber = ?", arrayOf(username, phoneNum))
        if (cursor.moveToFirst()) {
            val userID = cursor.getString(cursor.getColumnIndexOrThrow("UserID"))
            val username = cursor.getString(cursor.getColumnIndexOrThrow("Username"))
            val phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow("PhoneNumber"))
            val password = cursor.getString(cursor.getColumnIndexOrThrow("Password"))
            cursor.close()
            return Users(userID, username, password, phoneNumber)
        }
        cursor.close()
        return null
    }

    fun getCatFromDatabase(catID: String): Cats? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Cats WHERE CatID = ?", arrayOf(catID))

        if (cursor.moveToFirst()) {
            val catID = cursor.getString(cursor.getColumnIndexOrThrow("CatID"))
            val catName = cursor.getString(cursor.getColumnIndexOrThrow("CatName"))
            val catDescription = cursor.getString(cursor.getColumnIndexOrThrow("CatDescription"))
            val catImage = cursor.getString(cursor.getColumnIndexOrThrow("CatImage"))
            val catPrice = cursor.getInt(cursor.getColumnIndexOrThrow("CatPrice"))
            cursor.close()
            return Cats(catID, catName, catDescription, catImage, catPrice)
        }
        cursor.close()
        return null
    }

    fun getAllCats(): ArrayList<Cats> {
        val catList = ArrayList<Cats>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM Cats", null)
        if (cursor.moveToFirst()) {
            do {
                val catID = cursor.getString(cursor.getColumnIndexOrThrow("CatID"))
                val catName = cursor.getString(cursor.getColumnIndexOrThrow("CatName"))
                val catDescription = cursor.getString(cursor.getColumnIndexOrThrow("CatDescription"))
                val catImage = cursor.getString(cursor.getColumnIndexOrThrow("CatImage"))
                val catPrice = cursor.getInt(cursor.getColumnIndexOrThrow("CatPrice"))
                val cat = Cats(catID, catName, catDescription, catImage, catPrice)
                catList.add(cat)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return catList
    }

    fun addCheckoutHistory(checkoutHistory: CheckoutHistory) {
        val db = writableDatabase

        val values = ContentValues().apply {
            put("CheckoutID", checkoutHistory.checkoutID)
            put("UserID", checkoutHistory.userID)
            put("CheckoutDate", checkoutHistory.checkoutDate.time)
            put("TotalPrice", checkoutHistory.totalPrice)
        }
        db.insert("CheckoutHistory", null, values)

        var i = 0
        for (cartItem in checkoutHistory.checkedOutItems) {
            i+=1
            val itemValues = ContentValues().apply {
                put("CheckedOutItemID", UUID.randomUUID().toString())
                put("CheckoutID", checkoutHistory.checkoutID)
                put("CatID", cartItem.CatID)
                put("Quantity", cartItem.Quantity)
            }
            db.insert("CheckedOutItems", null, itemValues)
        }

        db.close()
    }
    fun getCheckoutHistory(userID: String): ArrayList<CheckoutHistory> {
        val db = readableDatabase
        val checkoutHistoryList = ArrayList<CheckoutHistory>()
        val cursor = db.rawQuery("SELECT * FROM CheckoutHistory WHERE userID = ?", arrayOf(userID))

        if (cursor.moveToFirst()) {
            do {
                val checkoutID = cursor.getString(cursor.getColumnIndexOrThrow("checkoutID"))
                val userID = cursor.getString(cursor.getColumnIndexOrThrow("userID"))
                val checkoutDate = Date(cursor.getLong(cursor.getColumnIndexOrThrow("checkoutDate")))
                val totalPrice = cursor.getInt(cursor.getColumnIndexOrThrow("totalPrice"))

                val checkedOutItems = ArrayList<CartData>()
                //Get checked out items for this checkoutID
                val checkedOutItemsCursor = db.rawQuery("SELECT * FROM CheckedOutItems WHERE CheckoutID = ?", arrayOf(checkoutID))

                if (checkedOutItemsCursor.moveToFirst()) {
                    do {
                        val cartID = checkedOutItemsCursor.getString(checkedOutItemsCursor.getColumnIndexOrThrow("CheckedOutItemID"))
                        val catID = checkedOutItemsCursor.getString(checkedOutItemsCursor.getColumnIndexOrThrow("CatID"))
                        val quantity = checkedOutItemsCursor.getInt(checkedOutItemsCursor.getColumnIndexOrThrow("Quantity"))
                        checkedOutItems.add(CartData(cartID, catID, checkoutID, userID, quantity))
                    } while (checkedOutItemsCursor.moveToNext())
                }
                checkedOutItemsCursor.close()

                val checkoutHistory = CheckoutHistory(checkoutID, userID, checkoutDate, checkedOutItems, totalPrice)
                checkoutHistoryList.add(checkoutHistory)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return checkoutHistoryList
    }

    fun addOrUpdateCartItem(cartItem: CartData) {
        val db = writableDatabase
        val cursor = db.rawQuery(
            "SELECT Quantity FROM CartData WHERE CatID = ? AND UserID = ?",
            arrayOf(cartItem.CatID, cartItem.UserID)
        )

        if (cursor.moveToFirst()) {
            val currentQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))
            val newQuantity = currentQuantity + cartItem.Quantity
            val sqlUpdate = "UPDATE CartData SET Quantity = ? WHERE CatID = ? AND UserID = ?"
            val statementUpdate = db.compileStatement(sqlUpdate)
            statementUpdate.bindLong(1, newQuantity.toLong())
            statementUpdate.bindString(2, cartItem.CatID)
            statementUpdate.bindString(3, cartItem.UserID)
            statementUpdate.executeUpdateDelete()
        } else {
            val sqlInsert = "INSERT INTO CartData (CartID, CatID, CheckoutID, UserID, Quantity) VALUES (?, ?, ?, ?, ?)"
            val statementInsert = db.compileStatement(sqlInsert)
            statementInsert.bindString(1, cartItem.CartID)
            statementInsert.bindString(2, cartItem.CatID)
            statementInsert.bindString(3, cartItem.CheckoutID)
            statementInsert.bindString(4, cartItem.UserID)
            statementInsert.bindLong(5, cartItem.Quantity.toLong())
            statementInsert.executeInsert()
        }
        cursor.close()
    }

    fun getUserCartList(userID: String): ArrayList<CartData> {
        val cartList = ArrayList<CartData>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM CartData WHERE UserID = ?", arrayOf(userID))
        if (cursor.moveToFirst()) {
            do {
                val cartID = cursor.getString(cursor.getColumnIndexOrThrow("CartID"))
                val catID = cursor.getString(cursor.getColumnIndexOrThrow("CatID"))
                val checkoutID = cursor.getString(cursor.getColumnIndexOrThrow("CheckoutID"))
                val userID = cursor.getString(cursor.getColumnIndexOrThrow("UserID"))
                val quantity = cursor.getInt(cursor.getColumnIndexOrThrow("Quantity"))
                val cartItem = CartData(cartID, catID, checkoutID, userID, quantity)
                cartList.add(cartItem)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return cartList
    }

    fun deleteUserCart(userID: String) {
        val db = this.writableDatabase
        db.delete("CartData", "UserID = ?", arrayOf(userID))
        db.close()
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS Users")
        db?.execSQL("DROP TABLE IF EXISTS Cats")
        db?.execSQL("DROP TABLE IF EXISTS CartData")
        db?.execSQL("DROP TABLE IF EXISTS CheckoutHistory")
        db?.execSQL("DROP TABLE IF EXISTS CheckedOutItems")
        onCreate(db)
    }
}