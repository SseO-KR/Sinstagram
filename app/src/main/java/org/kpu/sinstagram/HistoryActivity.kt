package org.kpu.sinstagram

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import org.kpu.sinstagram.utils.Constants.TAG
import java.io.Serializable

class HistoryActivity : AppCompatActivity() {

    var following : MutableMap<String, String>? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val history_back : ImageView = findViewById(R.id.history_back)
        val history_rcv : RecyclerView = findViewById(R.id.history_rcv)

        history_back.setOnClickListener {
            finish()
        }

        val currentuser_email = FirebaseAuth.getInstance().currentUser?.email
        val db = FirebaseFirestore.getInstance()

        val history_data = mutableMapOf<String, History_collect>()
/*
        db.collection("data").whereEqualTo("email", currentuser_email).get().addOnSuccessListener { snapshots_data ->
            if(snapshots_data != null && snapshots_data.isEmpty == false){
                for(snapshot_data in snapshots_data){
                    val favorite_map = snapshot_data["favorite_map"] as MutableMap<String, String>
                    val photo = snapshot_data.data["photo"] as String

                    favorite_map.forEach() { (timestamp, email) ->
                        db.collection("profile").whereEqualTo("email", email).get().addOnSuccessListener { snapshots_profile ->
                            if(snapshots_profile != null && snapshots_profile.isEmpty == false) {
                                for (snapshot_profile in snapshots_profile) {
                                    val displayName = snapshot_profile.data["displayName"] as String
                                    val profile_photo = snapshot_profile.data["profile_photo"] as String

                                    history_data.put(timestamp, History_collect(displayName, profile_photo, email, "favorite", photo)
                                    )
                                }

                            }

                            val comment = snapshot_data.data["comment"] as MutableMap<String, MutableMap<String, ArrayList<String>>>

                            comment.forEach(){ (timestamp_comment, map) ->
                                map.forEach(){ (timestamp_recomment, arraylist) ->

                                    if(timestamp_comment == timestamp_recomment){
                                        db.collection("profile").whereEqualTo("email", arraylist[0]).get().addOnSuccessListener { snapshots_profile2 ->

                                            if(snapshots_profile2 != null && snapshots_profile2.isEmpty == false) {
                                                for (snapshot_profile2 in snapshots_profile2) {
                                                    val displayName = snapshot_profile2.data["displayName"] as String
                                                    val profile_photo = snapshot_profile2.data["profile_photo"] as String

                                                    history_data.put(timestamp_comment, History_collect(displayName, profile_photo, arraylist[0], "comment", photo))
                                                }
                                            }

                                            db.collection("follow").whereEqualTo("email", currentuser_email).get().addOnSuccessListener { snapshots_follow ->
                                                if (snapshots_follow != null && snapshots_follow.isEmpty == false) {
                                                    for (snapshot_follow in snapshots_follow) {
                                                        following =
                                                            snapshot_follow.data["following"] as MutableMap<String, String>
                                                        val follower =
                                                            snapshot_follow.data["follower"] as MutableMap<String, String>
                                                        follower.forEach() { (timestamp_follower, email_follower) ->
                                                            db.collection("profile")
                                                                .whereEqualTo("email", email_follower).get()
                                                                .addOnSuccessListener { snapshots_follow_profile ->

                                                                    if (snapshots_follow_profile != null && snapshots_follow_profile.isEmpty == false) {
                                                                        for (snapshot_follow_profile in snapshots_follow_profile) {
                                                                            val displayName =
                                                                                snapshot_follow_profile.data["displayName"] as String
                                                                            val profile_photo =
                                                                                snapshot_follow_profile.data["profile_photo"] as String

                                                                            history_data.put(
                                                                                timestamp_follower,
                                                                                History_collect(
                                                                                    displayName,
                                                                                    profile_photo,
                                                                                    email_follower,
                                                                                    "follow"
                                                                                )
                                                                            )

                                                                        }

                                                                        val history_rcv_list = arrayListOf<C_rcv_history>()
                                                                        val sorted_history_data = history_data.toSortedMap(Comparator.reverseOrder())

                                                                        history_rcv_list.clear()
                                                                        for((timestamp_history, history_collect) in sorted_history_data){
                                                                            history_rcv_list.add(C_rcv_history(timestamp_history, history_collect.displayName, history_collect.profile_photo, history_collect.email, history_collect.type, history_collect.photo))
                                                                        }

                                                                        val rcv_adapter = History_rcv_Adapter(history_rcv_list)
                                                                        history_rcv.adapter = rcv_adapter
                                                                        history_rcv.layoutManager = LinearLayoutManager(this@HistoryActivity)
                                                                        history_rcv.setHasFixedSize(true)
                                                                    }
                                                                }
                                                        }
                                                    }
                                                }
                                            }


                                        }
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
*/

        db.collection("data").whereEqualTo("email", currentuser_email).addSnapshotListener { snapshots, e ->
            if(e != null){
                Log.e(TAG, "HistoryActivity - FireStore data Listen fail", e)
                return@addSnapshotListener
            }

            if(snapshots != null && snapshots.isEmpty == false){
                for(snapshot in snapshots){
                    val favorite_map = snapshot.data["favorite_map"] as MutableMap<String, String>
                    val photo = snapshot.data["photo"] as String
                    favorite_map.forEach(){ (timestamp, email) ->
                        db.collection("profile").whereEqualTo("email", email).addSnapshotListener { snapshots_profile, e_profile ->
                            if(e_profile != null){
                                Log.e(TAG, "HistoryActivity - FireStore profile Listen fail", e_profile)
                                return@addSnapshotListener
                            }

                            if(snapshots_profile != null && snapshots_profile.isEmpty == false){
                                for(snapshot_profile in snapshots_profile){
                                    val displayName = snapshot_profile.data["displayName"] as String
                                    val profile_photo = snapshot_profile.data["profile_photo"] as String

                                    history_data.put(timestamp, History_collect(displayName, profile_photo, email, "favorite", photo))
                                }

                                val history_rcv_list = arrayListOf<C_rcv_history>()
                                val sorted_history_data = history_data.toSortedMap(Comparator.reverseOrder())

                                history_rcv_list.clear()
                                for((timestamp, history_collect) in sorted_history_data){
                                    history_rcv_list.add(C_rcv_history(timestamp, history_collect.displayName, history_collect.profile_photo, history_collect.email, history_collect.type, history_collect.photo))
                                }

                                val rcv_adapter = History_rcv_Adapter(history_rcv_list)
                                history_rcv.adapter = rcv_adapter
                                history_rcv.layoutManager = LinearLayoutManager(this@HistoryActivity)
                                history_rcv.setHasFixedSize(true)
                            }
                        }
                    }


                    val comment = snapshot.data["comment"] as MutableMap<String, MutableMap<String, ArrayList<String>>>

                    comment.forEach(){ (timestamp, map) ->
                        map.forEach(){ (timestamp_recomment, arraylist) ->

                            if(timestamp == timestamp_recomment){
                                db.collection("profile").whereEqualTo("email", arraylist[0]).addSnapshotListener { snapshots_profile, e_profile ->
                                    if(e_profile != null){
                                        Log.e(TAG, "HistoryActivity - FireStore profile Listen fail", e_profile)
                                        return@addSnapshotListener
                                    }

                                    if(snapshots_profile != null && snapshots_profile.isEmpty == false){
                                        for(snapshot_profile in snapshots_profile){
                                            val displayName = snapshot_profile.data["displayName"] as String
                                            val profile_photo = snapshot_profile.data["profile_photo"] as String

                                            history_data.put(timestamp, History_collect(displayName, profile_photo, arraylist[0],"comment", photo))
                                        }

                                        val history_rcv_list = arrayListOf<C_rcv_history>()
                                        val sorted_history_data = history_data.toSortedMap(Comparator.reverseOrder())

                                        history_rcv_list.clear()
                                        for((timestamp, history_collect) in sorted_history_data){
                                            history_rcv_list.add(C_rcv_history(timestamp, history_collect.displayName, history_collect.profile_photo, history_collect.email, history_collect.type, history_collect.photo))
                                        }

                                        val rcv_adapter = History_rcv_Adapter(history_rcv_list)
                                        history_rcv.adapter = rcv_adapter
                                        history_rcv.layoutManager = LinearLayoutManager(this@HistoryActivity)
                                        history_rcv.setHasFixedSize(true)

                                    }
                                }

                            }

                        }
                    }


                }
            }
        }




        db.collection("follow").whereEqualTo("email", currentuser_email).addSnapshotListener { snapshots, e ->
            if(e != null){
                Log.e(TAG, "HistoryActivity - FireStore follow Listen fail", e)
                return@addSnapshotListener
            }

            if(snapshots != null && snapshots.isEmpty == false) {
                for (snapshot in snapshots) {
                    val follower = snapshot.data["follower"] as MutableMap<String, String>
                    follower.forEach(){(timestamp, email) ->
                        db.collection("profile").whereEqualTo("email", email).addSnapshotListener { snapshots_profile, e_profile ->
                            if(e_profile != null){
                                Log.e(TAG, "HistoryActivity - FireStore follow Listen fail", e)
                                return@addSnapshotListener
                            }

                            if(snapshots_profile != null && snapshots_profile.isEmpty == false) {
                                for (snapshot_profile in snapshots_profile) {
                                    val displayName = snapshot_profile.data["displayName"] as String
                                    val profile_photo = snapshot_profile.data["profile_photo"] as String

                                    history_data.put(timestamp, History_collect(displayName, profile_photo, email,"follow"))

                                }

                                val history_rcv_list = arrayListOf<C_rcv_history>()
                                val sorted_history_data = history_data.toSortedMap(Comparator.reverseOrder())

                                history_rcv_list.clear()
                                for((timestamp, history_collect) in sorted_history_data){
                                    history_rcv_list.add(C_rcv_history(timestamp, history_collect.displayName, history_collect.profile_photo, history_collect.email, history_collect.type, history_collect.photo))
                                }

                                val rcv_adapter = History_rcv_Adapter(history_rcv_list)
                                history_rcv.adapter = rcv_adapter
                                history_rcv.layoutManager = LinearLayoutManager(this@HistoryActivity)
                                history_rcv.setHasFixedSize(true)
                            }
                        }
                    }

                    following = snapshot.data["following"] as MutableMap<String, String>



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
                val post_user_email = intent.getStringExtra("post_user_email")
                val fav_rcv_list : ArrayList<C_rcv_favorite> = intent.getSerializableExtra("favorite_rcv_list") as ArrayList<C_rcv_favorite>

                val ptf_intent = Intent(this, MainActivity::class.java)
                ptf_intent.putExtra("user_email", user_email)
                ptf_intent.putExtra("flag", flag)
                ptf_intent.putExtra("photo", photo)
                ptf_intent.putExtra("post_user_email", post_user_email)
                ptf_intent.putExtra("favorite_rcv_list", fav_rcv_list as Serializable)
                setResult(Activity.RESULT_OK, ptf_intent)
                finish()
            }
        }
    }
}