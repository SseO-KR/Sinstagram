package org.kpu.sinstagram

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import org.kpu.sinstagram.C_rcv_home
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import org.kpu.sinstagram.main_fragment.Main_Fragment3
import org.kpu.sinstagram.main_fragment.Main_Fragment4
import org.kpu.sinstagram.utils.Constants
import org.kpu.sinstagram.utils.Constants.TAG
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Home_rcv_Adapter(private var home_rcv_list : ArrayList<C_rcv_home>) : RecyclerView.Adapter<Home_rcv_Adapter.ViewHolder>() {

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val storage : FirebaseStorage
        val profile_img_home : ImageView
        val displayname_home : TextView
        val imgview_home : ImageView
        val favorite_home : ImageView
        val comment_home : ImageView
        val send_home : ImageView
        val favnum_home : TextView
        val displayname_small_home : TextView
        val contents_home : TextView
        val currentuser_email : String
        var displayName : String
        var profile_photo : String
        init {
            profile_photo = ""
            displayName = ""
            currentuser_email = FirebaseAuth.getInstance().currentUser?.email!!
            storage = FirebaseStorage.getInstance()
            profile_img_home = view.findViewById(R.id.item_profile_img_home)
            displayname_home = view.findViewById(R.id.item_displayname_home)
            imgview_home = view.findViewById(R.id.item_imgview_home)
            favorite_home = view.findViewById(R.id.item_favorite_home)
            comment_home = view.findViewById(R.id.item_comment_home)
            send_home = view.findViewById(R.id.item_send_home)
            favnum_home = view.findViewById(R.id.item_favnum_home)
            displayname_small_home = view.findViewById(R.id.item_displayname_small_home)
            contents_home = view.findViewById(R.id.item_contents_home)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.rcview_home, parent, false)
            return ViewHolder(view)
        }

    override fun getItemCount(): Int = home_rcv_list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val mainactivity = holder.itemView.context as MainActivity

        val db = FirebaseFirestore.getInstance()
        db.collection("profile").whereEqualTo("email", home_rcv_list[position].email).addSnapshotListener { snapshots, e ->
            if (e != null) {
                Log.e(Constants.TAG, "FireStore Listen falied", e)
                return@addSnapshotListener
            }

            if (snapshots != null && snapshots.isEmpty == false) {
                for (snapshot in snapshots) {
                    holder.displayName = snapshot.data["displayName"] as String
                    holder.profile_photo = snapshot.data["profile_photo"] as String

                    Glide.with(holder.itemView.context).load(holder.profile_photo).into(holder.profile_img_home)
                    holder.displayname_home.text = holder.displayName
                    holder.displayname_small_home.text = holder.displayName
                }
            }
        }

        fun goProfile() {
            if (home_rcv_list[position].email == holder.currentuser_email) {
                val fragment3 = Main_Fragment3()
//                val bundle = Bundle()
//                bundle.putString("user_email", holder.currentuser_email)
//                fragment3.arguments = bundle
                mainactivity.replaceFragment(fragment3)

            } else {
                val fragment4 = Main_Fragment4()
                val bundle = Bundle()
                bundle.putString("user_email", home_rcv_list[position].email)
                fragment4.arguments = bundle
                mainactivity.replaceFragment(fragment4)
            }
        }

        holder.displayname_home.setOnClickListener {
            goProfile()
        }

        holder.profile_img_home.setOnClickListener {
            goProfile()
        }

        holder.displayname_small_home.setOnClickListener {
            goProfile()
        }

        Glide.with(holder.itemView.context).load(home_rcv_list[position].photo).into(holder.imgview_home)

        if(home_rcv_list[position].favorite_boolean == true){
            holder.favorite_home.setImageResource(R.drawable.ic_baseline_favorite_24)
        }else{
            holder.favorite_home.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }

        holder.favorite_home.setOnClickListener {
            if(home_rcv_list[position].favorite_boolean == true){

                holder.favorite_home.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                home_rcv_list[position].favorite_boolean = false

                val favorite_map : MutableMap<String, String>? = home_rcv_list[position].favorite_map
                favorite_map?.forEach(){ (key, value) ->
                    if(value == holder.currentuser_email){
                        favorite_map.remove(key)
                    }
                }


                val data = hashMapOf(
                    "favorite_map" to favorite_map
                )
                db.collection("data").document(home_rcv_list[position].timestamp).update(data as Map<String, Any>).addOnSuccessListener {
                    Log.d(TAG, "Home_rcv_Adapter - favorite data update success")
                }.addOnFailureListener {
                    Log.e(TAG, "Home_rcv_Adapter - favorite data update fail")
                }

            }else{

                holder.favorite_home.setImageResource(R.drawable.ic_baseline_favorite_24)
                home_rcv_list[position].favorite_boolean = true

                var favorite_map = home_rcv_list[position].favorite_map
                val timestamp = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
                favorite_map?.put(timestamp, holder.currentuser_email)
                //home_rcv_list[position].favorite_map?.put(timestamp, holder.currentuser_email)

                val data = hashMapOf("favorite_map" to favorite_map)
                db.collection("data").document(home_rcv_list[position].timestamp).update(data as Map<String, Any>).addOnSuccessListener {
                    Log.d(TAG, "Home_rcv_Adapter - favorite data update success")
                }.addOnFailureListener {
                    Log.e(TAG, "Home_rcv_Adapter - favorite data update fail")
                }

            }
        }

        holder.comment_home.setOnClickListener {
            val intent = Intent(holder.itemView.context, CommentActivity::class.java)
            intent.putExtra("comment", home_rcv_list[position].comment as Serializable)
            intent.putExtra("displayName", holder.displayName)
            intent.putExtra("profile_photo", holder.profile_photo)
            intent.putExtra("timestamp", home_rcv_list[position].timestamp)
            intent.putExtra("contents", home_rcv_list[position].contents)
            mainactivity.resultlauncher.launch(intent)
        }

        holder.send_home.setOnClickListener {

        }

        if(home_rcv_list[position].favorite_num == 0){
            holder.favnum_home.visibility = View.GONE
        }else{
            holder.favnum_home.visibility = View.VISIBLE
            holder.favnum_home.text = "좋아요 " + home_rcv_list[position].favorite_num.toString() + "개"
        }



        holder.favnum_home.setOnClickListener {
            val intent = Intent(holder.itemView.context, FavoriteActivity::class.java)
            intent.putExtra("fav_data_map", home_rcv_list[position].favorite_map as Serializable)
            mainactivity.resultlauncher.launch(intent)
        }


        holder.contents_home.text = home_rcv_list[position].contents
    }
}