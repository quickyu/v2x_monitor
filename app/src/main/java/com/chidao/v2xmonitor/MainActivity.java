package com.chidao.v2xmonitor;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.chidao.v2xmonitor.ui.main.DataCommViewModel;
import com.chidao.v2xmonitor.ui.main.MainFragment;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    private final static String NETWORK_PREFIX = "10.1.1";

    private TextView mNetworkNotExistText;
    private TextView mGotoSetting;

    private BluetoothGatt mBtGatt;
    private Timer mRssiTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mNetworkNotExistText = findViewById(R.id.network_not_exist_text);
        mGotoSetting = findViewById(R.id.setting_text);

        // Get the default bluetoothAdapter to store bonded devices into a Set of BluetoothDevice(s)
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // It will work if your bluetooth device is already bounded to your phone
        // If not, you can use the startDiscovery() method and connect to your device
        Set<BluetoothDevice> bluetoothDeviceSet = bluetoothAdapter.getBondedDevices();

        for (BluetoothDevice bluetoothDevice : bluetoothDeviceSet) {
            String name = bluetoothDevice.getName();
            if (name.equals("DT100")) {
                mBtGatt = bluetoothDevice.connectGatt(this, true, new BluetoothGattCallback() {
                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        super.onConnectionStateChange(gatt, status, newState);
                    }

                    @Override
                    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            Log.d(TAG, String.format("BluetoothGat ReadRssi[%d]", rssi));
                        }
                    }
                });
            }
        }

        mRssiTimer = new Timer();
        mRssiTimer.scheduleAtFixedRate( new TimerTask() {
            @Override
            public void run() {
                if (mBtGatt != null)
                    mBtGatt.readRemoteRssi();
            }}, 1000, 1000);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!IsAreaNetworkExist()) {
            closeFragment();
            mNetworkNotExistText.setVisibility(VISIBLE);
            mGotoSetting.setVisibility(VISIBLE);

            mGotoSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNetworkNotExistText.setVisibility(GONE);
                    mGotoSetting.setVisibility(GONE);

                    Intent intentOpenBluetoothSettings = new Intent();
                    intentOpenBluetoothSettings.setAction(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                    startActivity(intentOpenBluetoothSettings);
                }
            });

        } else {
            displayFragment();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private boolean IsAreaNetworkExist() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();

                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress address = inetAddresses.nextElement();
                    if (address.getHostAddress().contains(NETWORK_PREFIX))
                        return true;
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return false;
    }

    private void displayFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        MainFragment mainFragment = (MainFragment) fragmentManager
                .findFragmentById(R.id.container);

        if (mainFragment == null) {
            mainFragment = MainFragment.newInstance();
            FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fragmentTransaction.add(R.id.container,
                    mainFragment).commit();
        }
    }

    private void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        MainFragment mainFragment = (MainFragment) fragmentManager
                .findFragmentById(R.id.container);

        if (mainFragment != null) {
            // Create and commit the transaction to remove the fragment.
            FragmentTransaction fragmentTransaction =
                    fragmentManager.beginTransaction();
            fragmentTransaction.remove(mainFragment).commit();
        }
    }
}
