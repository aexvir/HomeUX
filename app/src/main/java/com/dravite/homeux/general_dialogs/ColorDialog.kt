package com.dravite.homeux.general_dialogs

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import android.support.v7.graphics.Palette
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView

import com.dravite.homeux.general_adapters.ColorPresetAdapter
import com.dravite.homeux.general_dialogs.helpers.ColorWatcher
import com.dravite.homeux.general_helpers.ColorUtils
import com.dravite.homeux.R

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar

/**
 * A Dialog that shows a color selector with HSL color format sliders.
 */
class ColorDialog @JvmOverloads constructor(private val mContext: Context, private val mTitle: String, private var mColor: Int, private val watcher: ColorWatcher, colorSet: IntArray? = null) {
    private val mDialog: Dialog = Dialog(mContext, R.style.DialogTheme)
    private val mContent: View = View.inflate(mContext, R.layout.empty_editor, null)
    private var mColorSet: MutableList<Int> = ArrayList()

    init {
        if(colorSet != null) {
            mColorSet.addAll(colorSet.toTypedArray())
            mColorSet.sort()
        }

        mDialog.setContentView(mContent)
        val okay = mContent.findViewById<View>(R.id.buttonOk) as Button
        okay.setOnClickListener {
            watcher.onColorSubmitted(mColor)
            mDialog.dismiss()
        }

        val cancel = mContent.findViewById<View>(R.id.buttonCancel) as Button
        cancel.setOnClickListener { mDialog.dismiss() }
        initColorDialog()
    }

    /**
     * Initializes all views from this dialog and what should happen when sliding sliders or selecting a preset.
     */
    private fun initColorDialog() {
        (mDialog.findViewById<View>(R.id.folderName) as TextView).text = mTitle
        (mDialog.findViewById<View>(R.id.folderName) as TextView).setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

        val colorChooser = View.inflate(mContext, R.layout.color_chooser_layout, null)
        colorChooser.tag = "colorLayout"
        colorChooser.alpha = 0f
        (mContent.findViewById<View>(R.id.content) as ViewGroup).addView(colorChooser)

        //Init Seekbars
        val hue = colorChooser.findViewById<View>(R.id.hue) as DiscreteSeekBar
        val saturation = colorChooser.findViewById<View>(R.id.saturation) as DiscreteSeekBar
        val value = colorChooser.findViewById<View>(R.id.value) as DiscreteSeekBar


        val hsl = ColorUtils.colorToHSL(mColor)
        hue.progress = hsl[0].toInt()
        saturation.progress = (1000f * hsl[1]).toInt()
        value.progress = (1000f * hsl[2]).toInt()

        updateColor(colorChooser, mColor)

        val listener = object : DiscreteSeekBar.OnProgressChangeListener {
            override fun onProgressChanged(seekBar: DiscreteSeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val hslInput = FloatArray(3)

                    hslInput[0] = hue.progress.toFloat()
                    hslInput[1] = saturation.progress / 1000f
                    hslInput[2] = value.progress / 1000f

                    val color = ColorUtils.HSLtoColor(hslInput)

                    updateColor(colorChooser, color)
                }
            }

            override fun onStartTrackingTouch(seekBar: DiscreteSeekBar) {}

            override fun onStopTrackingTouch(seekBar: DiscreteSeekBar) {}
        }

        hue.setOnProgressChangeListener(listener)
        saturation.setOnProgressChangeListener(listener)
        value.setOnProgressChangeListener(listener)

        val hexValue = colorChooser.findViewById<View>(R.id.hexValue) as EditText
        val button = colorChooser.findViewById<View>(R.id.submitButton) as ImageButton
        button.imageTintList = ColorStateList.valueOf(ColorUtils.getDarkerColor(mColor))
        button.setOnClickListener {
            if (hexValue.text.toString().matches("^[#][A-F,0-9,a-f]{6}$".toRegex())) {

                val color = Color.parseColor(hexValue.text.toString())

                val hsl = ColorUtils.colorToHSL(color)

                hue.progress = hsl[0].toInt()
                saturation.progress = (1000f * hsl[1]).toInt()
                value.progress = (1000f * hsl[2]).toInt()

                updateColor(colorChooser, color)
            }
        }

        hexValue.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                button.performClick()
                val imm = mContext.getSystemService(
                        Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                return@OnEditorActionListener true
            }
            false
        })


        val presets = colorChooser.findViewById<View>(R.id.presets) as RecyclerView

        presets.adapter = ColorPresetAdapter(mContext, object : ColorPresetAdapter.ColorListener {
            override fun onSelected(color: Int) {

                val oldColor = (colorChooser.findViewById<View>(R.id.colorView).background as ColorDrawable).color

                val oldHSL = ColorUtils.colorToHSL(oldColor)
                val newHSL = ColorUtils.colorToHSL(color)

                val topAnim = ObjectAnimator.ofArgb(mDialog.findViewById(R.id.folder_darker_panel), "backgroundColor", ColorUtils.getDarkerColor(oldColor), ColorUtils.getDarkerColor(color))
                topAnim.interpolator = DecelerateInterpolator()
                topAnim.duration = 400
                topAnim.start()

                val hueAnimator = ObjectAnimator.ofInt(hue, "progress", oldHSL[0].toInt(), newHSL[0].toInt())
                val satAnimator = ObjectAnimator.ofInt(saturation, "progress", (oldHSL[1] * 1000f).toInt(), (newHSL[1] * 1000f).toInt())
                val valAnimator = ObjectAnimator.ofInt(value, "progress", (oldHSL[2] * 1000f).toInt(), (newHSL[2] * 1000f).toInt())
                val colorAnimator = ObjectAnimator.ofArgb(colorChooser.findViewById(R.id.colorView), "backgroundColor",
                        oldColor, color)
                if (ColorUtils.isBrightColor(ColorUtils.getDarkerColor(color))) {
                    (mDialog.findViewById<View>(R.id.folderName) as TextView).setTextColor(-0x76000000)
                } else {
                    (mDialog.findViewById<View>(R.id.folderName) as TextView).setTextColor(Color.WHITE)
                }

                val set = AnimatorSet()
                set.playTogether(hueAnimator, satAnimator, valAnimator)
                set.interpolator = OvershootInterpolator()
                set.duration = 500
                set.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        updateColor(colorChooser, color)
                    }

                    override fun onAnimationEnd(animation: Animator) {}

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}
                })
                set.start()

                colorAnimator.interpolator = DecelerateInterpolator()
                colorAnimator.duration = 400
                colorAnimator.start()

                hexValue.setText(String.format("#%02X%02X%02X", Color.red(color), Color.green(color), Color.blue(color)))
            }
        })

        var ret: String = ""

        for(i in mColorSet) {
            ret += i
        }

        if(mColorSet.isNotEmpty()) {
            val palette = colorChooser.findViewById<View>(R.id.paletteColors) as RecyclerView

            palette.adapter = ColorPresetAdapter(mContext, object : ColorPresetAdapter.ColorListener {
                override fun onSelected(color: Int) {

                    val oldColor = (colorChooser.findViewById<View>(R.id.colorView).background as ColorDrawable).color

                    val oldHSL = ColorUtils.colorToHSL(oldColor)
                    val newHSL = ColorUtils.colorToHSL(color)

                    val topAnim = ObjectAnimator.ofArgb(mDialog.findViewById(R.id.folder_darker_panel), "backgroundColor", ColorUtils.getDarkerColor(oldColor), ColorUtils.getDarkerColor(color))
                    topAnim.interpolator = DecelerateInterpolator()
                    topAnim.duration = 400
                    topAnim.start()

                    val hueAnimator = ObjectAnimator.ofInt(hue, "progress", oldHSL[0].toInt(), newHSL[0].toInt())
                    val satAnimator = ObjectAnimator.ofInt(saturation, "progress", (oldHSL[1] * 1000f).toInt(), (newHSL[1] * 1000f).toInt())
                    val valAnimator = ObjectAnimator.ofInt(value, "progress", (oldHSL[2] * 1000f).toInt(), (newHSL[2] * 1000f).toInt())
                    val colorAnimator = ObjectAnimator.ofArgb(colorChooser.findViewById(R.id.colorView), "backgroundColor", oldColor, color)
                    if (ColorUtils.isBrightColor(ColorUtils.getDarkerColor(color))) {
                        (mDialog.findViewById<View>(R.id.folderName) as TextView).setTextColor(-0x76000000)
                    } else {
                        (mDialog.findViewById<View>(R.id.folderName) as TextView).setTextColor(Color.WHITE)
                    }

                    val set = AnimatorSet()
                    set.playTogether(hueAnimator, satAnimator, valAnimator)
                    set.interpolator = OvershootInterpolator()
                    set.duration = 500
                    set.addListener(object : Animator.AnimatorListener {
                        override fun onAnimationStart(animation: Animator) {
                            updateColor(colorChooser, color)
                        }

                        override fun onAnimationEnd(animation: Animator) {}

                        override fun onAnimationCancel(animation: Animator) {}

                        override fun onAnimationRepeat(animation: Animator) {}
                    })
                    set.start()

                    colorAnimator.interpolator = DecelerateInterpolator()
                    colorAnimator.duration = 400
                    colorAnimator.start()

                    hexValue.setText(String.format("#%02X%02X%02X", Color.red(color), Color.green(color), Color.blue(color)))
                }
            }, mColorSet.toTypedArray())

            colorChooser.findViewById<View>(R.id.paletteColorsLabel).visibility = View.VISIBLE
            palette.visibility = View.VISIBLE
            colorChooser.invalidate()
        }

        colorChooser.post { colorChooser.animate().alpha(1f) }
    }

    /**
     * Animates all sliders and the main color field to the given color values.
     * @param root The dialogs root view.
     * @param color The color to animate to.
     */
    internal fun updateColor(root: View, color: Int) {
        val colorView = root.findViewById<View>(R.id.colorView)
        val hexValue = root.findViewById<View>(R.id.hexValue) as EditText
        mColor = color
        val mElementsTint = ColorUtils.getDarkerColor(color)

        mDialog.findViewById<View>(R.id.folder_darker_panel).setBackgroundColor(mElementsTint)

        // TODO: For god's sake improve this mess

        (mDialog.findViewById<View>(R.id.buttonOk) as TextView).setTextColor(mElementsTint)
        (mDialog.findViewById<View>(R.id.buttonCancel) as TextView).setTextColor(mElementsTint)
        (mDialog.findViewById<View>(R.id.hue) as DiscreteSeekBar).setScrubberColor(mElementsTint)
        (mDialog.findViewById<View>(R.id.saturation) as DiscreteSeekBar).setScrubberColor(mElementsTint)
        (mDialog.findViewById<View>(R.id.value) as DiscreteSeekBar).setScrubberColor(mElementsTint)
        (mDialog.findViewById<View>(R.id.hue) as DiscreteSeekBar).setThumbColor(mElementsTint, mElementsTint)
        (mDialog.findViewById<View>(R.id.saturation) as DiscreteSeekBar).setThumbColor(mElementsTint, mElementsTint)
        (mDialog.findViewById<View>(R.id.value) as DiscreteSeekBar).setThumbColor(mElementsTint, mElementsTint)
        (mDialog.findViewById<View>(R.id.hue) as DiscreteSeekBar).invalidate()
        (mDialog.findViewById<View>(R.id.saturation) as DiscreteSeekBar).invalidate()
        (mDialog.findViewById<View>(R.id.value) as DiscreteSeekBar).invalidate()

        (mDialog.findViewById<View>(R.id.submitButton) as ImageButton).imageTintList = ColorStateList.valueOf(mElementsTint)

        if (ColorUtils.isBrightColor(color))
            (mDialog.findViewById<View>(R.id.folderName) as TextView).setTextColor(-0x76000000)
        else
            (mDialog.findViewById<View>(R.id.folderName) as TextView).setTextColor(Color.WHITE)

        hexValue.setText(String.format("#%02X%02X%02X", Color.red(color), Color.green(color), Color.blue(color)))
        colorView.setBackgroundColor(color)
    }

    /**
     * Displays the Dialog.
     */
    fun show() {
        mDialog.show()
    }
}
