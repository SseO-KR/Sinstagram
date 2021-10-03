package org.kpu.sinstagram

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.main_fragment.Main_Fragment4
import org.kpu.sinstagram.utils.Constants
import org.kpu.sinstagram.utils.Constants.TAG
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Favorite_rcv_Adapter(private var favorite_rcv_list: ArrayList<C_rcv_favorite>) :
    RecyclerView.Adapter<Favorite_rcv_Adapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rcv_favorite_follow: Button
        val rcv_favorite_displayName: TextView
        val rcv_favorite_profile: ImageView
        var currentuser_follower: MutableMap<String, String>
        var currentuser_following: MutableMap<String, String>
        var user_follower: MutableMap<String, String>
        var user_following: MutableMap<String, String>
        val currentuser_email: String
        var fFlag: Boolean

        init {
            fFlag = false
            currentuser_email = FirebaseAuth.getInstance().currentUser?.email!!
            currentuser_following = mutableMapOf<String, String>()
            currentuser_follower = mutableMapOf<String, String>()
            user_follower = mutableMapOf<String, String>()
            user_following = mutableMapOf<String, String>()
            rcv_favorite_profile = view.findViewById(R.id.rcv_favorite_profile)
            rcv_favorite_displayName = view.findViewById(R.id.rcv_favorite_displayName)
            rcv_favorite_follow = view.findViewById(R.id.rcv_favorite_follow)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Favorite_rcv_Adapter.ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.rcview_favorite, parent, false)
        return Favorite_rcv_Adapter.ViewHolder(view)
    }

    override fun getItemCount(): Int = favorite_rcv_list.size

    override fun onBindViewHolder(holder: Favorite_rcv_Adapter.ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(favorite_rcv_list[position].profile_photo)
            .into(holder.rcv_favorite_profile)
        holder.rcv_favorite_displayName.text = favorite_rcv_list[position].displayName

        if(favorite_rcv_list[position].email == holder.currentuser_email){
            holder.rcv_favorite_follow.visibility = View.GONE
        }

        val favorite_activity = holder.itemView.context as FavoriteActivity

        val db = FirebaseFirestore.getInstance()

        db.collection("follow").whereEqualTo("email", holder.currentuser_email)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e(Constants.TAG, "FireStore Listen follow falied", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && snapshots.isEmpty == false) {

                    for (snapshot in snapshots) {
                        holder.currentuser_following =
                            snapshot.data["following"] as MutableMap<String, String>
                        holder.currentuser_follower = snapshot.data["follower"] as MutableMap<String, String>

                    }
                }
            }

        db.collection("follow").whereEqualTo("email", favorite_rcv_list[position].email)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e(Constants.TAG, "FireStore Listen follow falied", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && snapshots.isEmpty == false) {

                    for (snapshot in snapshots) {
                        holder.user_follower = snapshot.data["follower"] as MutableMap<String, String>
                        holder.user_following = snapshot.data["following"] as MutableMap<String, String>
                    }
                }
            }

        if (holder.currentuser_following.containsValue(favorite_rcv_list[position].email) == true) {
            holder.rcv_favorite_follow.setBackgroundResource(R.drawable.profile_button_resource)
            holder.fFlag = true
        } else {
            holder.rcv_favorite_follow.setBackgroundResource(R.drawable.follow_button_resource)
            holder.fFlag = false
        }

        holder.rcv_favorite_follow.setOnClickListener {

            if (holder.fFlag == true) {

                for((timestamp, email) in holder.user_follower){
                    if(email == holder.currentuser_email) holder.user_follower.remove(timestamp)
                }
                for((timestamp, email) in holder.currentuser_following){
                    if(email == favorite_rcv_list[position].email) holder.currentuser_following.remove(timestamp)
                }
                holder.fFlag = false
                holder.rcv_favorite_follow.setBackgroundResource(R.drawable.follow_button_resource)
            } else {
                val timestamp = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
                holder.user_follower.put(timestamp, holder.currentuser_email)
                holder.currentuser_following.put(timestamp, favorite_rcv_list[position].email)
                holder.fFlag = true
                holder.rcv_favorite_follow.setBackgroundResource(R.drawable.profile_button_resource)
            }
            val currentuser_data = hashMapOf("following" to holder.currentuser_following)
            db.collection("follow").document(holder.currentuser_email)
                .update(currentuser_data as MutableMap<String, Any>).addOnSuccessListener {
                    Log.d(TAG, "Favorite - currentuser following update success")
                    val user_data = hashMapOf("follower" to holder.user_follower)
                    db.collection("follow").document(favorite_rcv_list[position].email)
                        .update(user_data as MutableMap<String, Any>).addOnSuccessListener {
                            Log.d(TAG, "Favorite - user follower update success")

                        }.addOnFailureListener {
                            Log.e(TAG, "Favorite - user follower update fail")
                        }
                }.addOnFailureListener {
                    Log.e(TAG, "Favorite - currentuser follow update fail", it)
                }

        }

        holder.rcv_favorite_displayName.setOnClickListener {
            if(favorite_activity.post_intent_flag == "postTofavorite"){
                val intent = Intent(holder.itemView.context, PostActivity::class.java)
                intent.putExtra("user_email", favorite_rcv_list[position].email)
                intent.putExtra("flag", "postTofavorite")
                intent.putExtra("favorite_rcv_list", favorite_rcv_list as Serializable)
                intent.putExtra("photo", favorite_activity.post_photo)
                favorite_activity.setResult(Activity.RESULT_OK, intent)
                favorite_activity.finish()
            }else {
                val intent = Intent(holder.itemView.context, MainActivity::class.java)
                intent.putExtra("user_email", favorite_rcv_list[position].email)
                intent.putExtra("flag", "favorite")
                intent.putExtra("favorite_rcv_list", favorite_rcv_list as Serializable)
                favorite_activity.setResult(Activity.RESULT_OK, intent)
                favorite_activity.finish()
            }
        }

        holder.rcv_favorite_profile.setOnClickListener {
            if(favorite_activity.post_intent_flag == "postTofavorite"){
                val intent = Intent(holder.itemView.context, PostActivity::class.java)
                intent.putExtra("user_email", favorite_rcv_list[position].email)
                intent.putExtra("flag", "postTofavorite")
                intent.putExtra("favorite_rcv_list", favorite_rcv_list as Serializable)
                intent.putExtra("photo", favorite_activity.post_photo)
                favorite_activity.setResult(Activity.RESULT_OK, intent)
                favorite_activity.finish()
            }else {
                val intent = Intent(holder.itemView.context, MainActivity::class.java)
                intent.putExtra("user_email", favorite_rcv_list[position].email)
                intent.putExtra("flag", "favorite")
                intent.putExtra("favorite_rcv_list", favorite_rcv_list as Serializable)
                favorite_activity.setResult(Activity.RESULT_OK, intent)
                favorite_activity.finish()
            }
        }

    }
}