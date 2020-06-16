package com.acelost.collectionadapter;

import android.util.SparseArray;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Пул для переиспользованных вью-холдеров элементов коллекции.
 */
public class CollectionViewHolderPool {

    /**
     * Максимальное количество вью-холдеров одного типа, которые
     * могут храниться в пуле по умолчанию.
     */
    private static final int DEFAULT_MAX_SCRAP = 5;

    /**
     * Данные о хранящихся в пуле вью-холдерах конкретного типа.
     */
    private static class ScrapData {
        /**
         * Вью-холдеры для переиспользования.
         */
        @NonNull
        final ArrayList<ChildViewHolder> mScrapHeap = new ArrayList<>();
        /**
         * Максимальное количество вью-холдеров в пуле.
         */
        int mMaxScrap = DEFAULT_MAX_SCRAP;
    }

    /**
     * Ассоциативный массив, где ключ - это тип вью, а значение -
     * это {@link ScrapData} для этого типа вью.
     */
    @NonNull
    private final SparseArray<ScrapData> mScrap = new SparseArray<>();

    /**
     * Удалить все вью-холдеры из пула.
     */
    public void clear() {
        for (int i = 0; i < mScrap.size(); i++) {
            final ScrapData data = mScrap.valueAt(i);
            data.mScrapHeap.clear();
        }
    }

    /**
     * Задать максимальное количество вью-холдеров указанного типа в пуле.
     *
     * @param viewType  - тип вью
     * @param max       - максимальное количество
     */
    public void setMaxRecycledViews(final int viewType, final int max) {
        final ScrapData scrapData = getScrapDataForType(viewType);
        scrapData.mMaxScrap = max;
        final ArrayList<ChildViewHolder> scrapHeap = scrapData.mScrapHeap;
        while (scrapHeap.size() > max) {
            scrapHeap.remove(scrapHeap.size() - 1);
        }
    }

    /**
     * Получить количество вью-холдеров указанного типа в пуле.
     */
    public int getRecycledViewCount(final int viewType) {
        return getScrapDataForType(viewType).mScrapHeap.size();
    }

    /**
     * Забрать вью-холдер указанного типа из пула. В случае, если
     * в пуле нет вью-холдера указанного типа, метод вернет null.
     *
     * @param viewType - тип вью
     * @return вью-холдер указанного типа или null
     */
    @Nullable
    public ChildViewHolder getRecycledView(final int viewType) {
        final ScrapData scrapData = mScrap.get(viewType);
        if (scrapData != null && !scrapData.mScrapHeap.isEmpty()) {
            final ArrayList<ChildViewHolder> scrapHeap = scrapData.mScrapHeap;
            return scrapHeap.remove(scrapHeap.size() - 1);
        }
        return null;
    }

    /**
     * Добавить вью-холдер в пул для переиспользования.
     * Если пул полон для указанного типа вью, вью-холдер будет сразу удален.
     *
     * @param scrap - вью-холдер для переиспользования
     */
    public void putRecycledView(@NonNull final ChildViewHolder scrap) {
        final int viewType = scrap.getViewType();
        final ArrayList<ChildViewHolder> scrapHeap = getScrapDataForType(viewType).mScrapHeap;
        if (mScrap.get(viewType).mMaxScrap <= scrapHeap.size()) {
            if (CollectionAdapterEnvironment.LOGGING_ENABLED) {
                CollectionAdapterEnvironment.log("View holder of type " + viewType + " removed because pool " + this + " is full.");
            }
            return;
        }
        if (CollectionAdapterEnvironment.DEBUG && scrapHeap.contains(scrap)) {
            throw new IllegalArgumentException("this scrap item already exists");
        }
        scrapHeap.add(scrap);
    }

    /**
     * Получить {@link ScrapData} для указанного типа вью. Если
     * в {@link #mScrap} нет ScrapData для указанного типа, будет создан
     * новый экземпляр ScrapData.
     *
     * @param viewType - тип вью
     * @return экземпляр ScrapData
     */
    @NonNull
    private ScrapData getScrapDataForType(final int viewType) {
        ScrapData scrapData = mScrap.get(viewType);
        if (scrapData == null) {
            scrapData = new ScrapData();
            mScrap.put(viewType, scrapData);
        }
        return scrapData;
    }

}

