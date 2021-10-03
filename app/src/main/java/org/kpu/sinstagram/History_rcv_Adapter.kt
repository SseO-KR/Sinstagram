package org.kpu.sinstagram

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.utils.Constants.TAG
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class History_rcv_Adapter(private var history_rcv_list : ArrayList<C_rcv_history>) : RecyclerView.Adapter<History_rcv_Adapter.ViewHolder>() {

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val rcv_history_displayName : TextView
        val rcv_history_photo : ImageView
        val rcv_history_profile_photo : ImageView
        val rcv_history_text : TextView
        val rcv_history_layout : LinearLayout
        init {
            rcv_history_layout = view.findViewById(R.id.rcv_history_layout)
            rcv_history_displayName = view.findViewById(R.id.rcv_history_displayName)
            rcv_history_photo = view.findViewById(R.id.rcv_history_photo)
            rcv_history_profile_photo = view.findViewById(R.id.rcv_history_profile_photo)
            rcv_history_text = view.findViewById(R.id.rcv_history_text)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rcview_history, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = history_rcv_list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.rcv_history_displayName.text = history_rcv_list[position].displayName
        Glide.with(holder.itemView.context).load(history_rcv_list[position].profile_photo).into(holder.rcv_history_profile_photo)

        val history_activity : HistoryActivity = holder.itemView.context as HistoryActivity
        holder.rcv_history_displayName.setOnClickListener {
            val intent = Intent(holder.itemView.context, MainActivity::class.java)
            intent.putExtra("user_email", history_rcv_list[position].email)
            intent.putExtra("flag", "no")
            history_activity.setResult(Activity.RESULT_OK, intent)
            history_activity.finish()
        }

        holder.rcv_history_profile_photo.setOnClickListener {
            val intent = Intent(holder.itemView.context, MainActivity::class.java)
            intent.putExtra("user_email", history_rcv_list[position].email)
            intent.putExtra("flag", "no")
            history_activity.setResult(Activity.RESULT_OK, intent)
            history_activity.finish()
        }

        when(history_rcv_list[position].type){
            "favorite" ->{
                holder.rcv_history_text.text = "님이 좋아요를 누르셨습니다."
                Glide.with(holder.itemView.context).load(history_rcv_list[position].photo).into(holder.rcv_history_photo)
                holder.rcv_history_photo.setOnClickListener {
                    val intent = Intent(holder.itemView.context, PostActivity::class.java)
                    intent.putExtra("photo", history_rcv_list[position].photo)
                    intent.putExtra("user_email", history_rcv_list[position].email)
                    history_activity.resultlauncher.launch(intent)
                }
                holder.rcv_history_layout.setOnClickListener {
                    val intent = Intent(holder.itemView.context, PostActivity::class.java)
                    intent.putExtra("photo", history_rcv_list[position].photo)
                    intent.putExtra("user_email", history_rcv_list[position].email)
                    history_activity.resultlauncher.launch(intent)
                }
            }
            "comment" -> {
                holder.rcv_history_text.text = "님이 댓글을 남기셨습니다."
                Glide.with(holder.itemView.context).load(history_rcv_list[position].photo).into(holder.rcv_history_photo)
                holder.rcv_history_photo.setOnClickListener {
                    val intent = Intent(holder.itemView.context, PostActivity::class.java)
                    intent.putExtra("photo", history_rcv_list[position].photo)
                    intent.putExtra("user_email", history_rcv_list[position].email)
                    holder.itemView.context.startActivity(intent)
                }
                holder.rcv_history_layout.setOnClickListener {
                    val intent = Intent(holder.itemView.context, PostActivity::class.java)
                    intent.putExtra("photo", history_rcv_list[position].photo)
                    intent.putExtra("user_email", history_rcv_list[position].email)
                    holder.itemView.context.startActivity(intent)
                }
            }
            "follow" -> {
                holder.rcv_history_text.text = "님이 팔로우 하셨습니다."
                val historyactivity = holder.itemView.context as HistoryActivity
                var fFlag = false
                val currentuser_email = FirebaseAuth.getInstance().currentUser?.email
                val db = FirebaseFirestore.getInstance()

                historyactivity.following?.forEach { (_, email) ->
                    if(email == history_rcv_list[position].email) {
                        holder.rcv_history_photo.setImageResource(R.drawable.follow)
                        fFlag = false
                    }else {
                        holder.rcv_history_photo.setImageResource(R.drawable.following)
                        fFlag = true
                    }
                }

                holder.rcv_history_photo.isClickable = true

                holder.rcv_history_photo.setOnClickListener {
                    if(fFlag == true){
                        historyactivity.following?.forEach(){ (timestamp, email) ->
                            if(email == history_rcv_list[position].email){
                                historyactivity.following?.remove(timestamp)
                            }
                        }
                        val currentuser_data = hashMapOf("following" to historyactivity.following)
                        db.collection("follow").document(currentuser_email!!).update(currentuser_data as MutableMap<String, Any>).addOnSuccessListener {
                            Log.d(TAG, "History_rcv_Adapter - currentuser follow data update success")
                        }.addOnFailureListener {
                            Log.e(TAG, "History_rcv_Adapter - currentuser follow data update fail", it)
                        }

                        var user_follower = mutableMapOf<String, String>()
                        db.collection("follow").whereEqualTo("email", history_rcv_list[position].email).addSnapshotListener { snapshots, e ->
                            if(e != null){
                                Log.e(TAG, "History_rcv_Adapter - FireStore follow Listen fail", e)
                                return@addSnapshotListener
                            }

                            if(snapshots != null && snapshots.isEmpty == false) {
                                for (snapshot in snapshots) {
                                    user_follower = snapshot.data["follower"] as MutableMap<String, String>
                                }
                            }

                            user_follower.forEach(){(timestamp, email) ->
                                if(email == currentuser_email) user_follower.remove(timestamp)
                            }

                            val user_data = hashMapOf("follower" to user_follower)
                            db.collection("follow").document(history_rcv_list[position].email).update(user_data as MutableMap<String, Any>).addOnSuccessListener {
                                Log.d(TAG, "History_rcv_Adapter - user follow data update success")
                            }.addOnFailureListener {
                                Log.e(TAG, "History_rcv_Adapter - user follow data update fail", it)
                            }

                            holder.rcv_history_photo.setImageResource(R.drawable.follow)
                            fFlag = false
                        }


                    }else{
                        val timestamp = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
                        historyactivity.following?.put(timestamp, history_rcv_list[position].email)

                        val currentuser_data = hashMapOf("following" to historyactivity.following)
                        db.collection("follow").document(currentuser_email!!).update(currentuser_data as MutableMap<String, Any>).addOnSuccessListener {
                            Log.d(TAG, "History_rcv_Adapter - currentuser follow data update success")
                        }.addOnFailureListener {
                            Log.e(TAG, "History_rcv_Adapter - currentuser follow data update fail", it)
                        }

                        var user_follower = mutableMapOf<String, String>()
                        db.collection("follow").whereEqualTo("email", history_rcv_list[position].email).addSnapshotListener { snapshots, e ->
                            if(e != null){
                                Log.e(TAG, "History_rcv_Adapter - FireStore follow Listen fail", e)
                                return@addSnapshotListener
                            }

                            if(snapshots != null && snapshots.isEmpty == false) {
                                for (snapshot in snapshots) {
                                    user_follower = snapshot.data["follower"] as MutableMap<String, String>
                                }
                            }

                            user_follower.put(timestamp, currentuser_email)

                            val user_data = hashMapOf("follower" to user_follower)
                            db.collection("follow").document(history_rcv_list[position].email).update(user_data as MutableMap<String, Any>).addOnSuccessListener {
                                Log.d(TAG, "History_rcv_Adapter - user follow data update success")
                            }.addOnFailureListener {
                                Log.e(TAG, "History_rcv_Adapter - user follow data update fail", it)
                            }

                            holder.rcv_history_photo.setImageResource(R.drawable.following)
                            fFlag = true

                        }

                    }

                }
            }
        }


    }

}