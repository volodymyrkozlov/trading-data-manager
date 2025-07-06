package com.volodymyrkozlov.tradingdatamanager.repository;

public class DoubleRingBuffer {
    private final double[] buffer;
    private final int capacity;
    private int position = 0;

    public DoubleRingBuffer(int capacity) {
        this.buffer = new double[capacity];
        this.capacity = capacity;
    }

    public void add(double value) {
        buffer[position % capacity] = value;
        position++;
    }

    public double getByIndex(int index) {
        if (index < position - capacity || index >= position) {
            throw new IndexOutOfBoundsException("Index out of buffer range");
        }
        return buffer[(index % capacity)];
    }

    public int currentIndex() {
        return position - 1;
    }

    public int size() {
        return Math.min(position, capacity);
    }
}
