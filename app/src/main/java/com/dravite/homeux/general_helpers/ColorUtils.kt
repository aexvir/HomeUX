package com.dravite.homeux.general_helpers

import android.graphics.Color
import android.support.annotation.Size

/**
 * A helper class to modify colors.
 */
object ColorUtils {

    /**
     * Converts a given HSL formatted array to a color int.
     * @param hsl The 3-part HSL array
     * @return The resulting color int.
     */
    @JvmStatic
    fun HSLtoColor(@Size(3) hsl: FloatArray): Int {
        if (hsl.size != 3)
            throw RuntimeException("HSL needs to have 3 components.")

        if (hsl[1] == 0f) {
            val component = (hsl[2] * 255).toInt()
            return Color.rgb(component, component, component)
        }

        val temp_1: Float

        if (hsl[2] < 0.5f) {
            temp_1 = hsl[2] * (1f + hsl[1])
        } else {
            temp_1 = hsl[2] + hsl[1] - hsl[2] * hsl[1]
        }

        val temp_2 = 2 * hsl[2] - temp_1
        val hue = hsl[0] / 360f

        var tempR = hue + 0.3333f
        var tempG = hue
        var tempB = hue - 0.3333f

        if (tempR > 1) {
            tempR -= 1f
        } else if (tempR < 0) {
            tempR += 1f
        }
        if (tempG > 1) {
            tempG -= 1f
        } else if (tempG < 0) {
            tempG += 1f
        }
        if (tempB > 1) {
            tempB -= 1f
        } else if (tempB < 0) {
            tempB += 1f
        }

        val red = testColor(tempR, temp_1, temp_2)
        val green = testColor(tempG, temp_1, temp_2)
        val blue = testColor(tempB, temp_1, temp_2)

        return Color.rgb(getBitColor(red), getBitColor(green), getBitColor(blue))
    }

    private fun getBitColor(value: Float): Int {
        return Math.round(value * 255f)
    }

    private fun testColor(temporaryColor: Float, t1: Float, t2: Float): Float {
        return if (6 * temporaryColor < 1) {
            t2 + (t1 - t2) * 6f * temporaryColor
        } else if (2 * temporaryColor < 1) {
            t1
        } else if (3 * temporaryColor < 2) {
            t2 + (t1 - t2) * (0.6666f - temporaryColor) * 6f
        } else {
            t2
        }
    }

    /**
     * Converts a color int into a 3-part HSL float array.
     * @param color The color to convert
     * @return A 3-part HSL float array from the color.
     */
    @JvmStatic
    fun colorToHSL(color: Int): FloatArray {
        val result = FloatArray(3)

        val red = Color.red(color) / 255f
        val green = Color.green(color) / 255f
        val blue = Color.blue(color) / 255f

        val min = Math.min(red, Math.min(green, blue))
        val max = Math.max(red, Math.max(green, blue))

        result[2] = (min + max) / 2f

        if (result[2] <= 0.5f) {
            result[1] = (max - min) / (max + min)
        } else {
            result[1] = (max - min) / (2f - max - min)
        }

        if (max == min) {
            result[0] = 0f
        } else if (max == red) {
            result[0] = (green - blue) / (max - min)
        } else if (max == green) {
            result[0] = 2.0f + (blue - red) / (max - min)
        } else if (max == blue) {
            result[0] = 4.0f + (red - green) / (max - min)
        }

        result[0] *= 60f

        if (result[0] < 0) {
            result[0] += 360f
        }

        return result
    }

    /**
     * Darkens the given color by 0.8x.
     * @param color The source color.
     * @return A darker tone of the source color.
     */
    @JvmStatic
    fun getDarkerColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.RGBToHSV(Color.red(color),
                Color.green(color),
                Color.blue(color),
                hsv)
        hsv[2] *= 0.8f
        return Color.HSVToColor(hsv)
    }

    /**
     * Checks if a given color has a brightness of over 200 and therefore is a light color tone.
     * This helps mainly when providing a dynamic background color for a dynamic text color, setting the latter depending on the background brightness.
     * @param color The color int to check
     * @return true, if the color is bright, false otherwise.
     */
    @JvmStatic
    fun isBrightColor(color: Int): Boolean {
        if (android.R.color.transparent == color)
            return false

        val rgb = intArrayOf(Color.red(color), Color.green(color), Color.blue(color))
        val brightness = Math.sqrt(rgb[0].toDouble() * rgb[0].toDouble() * .241 + (rgb[1].toDouble()
                * rgb[1].toDouble() * .691) + rgb[2].toDouble() * rgb[2].toDouble() * .068).toInt()

        // color is light
        return brightness >= 200
    }

    /**
     * Returns a light or dark color depending on the brightness of the background.
     * This allows getting readable text over different background colors
     * @param background The color of the background that contains the text
     * @return A readable text color
     */
    @JvmStatic
    fun getSuitableTextColor(background: Int): Int {
        return if(isBrightColor(background)) Color.parseColor("#232323") else Color.parseColor("#f5f5f5")
    }
}
