package com.mukesh.countrypicker

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter
import java.util.*

class CountryAdapter(context: Context, private var countries: List<Country>) : BaseAdapter(), StickyListHeadersAdapter {
  private var inflater: LayoutInflater = LayoutInflater.from(context)

  override fun getCount(): Int {
    return countries.size
  }

  override fun getItem(position: Int): Any {
    return countries[position]
  }

  override fun getItemId(position: Int): Long {
    return position.toLong()
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
    val holder: ViewHolder
    val view: View
    if (convertView == null) {
      holder = ViewHolder()
      view = inflater.inflate(R.layout.item_normal, parent, false)
      holder.text = view.findViewById(R.id.normal) as TextView
      holder.icon = view.findViewById(R.id.icon) as ImageView
      view.tag = holder
    } else {
      view = convertView
      holder = view.tag as ViewHolder
    }

    val drawableName = "flag_" + countries[position].code.toLowerCase(Locale.ENGLISH)
    val drawableId = getResId(drawableName)
    holder.icon.setImageResource(drawableId)
    holder.text.text = countries[position].name

    return view
  }

  override fun getHeaderView(position: Int, convertView: View?, parent: ViewGroup): View {
    val view: View
    val holder: HeaderViewHolder
    if (convertView == null) {
      holder = HeaderViewHolder()
      view = inflater.inflate(R.layout.item_header, parent, false)
      holder.text = view.findViewById(R.id.header) as TextView
      view.tag = holder
    } else {
      view = convertView
      holder = view.tag as HeaderViewHolder
    }
    val headerText = "" + countries[position].englishName[0]
    holder.text.text = headerText
    return view
  }

  override fun getHeaderId(position: Int): Long {
    return countries[position].englishName[0].toLong()
  }

  private fun getResId(drawableName: String): Int {

    try {
      val res = R.drawable::class.java
      val field = res.getField(drawableName)
      return field.getInt(null)
    } catch (e: Exception) {
      Log.e("CountryCodePicker", "Failure to get drawable id.", e)
    }
    return -1
  }

  internal inner class HeaderViewHolder {
    lateinit var text: TextView
  }

  internal inner class ViewHolder {
    lateinit var text: TextView
    lateinit var icon: ImageView
  }
}