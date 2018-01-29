package com.dravite.newlayouttest.general_helpers;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Size;

/**
 * A helper class to modify colors.
 */
public class ColorUtils {

    /**
     * Converts a given HSL formatted array to a color int.
     * @param hsl The 3-part HSL array
     * @return The resulting color int.
     */
    public static int HSLtoColor(@Size(3) float[] hsl){
        if(hsl.length!=3)
            throw new RuntimeException("HSL needs to have 3 components.");

        if(hsl[1] == 0){
            int component = (int)(hsl[2] * 255);
            return Color.rgb(component, component, component);
        }

        float temp_1;

        if(hsl[2]<0.5f){
            temp_1 = hsl[2]*(1f+hsl[1]);
        } else {
            temp_1 = hsl[2]+hsl[1]-(hsl[2]*hsl[1]);
        }

        float temp_2 = 2*hsl[2]-temp_1;
        float hue = hsl[0]/360f;

        float tempR = hue+0.3333f;
        float tempG = hue;
        float tempB = hue-0.3333f;

        if(tempR>1){
            tempR-=1f;
        } else if(tempR<0){
            tempR+=1f;
        }
        if(tempG>1){
            tempG-=1f;
        } else if(tempG<0){
            tempG+=1f;
        }
        if(tempB>1){
            tempB-=1f;
        } else if(tempB<0){
            tempB+=1f;
        }

        float red = testColor(tempR, temp_1, temp_2);
        float green = testColor(tempG, temp_1, temp_2);
        float blue = testColor(tempB, temp_1, temp_2);

        return Color.rgb(getBitColor(red), getBitColor(green), getBitColor(blue));
    }

    private static int getBitColor(float value){
        return Math.round(value*255f);
    }

    private static float testColor(float temporaryColor, float t1, float t2){
        if(6*temporaryColor<1){
            return t2 + (t1-t2)*6*temporaryColor;
        } else if (2*temporaryColor<1){
            return t1;
        } else if (3*temporaryColor<2){
            return t2+(t1-t2)*(0.6666f-temporaryColor)*6;
        } else {
            return t2;
        }
    }

    /**
     * Converts a color int into a 3-part HSL float array.
     * @param color The color to convert
     * @return A 3-part HSL float array from the color.
     */
    public static float[] colorToHSL(int color){
        float[] result = new float[3];

        float red = Color.red(color)/255f;
        float green = Color.green(color)/255f;
        float blue = Color.blue(color)/255f;

        float min = Math.min(red, Math.min(green, blue));
        float max = Math.max(red, Math.max(green, blue));

        result[2] = (min+max)/2f;

        if(result[2]<=0.5f){
            result[1] = (max-min)/(max+min);
        } else {
            result[1] = (max-min)/(2f-max-min);
        }

        if(max==min){
            result[0] = 0;
        } else if(max == red){
            result[0] = (green-blue)/(max-min);
        } else if(max == green){
            result[0] = 2.0f+(blue-red)/(max-min);
        } else if(max == blue){
            result[0] = 4.0f+(red-green)/(max-min);
        }

        result[0] *= 60f;

        if(result[0]<0){
            result[0] += 360;
        }

        return result;
    }

    /**
     * Darkens the given color by 0.8x.
     * @param color The source color.
     * @return A darker tone of the source color.
     */
    public static int getDarkerColor(int color){
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(color),
                Color.green(color),
                Color.blue(color),
                hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    /**
     * Checks if a given color has a brightness of over 200 and therefore is a light color tone.
     * This helps mainly when providing a dynamic background color for a dynamic text color, setting the latter depending on the background brightness.
     * @param color The color int to check
     * @return true, if the color is bright, false otherwise.
     */
    public static boolean isBrightColor(int color) {
        if (android.R.color.transparent == color)
            return false;

        int[] rgb = { Color.red(color), Color.green(color), Color.blue(color) };
        int brightness = (int) Math.sqrt(rgb[0] * rgb[0] * .241 + rgb[1]
                * rgb[1] * .691 + rgb[2] * rgb[2] * .068);

         // color is light
        return brightness >= 200;
    }
}
