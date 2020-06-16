package com.acelost.collectionadapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * Базовая реализация класса-посредника для связи {@link CollectionAdapter} с {@link ViewGroup}.
 * Наследнику нужно реализовать только метод {@link CollectionParent#addItemInLayout(View, int)}.
 */
public abstract class AbstractCollectionParent implements CollectionParent {

    protected final ViewGroup mViewGroup;

    protected AbstractCollectionParent(@NonNull final ViewGroup viewGroup) {
        this.mViewGroup = viewGroup;
    }

    @NonNull
    @Override
    public Context getContext() {
        return mViewGroup.getContext();
    }

    @NonNull
    @Override
    public ViewGroup getView() {
        return mViewGroup;
    }

    @Override
    public void removeViewInLayout(@NonNull final View view) {
        mViewGroup.removeViewInLayout(view);
    }

    @Override
    public void removeViewsInLayout(final int start, final int count) {
        mViewGroup.removeViewsInLayout(start, count);
    }

    @Override
    public int getChildCount() {
        return mViewGroup.getChildCount();
    }

    @Override
    public void requestLayout() {
        mViewGroup.requestLayout();
    }
}
