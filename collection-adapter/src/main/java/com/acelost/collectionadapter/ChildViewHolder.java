package com.acelost.collectionadapter;

import android.view.View;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

/**
 * Базовый класс вью-ходера для дочерней вью из коллекции.
 */
public abstract class ChildViewHolder {

    /**
     * Экземпляр {@link View}, которые содержит вью-холдер.
     */
    @NonNull
    public final View view;

    /**
     * Тип вью.
     */
    private int viewType;

    /**
     * Находится ли вью в скрытом состоянии.
     */
    private boolean inStash;

    /**
     * Видимость вью до скрытия.
     */
    private int beforeStashVisibility;

    /**
     * Позиция вью-холдера в адаптере.
     */
    private int adapterPosition = CollectionParent.NO_POSITION;

    public ChildViewHolder(@NonNull final View view) {
        if (view == null) {
            throw new IllegalArgumentException("View may not be null.");
        }
        this.view = view;
    }

    /**
     * Получить тип вью.
     */
    public final int getViewType() {
        return viewType;
    }

    /**
     * Задать тип вью.
     */
    void setViewType(final int viewType) {
        this.viewType = viewType;
    }

    /**
     * Получить позицию вью-холдера в адаптере.
     */
    public final int getAdapterPosition() {
        return adapterPosition;
    }

    /**
     * Задать позицию вью-холдера в адаптере.
     * @param position - позиция
     */
    void setAdapterPosition(final int position) {
        this.adapterPosition = position;
    }

    /**
     * Подготовить вью-холдер к использованию в коллекции.
     */
    @CallSuper
    public void prepare() {
        if (inStash) {
            inStash = false;
            view.setVisibility(beforeStashVisibility);
        }
    }

    /**
     * Спрятать вью-холдер и ассоциированную вью.
     */
    public void stash() {
        if (!inStash) {
            inStash = true;
            beforeStashVisibility = view.getVisibility();
            view.setVisibility(View.GONE);
        }
    }

    /**
     * Освободить ресурсы, захваченные вью-холдером,
     * перед перемещением в пул.
     */
    public void onRecycle() {
        // do nothing
    }

}