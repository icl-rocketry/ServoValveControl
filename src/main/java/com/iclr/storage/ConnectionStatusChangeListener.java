package com.iclr.storage;

public interface ConnectionStatusChangeListener<T extends ServoController> {
    public void onStatusChange(T controller);
}
