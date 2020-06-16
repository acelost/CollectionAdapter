package com.acelost.collectionadapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Базовая реализация {@link CollectionAdapter}, оперирующая списком элементов в качестве модели данных.
 *
 * @param <T>   - тип элементов списка
 * @param <VH>  - тип вью холдера
 */
public abstract class AbstractCollectionAdapter<T, VH extends ChildViewHolder> extends CollectionAdapter<VH> {

    @NonNull
    private final List<T> mItems = new ArrayList<>();

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    /**
     * Задать перечень элементов коллекции.
     *
     * @param items - новые элементы коллекции
     */
    public void set(@Nullable final List<T> items) {
        if (items == null) {
            if (!mItems.isEmpty()) {
                mItems.clear();
                notifyDataChanged();
            }
            return;
        }
        if (!mItems.equals(items)) {
            mItems.clear();
            mItems.addAll(items);
            notifyDataChanged();
        }
    }

    @Override
    protected final void onBindViewHolder(@NonNull final VH holder, final int position) {
        if (position < 0 || position >= mItems.size()) {
            throw new IndexOutOfBoundsException();
        }
        final T item = mItems.get(position);
        if (item == null) {
            throw new NullPointerException();
        }
        onBindViewHolder(holder, item, position);
    }

    protected abstract void onBindViewHolder(
            @NonNull final VH holder,
            @NonNull final T item,
            final int position
    );
}
