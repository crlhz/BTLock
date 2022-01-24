package com.example.btlock;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;


//  1.  Obsługa UI. Utworzenie fragmentów w zależności od zakładki na której aktualnie się znajdujemy.
//  2.  Przekazanie instancji utworzonych w MainActivity() do fragmentów


public class PagerAdapter extends FragmentPagerAdapter {

    private final int numOfTabs;
    private final Lock lock;
    private final Admin admin;
    private final MyBluetoothService.ConnectThread connectThread;

    public PagerAdapter(FragmentManager fm, int numOfTabs, Lock lock, MyBluetoothService.ConnectThread connectThread, Admin admin){
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.numOfTabs = numOfTabs;
        this.lock = lock;
        this.connectThread = connectThread;
        this.admin = admin;
    }

    //tworzenie fragmentów na podstawie otwartej zakładki w Page Viewer
    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                return new CodeFragment(lock, connectThread);
            case 1:
                return new SettingsFragment(lock, connectThread, admin);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}
