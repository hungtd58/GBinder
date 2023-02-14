package com.h2bros.reflection;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class ButterKnife {
    private static final String SUFFIX = "_GBinding";

    public static void bind(Activity activity) {
        bind(activity, activity.getWindow().getDecorView());
    }

    public static void bind(Object target, View view) {
        try {
            Constructor<?> constructor = findBindingConstructorForClass(target.getClass());
            if (constructor == null) {
                Log.e("@@@@@@", "Constructor Null: " + target.getClass().getName());
                return;
            }
            constructor.newInstance(target, view);
        } catch (IllegalAccessException |
                 InstantiationException | InvocationTargetException e) {
            Log.e("TAG", "Meaningful Message", e);
        }
    }

    public static Constructor<?> findBindingConstructorForClass(Class<?> cls) {
        if (cls == null || cls.getClassLoader() == null) return null;
        Constructor<?> bindingCtor;
        String clsName = cls.getName();
        try {
            Class<?> bindingClass = cls.getClassLoader().loadClass(clsName + SUFFIX);
            bindingCtor = bindingClass.getConstructor(cls, View.class);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            bindingCtor = findBindingConstructorForClass(cls.getSuperclass());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find binding constructor for " + clsName, e);
        }
        return bindingCtor;
    }
}
