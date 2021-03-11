package com.github.arekolek.phone

class Data {

    companion object{
        var Ddata: String =""
        var callStartTime : Long =0
        var callEndTime : Long =0
        var ringtime : Long =0
        var phonenumber =""
        var duration : Long =0
        var recYN = ""

        var agentNum:String = ""
        var agentID:String = ""
        var part:String = ""
        var level:String = ""
        var agentName:String = ""

        var agentPhone:String = ""

        fun setdata(data :String) {
            Ddata =data
        }
        fun retundata():String{
            return Ddata
        }

        fun setagentNum(data :String) {
            agentNum = data
        }
        fun retunagentNum():String{
            return agentNum
        }

        fun setagentID(data :String) {
            agentID =data
        }
        fun retunagentID():String{
            return agentID
        }

        fun setpart(data :String) {
            part =data
        }
        fun retunpart():String{
            return part
        }

        fun setlevel(data :String) {
            level =data
        }
        fun retunlevel():String{
            return level
        }

        fun setagentName(data :String) {
            agentName =data
        }
        fun retunagentName():String{
            return agentName
        }

        fun setagentPhone(data:String){
            agentPhone = data
        }
        fun returnagentPhone():String{
            return agentPhone
        }

        fun reset(){
            Ddata=""
            callStartTime=0
            callEndTime=0
            ringtime =0
            phonenumber =""
            duration=0
            recYN = ""
        }

    }
}