package org.kpu.sinstagram.main_fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import org.kpu.sinstagram.Home_rcv_Adapter
import org.kpu.sinstagram.Profile_rcv_Adapter
import org.kpu.sinstagram.R
import org.kpu.sinstagram.UploadActivity
import org.kpu.sinstagram.utils.Constants
import org.kpu.sinstagram.utils.Constants.TAG
import org.w3c.dom.Text
import java.lang.Exception

class Main_Fragment3 : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mainfragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var pc_bring_image : ImageView? = null


        val mf_profile_rcView = view.findViewById<RecyclerView>(R.id.mf_profile_rcView)
        val mf_profile_menu = view.findViewById<ImageView>(R.id.mf_profile_menu)
        val mf_profile_displayName = view.findViewById<TextView>(R.id.mf_profile_displayName)
        val mf_profile_follower_num = view.findViewById<TextView>(R.id.mf_profile_follower_num)
        val mf_profile_following_num = view.findViewById<TextView>(R.id.mf_profile_following_num)
        val mf_profile_find_follower = view.findViewById<AppCompatImageButton>(R.id.mf_profile_find_follower)
        val mf_profile_introduction = view.findViewById<TextView>(R.id.mf_profile_introduction)
        val mf_profile_post_num = view.findViewById<TextView>(R.id.mf_profile_post_num)
        val mf_profile_profile_edit = view.findViewById<AppCompatButton>(R.id.mf_profile_profile_edit)
        val mf_profile_profile_photo = view.findViewById<ImageView>(R.id.mf_profile_profile_photo)
        val mf_profile_upload = view.findViewById<ImageView>(R.id.mf_profile_upload)

        var user_photo_uri = ""
        val db = FirebaseFirestore.getInstance()
        val currentuser = Firebase.auth.currentUser
        var currentuser_email = currentuser?.email

        val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result : ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK){
                try{
                    val intent = result.data
                    user_photo_uri = intent?.data.toString()

                    Glide.with(view.context).load(user_photo_uri).into(pc_bring_image!!)
                    // 위는 glide 이용
                    //ImageView.setImageResource(uri)
                } catch (e : Exception){
                    Log.e(Constants.TAG, "Bring Image Error", e)
                }

            }
        }


        fun makeDialog(){
            val dialogView = layoutInflater.inflate(R.layout.profile_change_dialog, null)
            val builder = AlertDialog.Builder(view.context)

            val pc_edt_displayName = dialogView.findViewById<EditText>(R.id.pc_dialog_edt_displayName)
            val pc_edt_introduction = dialogView.findViewById<EditText>(R.id.pc_dialog_edt_introduction)
            pc_bring_image = dialogView.findViewById<ImageView>(R.id.pc_dialog_bring_photo)

            pc_bring_image?.setOnClickListener {
                val intent : Intent = Intent()
                intent.setType("image/*")
                intent.setAction(Intent.ACTION_GET_CONTENT)
                startForResult.launch(intent)
            }


            builder.setTitle("Profile Change")
            builder.setView(dialogView).setPositiveButton(R.string.signin){ _, _ ->
                val introduction = pc_edt_introduction?.text.toString()
                val displayName = pc_edt_displayName?.text.toString()

                val profile_data = hashMapOf("displayName" to displayName, "introduction" to introduction, "profile_photo" to user_photo_uri)

                db.collection("profile").document(currentuser_email!!).update(profile_data as Map<String, Any>).addOnSuccessListener {
                    Log.d(TAG, "MainFragment3 - profile update success")
                }.addOnFailureListener {
                    Log.e(TAG, "MainFragment3 - profile update fail")
                }

            }.setNegativeButton("Cancel"){_, _ ->

            }.show()
        }


        mf_profile_upload.setOnClickListener {
            val intent = Intent(view.context, UploadActivity::class.java)
            startActivity(intent)
        }

        mf_profile_profile_edit.setOnClickListener {
            makeDialog()
        }

        db.collection("profile").whereEqualTo("email", currentuser_email).addSnapshotListener { snapshots, e ->
            if(e != null){
                Log.e(Constants.TAG, "FireStore Listen profile falied", e)
                return@addSnapshotListener
            }

            if(snapshots != null && snapshots.isEmpty == false) {

                for (snapshot in snapshots) {
                    val user_displayName = snapshot.data["displayName"] as String
                    val introduction = snapshot.data["introduction"] as String
                    val profile_photo = snapshot.data["profile_photo"] as String

                    mf_profile_displayName.text = user_displayName
                    mf_profile_introduction.text = introduction
                    Glide.with(this).load(profile_photo).into(mf_profile_profile_photo)
                }
            }
        }



        db.collection("follow").whereEqualTo("email", currentuser_email).addSnapshotListener { snapshots, e ->
            if(e != null){
                Log.e(Constants.TAG, "FireStore Listen follow falied", e)
                return@addSnapshotListener
            }

            if(snapshots != null && snapshots.isEmpty == false) {

                for (snapshot in snapshots) {
                    val follower = snapshot.data["follower"] as MutableMap<String, String>
                    mf_profile_follower_num.text = follower.size.toString()

                    val following = snapshot.data["following"] as MutableMap<String, String>
                    mf_profile_following_num.text = following.size.toString()

                }
            }
        }


        val profile_rcv_list = arrayListOf<String>()

        db.collection("data").whereEqualTo("email", currentuser_email)
            .orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snapshots, e ->

                if(e != null){
                    Log.e(Constants.TAG, "FireStore Listen data falied", e)
                    return@addSnapshotListener
                }

                if(snapshots != null && snapshots.isEmpty == false) {
                    profile_rcv_list.clear()

                    for (snapshot in snapshots) {
                        val photo_uri = snapshot.data["photo"] as String

                        profile_rcv_list.add(photo_uri)
                    }

                    mf_profile_post_num.text = profile_rcv_list.size.toString()

                    var rcv_adapter = Profile_rcv_Adapter(profile_rcv_list, currentuser_email!!)
                    mf_profile_rcView.adapter = rcv_adapter
                    mf_profile_rcView.layoutManager = GridLayoutManager(view.context, 3)
                    mf_profile_rcView.setHasFixedSize(true)
                }
        }



    }


}