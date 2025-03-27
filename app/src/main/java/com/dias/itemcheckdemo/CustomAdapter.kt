package com.dias.itemcheckdemo

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView

class CustomAdapter(private var dataSet: ArrayList<DataModel>, mContext: Context):
    ArrayAdapter<DataModel>(mContext, R.layout.row_item, dataSet), Filterable{

    private var myDataSet = dataSet
    private class ViewHolder {
        lateinit var imgLed: ImageView
        lateinit var txtType: TextView
        lateinit var txtUii: TextView
        lateinit var txtName: TextView
        lateinit var imgScan: ImageView
    }

    override fun getCount(): Int {
        return dataSet.size
    }

    override fun getItem(position: Int): DataModel {
        return dataSet[position]
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        var convertView = convertView
        val viewHolder: ViewHolder
        val result: View
        if (convertView == null) {
            viewHolder = ViewHolder()
            convertView = LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
            viewHolder.imgLed = convertView.findViewById(R.id.imgLed)
            viewHolder.txtType = convertView.findViewById(R.id.tvType)
            viewHolder.txtUii = convertView.findViewById(R.id.tvUii)
            viewHolder.txtName = convertView.findViewById(R.id.tvName)
            viewHolder.imgScan =convertView.findViewById(R.id.imgScan)
            result = convertView
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
            result = convertView
        }

        val item: DataModel = getItem(position)
        viewHolder.imgLed.visibility = if(item.nTag) View.VISIBLE else View.GONE
        viewHolder.imgLed.setImageResource(if(item.imgOn) R.drawable.led_on else R.drawable.led_off)
        viewHolder.txtUii.text = item.type
        viewHolder.txtUii.text = item.uii
        viewHolder.txtName.text = item.name
        viewHolder.imgScan.setImageResource(if(item.checked) R.drawable.baseline_check_circle_24 else R.drawable.checkbox_selector)
        return result
    }

    override fun getFilter(): Filter {
        return customFilter
    }

    private val customFilter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            dataSet = myDataSet
            var filteredList:ArrayList<DataModel> = ArrayList<DataModel>()
            if (constraint.isNullOrEmpty())
                filteredList = dataSet
            else{
                for (item in dataSet) {
                    if (item.type == constraint.toString()) {
                        filteredList.add(item)
                    }
                }
            }
            val results = FilterResults()
            results.values = filteredList
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            dataSet = results.values as ArrayList<DataModel>
            notifyDataSetChanged()
        }
    }
}