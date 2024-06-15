package com.zs.jollycat.fragment

import android.content.Context
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import com.zs.jollycat.AboutActivity
import com.zs.jollycat.CheckoutHistoryActivity
import com.zs.jollycat.R
import com.zs.jollycat.RegisterActivity
import com.zs.jollycat.adapter.CatAdapter
import com.zs.jollycat.database.DatabaseHelper
import com.zs.jollycat.model.Cats
import org.json.JSONArray
import org.json.JSONException

class HomeFragment : Fragment() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var requestQueue: RequestQueue
    private lateinit var catRecyclerView: RecyclerView
    private lateinit var catAdapter: CatAdapter

    //private val catList = Database.catList

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
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val userID = arguments?.getString("userID")

        requestQueue = Volley.newRequestQueue(requireContext())

        val url = "https://api.npoint.io/3fa9a95557f89f097063"
        val request = JsonArrayRequest(
            Request.Method.GET, url, null, { response ->
                try {
                    val catList = parseJSON(response)
                    addCatsToDatabase(catList)
                    updateRecyclerView()
                }catch (e : JSONException){
                    e.printStackTrace()
                }
            },
            { error -> Log.e("Volley error", error.toString())
            }
        )
        requestQueue.add(request)

        val catList = dbHelper.getAllCats()

        catRecyclerView = view.findViewById<RecyclerView>(R.id.rvCatList)
        catAdapter = CatAdapter(catList, userID ?: "")
        catRecyclerView.adapter = catAdapter
        catRecyclerView.layoutManager = LinearLayoutManager(activity)

        val buttonAboutUs = view.findViewById<TextView>(R.id.btn_aboutUs)
        buttonAboutUs.setOnClickListener {
            val intent = Intent(activity, AboutActivity::class.java)
            intent.putExtra("userID", userID)
            startActivity(intent)
        }

        return view
    }

    private fun updateRecyclerView() {
        val updatedCatList = dbHelper.getAllCats()
        catAdapter.updateData(updatedCatList)
    }

    private fun parseJSON(jsonArray: JSONArray): ArrayList<Cats>{
        val catsList = ArrayList<Cats>()
        try {
            for (i in 0 until jsonArray.length()){
                val catJsonObject = jsonArray.getJSONObject(i)
                val catID = catJsonObject.getString("CatID")
                val catName = catJsonObject.getString("CatName")
                val catImage = catJsonObject.getString("CatImage")
                val catPrice = catJsonObject.getInt("CatPrice")
                val catDescription = catJsonObject.getString("CatDescription")
                catsList.add(Cats(catID, catName, catDescription,catImage, catPrice))
            }
        } catch(e : JSONException){
            e.printStackTrace()
        }
        return catsList
    }

    private fun addCatsToDatabase(catList: ArrayList<Cats>) {
        val db: SQLiteDatabase = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            for (cat in catList) {
                val existingCat = dbHelper.getCatFromDatabase(cat.CatID)
                if (existingCat == null) {
                    val sql = "INSERT INTO Cats (CatID, CatName, CatDescription, CatImage, CatPrice) VALUES (?, ?, ?, ?, ?)"
                    val statement = db.compileStatement(sql)
                    statement.bindString(1, cat.CatID)
                    statement.bindString(2, cat.CatName)
                    statement.bindString(3, cat.CatDescription)
                    statement.bindString(4, cat.CatImage)
                    statement.bindLong(5, cat.CatPrice.toLong())
                    statement.executeInsert()
                } else {
                    Log.d("Database", "Cat with ID ${cat.CatID} already exists")
                }
            }
            db.setTransactionSuccessful()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }
}