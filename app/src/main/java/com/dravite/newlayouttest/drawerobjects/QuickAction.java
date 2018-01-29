package com.dravite.newlayouttest.drawerobjects;

import java.io.Serializable;

/**
 * Class that represents a QuickAction. Just the icon, the package, class and position index.
 */
public class QuickAction implements Serializable {

    public String iconRes;
    public String intentPackage;
    public String intentClass;
    public int qaIndex;

    @SuppressWarnings("unused")
    public QuickAction(){}

    public QuickAction(String ir, String ip, String ic, int qi){
        iconRes = ir;
        intentPackage = ip;
        intentClass = ic;
        qaIndex = qi;
    }
}
