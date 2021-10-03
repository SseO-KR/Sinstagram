package org.kpu.sinstagram

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.main_fragment.Main_Fragment3
import org.kpu.sinstagram.main_fragment.Main_Fragment4
import org.kpu.sinstagram.utils.Constants
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Comment_rcv_Adapter(private var comment_list : ArrayList<C_rcv_comment>) : RecyclerView.Adapter<Comment_rcv_Adapter.ViewHolder>() {

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val rcv_comment_comment : TextView
        val rcv_comment_displayName : TextView
        val rcv_comment_profile_photo : ImageView
        val rcv_comment_recomment : TextView
        val rcv_comment_time : TextView
        val currentuser_email : String
        val rcv_comment_recomment_rcv : RecyclerView
        init{
            currentuser_email = FirebaseAuth.getInstance().currentUser?.email!!
            rcv_comment_comment = view.findViewById<TextView>(R.id.rcv_comment_comment)
            rcv_comment_displayName = view.findViewById<TextView>(R.id.rcv_comment_displayName)
            rcv_comment_profile_photo = view.findViewById<ImageView>(R.id.rcv_comment_profile_photo)
            rcv_comment_recomment = view.findViewById<TextView>(R.id.rcv_comment_recomment)
            rcv_comment_time = view.findViewById<TextView>(R.id.rcv_comment_time)
            rcv_comment_recomment_rcv = view.findViewById(R.id.rcv_comment_recomment_rcv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rcview_comment, parent, false)
        return Comment_rcv_Adapter.ViewHolder(view)
    }

    override fun getItemCount(): Int = comment_list.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val comment_activity = holder.itemView.context as CommentActivity

        holder.rcv_comment_comment.text = comment_list[position].comment

        var displayName = ""

        val db = FirebaseFirestore.getInstance()

        db.collection("profile").whereEqualTo("email", comment_list[position].email).addSnapshotListener { snapshots, e ->
            if(e != null){
                Log.e(Constants.TAG, "FireStore Listen profile falied", e)
                return@addSnapshotListener
            }

            if(snapshots != null && snapshots.isEmpty == false){

                for(snapshot in snapshots){
                    val profile_photo = snapshot.data["profile_photo"] as String
                    displayName = snapshot.data["displayName"] as String

                    Glide.with(holder.itemView.context).load(profile_photo).into(holder.rcv_comment_profile_photo)
                    holder.rcv_comment_displayName.text = displayName
                }
            }
        }

        fun goProfile() {
            val intent = Intent(holder.itemView.context, MainActivity::class.java)
            intent.putExtra("user_email", comment_list[position].email)
            intent.putExtra("flag", "comment")
            intent.putExtra("comment", comment_activity.comment_map as Serializable)
            intent.putExtra("user_displayName", comment_activity.user_displayName)
            intent.putExtra("user_profile_photo", comment_activity.user_profile_photo)
            intent.putExtra("timestamp", comment_activity.data_db_timestamp)
            intent.putExtra("contents", comment_activity.contents)
            comment_activity.setResult(Activity.RESULT_OK, intent)
            comment_activity.finish()
        }

        holder.rcv_comment_recomment.setOnClickListener {

            comment_activity.isRecomment = true
            comment_activity.recomment_timestamp = comment_list[position].timestamp
            comment_activity.recomment_email = comment_list[position].email
            comment_activity.recomment_comment = comment_list[position].comment
            comment_activity.recomment_displayName = displayName
            comment_activity.recomment_rcv_list = comment_list[position].recomment_list
            comment_activity.comment_put_comment?.hint = displayName + "님에게 답글 달기"

            comment_activity.comment_put_comment?.isFocusableInTouchMode = true
            comment_activity.comment_put_comment?.focusable = View.FOCUSABLE
            comment_activity.comment_put_comment?.requestFocus()
            val inputmethodmanager : InputMethodManager = comment_activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputmethodmanager.showSoftInput(comment_activity.comment_put_comment, InputMethodManager.SHOW_FORCED)
        }

        holder.rcv_comment_displayName.setOnClickListener {
            goProfile()
        }

        holder.rcv_comment_profile_photo.setOnClickListener {
            goProfile()
        }

        if(comment_list[position].recomment_list.size == 0){
            holder.rcv_comment_recomment_rcv.visibility = View.GONE
        }else{
            holder.rcv_comment_recomment_rcv.visibility = View.VISIBLE
            var rcv_adapter = Recomment_rcv_Adapter(comment_list[position].recomment_list, comment_activity, arrayListOf(comment_list[position].comment, comment_list[position].timestamp, displayName))
            holder.rcv_comment_recomment_rcv.adapter = rcv_adapter
            holder.rcv_comment_recomment_rcv.layoutManager = LinearLayoutManager(holder.itemView.context)
            holder.rcv_comment_recomment_rcv.setHasFixedSize(true)
        }

    }


}