package com.chidao.v2xmonitor.ui.main;

import java.util.Random;

public class Utils {
    static private int colors[]  = new int[]{
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7, 0xFF3F51B5, 0xFF03A9F4, 0xFF00BCD4,
        0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFFFC107, 0xFFFF9800, 0xFFFF5722, 0xFFB71C1C,
        0xFF880E4F, 0xFF4A148C, 0xFF311B92, 0xFF1A237E, 0xFF0D47A1, 0xFF01579B, 0xFF006064,
        0xFF004d40, 0xFF1B5E20, 0xFF33691E, 0xFF827717, 0xFFF57F17, 0xFFFF6F00, 0xFFE65100,
        0xFFBF360C, 0xFF3E2723
    };

    static public int getBackgroundColor() {
        Random rand = new Random();
        return colors[rand.nextInt(colors.length-1)];
    }
}
