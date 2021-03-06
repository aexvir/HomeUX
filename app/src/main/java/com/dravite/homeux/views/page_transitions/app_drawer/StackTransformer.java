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

package com.dravite.homeux.views.page_transitions.app_drawer;

import android.view.View;

import com.dravite.homeux.LauncherUtils;

public class StackTransformer extends BaseTransformer {

    @Override
    public void transformRaw(View view, float position) {
        view.setTranslationX(position < 0 ? 0f : -view.getWidth() * position);
        view.setElevation(LauncherUtils.dpToPx(10, view.getContext()) * -(position - 2));
        view.setAlpha(position>0?1-position:1f);
        view.setPivotY((view.getTop() + view.getBottom()) / 2);
        view.setPivotX((view.getLeft() + view.getRight()) / 2);
        if (position < 0) {
            float scale = Math.min(-(position - 1), 1.1f);
            view.setScaleX(scale);
            view.setScaleY(scale);
        }
    }
}
