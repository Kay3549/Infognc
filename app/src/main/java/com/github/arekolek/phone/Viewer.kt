package com.github.arekolek.phone

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView


class Viewer():BaseAdapter(){ //private val items: MutableList<Itemlist>
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
        var convertView = inflater.inflate(R.layout.listview_item,parent,false)


        val item: Itemlist = listViewItemList.get(position)
        var numtextview = convertView.findViewById<TextView>(R.id.num)
        var gogeaktextview = convertView.findViewById<TextView>(R.id.gogaek)
        var counttextview = convertView.findViewById<TextView>(R.id.count)
        var dbtextview = convertView.findViewById<TextView>(R.id.db)

        numtextview.setText(item.getNum())
        gogeaktextview.setText(item.getGogeak())
        counttextview.setText(item.getCount())
        dbtextview.setText(item.getDb())
        /*convertView.num.text = item.num
        convertView.gogaek.text = item.gogeak
        convertView.count.text = item.count
        convertView.db.text = item.db*/

        return convertView
    }

    fun addItem(num: String, gogeak: String, count: String, db: String){
        var item:Itemlist? = Itemlist()
        item?.setNum(num)
        item?.setGogeak(gogeak)
        item?.setCount(count)
        item?.setDb(db)

        if (item != null) {
            listViewItemList.add(item)
        }
    }
}