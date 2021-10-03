package org.kpu.sinstagram

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.utils.Constants
import org.kpu.sinstagram.utils.Constants.TAG

class FavoriteActivity : AppCompatActivity() {
    var post_intent_flag : String? = ""
    var post_photo : String? = ""
    var post_user_email : String? = ""

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite)

        var favorite_searchview : SearchView = findViewById(R.id.favorite_searchview)
        val favorite_rcv = findViewById<RecyclerView>(R.id.favorite_rcv)
        val favorite_back = findViewById<ImageView>(R.id.favorite_back)

        val intent = intent
        val favorite_rcv_list : ArrayList<C_rcv_favorite>? = intent.getSerializableExtra("favorite_rcv_list") as ArrayList<C_rcv_favorite>?
        post_intent_flag = intent.getStringExtra("flag")
        post_photo = intent.getStringExtra("photo")
        post_user_email = intent.getStringExtra("post_user_email")

        val db = FirebaseFirestore.getInstance()

        var fav_list = arrayListOf<C_rcv_favorite>()

        if(favorite_rcv_list != null){
            fav_list = favorite_rcv_list
        }else {
            val fav_data_map : MutableMap<String, String> = intent.getSerializableExtra("fav_data_map")!! as MutableMap<String, String>
            val sorted_fav_map = fav_data_map.toSortedMap(Comparator.reverseOrder())
            for((_, user_email) in sorted_fav_map){
                db.collection("profile").whereEqualTo("email", user_email).addSnapshotListener { snapshots, e ->
                    if(e != null){
                        Log.e(TAG, "FavoriteActivity - FireStore profile Listen failed", e)
                        return@addSnapshotListener
                    }

                    if(snapshots != null && snapshots.isEmpty == false){
                        for(snapshot in snapshots){
                            val email = snapshot.data["email"] as String
                            val displayName = snapshot.data["displayName"] as String
                            val profile_photo = snapshot.data["profile_photo"] as String

                            fav_list.add(C_rcv_favorite(displayName, profile_photo, email))
                        }

                        val rcv_adapter = Favorite_rcv_Adapter(fav_list)
                        favorite_rcv.adapter = rcv_adapter
                        favorite_rcv.layoutManager = LinearLayoutManager(this)
                        favorite_rcv.setHasFixedSize(true)
                    }
                }
            }

        }

        var rcv_adapter = Favorite_rcv_Adapter(fav_list)
        favorite_rcv.adapter = rcv_adapter
        favorite_rcv.layoutManager = LinearLayoutManager(this)
        favorite_rcv.setHasFixedSize(true)

        favorite_searchview.setOnClickListener {
            favorite_searchview.onActionViewExpanded()     // searchview whole clickable
        }

        favorite_searchview.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                val filterd_list = arrayListOf<C_rcv_favorite>()

                for(fav_data in fav_list){
                    if(fav_data.displayName.lowercase().contains(query!!.lowercase())){
                        filterd_list.add(fav_data)
                    }
                }

                rcv_adapter = Favorite_rcv_Adapter(filterd_list)
                favorite_rcv.adapter = rcv_adapter
                favorite_rcv.layoutManager = LinearLayoutManager(this@FavoriteActivity)
                favorite_rcv.setHasFixedSize(true)

                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                val filterd_list = arrayListOf<C_rcv_favorite>()

                for(fav_data in fav_list){
                    if(fav_data.displayName.lowercase().contains(query!!.lowercase())){
                        filterd_list.add(fav_data)
                    }
                }

                rcv_adapter = Favorite_rcv_Adapter(filterd_list)
                favorite_rcv.adapter = rcv_adapter
                favorite_rcv.layoutManager = LinearLayoutManager(this@FavoriteActivity)
                favorite_rcv.setHasFixedSize(true)

                return true
            }

        })

        favorite_back.setOnClickListener {
            finish()
        }



    }

    override fun onBackPressed() {
        super.onBackPressed()
        if(post_intent_flag == "backTopost"){
            val intent = Intent(this, PostActivity::class.java)
            intent.putExtra("flag", true)
            intent.putExtra("photo", post_photo)
            intent.putExtra("post_user_email", post_user_email)
            startActivity(intent)
        }
    }
}