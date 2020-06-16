package com.acelost.collectionadapter;

import android.util.SparseArray;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Адаптер для дочерних вью внутри {@link CollectionParent}. Отвечает за создание
 * дочерних вью, их жизненный цикл, привязку данных и переиспользование. Адаптер
 * управляет не обязательно всеми дочерними вью. Если реализация {@link CollectionParent}
 * предполагает наличие одиночных элементов (например, заголовок в начале или счетчик в конце),
 * диапазон дочерних вью, которыми управляет адаптер, может быть задан при помощи методов
 * {@link #getChildStartOffset()} и {@link #getChildEndOffset()}.
 * Каркас логики работы адаптера заимствован из android.support.v7.widget.RecyclerView.
 *
 * @param <VH> - тип вью-холдера для дочерних вью
 */
public abstract class CollectionAdapter<VH extends ChildViewHolder> {

    /**
     * Количество дочерних вью, которые могут быть спрятаты в родительском вью без удаления из него по умолчанию.
     */
    private static final int DEFAULT_STASH_SIZE = 3;

    /**
     * Коллекция вью-холдеров для дочерних вью, которые сейчас находятся в родительском вью.
     * Ключ - позиция вью среди дочерних элементов коллекции (НЕ позиция внутри {@link CollectionParent}).
     */
    @NonNull
    private final SparseArray<VH> mViewHolders = new SparseArray<>();

    /**
     * Пул для переиспользования дочерних вью.
     */
    @Nullable
    private CollectionViewHolderPool mRecycledPool;

    /**
     * Родительская вью, дочерними вью которого управляет адаптер.
     */
    @Nullable
    private CollectionParent mParent;

    /**
     * Получить количество необходимых дочерних вью. Это количество может не совпадать
     * с количеством дочерних вью в {@link CollectionParent}, т.к. реализация {@link CollectionParent}
     * может содержать как дочерние элементы коллекции, так и отдельные дочерние вью
     * (например, заголовок, футер или счетчик).
     *
     * @return количество дочерних вью, которые потребуется отобразить
     */
    public abstract int getItemCount();

    /**
     * Получить тип для дочерней вью на указанной позиции.
     * По умолчанию все дочерние вью имеют одинаковый тип.
     *
     * @param position - позиция дочерней вью
     * @return число, идентифицирующее тип вью
     */
    protected int getItemViewType(final int position) {
        return 0;
    }

    /**
     * Получить смещение коллекции дочерних вью внутри родительской от начала {@link CollectionParent}.
     * Переопределите этот метод, если в родительском вью
     * есть одиночные дочерние вью перед коллекцией (например, заголовок).
     *
     * @return количество дочерних вью в родительской ДО дочерних вью коллекции
     */
    protected int getChildStartOffset() {
        return 0;
    }

    /**
     * Получить смещение коллекции дочерних вью внутри родительской от конца {@link CollectionParent}.
     * Переопределите этот метод, если в родительском вью
     * есть одиночные дочерние вью после коллекции (например, счетчик "еще").
     *
     * @return количество дочерних вью в родительской ПОСЛЕ дочерних вью коллекции
     */
    protected int getChildEndOffset() {
        return 0;
    }

    // region Attach/Detach logic

    /**
     * Присоединить экземпляр {@link CollectionParent} к адаптеру.
     *
     * @param parent - экземпляр {@link CollectionParent}
     */
    @MainThread
    @CallSuper
    public void attachToParent(@NonNull final CollectionParent parent) {
        if (mParent == parent) {
            return;
        }
        mParent = parent;
        notifyDataChanged();
    }

    /**
     * Отсоединить адаптер от {@link CollectionParent}.
     */
    @MainThread
    @CallSuper
    public void detachFromParent() {
        if (mParent == null) {
            return;
        }
        // Отправляем вью-холдеры на переиспользование
        final int size = mViewHolders.size();
        for (int i = 0; i < size; ++i) {
            final VH holder = mViewHolders.valueAt(i);
            if (holder != null) {
                recycleViewHolder(holder);
            }
        }
        // Удаляем все вью из родительского вью
        mViewHolders.clear();
        mParent.removeViewsInLayout(getChildStartOffset(), mParent.getChildCount() - getChildEndOffset());
        mParent = null;
    }

    // endregion

    /**
     * Уведомить адаптер об изменении данных для перепривязки дочерних вью.
     * Данный метод выполняет досоздание/удаление/скрытие/переиспользование
     * дочерних вью коллекции и перепривязывает к ним данные, после чего
     * вызывает {@link CollectionParent#requestLayout()} для перестроения макета.
     */
    @CallSuper
    public void notifyDataChanged() {
        final CollectionParent parent = mParent;
        if (parent == null) {
            return;
        }
        final int count = getItemCount();
        final int childOffset = getChildStartOffset();
        // Складываем в стеш лишние вью-холдеры
        final int stashSize = getStashSize();
        for (int i = 0; i < stashSize; ++i) {
            final VH holder = getViewHolder(count + i);
            if (holder != null) {
                stashViewHolder(holder);
            }
        }
        // Отправляем не поместившиеся в стеш вью-холдеры на переиспользование
        final int start = count + stashSize;
        final int end = parent.getChildCount() - getChildEndOffset() - childOffset;
        if (start < end) {
            for (int i = start; i < end; ++i) {
                final VH holder = getViewHolder(i);
                if (holder != null) {
                    recycleViewHolder(holder);
                    mViewHolders.delete(i);
                }
            }
            parent.removeViewsInLayout(start + getChildStartOffset(), end - start);
        }
        // Привязываем коллекцию данных к вью-холдерам
        for (int i = 0; i < count; ++i) {
            final VH holder = getViewHolderForPosition(parent, i, childOffset);
            if (holder == null) {
                throw new IllegalStateException("View holder for " + i + " position is null.");
            }
            bindViewHolder(holder, i);
        }
        parent.requestLayout();
    }

    // region Holder relevant methods

    /**
     * Получить вью холдер для указанной позиции в коллекции.
     *
     * @param position - позиция в коллекции
     * @return экземпляр вью-холдера или null, если вью-холдер
     * для указанной позиции не найден
     */
    @Nullable
    public final VH getViewHolder(final int position) {
        return mViewHolders.get(position);
    }

    /**
     * Получить правильный вью-холдер для указанной позиции. В случае, если вью-холдер на
     * указанной позции подходит по типу, он будет переиспользован, иначе старый вью-холлдер
     * будет отдан в пул для переиспользования, а новый будет получен из {@link #mRecycledPool},
     * либо создан методом {@link #onCreateViewHolder(CollectionParent, int)}.
     *
     * @param parent        - родительская вью
     * @param position      - позиция, для которой необходимо получить вью-холдер
     * @param childOffset   - смещение дочерних вью коллекции внутри родительской вью
     * @return подготовленный к использованию экземпляр вью-холдера правильного типа
     */
    @Nullable
    private VH getViewHolderForPosition(@NonNull final CollectionParent parent,
                                        final int position, final int childOffset) {
        // Получаем тип элемента
        final int type = getItemViewType(position);
        // Получаем вью-холдер, который на данный момент на указанной позиции
        VH holder = getViewHolder(position);
        boolean useExists = false;
        if (holder != null) {
            if (holder.getViewType() == type) {
                // Тип вью-холдера совпадает с типом элемента
                useExists = true;
                prepareViewHolder(parent, holder);
            } else {
                // Вью-холдер не подходит по типу
                recycleViewHolder(holder);
                parent.removeViewInLayout(holder.view);
            }
        }
        if (!useExists) {
            // Создаем новый вью-холдер и добавляем его в родительский вью
            holder = createViewHolder(parent, type);
            parent.addItemInLayout(holder.view, position + childOffset);
            prepareViewHolder(parent, holder);
            mViewHolders.put(position, holder);
        }
        return holder;
    }

    /**
     * Создать вью-холдер указанного типа. Если в {@link #mRecycledPool}
     * есть подходящий вью-холдер, он будет переиспользован. Иначе будет создан
     * новый методом {@link #onCreateViewHolder(CollectionParent, int)}.
     *
     * @param parent    - родительская вью
     * @param viewType  - тип вью
     * @return экземпляр вью-холдера указанного типа
     */
    @NonNull
    private VH createViewHolder(@NonNull final CollectionParent parent, final int viewType) {
        final ChildViewHolder recycled = getRecycledViewPool().getRecycledView(viewType);
        if (recycled != null) {
            if (CollectionAdapterEnvironment.LOGGING_ENABLED) {
                CollectionAdapterEnvironment.log("View holder of type " + viewType + " taken from pool " + getRecycledViewPool() + ".");
            }
            //noinspection unchecked
            return (VH) recycled;
        }
        if (CollectionAdapterEnvironment.LOGGING_ENABLED) {
            CollectionAdapterEnvironment.log("View holder of type " + viewType + " created by adapter.");
        }
        final VH holder = onCreateViewHolder(parent, viewType);
        holder.setViewType(viewType);
        return holder;
    }

    /**
     * Создать новый экземпляр вью-холерда указанного типа.
     *
     * @param parent    - родительская вью
     * @param viewType  - тип вью
     * @return экземпляр вью-холдера
     */
    @NonNull
    protected abstract VH onCreateViewHolder(@NonNull CollectionParent parent, int viewType);

    /**
     * Привязать данные к вью холдеру на указанной позиции.
     *
     * @param holder    - вью-холдер
     * @param position  - позиция вью-холдера в коллекции
     */
    private void bindViewHolder(@NonNull final VH holder, final int position) {
        onBindViewHolder(holder, position);
        holder.setAdapterPosition(position);
    }

    /**
     * Привязать данные к вью холдеру на указанной позиции.
     *
     * @param holder    - вью-холдер
     * @param position  - позиция вью-холдера в коллекции
     */
    protected abstract void onBindViewHolder(@NonNull final VH holder, final int position);

    /**
     * Подготовить вью-холдер к использованию.
     *
     * @param parent    - родительская вью
     * @param holder    - вью-холдер дочерней вью
     */
    private void prepareViewHolder(@NonNull final CollectionParent parent, @NonNull final VH holder) {
        holder.prepare();
        onPrepareViewHolder(parent, holder);
    }

    /**
     * Подготовить вью-холдер к использованию.
     *
     * @param parent    - родительская вью
     * @param holder    - вью-холдер дочерней вью
     */
    protected void onPrepareViewHolder(@NonNull final CollectionParent parent,
                                       @NonNull final VH holder) {
        // do nothing
    }

    /**
     * Освободить ресурсы, захваченные вью-холдером
     * и поместить в пул для переиспользования.
     *
     * @param holder - вью-холдер
     */
    private void recycleViewHolder(@NonNull final VH holder) {
        onRecycleViewHolder(holder);
        holder.onRecycle();
        holder.setAdapterPosition(CollectionParent.NO_POSITION);
        getRecycledViewPool().putRecycledView(holder);
    }

    /**
     * Обработать событие передачи вью-холдера на переиспользование.
     *
     * @param holder - вью-холдер для переиспользования
     */
    protected void onRecycleViewHolder(@NonNull final VH holder) {
        // do nothing
    }

    /**
     * Скрыть вью, которое содержит указанный вью-холдер, без удаления из макета.
     *
     * @param holder - вью-холдер, который необходимо скрыть
     */
    private void stashViewHolder(@NonNull final VH holder) {
        holder.stash();
    }

    // endregion

    // region Recycling related methods

    /**
     * Получить максимальное количество дочерних вью, которые
     * могут быть скрыты без удаления из макета.
     */
    protected int getStashSize() {
        return DEFAULT_STASH_SIZE;
    }

    /**
     * Получить пул для переиспользованных вью-холдеров.
     */
    @NonNull
    public CollectionViewHolderPool getRecycledViewPool() {
        if (mRecycledPool == null) {
            mRecycledPool = new CollectionViewHolderPool();
        }
        return mRecycledPool;
    }

    /**
     * Задать пул для переиспользованных вью-холдеров.
     */
    public void setRecycledViewPool(@Nullable final CollectionViewHolderPool pool) {
        mRecycledPool = pool;
    }

    // endregion

}
