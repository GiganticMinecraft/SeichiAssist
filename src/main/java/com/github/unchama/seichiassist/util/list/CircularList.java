package com.github.unchama.seichiassist.util.list;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by karayuu on 2019/05/06
 */
public class CircularList<E> extends ArrayList<E> {
    public CircularList(Collection<? extends E> collection) {
        super(collection);
    }

    @Override
    public E get(int index) {
        return super.get(index % size());
    }
}
