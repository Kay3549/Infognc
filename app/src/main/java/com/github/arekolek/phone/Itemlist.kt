package com.github.arekolek.phone

/*
data class Itemlist(val num:String, val gogeak:String, val count:String, val db:String)*/
class Itemlist() {
    var numItem = ""
    var gogeakItem = ""
    var countItem = ""
    var dbItem = ""
    var startcallItem = ""

    fun getNum(): String? {
        return numItem
    }
    fun setNum(num:String){
        this.numItem = num
    }

    fun getGogeak():String?{
        return gogeakItem
    }
    fun setGogeak(gogeak:String){
        this.gogeakItem = gogeak
    }

    fun getCount():String?{
        return countItem
    }
    fun setCount(count:String){
        this.countItem = count
    }

    fun getDb():String?{
        return dbItem
    }
    fun setDb(db:String){
        this.dbItem = db
    }

    fun getStartcall():String?{
        return startcallItem
    }
    fun setStartcall(startcall:String){
        this.startcallItem = startcall
    }
}