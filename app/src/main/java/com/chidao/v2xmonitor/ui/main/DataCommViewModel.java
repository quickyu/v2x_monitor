package com.chidao.v2xmonitor.ui.main;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.chidao.v2xmonitor.R;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

public class DataCommViewModel extends AndroidViewModel {
    private static final String TAG = "DataCommViewModel";

    private static final int GNSS_DATA_START_INDEX = 8;
    private static final int CAN_DATA_START_INDEX = GNSS_DATA_START_INDEX + 41;

    private static final int PGN_EEC1 = 61444;
    private static final int PGN_CCVS = 65265;
    private static final int PGN_ET1 = 65262;
    private static final int PGN_EFLP1 = 65263;
    private static final int PGN_LFE = 65266;
    private static final int PGN_VEP = 65271;
    private static final int PGN_EEC2 = 61443;
    private static final int PGN_VDHR = 65217;

    public MutableLiveData<ArrayList<DataContent>> mDeviceData;

    private static CommThread mCommThread = null;
    private static boolean threadRunning = true;

    private Timer updateTimer;

    private static ArrayList<DataContent> mContent = null;
    private static Object mLock;

    private static int refCount = 0;

    public DataCommViewModel(Application application) {
        super(application);

        refCount++;

        mDeviceData = new MutableLiveData<ArrayList<DataContent>>();

        if (mContent == null)
            mContent = new ArrayList<DataContent>();

        if (mLock == null)
            mLock = new Object();

        if (mCommThread == null) {
            threadRunning = true;
            mCommThread = new CommThread();
            mCommThread.start();
        }

        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new UpdateTask(), 500, 500);
    }

    @Override
    protected void onCleared() {
        updateTimer.cancel();

        refCount--;
        if (refCount == 0) {
            Log.d(TAG, "ViewModel cleared");

            threadRunning = false;
            try {
                mCommThread.join(100);
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            mCommThread = null;
            mContent = null;
            mLock = null;
        }
    }

    private class UpdateTask extends TimerTask {
        @Override
        public void run() {
            synchronized (mLock) {
                mDeviceData.postValue(mContent);
            }
        }
    }

    private int getUnsigedShortLE(byte[] frame, int offset) {
        ByteBuffer byteBuff = ByteBuffer.wrap(frame, offset, 2);
        byteBuff.order(ByteOrder.LITTLE_ENDIAN);
        return (int)byteBuff.getShort()&0xffff;
    }

    private long getUnsigedIntLE(byte[] frame, int offset) {
        ByteBuffer byteBuff = ByteBuffer.wrap(frame, offset, 4);
        byteBuff.order(ByteOrder.LITTLE_ENDIAN);
        return (long)byteBuff.getInt()&0xffffffff;
    }

    private void protoAnalysis(byte[] buffer, int length, int id) {
        String[] fixModeString = getApplication().getResources().getStringArray(R.array.fix_mode);

        DataContent currentContent = null;

        for (DataContent c : mContent) {
            if (c.ueid == id) {
                currentContent = c;
                break;
            }
        }

        if (currentContent == null)
            return;

        currentContent.setContent(DataContent.FIX_MODE, fixModeString[buffer[GNSS_DATA_START_INDEX]]);

        long utc = (long)(ByteBuffer.wrap(buffer, GNSS_DATA_START_INDEX + 1, 8).getDouble()*1000);
        Date date = new Date(utc);
        currentContent.setContent(DataContent.UTC_TIME, date.toString());

        double latitude = ByteBuffer.wrap(buffer, GNSS_DATA_START_INDEX + 1 + 8, 8).getDouble();
        currentContent.setContent(DataContent.LATITUDE, String.format("%.7f", latitude));

        double longitude = ByteBuffer.wrap(buffer, GNSS_DATA_START_INDEX + 1 + 16, 8).getDouble();
        currentContent.setContent(DataContent.LONGITUDE, String.format("%.7f", longitude));

        double altitude = ByteBuffer.wrap(buffer, GNSS_DATA_START_INDEX + 1 + 24, 8).getDouble();
        currentContent.setContent(DataContent.ALTITUDE, String.format("%.2f m", altitude));

        double gnssSpeed = ByteBuffer.wrap(buffer, GNSS_DATA_START_INDEX + 1 + 32, 8).getDouble(); //meters per second
        currentContent.setContent(DataContent.GNSS_SPEED, String.format("%.2f m/s", gnssSpeed));

        int canFrameNum = (length - CAN_DATA_START_INDEX)/13;
        for (int n = 0; n < canFrameNum; n++) {
            int from = CAN_DATA_START_INDEX + n*13;
            int to = from + 13;
            byte[] canFrame = Arrays.copyOfRange(buffer, from, to);

            int canId = ByteBuffer.wrap(canFrame, 0, 4).getInt();
            int pgn = (canId >> 8) & 0xffff;

            switch (pgn) {
                case PGN_EEC1:
                    double rpm = (double)getUnsigedShortLE(canFrame, 5 + 3) * 0.125;
                    currentContent.setContent(DataContent.ENGINE_ROTATING_SPEED, String.format("%.3f rpm", rpm));
                    break;

                case PGN_CCVS:
                    double speed = (double)getUnsigedShortLE(canFrame, 5 + 1)/256;
                    currentContent.setContent(DataContent.CURRENT_SPEED, String.format("%.2f km/h", speed));
                    break;

                case PGN_ET1:
                    int ect = ((int)canFrame[5]&0xff) - 40;
                    currentContent.setContent(DataContent.ENGINE_TEMP, String.format("%d ℃", ect));

                    double oilTemp = (double)getUnsigedShortLE(canFrame, 5 + 2)*0.03125 - 273;
                    currentContent.setContent(DataContent.ENGINE_OIL_TEMP, String.format("%.2f ℃", oilTemp));

                    break;

                case PGN_EFLP1:
                    int oilPressure = ((int)canFrame[5 + 3]&0xff) * 4;
                    currentContent.setContent(DataContent.OIL_PRESSURE, String.format("%d kpa", oilPressure));

                    int coolantPressure = ((int)canFrame[5 + 6]&0xff) * 2;
                    currentContent.setContent(DataContent.COOLANT_PRESSURE, String.format("%d kpa", coolantPressure));

                    break;

                case PGN_LFE:
                    double tp = (double)((short)canFrame[5 + 6]&0xff) * 0.4;
                    currentContent.setContent(DataContent.THROTTLE_POSITION, String.format("%.1f %%", tp));
                    break;

                case PGN_VEP:
                    double batVolt = (double)getUnsigedShortLE(canFrame, 5 + 6)*0.05;
                    currentContent.setContent(DataContent.BATTERY_VOLTAGE, String.format("%.2f v", batVolt));
                    break;

                case PGN_EEC2:
                    int pcs = (int)canFrame[5 + 2]&0xff;
                    currentContent.setContent(DataContent.PERCENT_LOAD, String.format("%d %%", pcs));
                    break;

                case PGN_VDHR:
                    long totalDist = getUnsigedIntLE(canFrame, 5) * 5 /1000;
                    currentContent.setContent(DataContent.TOTAL_DISTANCE, String.format("%d km", totalDist));

                    long tripDist = getUnsigedIntLE(canFrame, 5 + 4) * 5 /1000;
                    currentContent.setContent(DataContent.TRIP_DISTANCE, String.format("%d km", tripDist));

                    break;

                default: ;
            }
        }
    }

    private class CommThread extends Thread {
        public CommThread() {

        }

        @Override
        public void run() {
            Log.d(TAG, "CommThread running");

            byte[] message = new byte[1000];

            DatagramSocket udpSocket;
            DatagramPacket packet;

            final byte[] protoPattern = {'N', 'T', '1', '0', '0'};

            try {
                udpSocket = new DatagramSocket(12345, InetAddress.getByName("10.1.1.255"));
                packet = new DatagramPacket(message, message.length);
            } catch (Exception e) {
                Log.e(TAG, "error: ", e);
                return;
            }

            while (threadRunning) {
                try {
                    udpSocket.receive(packet);

                    byte[] buffer = packet.getData();
                    byte[] protoIdentifier = Arrays.copyOfRange(buffer, 0, 5);

                    if (!Arrays.equals(protoIdentifier, protoPattern))
                        continue;

                    int ueid = (int)buffer[5];
                    boolean exist = false;

                    for (DataContent c : mContent) {
                        if (c.getId() == ueid) {
                            exist = true;
                            break;
                        }
                    }

                    if (!exist) {
                        synchronized (mLock) {
                            mContent.add(new DataContent(ueid));
                        }
                    }

                    synchronized (mLock) {
                        protoAnalysis(buffer, packet.getLength(), ueid);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "udp receive error: ", e);
                }
            }

            udpSocket.close();
            Log.d(TAG, "CommThread exit");
        }
    }

    public class DataContent {
        public static final int FIX_MODE = 0;
        public static final int UTC_TIME = 1;
        public static final int LATITUDE = 2;
        public static final int LONGITUDE = 3;
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
        public static final int CONTENT_NUM = 17;

        private int ueid = 0;
        private String[] content = new String[CONTENT_NUM];

        public DataContent(int id) {
            ueid = id;
            for (int i = 0; i < content.length; i++)
                content[i] = "n/a";
        }

        public int getId() {
            return ueid;
        }

        public String getContent(int dataNum) {
            return content[dataNum];
        }

        public void setContent(int dataNum, String value) {
            content[dataNum] = value;
        }
    }
}
