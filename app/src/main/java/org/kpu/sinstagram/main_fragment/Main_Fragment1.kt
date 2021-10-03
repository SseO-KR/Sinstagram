package org.kpu.sinstagram.main_fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi

import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import org.kpu.sinstagram.*
import org.kpu.sinstagram.utils.Constants.TAG




class Main_Fragment1() : Fragment() {


    var main_activity : MainActivity? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        main_activity = context as MainActivity
    }
    var mf_home_rcView : RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.mainfragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mf_home_favorite = view.findViewById<ImageView>(R.id.mf_home_favorite)
        val mf_home_swipe = view.findViewById<SwipeRefreshLayout>(R.id.mf_home_swipe)
        mf_home_rcView = view.findViewById<RecyclerView>(R.id.mf_home_rcView)
        val mf_home_upload = view.findViewById<ImageView>(R.id.mf_home_upload)

        var home_rcv_list = arrayListOf<C_rcv_home>()
        val db = FirebaseFirestore.getInstance()

        mf_home_favorite.setOnClickListener {
            val intent = Intent(view.context, HistoryActivity::class.java)
            main_activity?.resultlauncher?.launch(intent)
        }

        db.collection("data").orderBy("timestamp", Query.Direction.DESCENDING).addSnapshotListener { snapshots, e ->
            if(e != null){
                Log.e(TAG, "FireStore Listen falied", e)
                return@addSnapshotListener
            }

            if(snapshots != null && snapshots.isEmpty == false){
                home_rcv_list.clear()
                for(snapshot in snapshots){
                    val email = snapshot.data["email"] as String
                    val photo = snapshot.data["photo"] as String
                    val contents = snapshot.data["contents"] as String
                    val favorite_map = snapshot.data["favorite_map"] as MutableMap<String, String>
                    val favorite_num = favorite_map.size
                    val comment = snapshot.data["comment"] as MutableMap<String, MutableMap<String, ArrayList<String>>>
                    var favorite_boolean : Boolean
                    if(favorite_map.containsValue(email) == true){
                        favorite_boolean = true
                    }else{
                        favorite_boolean = false
                    }
                    //val favorite_boolean = if(favorite_list[displayName] != null) true else false
                    val timestamp = snapshot.data["timestamp"] as String

                    home_rcv_list.add(C_rcv_home(email, photo, contents, favorite_num, favorite_boolean, favorite_map, comment, timestamp))
                }
                var rcv_adapter = Home_rcv_Adapter(home_rcv_list)
                mf_home_rcView?.adapter = rcv_adapter
                mf_home_rcView?.layoutManager = LinearLayoutManager(view.context)
                mf_home_rcView?.setHasFixedSize(true)

                (mf_home_rcView?.layoutManager as LinearLayoutManager).scrollToPosition(current_position)

            }
        }

        mf_home_rcView?.layoutManager = LinearLayoutManager(view.context)



        mf_home_swipe.setOnRefreshListener {
            if (mf_home_swipe.isRefreshing) {
                home_rcv_list.clear()

                //orderby("timestamp")
                db.collection("data").orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snapshots, e ->
                        if (e != null) {
                            Log.e(TAG, "FireStore Listen falied", e)
                            return@addSnapshotListener
                        }

                        if (snapshots != null && snapshots.isEmpty == false) {
                            for(snapshot in snapshots){
                                val email = snapshot.data["email"] as String
                                val photo = snapshot.data["photo"] as String
                                val contents = snapshot.data["contents"] as String
                                val favorite_map = snapshot.data["favorite_map"] as MutableMap<String, String>
                                val favorite_num = favorite_map.size
                                val comment = snapshot.data["comment"] as MutableMap<String, MutableMap<String, ArrayList<String>>>
                                var favorite_boolean : Boolean
                                if(favorite_map.containsValue(email) == true){
                                    favorite_boolean = true
                                }else{
                                    favorite_boolean = false
                                }
                                //val favorite_boolean = if(favorite_list[displayName] != null) true else false
                                val timestamp = snapshot.data["timestamp"] as String

                                home_rcv_list.add(C_rcv_home(email, photo, contents, favorite_num, favorite_boolean, favorite_map, comment, timestamp))
                            }
                            var rcv_adapter = Home_rcv_Adapter(home_rcv_list)
                            mf_home_rcView?.adapter = rcv_adapter
                            mf_home_rcView?.layoutManager = LinearLayoutManager(view.context)
                            mf_home_rcView?.setHasFixedSize(true)

                            mf_home_swipe.isRefreshing = false
                        }


                    }
            }
        }

        mf_home_upload.setOnClickListener {
            val intent = Intent(view.context, UploadActivity::class.java)
            startActivity(intent)
        }


    }

    private var current_position : Int = 0

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Main_Fragment1 - onPause()")
        val layoutmanager = mf_home_rcView?.layoutManager as LinearLayoutManager
        current_position = layoutmanager.findFirstCompletelyVisibleItemPosition()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Main_Fragment1 - onResume()")
        val layoutmanager = mf_home_rcView?.layoutManager as LinearLayoutManager
        layoutmanager.scrollToPosition(current_position)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Main_Fragment1 - onDestroy()")
    }
}