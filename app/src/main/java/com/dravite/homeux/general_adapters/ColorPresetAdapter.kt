package com.dravite.homeux.general_adapters

import android.content.Context
import android.content.res.ColorStateList
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout

import com.dravite.homeux.R

/**
 * An Adapter used in a [com.dravite.homeux.general_dialogs.ColorDialog] to display preset color fields.
 */
class ColorPresetAdapter(internal var mContext: Context, private var mListener: ColorListener) : RecyclerView.Adapter<ColorPresetAdapter.Holder>() {

    // Our default preset colors.
    internal var colors = intArrayOf(-0xbbcca, -0x16e19d, -0x63d850, -0x98c549, -0xc0ae4b, -0xde690d, -0xfc560c, -0xff432c, -0xff6978, -0xb350b0, -0x743cb6, -0x3223c7, -0x14c5, -0x3ef9, -0x6800, -0xa8de, -0x86aab8, -0x616162, -0x9f8275)

    constructor(mContext: Context, mListener: ColorListener, mColors: Array<Int>) : this(mContext, mListener) {
        colors = mColors.toIntArray()
    }

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        internal var t: LinearLayout = view.findViewById<View>(R.id.color_field) as LinearLayout
    }

    /**
     * A listener for listening to a selection in this adapter.
     */
    interface ColorListener {
        fun onSelected(color: Int)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): Holder {
        val view = View.inflate(mContext, R.layout.color_preset_item, null)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, i: Int) {
        holder.t.findViewById<View>(R.id.color_field_drawable).backgroundTintList = ColorStateList.valueOf(colors[i])
        holder.t.setOnClickListener { mListener.onSelected(colors[i]) }
    }

    override fun getItemCount(): Int {
        return colors.size
    }
}
