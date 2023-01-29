package com.cursoandroid.uber.helper;

import android.content.Context;
import android.content.pm.PackageManager;

public class Packager {
    public static boolean isExited(Context context, String targetPackage){
        PackageManager pm = context.getPackageManager();
        try{
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        }catch (PackageManager.NameNotFoundException e){
            return false;
        }
        return true;
    }
}
