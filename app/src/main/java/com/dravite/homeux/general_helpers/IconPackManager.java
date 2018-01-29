package com.dravite.homeux.general_helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;

import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Manages all the things about icon packs, providing helping structures and methods.
 */
public class IconPackManager
{

    /**
     * A full icon pack, initially unloaded. Call {@link IconPack#loadAll(UpdateListener)} or {@link IconPack#loadAllInstalled(UpdateListener)} or equivalent to load all needed icons.
     */
    public static class IconPack extends DefaultHandler{

        //Basic variables
        public String mPackageName;
        public Context mContext;
        //The icon pack Resources object
        public Resources mPackRes;
        //Checks to see if the icon names consist only of Strings
        public boolean hasAlternateStructure;
        //A randomizer for selecting an icon background for icons that don't exist in the icon pack
        private Random mRandom;
        //Set this to true as soon as the icon pack has been loaded.
        public boolean isLoaded;

        //Lets other classes interact with the loading progress
        public UpdateListener mCountListener;

        //Line count of the appfilter.xml file
        public int mLineCount;
        //The current item index when iterating through the xml
        public int mCurrentItem;

        //Maps a componentName to its icon pack integer resource id
        public HashMap<String, Integer> mIconMap;
        //Maps a componentName to its icon pack String resource identifier
        public HashMap<String, String> mIconMapStrings;

        /** A list of apps of which the icon pack will load the icons when calling {@link IconPack#loadSelected(UpdateListener, List)} or equivalent. */
        private List<String> mPendingApps;

        //A list of resource IDs for the icon pack icon backgrounds
        public List<Integer> mIconBackInts = new ArrayList<>();
        //The resource ID of this icon packs icon mask and icon overlay
        public Integer mIconMaskInt = Integer.MIN_VALUE,
                mIconUponInt = Integer.MIN_VALUE;

        //A list of resource identifier Strings for the icon pack icon backgrounds
        public List<String> mIconBackNames = new ArrayList<>();
        //The resource identifier String of this icon packs icon mask and icon overlay
        public String mIconMaskName = null,
                mIconUponName = null;

        //Scale the default icon by this amount (to be set when loading) before adding it to the themed icon construct
        public float mScale = 1;

        //The icon pack's best fitting and existing device density extension String.
        String mExtention = null;

        //The addition paint for assembling a non-existent icon
        Paint defaultIconPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
        //The division paint for cutting out the default icon through the icon mask
        Paint iconMaskPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

        /**
         * This sax handler just simply counts the necessary lines in the icon pack
         */
        public class CountHandler extends DefaultHandler{

            public CountHandler(){
                mLineCount = 0;
            }

            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                if(attributes.getValue(0)!=null && attributes.getValue(0).contains("ComponentInfo")) {
                    //Only add if the line is suitable (contains a ComponentName)
                    mLineCount++;
                }
            }
        }

        public IconPack(Context context, String packageName) throws PackageManager.NameNotFoundException{
            this.mPackageName=packageName;
            this.mContext = context;
            if(!mPackageName.equals(""))
                this.mPackRes = mContext.getPackageManager().getResourcesForApplication(mPackageName);
            mIconMap = new HashMap<>();
            mIconMapStrings = new HashMap<>();
            mRandom = new Random(System.currentTimeMillis());
        }

        //Sax handler method
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            mCurrentItem++;
            if(attributes.getValue(0)!=null && attributes.getValue(0).contains("ComponentInfo") && mPendingApps !=null && !mPendingApps.contains(attributes.getValue(0))) {
                //Don't look at the line if it's not needed.
                return;
            }

            //Update for other classes
            if(mCountListener!=null)
                mCountListener.update(mCurrentItem, mLineCount);

            //First, add all backs, the mask, the overlay and the icon scale
            if(hasAlternateStructure){
                //Add as String
                switch (qName.toLowerCase()){
                    case "iconback":
                        for (int i = 0; i < attributes.getLength(); i++) {
                            mIconBackNames.add(attributes.getValue(i));
                        }
                        return;
                    case "iconmask":
                        mIconMaskName=attributes.getValue(0);
                        return;
                    case "iconupon":
                        mIconUponName=attributes.getValue(0);
                        return;
                    case "scale":
                        mScale=Float.parseFloat(attributes.getValue(0));
                        return;
                }
            } else {
                //Add as integer
                switch (qName.toLowerCase()) {
                    case "iconback":
                        for (int i = 0; i < attributes.getLength(); i++) {
                            mIconBackInts.add(mPackRes.getIdentifier(attributes.getValue(i), "drawable", mPackageName));
                        }
                        return;
                    case "iconmask":
                        mIconMaskInt = mPackRes.getIdentifier(attributes.getValue(0), "drawable", mPackageName);
                        return;
                    case "iconupon":
                        mIconUponInt = mPackRes.getIdentifier(attributes.getValue(0), "drawable", mPackageName);
                        return;
                    case "scale":
                        mScale = Float.parseFloat(attributes.getValue(0));
                        return;
                }
            }

            //Second, load the icons themselves.
            if(attributes.getValue(0)!=null && attributes.getValue(0).contains("ComponentInfo")) {
                if(hasAlternateStructure)
                    //As string
                    mIconMapStrings.put(attributes.getValue(0), attributes.getValue(1));
                else {
                    //As integer res ID
                    int intRes = mPackRes.getIdentifier(attributes.getValue(1), "drawable", mPackageName);
                    mIconMap.put(attributes.getValue(0), intRes);
                }
            }

        }

        /**
         * Loads all icons for a given package. Calls {@link #loadSelected(UpdateListener, List)}.
         * @param listener A listener to listen for loading updates
         * @param packageName Load icons for this package name. Set to null to load all installed.
         * @throws SAXException
         * @throws PackageManager.NameNotFoundException
         * @throws IOException
         * @throws ParserConfigurationException
         */
        public void loadSelectedPackage(UpdateListener listener, String packageName) throws SAXException, PackageManager.NameNotFoundException, IOException, ParserConfigurationException{
            LauncherApps launcherApps = (LauncherApps)mContext.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            List<LauncherActivityInfo> infos = launcherApps.getActivityList(packageName, android.os.Process.myUserHandle());
            loadSelected(listener, infos);
        }

        /**
         * Parses the appfilter.xml file from the iconPack and loads all needed icons provided by a list of LauncherActivityInfos.
         * @param listener A listener to listen for loading updates
         * @param installedApps Load for all apps from this list. Set to null to just load all icons.
         * @throws SAXException
         * @throws PackageManager.NameNotFoundException
         * @throws IOException
         * @throws ParserConfigurationException
         */
        public void loadSelected(UpdateListener listener, List<LauncherActivityInfo> installedApps) throws SAXException, PackageManager.NameNotFoundException, IOException, ParserConfigurationException{
            if(installedApps==null){
                //load all icons...
                mPendingApps = null;
            } else {
                mPendingApps = new ArrayList<>();
                for (LauncherActivityInfo info : installedApps) {
                    mPendingApps.add(info.getComponentName().toString());
                }
            }
            isLoaded = true;
            if(mPackageName.equals("")){
                return;
            }
            mCountListener=listener==null?new UpdateListener() {
                @Override
                public void update(int current, int max) {

                }
            }:listener;

            InputStream raw;
            try{
                raw = mPackRes.getAssets().open("appfilter.xml");
                hasAlternateStructure=false;
            } catch (FileNotFoundException e){
                raw = mPackRes.getAssets().open("icons/res/xml/appfilter.xml");
                hasAlternateStructure=true;
            }
            SAXParserFactory saxFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxFactory.newSAXParser();
            saxParser.parse(raw, new CountHandler());
            try{
                raw = mPackRes.getAssets().open("appfilter.xml");
                hasAlternateStructure=false;
            } catch (FileNotFoundException e){
                raw = mPackRes.getAssets().open("icons/res/xml/appfilter.xml");
                hasAlternateStructure=true;
            }
            mCurrentItem = 0;
            saxParser.parse(raw, this);
        }

        /**
         * Loads icons for all installed packages. Calls {@link #loadSelectedPackage(UpdateListener, String)} with a null String.
         * @param listener A listener to listen for loading updates
         * @throws SAXException
         * @throws PackageManager.NameNotFoundException
         * @throws IOException
         * @throws ParserConfigurationException
         */
        public void loadAllInstalled(UpdateListener listener) throws SAXException, PackageManager.NameNotFoundException, IOException, ParserConfigurationException{
            mIconMap.clear();
            mIconMapStrings.clear();
            loadSelectedPackage(listener, null);
        }

        /**
         * Loads all icons from the icon pack.
         * @param listener A listener to listen for loading updates
         * @throws SAXException
         * @throws PackageManager.NameNotFoundException
         * @throws IOException
         * @throws ParserConfigurationException
         */
        public void loadAll(UpdateListener listener) throws SAXException, PackageManager.NameNotFoundException, IOException, ParserConfigurationException{
            mIconMap.clear();
            mIconMapStrings.clear();
            loadSelected(listener, null);
        }

        /**
         * Load an icon for a given componentName in Bitmap form.
         * @param componentName Load for this component
         * @param defaultIcon The component's default icon for when there is no icon available in the icon pack.
         * @return Either the corresponding icon for the component, or the modified default icon, or just the default icon in case of the default icon pack.
         */
        public Bitmap getIconBitmap(String componentName, Bitmap defaultIcon){
            if(mPackageName.equals("")) {
                //Default icon pack
                return defaultIcon;
            }
            if((!hasAlternateStructure && !mIconMap.containsKey(componentName)) || (hasAlternateStructure&&!mIconMapStrings.containsKey(componentName))){
                //modify default icon according to the icon pack
                return compileIcon(defaultIcon);
            }
            if(hasAlternateStructure)
                try{
                    //Get the icon suiting for the device density.
                    return getBitmapForDensityWithComponent(componentName);
                } catch (IOException e){
                    e.printStackTrace();
                    return compileIcon(defaultIcon);
                }
            try{
                Bitmap bmp = BitmapFactory.decodeResource(mPackRes, mIconMap.get(componentName));
                if(bmp==null)
                    return compileIcon(defaultIcon);
                return bmp;
            } catch (Resources.NotFoundException e){
                return compileIcon(defaultIcon);
            }
        }

        /**
         * Loads the density extension (i.e. hdpi, xhdpi etc.) suiting for the device, then decodes the icon bitmap from the icon pack resource.
         * @param filename The fileName of the icon.
         * @return The icon bitmap itself
         * @throws IOException
         */
        Bitmap getBitmapForDensityWithName(String filename) throws IOException{
            if(mExtention==null)
                mExtention = getHighestFittingExistingExtentionWithName(filename);
            return decodeSampledBitmapFromResource(filename, mExtention, LauncherUtils.dpToPx(72, mContext), LauncherUtils.dpToPx(72, mContext));
        }

        /**
         * Loads the density extension (i.e. hdpi, xhdpi etc.) suiting for the device, then decodes the icon bitmap from the icon pack resource.
         * @param componentName The app's componentName
         * @return The icon bitmap itself
         * @throws IOException
         */
        Bitmap getBitmapForDensityWithComponent(String componentName) throws IOException{
            return getBitmapForDensityWithName( mIconMapStrings.get(componentName));
        }

        /**
         * Quickly load a fitting sized bitmap with an according inSampleSize to improve loading speed
         * @param filename load this file
         * @param ext the density extension (hdpi etc.)
         * @param reqWidth The required minimum bitmap width
         * @param reqHeight The required minimum bitmap height
         * @return The decoded and properly sized bitmap
         * @throws IOException
         */
        public Bitmap decodeSampledBitmapFromResource(String filename, String ext,
                                                             int reqWidth, int reqHeight) throws IOException{
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(mPackRes.getAssets().open("icons/res/drawable-" + ext + "/" + filename + ".png"), null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeStream(mPackRes.getAssets().open("icons/res/drawable-" + ext + "/" + filename + ".png"), null, options);
        }

        /**
         * Calculates the smallest inSampleSize for the bitmap to be larger than the given required width and height.
         * @param options The bitmap's BitmapFactory.Options.
         * @param reqWidth The minimum required width
         * @param reqHeight The minimum required height
         * @return A proper inSampleSize integer
         */
        public static int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {

                final int halfHeight = height / 2;
                final int halfWidth = width / 2;

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while ((halfHeight / inSampleSize) > reqHeight
                        && (halfWidth / inSampleSize) > reqWidth) {
                    inSampleSize *= 2;
                }
            }

            return inSampleSize;
        }

        /**
         * Provides the best existing density extension (hdpi etc.) in the icon pack suiting for the device.
         * (Provides the next larger one in case the exact one doesn't exist, or the next smaller one if both don't exist)
         * @param fileName Get extension for this file
         * @return The best density extension
         */
        String getHighestFittingExistingExtentionWithName(String fileName){
            DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
            int density = metrics.densityDpi;
            String useExt = "null";
            int[] densities = new int[]{DisplayMetrics.DENSITY_LOW, DisplayMetrics.DENSITY_MEDIUM, DisplayMetrics.DENSITY_HIGH, DisplayMetrics.DENSITY_XHIGH, DisplayMetrics.DENSITY_XXHIGH, DisplayMetrics.DENSITY_XXXHIGH};
            boolean[] existence = new boolean[densities.length];
            for (int e = 0; e < existence.length; e++) {
                if(checkIfAssetExistsWithName(densities[e], fileName) && (useExt.equals("null") || useExt.equals(getExtForDensity(density)) || (! useExt.equals(getExtForDensity(density)) && densities[e]>density))){
                    useExt = getExtForDensity(densities[e]);
                }
            }
            return useExt;
        }

        /**
         * Gets a fitting density extension String for a given extension integer.
         * @param density Check against this density.
         * @return The density string.
         */
        String getExtForDensity(int density){
            String ext;
            if(density<=DisplayMetrics.DENSITY_LOW){
                ext = "ldpi";

            } else if(density<=DisplayMetrics.DENSITY_MEDIUM){
                ext = "mdpi";

            } else if(density<=DisplayMetrics.DENSITY_HIGH){
                ext = "hdpi";

            } else if(density<=DisplayMetrics.DENSITY_XHIGH){
                ext = "xhdpi";

            } else if(density<=DisplayMetrics.DENSITY_XXHIGH){
                ext = "xxhdpi";

            } else {
                ext = "xxxhdpi";
            }
            return ext;
        }

        /**
         * A little file check.
         * @param density The needed density.
         * @param fileName Check for this file.
         * @return true if the file exists for the given density, false otherwise
         */
        boolean checkIfAssetExistsWithName(int density, String fileName){
            try{
                String ext = getExtForDensity(density);
                mPackRes.getAssets().open("icons/res/drawable-" + ext + "/" + fileName + ".png");
                return true;
            }catch (IOException e){
                return false;
            }
        }

        /**
         * Assembles the themed icon from a given default icon, applying a scale, a background, mask and overlay to it. All components only if existent.
         * @param defaultBitmap The default icon
         * @return A themed version of the default icon
         */
        Bitmap compileIcon(Bitmap defaultBitmap){
            int iconSize = LauncherUtils.dpToPx(72, mContext);

            //initialize paints
            defaultIconPaint.setAntiAlias(true);
            iconMaskPaint.setAntiAlias(true);
            iconMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));

            //initialize mask, back and overlay if needed.
            defaultBitmap = LauncherUtils.getResizedBitmap(defaultBitmap, ((int) (iconSize * mScale)), ((int) (iconSize * mScale)));
            Bitmap maskBitmap=null, backBitmap=null, uponBitmap=null, defaultBitmapModified = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888),
                    finalIcon = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888);
            if(mIconMaskInt!=Integer.MIN_VALUE)
                maskBitmap = getBitmap(mIconMaskInt);
            else if(mIconMaskName!=null && hasAlternateStructure){
                try{
                    maskBitmap = getBitmapForDensityWithName(mIconMaskName);
                }catch (IOException e){

                }
            }
            if (mIconBackInts.size() != 0) {
                int randomBack = mRandom.nextInt(mIconBackInts.size());
                backBitmap = getBitmap(mIconBackInts.get(randomBack));
            } else if(mIconBackNames.size()!=0 && hasAlternateStructure){
                try{
                    int randomBack = mRandom.nextInt(mIconBackNames.size());
                    backBitmap = getBitmapForDensityWithName(mIconBackNames.get(randomBack));
                } catch (IOException e){

                }
            }
            if (mIconUponInt != Integer.MIN_VALUE) {
                uponBitmap = getBitmap(mIconUponInt);
            } else if(mIconUponName!=null && hasAlternateStructure){
                try{
                    uponBitmap = getBitmapForDensityWithName(mIconUponName);
                } catch (IOException e){

                }
            }

            //Now assemble the icon

            Canvas finalCanvas = new Canvas(finalIcon);
            if(backBitmap!=null){
                finalCanvas.drawBitmap(backBitmap, LauncherUtils.getResizedMatrix(backBitmap, iconSize, iconSize), defaultIconPaint);
            }

            int bmpLeft = ((int) ((iconSize - iconSize * mScale) / 2f));
            int bmpTop = ((int) ((iconSize - iconSize * mScale) / 2f));
            Canvas defaultCanvas = new Canvas(defaultBitmapModified);
            defaultCanvas.drawBitmap(defaultBitmap, bmpLeft, bmpTop, defaultIconPaint);
            if(maskBitmap!=null){
                defaultCanvas.drawBitmap(maskBitmap, LauncherUtils.getResizedMatrix(maskBitmap, iconSize, iconSize), iconMaskPaint);
            }

            //compile full icon
            finalCanvas.drawBitmap(defaultBitmapModified, LauncherUtils.getResizedMatrix(defaultBitmapModified, iconSize, iconSize), defaultIconPaint);
            if(uponBitmap!=null){
                finalCanvas.drawBitmap(uponBitmap, LauncherUtils.getResizedMatrix(uponBitmap, iconSize, iconSize), defaultIconPaint);
            }

            //TODO really needed?
            FileManager.saveBitmap(mContext, defaultBitmap, "/testbmp/", "default.png");
            FileManager.saveBitmap(mContext, defaultBitmapModified, "/testbmp/", "defaultModified.png");
            FileManager.saveBitmap(mContext, backBitmap, "/testbmp/", "back.png");
            FileManager.saveBitmap(mContext, maskBitmap, "/testbmp/", "mask.png");
            FileManager.saveBitmap(mContext, uponBitmap, "/testbmp/", "upon.png");

            // Recycle/clean all bitmaps.
           /* defaultBitmap.recycle();
            defaultBitmapModified.recycle();
            if(backBitmap!=null)
                backBitmap.recycle();
            if(maskBitmap!=null)
                maskBitmap.recycle();
            if(uponBitmap!=null)
                uponBitmap.recycle();*/

            return finalIcon;
        }

        /**
         * Decodes a bitmap from the icon packs resources by providing a res ID.
         * @param resID decode this bitmap
         * @return The decoded Bitmap
         */
        Bitmap getBitmap(int resID){
            return BitmapFactory.decodeResource(mPackRes, resID);
        }
    }

    /**
     * A very simplified IconPack structure that just holds the most important info.
     */
    public static class Theme{
        public String packageName;
        public Drawable icon;
        public String label;

        public Theme(String pn, String lbl, Drawable d){
            packageName=pn;
            label=lbl;
            icon=d;
        }
    }

    /**
     * Finds all installed IconPacks.
     * @param context The current context
     * @param includeDefault Set to true, if you want to add a dummy "default" icon pack item to the result list
     * @return A list of all installed icon packs
     */
    public static List<Theme> getAllThemes(Context context, boolean includeDefault) {
        PackageManager pm = context.getPackageManager();
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory("com.anddoes.launcher.THEME");
        List<ResolveInfo> packs = pm.queryIntentActivities(mainIntent, 0);
        List<Theme> pacs = new ArrayList<>();
        int boundsSize = LauncherUtils.dpToPx(40, context);
        if(includeDefault) {
            Drawable icon1 = context.getDrawable(R.mipmap.ic_launcher);
            icon1.setBounds(0, 0, boundsSize, boundsSize);
            pacs.add(new Theme("", "Default icons", icon1));
        }
        for (int i = 0; i < packs.size(); i++) {
            Drawable icon = packs.get(i).activityInfo.loadIcon(pm);
            icon.setBounds(0, 0, boundsSize, boundsSize);
            pacs.add(new Theme(packs.get(i).activityInfo.packageName, packs.get(i).activityInfo.loadLabel(pm).toString(), icon));
        }
        return pacs;
    }

}