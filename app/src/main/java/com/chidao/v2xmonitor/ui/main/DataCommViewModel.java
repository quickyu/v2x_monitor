package com.chidao.v2xmonitor.ui.main;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class DataCommViewModel extends ViewModel {
    private static final String TAG = "DataCommViewModel";

    public MutableLiveData<Integer> mNewDevice;
    public MutableLiveData<Utils.DataContent> mDeviceData;

    private CommThread mCommThread;
    private boolean threadRunning = true;

    private Timer updateTimer;

    private ArrayList<DeviceId> mFoundDevices;
    private Object mLock;

    public DataCommViewModel() {
        mNewDevice = new MutableLiveData<Integer>();
        mDeviceData = new  MutableLiveData<Utils.DataContent>();
        mFoundDevices = new ArrayList<DeviceId>();
        mLock = new Object();

        mCommThread = new CommThread();
        mCommThread.start();

        updateTimer = new Timer();
        updateTimer.scheduleAtFixedRate(new UpdateTask(), 500, 500);
    }

    @Override
    protected void onCleared() {
        Log.d(TAG, "ViewModel cleared");
        threadRunning = false;
        updateTimer.cancel();
    }

    private class UpdateTask extends TimerTask {
        @Override
        public void run() {
            synchronized (mLock) {
                for (DeviceId id : mFoundDevices) {
                    if (!id.updated) {
                        mNewDevice.postValue(id.Id);
                        id.updated = true;
                        break;
                    }
                }
            }
        }
    }

    private class CommThread extends Thread {
        public CommThread() {

        }

        @Override
        public void run() {
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

                    for (DeviceId found : mFoundDevices) {
                        if (found.Id == ueid) {
                            exist = true;
                            break;
                        }
                    }

                    if (!exist) {
                        synchronized (mLock) {
                            mFoundDevices.add(new DeviceId(ueid));
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "udp receive error: ", e);
                }
            }

            udpSocket.close();
            Log.d(TAG, "CommThread exit");
        }
    }

    private class DeviceId {
        public int Id;
        public boolean updated;

        public DeviceId(int id) {
            Id = id;
            updated = false;
        }
    }
}
