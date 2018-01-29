package com.dravite.newlayouttest.iconpacks;

import java.util.Observable;

/**
 * Created by Johannes on 03.11.2015.
 * Here, we notify the LauncherActivity when there is a license update.
 */
public class LicensingObserver extends Observable {
    private static LicensingObserver instance = new LicensingObserver();

    public static LicensingObserver getInstance() {
        return instance;
    }

    private LicensingObserver() {
    }

    public void updateValue(Object data) {
        synchronized (this) {
            setChanged();
            notifyObservers(data);
        }
    }
}
