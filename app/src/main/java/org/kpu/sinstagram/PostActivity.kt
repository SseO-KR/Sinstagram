package org.kpu.sinstagram

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.utils.Constants
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PostActivity : AppCompatActivity() {
    var flag = false
    var post_user_email : String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val post_back : ImageView = findViewById(R.id.post_back)
        val post_comment : ImageView = findViewById(R.id.post_comment)
        val post_contents : TextView = findViewById(R.id.post_contents)
        val post_displayname : TextView = findViewById(R.id.post_displayname)
        val post_displayname_small : TextView = findViewById(R.id.post_displayname_small)
        val post_favnum : TextView = findViewById(R.id.post_favnum)
        val post_favorite : ImageView = findViewById(R.id.post_favorite)
        val post_imgview : ImageView = findViewById(R.id.post_imgview)
        val post_menu : ImageView = findViewById(R.id.post_menu)
        val post_profile : ImageView = findViewById(R.id.post_profile_img)
        val post_send : ImageView = findViewById(R.id.post_send)

        val get_intent = intent
        val photo : String = get_intent.getStringExtra("photo")!!
        post_user_email = get_intent.getStringExtra("user_email")
        flag = get_intent.getBooleanExtra("flag", false)

        val currentuser_email = FirebaseAuth.getInstance().currentUser?.email
        val db = FirebaseFirestore.getInstance()

        var timestamp = ""
        var displayName = ""
        var profile_photo = ""
        var contents = ""
        var fFlag = false
        var comment : MutableMap<String, MutableMap<String, ArrayList<String>>>? = null
        var favorite_map = mutableMapOf<String,String>()

        Glide.with(this).load(photo).into(post_imgview)

        post_back.setOnClickListener {
            finish()
        }

        post_comment.setOnClickListener {
            val intent = Intent(this, CommentActivity::class.java)
            intent.putExtra("comment", comment as Serializable)
            intent.putExtra("profile_photo", profile_photo)
            intent.putExtra("displayName", displayName)
            intent.putExtra("timestamp", timestamp)
            intent.putExtra("contents", contents)
            startActivity(intent)
        }

        post_favnum.setOnClickListener {
            val intent = Intent(this, FavoriteActivity::class.java)
            intent.putExtra("fav_data_map", favorite_map as Serializable)
            intent.putExtra("flag", "postTofavorite")
            intent.putExtra("photo", photo)
            resultlauncher.launch(intent)
        }

        post_favorite.setOnClickListener {
            if(fFlag == true){

                post_favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                fFlag = false

                for((key, value) in favorite_map){
                    if(value == currentuser_email){
                        favorite_map.remove(key)
                    }
                }

                val data = hashMapOf(
                    "favorite_map" to favorite_map
                )
                if(favorite_map.size == 0){
                    post_favnum.visibility = View.GONE
                }else {
                    post_favnum.visibility = View.VISIBLE
                    post_favnum.text = "좋아요 " + favorite_map.size.toString() + "개"
                }
                db.collection("data").document(timestamp).update(data as Map<String, Any>).addOnSuccessListener {
                    Log.d(Constants.TAG, "PostActivity - favorite data update success")
                }.addOnFailureListener {
                    Log.e(Constants.TAG, "PostActivity - favorite data update fail")
                }

            }else{

                post_favorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                fFlag = true

                val favorite_timestamp = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
                favorite_map.put(favorite_timestamp, currentuser_email!!)

                if(favorite_map.size == 0){
                    post_favnum.visibility = View.GONE
                }else {
                    post_favnum.visibility = View.VISIBLE
                    post_favnum.text = "좋아요 " + favorite_map.size.toString() + "개"
                }
                val data = hashMapOf("favorite_map" to favorite_map)
                db.collection("data").document(timestamp).update(data as Map<String, Any>).addOnSuccessListener {
                    Log.d(Constants.TAG, "Home_rcv_Adapter - favorite data update success")
                }.addOnFailureListener {
                    Log.e(Constants.TAG, "Home_rcv_Adapter - favorite data update fail")
                }

            }
        }

        db.collection("profile").whereEqualTo("email", post_user_email).addSnapshotListener { snapshots, e ->
            if(e != null){
                Log.e(Constants.TAG, "HistoryActivity - FireStore data Listen fail", e)
                return@addSnapshotListener
            }

            if(snapshots != null && snapshots.isEmpty == false) {
                for (snapshot in snapshots) {
                    displayName = snapshot.data["displayName"] as String
                    profile_photo = snapshot.data["profile_photo"] as String

                    post_displayname.text = displayName
                    post_displayname_small.text = displayName
                    Glide.with(this).load(profile_photo).into(post_profile)
                }

            }
        }

        db.collection("data").whereEqualTo("photo", photo).addSnapshotListener { snapshots, e ->
            if(e != null){
                Log.e(Constants.TAG, "HistoryActivity - FireStore data Listen fail", e)
                return@addSnapshotListener
            }

            if(snapshots != null && snapshots.isEmpty == false) {
                for (snapshot in snapshots) {
                    contents = snapshot.data["contents"] as String
                    timestamp = snapshot.data["timestamp"] as String
                    comment = snapshot.data["comment"] as MutableMap<String, MutableMap<String, ArrayList<String>>>
                    favorite_map = snapshot.data["favorite_map"] as MutableMap<String, String>

                    post_contents.text = contents
                    if(favorite_map.size == 0){
                        post_favnum.visibility = View.GONE
                    }else {
                        post_favnum.visibility = View.VISIBLE
                        post_favnum.text = "좋아요 " + favorite_map.size.toString() + "개"
                    }
                    if(favorite_map.containsValue(currentuser_email) == true){
                        fFlag = true
                        post_favorite.setImageResource(R.drawable.ic_baseline_favorite_24)
                    }else{
                        fFlag = false
                        post_favorite.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                    }
                }
            }
        }



    }

    val resultlauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val intent = it.data
            val user_email = intent?.getStringExtra("user_email")!!
            val flag = intent.getStringExtra("flag")!!

            if(flag == "postTofavorite"){
                val photo = intent.getStringExtra("photo")
                val fav_rcv_list : ArrayList<C_rcv_favorite> = intent.getSerializableExtra("favorite_rcv_list") as ArrayList<C_rcv_favorite>

                val ptf_intent = Intent(this, HistoryActivity::class.java)
                ptf_intent.putExtra("user_email", user_email)
                ptf_intent.putExtra("post_user_email", post_user_email)
                ptf_intent.putExtra("flag", flag)
                ptf_intent.putExtra("photo", photo)
                ptf_intent.putExtra("favorite_rcv_list", fav_rcv_list as Serializable)
                setResult(Activity.RESULT_OK, ptf_intent)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        if(flag == true){
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }

    }
}