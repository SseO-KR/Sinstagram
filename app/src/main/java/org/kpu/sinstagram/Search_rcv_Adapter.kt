package org.kpu.sinstagram

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.kpu.sinstagram.main_fragment.Main_Fragment3
import org.kpu.sinstagram.main_fragment.Main_Fragment4
import org.kpu.sinstagram.utils.Constants.TAG

class Search_rcv_Adapter(private var search_rcv_list : ArrayList<C_rcv_search>, private var sFlag : Boolean, private var currentuser_search_list : ArrayList<String>): RecyclerView.Adapter<Search_rcv_Adapter.ViewHolder>(){

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val rcv_search_clear : ImageView
        val rcv_search_displayName : TextView
        val rcv_search_profile_photo : ImageView
        val rcv_search_layout : LinearLayout
        init {
            rcv_search_layout = view.findViewById(R.id.rcv_search_layout)
            rcv_search_clear = view.findViewById(R.id.rcv_search_clear)
            rcv_search_displayName = view.findViewById(R.id.rcv_search_displayName)
            rcv_search_profile_photo = view.findViewById(R.id.rcv_search_profile_photo)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Search_rcv_Adapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rcview_search, parent, false)
        return Search_rcv_Adapter.ViewHolder(view)
    }

    override fun getItemCount(): Int = search_rcv_list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentuser_email = FirebaseAuth.getInstance().currentUser?.email
        val mainactivity : MainActivity = holder.itemView.context as MainActivity

        fun goProfile() {
            //mainactivity.flag = "search"
            //mainactivity.user_displayName = search_rcv_list[position].displayName
            if (search_rcv_list[position].email == currentuser_email) {
                //val fragmentTransaction = mainactivity.supportFragmentManager.beginTransaction()
                //fragmentTransaction?.replace(R.id.main_fragment, Main_Fragment3())
                //fragmentTransaction?.commit()
                mainactivity.replaceFragment(Main_Fragment3())

            } else {
                val fragment4 = Main_Fragment4()
                val fragmentTransaction = mainactivity.supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.main_fragment, Main_Fragment4())
                val bundle = Bundle()
                bundle.putString("user_email", search_rcv_list[position].email)
                fragment4.arguments = bundle
                fragmentTransaction.commit()
            }
        }

        fun updateSearchlist(){

            val db = FirebaseFirestore.getInstance()

            if(currentuser_search_list.size != 0) {
                if (currentuser_search_list.contains(search_rcv_list[position].email) == true) {
                    currentuser_search_list.remove(search_rcv_list[position].email)
                    currentuser_search_list.add(search_rcv_list[position].email)

                    val search_data = hashMapOf("search_list" to currentuser_search_list)

                    db.collection("search").document(currentuser_email!!)
                        .update(search_data as HashMap<String, Any>).addOnSuccessListener {
                            Log.d(TAG, "Search_rcv_Adapter - search data update success")
                        }.addOnFailureListener {
                            Log.e(TAG, "Search_rcv_Adapter - search data update fail")
                        }
                } else {
                    currentuser_search_list.add(search_rcv_list[position].email)
                    val search_data = hashMapOf("search_list" to currentuser_search_list)

                    db.collection("search").document(currentuser_email!!)
                        .update(search_data as HashMap<String, Any>).addOnSuccessListener {
                            Log.d(TAG, "Search_rcv_Adapter - search data update success")
                        }.addOnFailureListener {
                            Log.e(TAG, "Search_rcv_Adapter - search data update fail")
                        }
                }
            }else{
                currentuser_search_list.add(search_rcv_list[position].email)
                val search_data = hashMapOf("search_list" to currentuser_search_list)

                db.collection("search").document(currentuser_email!!)
                    .update(search_data as HashMap<String, Any>).addOnSuccessListener {
                        Log.d(TAG, "Search_rcv_Adapter - search data update success")
                    }.addOnFailureListener {
                        Log.e(TAG, "Search_rcv_Adapter - search data update fail")
                    }
            }
        }

        if(sFlag == true) holder.rcv_search_clear.visibility = View.VISIBLE
        else{
            holder.rcv_search_clear.visibility = View.GONE
            holder.rcv_search_clear.isClickable = false
            holder.rcv_search_layout.setOnClickListener {
                goProfile()
                updateSearchlist()

            }
        }
        holder.rcv_search_clear.setOnClickListener {
            val db = FirebaseFirestore.getInstance()

            currentuser_search_list.remove(search_rcv_list[position].email)
            val search_data = hashMapOf("search_list" to currentuser_search_list)
            db.collection("search").document(currentuser_email!!).update(search_data as HashMap<String, Any>).addOnSuccessListener {
                Log.d(TAG, "Search_rcv_Adapter - search data update success")
            }.addOnFailureListener {
                Log.e(TAG, "Search_rcv_Adapter - search data update fail")
            }
        }


        holder.rcv_search_displayName.text = search_rcv_list[position].displayName
        Glide.with(holder.itemView.context).load(search_rcv_list[position].profile_photo).into(holder.rcv_search_profile_photo)


        holder.rcv_search_displayName.setOnClickListener {
            goProfile()
            updateSearchlist()
        }

        holder.rcv_search_profile_photo.setOnClickListener {
            goProfile()
            updateSearchlist()
        }


    }

}