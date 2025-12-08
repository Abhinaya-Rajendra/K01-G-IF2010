package com.nimonscooked.model.entities;

// Tambahkan <T> di sini agar fleksibel
public class Inventory<T> {
    private T item;

    public Inventory() {
        this.item = null;
    }

    public void setItem(T item) {
        this.item = item;
    }

    public T takeItem() {
        T temp = this.item;
        this.item = null;
        return temp;
    }

    public T getItem() {
        return item;
    }

    public boolean isEmpty() {
        return item == null;
    }
}