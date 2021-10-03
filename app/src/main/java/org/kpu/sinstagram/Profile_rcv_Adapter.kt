package org.kpu.sinstagram

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import org.kpu.sinstagram.main_fragment.Main_Fragment3
import org.kpu.sinstagram.main_fragment.Main_Fragment4
import org.kpu.sinstagram.main_fragment.Main_Fragment5

class Profile_rcv_Adapter(private var profile_rcv_list : ArrayList<String>, private var user_email : String) : RecyclerView.Adapter<Profile_rcv_Adapter.ViewHolder>(){

    class ViewHolder(view : View) : RecyclerView.ViewHolder(view){
        val rcv_profile_photo : ImageView

        init {
            rcv_profile_photo = view.findViewById(R.id.rcv_profile_photo)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Profile_rcv_Adapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.rcview_profile, parent, false)
        return Profile_rcv_Adapter.ViewHolder(view)
    }

    override fun getItemCount(): Int = profile_rcv_list.size

    override fun onBindViewHolder(holder: Profile_rcv_Adapter.ViewHolder, position: Int) {
        Glide.with(holder.itemView.context).load(profile_rcv_list[position]).into(holder.rcv_profile_photo)
        holder.rcv_profile_photo.setOnClickListener {
            val mainactivity : MainActivity = holder.itemView.context as MainActivity
            val fragment5 = Main_Fragment5()
            val bundle = Bundle()
            bundle.putString("user_email", user_email)
            bundle.putInt("position", position)
            fragment5.arguments = bundle
            mainactivity.replaceFragment(fragment5)
        }

    }
}