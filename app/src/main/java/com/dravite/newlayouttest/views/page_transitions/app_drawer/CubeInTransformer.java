/*
 * Copyright 2014 Toxic Bakery
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dravite.newlayouttest.views.page_transitions.app_drawer;

import android.support.v4.view.ViewPager;
import android.view.View;

public class CubeInTransformer implements ViewPager.PageTransformer{

    @Override
    public void transformPage(View view, float position) {
        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);
            view.setTranslationZ(-1);

        } else if (position <= 1) { // [-1,1]
            view.setAlpha(1);
            view.setTranslationZ(0);
            // Rotate the fragment on the left or right edge
            view.setPivotX(position > 0 ? 0 : view.getWidth());
            view.setPivotY(0);
            view.setRotationY(-90f * position);
        } else {
            view.setTranslationZ(-1);
            view.setAlpha(0);
        }
    }
}
