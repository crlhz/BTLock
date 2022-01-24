package com.example.btlock;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


//  1.  ConnectThread() -> połączenie z wybranym w MainActivity() urządzeniem.
//  2.  ConnectedThread() -> obsługa odbierania i wysyłania danych oraz ich interpretacja.
//  3.  Przekazywanie danych do UI za pomocą MutableLiveData.


public class MyBluetoothService {
    private static final String TAG = "Debug";

    //zdefiniowanie komunikatów wysyłanych przez zamek
    private static final String OPENED = "0";
    public static final String CLOSED = "1";
    public static final String WRONG = "2";
    public static final String BLOCKED = "3";
    public static final String LOGGED = "4";
    public static final String LOGGED_OUT = "5";

    private final Lock lock;
    private final Admin admin;

    public MyBluetoothService(Lock lock, Admin admin){
        this.lock = lock;
        this.admin = admin;
    }

    //wątek wykorzystywany do połaczenia się z urządzeniem
    public class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        //wspólne ID dla aplikacji i zamka
        private UUID MY_UUID = UUID.fromString("2c4ab349-4b88-4753-9dfd-7bb4b8607530");
        private static final String TAG = "Debug";

        public ConnectThread(BluetoothDevice device) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            //stworzenie socketu dla aplikacji
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run(BluetoothAdapter bluetoothAdapter) {
            // Wyłączenie rozgłaszania, w celu przyspieszenia komunikacji
            bluetoothAdapter.cancelDiscovery();

            try {
                //Połączenie przez socket utworzony powyżej
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            ConnectedThread connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();
        }

        public void write(String message){
            ConnectedThread connectedThread = new ConnectedThread(mmSocket);
            connectedThread.write(message.getBytes(StandardCharsets.UTF_8));
        }

    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private byte[] mmBuffer; // mmBuffer dla odbieranych danych

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating input stream", e);
            }
            try {
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when creating output stream", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            mmBuffer = new byte[1];
            String publicBuffer;

            // Ciagłe nasłuchiwanie do momentu wystąpienia wyjątki
            while (true) {
                try {
                    mmInStream.read(mmBuffer);
                    publicBuffer = new String(mmBuffer, StandardCharsets.UTF_8);

                    //ustawienie Lock() i Admin() na podstawie przychodzących danych
                    switch (publicBuffer){
                        case OPENED:
                            lock.setOpen(false);
                            lock.setCorrectCode(true);
                            lock.setBlockade(false);
                            break;
                        case CLOSED:
                            lock.setOpen(true);
                            lock.setCorrectCode(true);
                            break;
                        case WRONG:
                            lock.setCorrectCode(false);
                            break;
                        case BLOCKED:
                            lock.setCorrectCode(false);
                            lock.setBlockade(true);
                            break;
                        case LOGGED:
                            admin.setAccess(true);
                            break;
                        case LOGGED_OUT:
                            admin.setAccess(false);
                            break;
                    }
                    //przesłanie otrzymanej wartości do UI
                    CodeFragment.mCurrentIndex.postValue(publicBuffer);
                } catch (IOException e) {
                    Log.d(TAG, "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }
    }
}
