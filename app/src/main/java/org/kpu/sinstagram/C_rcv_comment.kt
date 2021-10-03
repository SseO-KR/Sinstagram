package org.kpu.sinstagram

import java.io.Serializable

class C_rcv_comment(var email : String
                    ,var comment : String
                    ,var timestamp : String
                    ,var recomment_list : ArrayList<C_rcv_recomment>
) : Serializable{
}