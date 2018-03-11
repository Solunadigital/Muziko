package com.muziko.adapters;

import com.sababado.circularview.Marker;
import com.sababado.circularview.SimpleCircularViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bogdan Melnychuk
 */
public abstract class CircularNavigationAdapter<E> extends SimpleCircularViewAdapter {
    public static final int DEFAULT_RADIUS = 50;

    private List<E> objects;

    public CircularNavigationAdapter() {
        this(new ArrayList<>());
    }

    public CircularNavigationAdapter(List<E> objects) {
        this.objects = objects;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public void setupMarker(int i, Marker marker) {
        marker.setFitToCircle(false);
        marker.setRadius(DEFAULT_RADIUS);
    }

    public E getItemAt(int position) {
        return objects.get(position);
    }
}
