package org.kpu.sinstagram

import android.net.Uri

class C_rcv_home(var email : String
                ,var photo : String
                ,var contents : String
                ,var favorite_num : Int
                ,var favorite_boolean : Boolean
                ,var favorite_map : MutableMap<String, String>?
                ,var comment : MutableMap<String, MutableMap<String, ArrayList<String>>>?
                ,var timestamp : String) {
}