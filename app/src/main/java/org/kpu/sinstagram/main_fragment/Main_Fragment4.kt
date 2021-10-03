package org.kpu.sinstagram.main_fragment

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.kpu.sinstagram.MainActivity
import org.kpu.sinstagram.Profile_rcv_Adapter
import org.kpu.sinstagram.R
import org.kpu.sinstagram.utils.Constants
import java.text.SimpleDateFormat
import java.util.*

class Main_Fragment4() : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mainfragment_user_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userprofile_menu = view.findViewById<ImageView>(R.id.mf_userprofile_menu)
        val userprofile_introduction = view.findViewById<TextView>(R.id.mf_userprofile_introduction)
        val userprofile_displayName = view.findViewById<TextView>(R.id.mf_userprofile_displayName)
        val userprofile_follower_num = view.findViewById<TextView>(R.id.mf_userprofile_follower_num)
        val userprofile_message = view.findViewById<AppCompatButton>(R.id.mf_userprofile_message)
        val userprofile_following_num = view.findViewById<TextView>(R.id.mf_userprofile_following_num)
        val userprofile_post_num = view.findViewById<TextView>(R.id.mf_userprofile_post_num)
        val userprofile_follow = view.findViewById<AppCompatButton>(R.id.mf_userprofile_follow)
        val userprofile_profile_photo = view.findViewById<ImageView>(R.id.mf_userprofile_profile_photo)
        val userprofile_rcView = view.findViewById<RecyclerView>(R.id.mf_userprofile_rcView)
        val userprofile_back = view.findViewById<ImageView>(R.id.mf_userprofile_back)

        val user_email = arguments?.getString("user_email")


        val db = FirebaseFirestore.getInstance()
        val currentuser = FirebaseAuth.getInstance().currentUser
        val currentuser_email = currentuser?.email

        var fFlag = false
        var user_follower_map: MutableMap<String, String> = mutableMapOf()
        var user_following_map: MutableMap<String, String> = mutableMapOf()
        val profile_rcv_list = arrayListOf<String>()
        val following_map : MutableMap<String, String> = mutableMapOf()
        val follower_map : MutableMap<String, String> = mutableMapOf()

        userprofile_back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        db.collection("profile").whereEqualTo("email", user_email)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e(Constants.TAG, "FireStore profile Listen falied", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && snapshots.isEmpty == false) {

                    for (snapshot in snapshots) {
                        val displayName = snapshot.data["displayName"] as String
                        userprofile_displayName.text = displayName

                        val profile_photo = snapshot.data["profile_photo"] as String
                        Glide.with(this).load(profile_photo).into(userprofile_profile_photo)

                        val introduction = snapshot.data["introduction"] as String
                        userprofile_introduction.text = introduction
                    }

                }

            }

        db.collection("data").whereEqualTo("email", user_email)
            .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snapshots, e ->

                if (e != null) {
                    Log.e(Constants.TAG, "FireStore data Listen falied", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && snapshots.isEmpty == false) {
                    profile_rcv_list.clear()

                    for (snapshot in snapshots) {
                        val photo_uri = snapshot.data["photo"] as String

                        profile_rcv_list.add(photo_uri)
                    }



                    userprofile_post_num.text = profile_rcv_list.size.toString()

                    var rcv_adapter = Profile_rcv_Adapter(profile_rcv_list, user_email!!)
                    userprofile_rcView.adapter = rcv_adapter
                    userprofile_rcView.layoutManager = GridLayoutManager(view.context, 3)
                    userprofile_rcView.setHasFixedSize(true)
                }
            }

        db.collection("follow").whereEqualTo("email", user_email)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.e(Constants.TAG, "FireStore user follow Listen falied", e)
                    return@addSnapshotListener
                }

                if (snapshots != null && snapshots.isEmpty == false) {
                    user_follower_map.clear()
                    user_following_map.clear()

                    for (snapshot in snapshots) {
                        val following = snapshot.data["following"] as MutableMap<String, String>
                        val followers = snapshot.data["follower"] as MutableMap<String, String>
                        following.forEach { (timestamp, email) ->
                            user_following_map.put(timestamp, email)

                        }
                        followers.forEach { (timestamp, email) ->
                            user_follower_map.put(timestamp, email)
                        }
                    }

                    userprofile_following_num.text = user_following_map.size.toString()
                    userprofile_follower_num.text = user_follower_map.size.toString()
                }
            }



        db.collection("follow").whereEqualTo("email", currentuser_email)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e(Constants.TAG, "FireStore currentuser follow Listen failed", error)
                    return@addSnapshotListener
                }

                if (snapshots != null && snapshots.isEmpty == false) {
                    for (snapshot in snapshots) {
                        following_map.clear()
                        follower_map.clear()

                        val following = snapshot.data["following"] as MutableMap<String, String>
                        val followers = snapshot.data["follower"] as MutableMap<String, String>
                        for((timestamp, email) in following){
                            following_map.put(timestamp, email)
                        }
                        for((timestamp, email) in followers) {
                            follower_map.put(timestamp, email)
                        }
                    }
                }

            }


        userprofile_follow.setOnClickListener {
            if (fFlag == true) {
                for((timestamp, email) in user_follower_map) {
                    if (email == currentuser_email) user_follower_map.remove(timestamp)
                }
                //val user_follower_array : Array<String> = user_follower_list.toArray(arrayOf<String>())
                val user_follow_data = hashMapOf(
                    "follower" to user_follower_map
                )
                db.collection("follow").document(user_email!!)
                    .update(user_follow_data as Map<String, Any>).addOnSuccessListener {
                        userprofile_follow.setBackgroundResource(R.drawable.follow_button_resource)
                        Log.d(Constants.TAG, "ProfileActivity - User Follower Update Success")
                    }.addOnFailureListener {
                        Log.e(Constants.TAG, "ProfileActivity - User Follower Update Fail", it)
                    }

                for((timestamp, email) in following_map){
                    if (email == user_email) following_map.remove(timestamp)
                }
                val follow_data = hashMapOf(
                    "following" to following_map
                )
                db.collection("follow").document(currentuser_email!!)
                    .update(follow_data as Map<String, Any>).addOnSuccessListener {
                        Log.d(Constants.TAG, "ProfileActivity - CurrentUser Following Update Success")
                    }.addOnFailureListener {
                        Log.e(Constants.TAG, "ProfileActivity - CurrentUser Following Update Fail", it)
                    }
                fFlag = false

            } else {
                val timestamp = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
                user_follower_map.put(timestamp, currentuser_email!!)
                val user_follow_data = hashMapOf(
                    "follower" to user_follower_map
                )
                db.collection("follow").document(user_email!!)
                    .update(user_follow_data as Map<String, Any>).addOnSuccessListener {
                        userprofile_follow.setBackgroundResource(R.drawable.profile_button_resource)
                        Log.d(Constants.TAG, "ProfileActivity - Follower User Update Success")
                    }.addOnFailureListener {
                        Log.e(Constants.TAG, "ProfileActivity - Follower User Update Fail", it)
                    }

                following_map.put(timestamp, user_email)
                val follow_data = hashMapOf(
                    "following" to following_map
                )
                db.collection("follow").document(currentuser_email)
                    .update(follow_data as Map<String, Any>).addOnSuccessListener {
                        Log.d(Constants.TAG, "ProfileActivity - Following Update Success")
                    }.addOnFailureListener {
                        Log.e(Constants.TAG, "ProfileActivity - Following Update Fail", it)
                    }
                fFlag = true
            }
        }
    }
}