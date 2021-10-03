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
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.main_fragment.Main_Fragment3
import org.kpu.sinstagram.main_fragment.Main_Fragment4
import org.kpu.sinstagram.utils.Constants
import java.io.Serializable

class Recomment_rcv_Adapter (private var recomment_list : ArrayList<C_rcv_recomment>, private var comment_activity : CommentActivity, private var comment_data : ArrayList<String>) : RecyclerView.Adapter<Recomment_rcv_Adapter.ViewHolder>(){

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){

        val rcv_recomment_comment : TextView
        val rcv_recomment_displayName : TextView
        val rcv_recomment_profile_photo : ImageView
        val rcv_recomment_recomment : TextView
        val rcv_recomment_time : TextView
        val currentuser_email : String
        init {
            currentuser_email = FirebaseAuth.getInstance().currentUser?.email!!
            rcv_recomment_comment = view.findViewById(R.id.rcv_recomment_comment)
            rcv_recomment_displayName = view.findViewById(R.id.rcv_recomment_displayName)
            rcv_recomment_profile_photo = view.findViewById(R.id.rcv_recomment_profile_photo)
            rcv_recomment_recomment = view.findViewById(R.id.rcv_recomment_recomment)
            rcv_recomment_time = view.findViewById(R.id.rcv_recomment_time)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rcview_recomment, parent, false)
        return Recomment_rcv_Adapter.ViewHolder(view)
    }

    override fun getItemCount(): Int = recomment_list.size

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.rcv_recomment_comment.text = recomment_list[position].comment



        val db = FirebaseFirestore.getInstance()

        db.collection("profile").whereEqualTo("email", recomment_list[position].email).addSnapshotListener { snapshots, e ->
            if(e != null){
                Log.e(Constants.TAG, "FireStore Listen profile falied", e)
                return@addSnapshotListener
            }

            if(snapshots != null && snapshots.isEmpty == false){

                for(snapshot in snapshots){
                    val profile_photo = snapshot.data["profile_photo"] as String
                    val displayName = snapshot.data["displayName"] as String

                    Glide.with(holder.itemView.context).load(profile_photo).into(holder.rcv_recomment_profile_photo)
                    holder.rcv_recomment_displayName.text = displayName
                }
            }
        }

        fun goProfile() {
            val intent = Intent(comment_activity, MainActivity::class.java)
            intent.putExtra("user_email", recomment_list[position].email)
            intent.putExtra("flag", "recomment")
            intent.putExtra("user_displayName", comment_activity.user_displayName)
            intent.putExtra("user_profile_photo", comment_activity.user_profile_photo)
            intent.putExtra("timestamp", comment_activity.data_db_timestamp)
            intent.putExtra("contents", comment_activity.contents)
            comment_activity.setResult(Activity.RESULT_OK, intent)
            comment_activity.finish()
        }

        holder.rcv_recomment_recomment.setOnClickListener {
            comment_activity.isRecomment = true
            comment_activity.recomment_comment = comment_data[0]
            comment_activity.recomment_email = recomment_list[position].email
            comment_activity.recomment_rcv_list = recomment_list
            comment_activity.recomment_timestamp = comment_data[1]
            comment_activity.recomment_displayName = comment_data[2]
            comment_activity.comment_put_comment?.hint = comment_data[2] + "님에게 답글 달기"

            comment_activity.comment_put_comment?.isFocusableInTouchMode = true
            comment_activity.comment_put_comment?.focusable = View.FOCUSABLE
            comment_activity.comment_put_comment?.requestFocus()
            val inputmethodmanager : InputMethodManager = comment_activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputmethodmanager.showSoftInput(comment_activity.comment_put_comment, InputMethodManager.SHOW_FORCED)
        }

        holder.rcv_recomment_displayName.setOnClickListener {
            goProfile()
        }

        holder.rcv_recomment_profile_photo.setOnClickListener {
            goProfile()
        }


    }

}