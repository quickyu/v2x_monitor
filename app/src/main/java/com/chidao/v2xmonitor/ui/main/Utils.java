package com.chidao.v2xmonitor.ui.main;

import java.util.Random;

public class Utils {
    static private int colors[] = new int[] {
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5, 0xFF03A9F4, 0xFF00BCD4,
        0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFFFC107, 0xFFFF9800, 0xFFFF5722, 0xFFB71C1C,
        0xFF880E4F, 0xFF4A148C, 0xFF311B92, 0xFF1A237E, 0xFF0D47A1, 0xFF01579B, 0xFF006064,
        0xFF004d40, 0xFF1B5E20, 0xFF33691E, 0xFF827717, 0xFFF57F17, 0xFFFF6F00, 0xFFE65100,
        0xFFBF360C, 0xFF3E2723
    };

    static private int deviceColor[] = new int[] {
        0xffe91e63, 0xff4caf50, 0xffff5722, 0xff9c27b0, 0xffffc107
    };

    static public int getRandomColor() {
        Random rand = new Random();
        return colors[rand.nextInt(colors.length-1)];
    }

    static public int getDeviceColor(int id) {
        if (id < 1)
            id = 1;
        return deviceColor[(id-1)%5];
    }

    public class DataContent {
        public static final int FIX_MODE = 0;
        public static final int UTC_TIME = 1;
        public static final int LONGITUDE = 2;
        public static final int LATITUDE = 3;
        public static final int ALTITUDE = 4;
        public static final int GNSS_SPEED = 5;
        public static final int ENGINE_ROTATING_SPEED = 6;
        public static final int CURRENT_SPEED = 7;
        public static final int ENGINE_TEMP = 8;
        public static final int ENGINE_OIL_TEMP = 9;
        public static final int OIL_PRESSURE = 10;
        public static final int THROTTLE_POSITION = 11;
        public static final int BATTERY_VOLTAGE = 12;
        public static final int PERCENT_LOAD = 13;
        public static final int COOLANT_PRESSURE = 14;
        public static final int TOTAL_DISTANCE = 15;
        public static final int TRIP_DISTANCE = 16;

        public int ueid = 0;
        public String[] content = new String[17];
    }
}
