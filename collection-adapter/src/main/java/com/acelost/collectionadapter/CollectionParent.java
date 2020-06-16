package com.acelost.collectionadapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

/**
 * Интерфейс вью, содержащего коллекцию элементов. Используется для вью, предполагающих отображение
 * коллекции элементов (как однотипных, так и разнотипных). Позволяет быстро и гибко реализовывать подобные
 * вью. Воркфлоу: реализовать интерфейс {@link CollectionParent} полем наследника {@link ViewGroup};
 * а так же реализовать {@link CollectionAdapter} для элементов коллекции.
 * Сущности {@link CollectionAdapter} и {@link CollectionParent} должны быть связаны
 * методом {@link CollectionAdapter#attachToParent(CollectionParent)}.
 */
public interface CollectionParent {

    /**
     * Значение, сигнализирующее о неуказанной позиции вью-холдера.
     */
    int NO_POSITION = -1;

    /**
     * Получить контекст для создания дочерних элементов.
     */
    @NonNull
    Context getContext();

    /**
     * Получить родительскую {@link View}.
     */
    @NonNull
    ViewGroup getView();

    /**
     * Добавить дочернюю вью в родительскую на указанную позицию. Предполагаемая реализация - вызов
     * метода {@link ViewGroup#addViewInLayout(View, int, ViewGroup.LayoutParams, boolean)}}
     * с указанием параметра preventRequestLayout = true и необходимых layout params.
     *
     * @param view      - дочерняя вью, которую необходимо добавить
     * @param position  - позиция, на которую необходимо добавить вью
     */
    void addItemInLayout(@NonNull View view, int position);

    /**
     * Удалить дочернюю вью из родительской. Предполагаемая реализация - прямой роутинг на метод
     * {@link ViewGroup#removeViewInLayout(View)}. Если интерфейс {@link CollectionParent}
     * имплементирует {@link ViewGroup} - переопределять этот метод не нужно.
     *
     * @param view - дочерняя вью, которую необходимо удалить
     */
    void removeViewInLayout(@NonNull View view);

    /**
     * Удалить <code>count</code> дочерних вью из родительской, начиная с позиции <code>start</code>.
     * Предполагаемая реализация - прямой роутинг на метод {@link ViewGroup#removeViewsInLayout(int, int)}.
     * Если интерфейс {@link CollectionParent} имплементируется {@link ViewGroup} - переопределять этот метод не нужно.
     *
     * @param start     - позиция, начиная с которой необходимо удалять дочерние вью
     * @param count     - количество дочерних вью, которые необходимо удалить
     */
    void removeViewsInLayout(int start, int count);

    /**
     * Получить количество дочерних вью. Предполагаемая реализация - прямой
     * роутинг на метод {@link ViewGroup#getChildCount()}. Если интерфейс {@link CollectionParent}
     * имплементируется {@link ViewGroup} - переопределять этот метод не нужно.
     *
     * @return количество дочерних вью
     */
    int getChildCount();

    /**
     * Вызвать перерассчет макета. Предполагаемая реализация - прямой роутинг на метод
     * {@link View#requestLayout()}. Если интерфейс {@link CollectionParent} имплементируется
     * {@link View} - переопределять этот метод не нужно.
     */
    void requestLayout();

}