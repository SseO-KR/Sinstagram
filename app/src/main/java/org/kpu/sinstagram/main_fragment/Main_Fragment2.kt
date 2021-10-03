package org.kpu.sinstagram.main_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.C_rcv_search
import org.kpu.sinstagram.Home_rcv_Adapter
import org.kpu.sinstagram.R
import org.kpu.sinstagram.Search_rcv_Adapter
import org.kpu.sinstagram.utils.Constants
import org.kpu.sinstagram.utils.Constants.TAG

class Main_Fragment2 : Fragment() {
    var mf_search_searchview : SearchView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mainfragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mf_search_searchview = view.findViewById(R.id.mf_search_searchview)
        val mf_search_text : TextView = view.findViewById(R.id.mf_search_text)
        val mf_search_rcv : RecyclerView = view.findViewById(R.id.mf_search_rcv)

        val search_rcv_list = arrayListOf<C_rcv_search>()

        val db = FirebaseFirestore.getInstance()
        val currentuser_email = FirebaseAuth.getInstance().currentUser?.email!!

        var search_list = arrayListOf<String>()
        db.collection("search").whereEqualTo("email", currentuser_email).addSnapshotListener { snapshots, e ->
            if(e!= null){
                Log.e(Constants.TAG, "FireStore search Listen falied", e)
                return@addSnapshotListener
            }
            search_list.clear()
            search_rcv_list.clear()
            if (snapshots != null && snapshots.isEmpty == false) {
                for (snapshot in snapshots) {
                    search_list = snapshot.data["search_list"] as ArrayList<String>
                    for(user_email in search_list){
                        db.collection("profile").whereEqualTo("email", user_email).addSnapshotListener { snapshots_profile, e_profile ->
                            if(e!= null){
                                Log.e(Constants.TAG, "FireStore profile Listen falied", e)
                                return@addSnapshotListener
                            }

                            if(snapshots_profile != null && snapshots_profile.isEmpty == false) {
                                for (snapshot_profile in snapshots_profile) {
                                    val displayName = snapshot_profile.data["displayName"] as String
                                    val profile_photo = snapshot_profile.data["profile_photo"] as String

                                    search_rcv_list.add(C_rcv_search(displayName, profile_photo, user_email))
                                }
                            }

                            if(user_email == search_list[search_list.size - 1]){
                                val rcv_adapter = Search_rcv_Adapter(search_rcv_list, true, search_list)
                                mf_search_rcv.adapter = rcv_adapter
                                mf_search_rcv.layoutManager = LinearLayoutManager(view.context)
                                mf_search_rcv.setHasFixedSize(true)
                            }

                        }
                    }
                    val rcv_adapter = Search_rcv_Adapter(search_rcv_list, true, search_list)
                    mf_search_rcv.adapter = rcv_adapter
                    mf_search_rcv.layoutManager = LinearLayoutManager(view.context)
                    mf_search_rcv.setHasFixedSize(true)

                }
            }



        }

        mf_search_searchview?.setOnClickListener {
            mf_search_searchview?.onActionViewExpanded() // searchview whole clickable
        }


        mf_search_searchview?.setOnQueryTextListener(object : SearchView.OnQueryTextListener{

            override fun onQueryTextSubmit(query: String?): Boolean {
                mf_search_text.visibility = View.GONE

                var filterd_list = arrayListOf<C_rcv_search>()

                db.collection("profile").whereArrayContains("displayName", query!!).addSnapshotListener { snapshots, e ->
                    if(e!= null){
                        Log.e(Constants.TAG, "FireStore profile Listen falied", e)
                        return@addSnapshotListener
                    }

                    if (snapshots != null && snapshots.isEmpty == false) {
                        for (snapshot in snapshots) {
                            val displayName = snapshot.data["displayName"] as String
                            val profile_photo = snapshot.data["profile_photo"] as String
                            val email = snapshot.data["email"] as String

                            filterd_list.add(C_rcv_search(displayName, profile_photo, email))
                        }
                    }

                    val rcv_adapter = Search_rcv_Adapter(filterd_list, false, search_list)
                    mf_search_rcv.adapter = rcv_adapter
                    mf_search_rcv.layoutManager = LinearLayoutManager(view.context)
                    mf_search_rcv.setHasFixedSize(true)
                }

                return true
            }

            override fun onQueryTextChange(query: String?): Boolean {
                if(query != ""){

                    mf_search_text.visibility = View.GONE

                    var filterd_list = arrayListOf<C_rcv_search>()

                    db.collection("profile").whereGreaterThanOrEqualTo("displayName", query!!).addSnapshotListener { snapshots, e ->
                        if(e!= null){
                            Log.e(Constants.TAG, "FireStore profile Listen falied", e)
                            return@addSnapshotListener
                        }
                        if (snapshots != null && snapshots.isEmpty == false) {
                            for (snapshot in snapshots) {
                                val displayName = snapshot.data["displayName"] as String
                                val profile_photo = snapshot.data["profile_photo"] as String
                                val email = snapshot.data["email"] as String

                                filterd_list.add(C_rcv_search(displayName, profile_photo, email))
                            }
                        }

                        val rcv_adapter = Search_rcv_Adapter(filterd_list, false, search_list)
                        mf_search_rcv.adapter = rcv_adapter
                        mf_search_rcv.layoutManager = LinearLayoutManager(view.context)
                        mf_search_rcv.setHasFixedSize(true)
                    }
                }

                return true
            }

        })
    }

    override fun onResume() {
        super.onResume()

        Log.d(TAG, "Main_Fragment2 - onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Main_Fragment2 - onPause()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Main_Fragment2 - onDestory()")
    }
}