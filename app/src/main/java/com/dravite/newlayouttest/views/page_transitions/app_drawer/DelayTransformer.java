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

public class DelayTransformer implements ViewPager.PageTransformer {


    @Override
    public void transformPage(View view, float position) {
        ///TODO
//        if(view.getOutlineProvider()==null || !(view.getOutlineProvider() instanceof RevealOutlineProvider)) {
//            RevealOutlineProvider mOutlineProvider = new RevealOutlineProvider((view.getLeft() + view.getRight()) / 2, (view.getTop() + view.getBottom()) / 2, 0, Math.max(view.getMeasuredHeight(), view.getMeasuredWidth()));
//            view.setOutlineProvider(mOutlineProvider);
//        }
//
//        if(Math.abs(position)>1){
//            view.setVisibility(View.INVISIBLE);
//            view.setOutlineProvider(null);
//        } else {
//            view.setVisibility(View.VISIBLE);
//            view.setTranslationX(-position*view.getMeasuredWidth());
//            ((RevealOutlineProvider) view.getOutlineProvider()).setProgress(Math.min(1, 1 - position));
//            view.invalidateOutline();
//        }
    }
}
