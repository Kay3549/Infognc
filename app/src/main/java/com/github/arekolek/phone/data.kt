package com.github.arekolek.phone

class Data {

    companion object{
        var Ddata: String =""

        fun setdata(data :String) {
            Ddata =data
        }

        fun retundata():String{
            return Ddata
        }
    }
}