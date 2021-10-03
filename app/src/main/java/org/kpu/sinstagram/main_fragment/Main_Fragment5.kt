package org.kpu.sinstagram.main_fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.kpu.sinstagram.C_rcv_home
import org.kpu.sinstagram.Home_rcv_Adapter
import org.kpu.sinstagram.R
import org.kpu.sinstagram.utils.Constants.TAG

class Main_Fragment5 : Fragment() {
    var mf_post_rcv: RecyclerView? = null
    private var current_position : Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.mainfragment_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mf_post_back: ImageView = view.findViewById(R.id.mf_post_back)
        mf_post_rcv = view.findViewById(R.id.mf_post_rcv)

        val user_email = arguments?.getString("user_email")
        current_position = arguments?.getInt("position")!!
        val db = FirebaseFirestore.getInstance()
        val currentuser_email = FirebaseAuth.getInstance().currentUser?.email

        mf_post_back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        val data_rcv_list = arrayListOf<C_rcv_home>()

        db.collection("data").whereEqualTo("email", user_email)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots_data, e_data ->
                if (e_data != null) {
                    Log.e(TAG, "Main_Fragment5 - FireStore data listen fail", e_data)
                    return@addSnapshotListener
                }

                if (snapshots_data != null && snapshots_data.isEmpty == false) {
                    for (snapshot_data in snapshots_data) {
                        val contents = snapshot_data.data["contents"] as String
                        val photo = snapshot_data.data["photo"] as String
                        val timestamp = snapshot_data.data["timestamp"] as String
                        val favorite_map =
                            snapshot_data.data["favorite_map"] as MutableMap<String, String>
                        val favorite_num = favorite_map.size
                        var favorite_boolean: Boolean
                        if (favorite_map.containsValue(currentuser_email) == true) {
                            favorite_boolean = true
                        } else {
                            favorite_boolean = false
                        }
                        val comment =
                            snapshot_data.data["comment"] as MutableMap<String, MutableMap<String, ArrayList<String>>>

                        data_rcv_list.add(
                            C_rcv_home(
                                user_email!!,
                                photo,
                                contents,
                                favorite_num,
                                favorite_boolean,
                                favorite_map,
                                comment,
                                timestamp
                            )
                        )
                    }

                    val adapter = Home_rcv_Adapter(data_rcv_list)
                    mf_post_rcv?.adapter = adapter
                    mf_post_rcv?.layoutManager = LinearLayoutManager(view.context)
                    mf_post_rcv?.setHasFixedSize(true)

                    (mf_post_rcv?.layoutManager as LinearLayoutManager).scrollToPosition(current_position)
                }
            }



        mf_post_rcv?.layoutManager = LinearLayoutManager(view.context)

    }


    override fun onPause() {
        super.onPause()
        Log.d(TAG, "Main_Fragment5 - onPause()")
        val layoutmanager = mf_post_rcv?.layoutManager as LinearLayoutManager
        current_position = layoutmanager.findFirstCompletelyVisibleItemPosition()
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "Main_Fragment5 - onResume()")
        val layoutmanager = mf_post_rcv?.layoutManager as LinearLayoutManager
        layoutmanager.scrollToPosition(current_position)
    }

}