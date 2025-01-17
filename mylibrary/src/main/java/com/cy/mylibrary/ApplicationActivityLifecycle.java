package com.cy.mylibrary;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.core.view.LayoutInflaterCompat;

import com.cy.mylibrary.utils.SkinThemeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Observable;

public class ApplicationActivityLifecycle implements Application.ActivityLifecycleCallbacks {

    private Observable mObserable;
    private ArrayMap<Activity, SkinLayoutInflaterFactory> mLayoutInflaterFactories = new
            ArrayMap<>();

    public ApplicationActivityLifecycle(Observable observable) {
        mObserable = observable;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.e("aaa", "ApplicationActivityLifecycle onActivityCreated");
        /**
         *  更新状态栏
         */
        SkinThemeUtils.updateStatusBarColor(activity);

        /**
         *  更新布局视图
         */
        //获得Activity的布局加载器
        LayoutInflater layoutInflater = activity.getLayoutInflater();

        /**
         * 安卓9以前可以
         */
        /*try {
            //Android 布局加载器 使用 mFactorySet 标记是否设置过Factory
            //如设置过抛出一次
            //设置 mFactorySet 标签为false
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(layoutInflater, false);
            boolean mFactorySet = field.getBoolean("mFactorySet");

        } catch (Exception e) {
            e.printStackTrace();
        }*/
        SkinLayoutInflaterFactory skinLayoutInflaterFactory = new SkinLayoutInflaterFactory
                (activity);
        Class<LayoutInflaterCompat> compatClass = LayoutInflaterCompat.class;
        Field sCheckedField = null;
        try {
            sCheckedField = compatClass.getDeclaredField("sCheckedField");
            sCheckedField.setAccessible(true);
            sCheckedField.setBoolean(layoutInflater, false);

            Method forceSetFactory2 = compatClass.getDeclaredMethod("forceSetFactory2", LayoutInflater.class, LayoutInflater.Factory2.class);
            forceSetFactory2.setAccessible(true);
            forceSetFactory2.invoke(compatClass, layoutInflater, skinLayoutInflaterFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }


        //使用factory2 设置布局加载工程

        //LayoutInflaterCompat.setFactory2(layoutInflater, skinLayoutInflaterFactory);
        mLayoutInflaterFactories.put(activity, skinLayoutInflaterFactory);

        mObserable.addObserver(skinLayoutInflaterFactory);
    }


    @Override
    public void onActivityStarted(Activity activity) {
        Log.e("aaa", "ApplicationActivityLifecycle onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Log.e("aaa", "ApplicationActivityLifecycle onActivityResumed");
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        SkinLayoutInflaterFactory observer = mLayoutInflaterFactories.remove(activity);
        SkinManager.getInstance().deleteObserver(observer);
    }
}
