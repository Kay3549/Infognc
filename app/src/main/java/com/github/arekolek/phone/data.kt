package com.github.arekolek.phone

class Data {

    companion object{
        var Ddata: String =""
        var callStartTime : Long =0
        var callEndTime : Long =0
        var ringtime : Long =0
        var phonenumber =""

        fun setdata(data :String) {
            Ddata =data
        }

        fun retundata():String{
            return Ddata
        }
    }
}