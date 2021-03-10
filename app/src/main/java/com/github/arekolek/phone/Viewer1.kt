package com.github.arekolek.phone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class Viewer1():BaseAdapter(){ //private val items: MutableList<Itemlist>
    var listViewItemList = ArrayList<Itemlist>()

    override fun getCount(): Int {
        return listViewItemList.size
    }

    override fun getItem(position:Int): Itemlist {
        return listViewItemList.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, view: View?, parent:ViewGroup?):View{
        var mcontext: Context? = parent?.context

        var inflater = mcontext?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var convertView = inflater.inflate(R.layout.listview_item1,parent,false)


        val item: Itemlist = listViewItemList.get(position)
        var numtextview = convertView.findViewById<TextView>(R.id.num)
        var startcalltextview = convertView.findViewById<TextView>(R.id.startcall)
        var gogeaktextview = convertView.findViewById<TextView>(R.id.gogaek)
        var counttextview = convertView.findViewById<TextView>(R.id.count)


        numtextview.setText(item.getNum())
        startcalltextview.setText(item.getStartcall())
        gogeaktextview.setText(item.getGogeak())
        counttextview.setText(item.getCount())

        return convertView
    }

    fun addItem(num: String, startcall: String, gogeak: String, count: String){
        var item:Itemlist? = Itemlist()
        if(num.length==1) {
            item?.setNum("0" + num)
        }
        else{
            item?.setNum(num)
        }
        item?.setStartcall(startcall)
        item?.setGogeak(gogeak)
        item?.setCount(count)

        if (item != null) {
            listViewItemList.add(item)
        }
    }
}