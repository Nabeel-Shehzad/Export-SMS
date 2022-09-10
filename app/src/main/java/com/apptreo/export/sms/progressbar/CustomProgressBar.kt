package com.apptreo.export.sms.progressbar

import android.app.Dialog
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.cardview.widget.CardView
import androidx.core.content.res.ResourcesCompat
import com.apptreo.export.sms.R

class CustomProgressBar {
    lateinit var dialog: Dialog

    fun show(context: Context): Dialog {
        return show(context, null)
    }

    fun show(context: Context, title:CharSequence?): Dialog {
        val inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflator.inflate(R.layout.progress_bar, null)
        if (title != null) {
            view.findViewById<TextView>(R.id.cp_title).text = title
        }
        view.findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.cp_bg_view).setBackgroundColor(
            Color.parseColor("#60000000")) //Background Color
        view.findViewById<CardView>(R.id.cp_cardview).setCardBackgroundColor(Color.parseColor("#70000000")) //Box Color
        setColorFilter(view.findViewById<ProgressBar>(R.id.cp_pbar).indeterminateDrawable,
            ResourcesCompat.getColor(context.resources, R.color.purple_500, null)) //Progress Bar Color
        view.findViewById<TextView>(R.id.cp_title).setTextColor(Color.WHITE) //Text Color

        dialog = Dialog(context, R.style.CustomProgressBarTheme)
        dialog.setContentView(view)
        dialog.show()

        return dialog
    }

    private fun setColorFilter(@NonNull drawable: Drawable, color:Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            drawable.colorFilter = BlendModeColorFilter(color, BlendMode.SRC_ATOP)
        } else {
            @Suppress("DEPRECATION")
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
        }
    }

}