package org.kpu.sinstagram

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.utils.Constants.TAG
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CommentActivity : AppCompatActivity() {

    var comment_map: MutableMap<String, MutableMap<String, ArrayList<String>>>? = null
    var data_db_timestamp: String? = null
    var comment_put_comment: EditText? = null
    var isRecomment: Boolean = false
    var recomment_timestamp: String = ""
    var recomment_email: String = ""
    var recomment_displayName: String = ""
    var recomment_comment: String = ""
    var recomment_rcv_list: ArrayList<C_rcv_recomment>? = null
    var user_displayName = ""
    var user_profile_photo = ""
    var contents = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)

        val comment_contents: TextView = findViewById(R.id.comment_contents)
        val comment_displayName: TextView = findViewById(R.id.comment_displayName)
        val comment_profile_photo: ImageView = findViewById(R.id.comment_profile_photo)
        val comment_rcv: RecyclerView = findViewById(R.id.comment_rcv)
        val comment_back: ImageView = findViewById(R.id.comment_back)
        val comment_put: Button = findViewById(R.id.comment_put)
        comment_put_comment = findViewById(R.id.comment_put_comment)
        val comment_currentuser_profile_photo : ImageView = findViewById(R.id.comment_currentuser_profile_photo)


        val intent = intent




        comment_map = intent.getSerializableExtra("comment") as MutableMap<String, MutableMap<String, ArrayList<String>>>

        val comment_list = arrayListOf<C_rcv_comment>()

        comment_map = comment_map?.toSortedMap()

        for ((key, values) in comment_map!!) {
            val comment_timestamp = key
            val recomment_list = arrayListOf<C_rcv_recomment>()
            var email = ""
            var comment = ""

            val sorted_recomment_map = values.toSortedMap()

            for ((second_key, arraylist) in sorted_recomment_map) {
                if (key == second_key) {
                    email = arraylist[0]
                    comment = arraylist[1]
                } else {
                    recomment_list.add(C_rcv_recomment(arraylist[0], arraylist[1], second_key))
                }
            }
            comment_list.add(C_rcv_comment(email, comment, comment_timestamp, recomment_list))

        }


        user_displayName = intent.getStringExtra("displayName")!!
        user_profile_photo = intent.getStringExtra("profile_photo")!!
        data_db_timestamp = intent.getStringExtra("timestamp")
        contents = intent.getStringExtra("contents")!!

        comment_contents.text = contents


        val currentuser_email: String = FirebaseAuth.getInstance().currentUser?.email!!

        comment_back.setOnClickListener {
            finish()
        }

        comment_displayName.text = user_displayName
        Glide.with(this).load(user_profile_photo).into(comment_profile_photo)
        Glide.with(this).load(user_profile_photo).into(comment_currentuser_profile_photo)


        var rcv_adapter = Comment_rcv_Adapter(comment_list)
        comment_rcv.adapter = rcv_adapter
        comment_rcv.layoutManager = LinearLayoutManager(this)
        comment_rcv.setHasFixedSize(true)

        val db = FirebaseFirestore.getInstance()

        comment_put_comment?.setOnClickListener {
            isRecomment = false
        }

        comment_put.setOnClickListener {
            if (isRecomment == false) {
                val timestamp = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
                val map = mutableMapOf<String, ArrayList<String>>(
                    timestamp to arrayListOf<String>(
                        currentuser_email,
                        comment_put_comment?.text.toString()
                    )
                )
                comment_map?.put(timestamp, map)
                val data = hashMapOf("comment" to comment_map)

                db.collection("data").document(data_db_timestamp!!)
                    .update(data as HashMap<String, Any>).addOnSuccessListener {
                    Log.d(TAG, "CommentActivity - data comment update success")
                        db.collection("data").document(data_db_timestamp!!).addSnapshotListener { snapshot, e ->
                            if(e != null){
                                Log.e(TAG, "CommentActivity - FireStore data Listen fail", e)
                            }

                            if(snapshot != null && snapshot.exists()){
                                var comment = snapshot.data?.get("comment") as MutableMap<String, MutableMap<String, ArrayList<String>>>

                                comment_list.clear()

                                comment = comment.toSortedMap()

                                for ((key, values) in comment) {
                                    val comment_timestamp = key
                                    val recomment_list = arrayListOf<C_rcv_recomment>()
                                    var email = ""
                                    var comment = ""

                                    val sorted_recomment_map = values.toSortedMap()

                                    for ((second_key, arraylist) in sorted_recomment_map) {
                                        if (key == second_key) {
                                            email = arraylist[0]
                                            comment = arraylist[1]
                                        } else {
                                            recomment_list.add(C_rcv_recomment(arraylist[0], arraylist[1], second_key))
                                        }
                                    }
                                    comment_list.add(C_rcv_comment(email, comment, comment_timestamp, recomment_list))

                                }
                            }

                            comment_put_comment?.setText("")

                            rcv_adapter = Comment_rcv_Adapter(comment_list)
                            comment_rcv.adapter = rcv_adapter
                            comment_rcv.layoutManager = LinearLayoutManager(this)
                            comment_rcv.setHasFixedSize(true)

                        }
                }.addOnFailureListener {
                    Log.e(TAG, "CommentActivity - data comment update fail", it)
                }

            } else {
                val timestamp = SimpleDateFormat("yyyyMMddhhmmss").format(Date())
                comment_map?.getValue(recomment_timestamp)?.put(timestamp, arrayListOf(currentuser_email, comment_put_comment?.text.toString()))
                val data = hashMapOf("comment" to comment_map)

                db.collection("data").document(data_db_timestamp!!)
                    .update(data as HashMap<String, Any>).addOnSuccessListener {
                    Log.d(TAG, "CommentActivity - data recomment update success")

                        db.collection("data").document(data_db_timestamp!!).addSnapshotListener { snapshot, e ->
                            if(e != null){
                                Log.e(TAG, "CommentActivity - FireStore data Listen fail", e)
                            }

                            if(snapshot != null && snapshot.exists()){
                                var comment = snapshot.data?.get("comment") as MutableMap<String, MutableMap<String, ArrayList<String>>>

                                comment_list.clear()

                                comment = comment.toSortedMap()

                                for ((key, values) in comment) {
                                    val comment_timestamp = key
                                    val recomment_list = arrayListOf<C_rcv_recomment>()
                                    var email = ""
                                    var comment = ""

                                    val sorted_recomment_map = values.toSortedMap()

                                    for ((second_key, arraylist) in sorted_recomment_map) {
                                        if (key == second_key) {
                                            email = arraylist[0]
                                            comment = arraylist[1]
                                        } else {
                                            recomment_list.add(C_rcv_recomment(arraylist[0], arraylist[1], second_key))
                                        }
                                    }
                                    comment_list.add(C_rcv_comment(email, comment, comment_timestamp, recomment_list))

                                }
                            }

                            comment_put_comment?.setText("")

                            rcv_adapter = Comment_rcv_Adapter(comment_list)
                            comment_rcv.adapter = rcv_adapter
                            comment_rcv.layoutManager = LinearLayoutManager(this)
                            comment_rcv.setHasFixedSize(true)

                        }
                }.addOnFailureListener {
                    Log.e(TAG, "CommentActivity - data recomment update fail", it)
                }


            }


        }


    }
}