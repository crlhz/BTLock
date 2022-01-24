package com.example.btlock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import java.util.Set;


//  1.  Odnalezienie na liście sparowanych urządzeń zamka elektronicznego, przypisanie go
//      do obiektu correctDevice i przekazanie go jako parametr do wątku ConnectThread()
//      obsługującego połączenie się z urządzeniem.
//  2.  Utworzenie instancji obiektów Admin() i Lock() oraz przekazanie ich jako parametry
//      klasy MyBluetoothService.
//  3.  Utworzenie układu graficznego Page Viewer.


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //sprawdzenie czy bluetooth jest włączony
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //pobranie listy sparowanych urządzeń i przypisanie do correctDevice tego o nazwie "rpi" (zamek)
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        BluetoothDevice correctDevice = null;
        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d("BTapp", deviceName + " " + deviceHardwareAddress);
                if (deviceName.equals("rpi")) {
                    correctDevice = device;
                }
            }
        }

        //jeśli zamka nie ma w pobliżu lub nie został sparowany -> komunikat
        //jeśli zamek jest sparowany i jest dostępny -> utworzenie niezbędnych instancji i widoku
        if(correctDevice == null){
            Snackbar.make(findViewById(android.R.id.content), "Zamek nie jest sparowany lub jesteś zbyt daleko", Snackbar.LENGTH_LONG).show();
        }else{
            Admin admin = new Admin();
            Lock lock = new Lock();
            MyBluetoothService myBluetoothService = new MyBluetoothService(lock, admin);
            MyBluetoothService.ConnectThread connectThread = myBluetoothService.new ConnectThread(correctDevice);
            connectThread.run(bluetoothAdapter);
            TabLayout tabLayout = findViewById(R.id.tabBar);
            ViewPager viewPager = findViewById(R.id.viewPager);

            //obsługa Page Viewera
            PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager(),tabLayout.getTabCount(), lock, connectThread, admin);
            viewPager.setAdapter(pagerAdapter);

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) { }

                @Override
                public void onTabReselected(TabLayout.Tab tab) { }
            });

            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        }
    }
}