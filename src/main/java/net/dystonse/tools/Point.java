package net.dystonse.tools;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Point { 

    double latitude;
    double longitude;

    public Point() {

    }

    public Point(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Point(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        byte byteOrderByte = buffer.get(0);
        buffer.order(byteOrderByte == 1 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN);
        this.longitude = buffer.getDouble(9);
        this.latitude = buffer.getDouble(17);
    }
}