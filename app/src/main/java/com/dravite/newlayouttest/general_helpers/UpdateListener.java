package com.dravite.newlayouttest.general_helpers;

/**
 * Usually used in combination with an {@link com.dravite.newlayouttest.general_helpers.IconPackManager.IconPack} to indicate the loading status.
 */
public interface UpdateListener{
    void update(int current, int max);
}
