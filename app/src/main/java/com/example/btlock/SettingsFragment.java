package com.example.btlock;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


//  1.  Utworzenie widoku i jego modyfikacja na podstawie parametrów Admin() i Lock()
//  2.  Modyfikacja wywoływana jest zmianą wartości danych odbieranych przez Bluetooth (MutableLiveData())
//  3.  Otwieranie okna dialogowego umożliwiającego wpisanie hasła serwisowego


public class SettingsFragment extends Fragment implements View.OnClickListener, PasswordDialog.OnInputSelected {

    private View view;
    private final Lock lock;
    private final Admin admin;
    private final MyBluetoothService.ConnectThread connectThread;

    private TextView descNewCode;
    private TextView descNewAttempts;
    private TextView message;
    private EditText newCode;
    private EditText newAttempt;
    private Button changeButton;
    private Button loginButton;


    public SettingsFragment(Lock lock, MyBluetoothService.ConnectThread connectThread, Admin admin) {
        this.lock = lock;
        this.connectThread = connectThread;
        this.admin = admin;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_settings, container, false);
        descNewCode =  view.findViewById(R.id.descNewCode);
        descNewAttempts =  view.findViewById(R.id.descNewAttempts);
        newCode =  view.findViewById(R.id.newCode);
        newAttempt =  view.findViewById(R.id.newAttempt);
        changeButton =  view.findViewById(R.id.changeButton);
        loginButton =   view.findViewById(R.id.loginButton);
        message = view.findViewById(R.id.message);

        message.setVisibility(View.INVISIBLE);
        descNewCode.setVisibility(View.INVISIBLE);
        descNewAttempts.setVisibility(View.INVISIBLE);
        newCode.setVisibility(View.INVISIBLE);
        newAttempt.setVisibility(View.INVISIBLE);
        changeButton.setVisibility(View.INVISIBLE);

        loginButton.setOnClickListener(this);
        changeButton.setOnClickListener(this);

        //modyfikacja wyświetlanych grafik w zależności od parametrów Admin()
        //wywoływane wyłącznie w momencie zmiany wartości wysyłanej przez zamek
        CodeFragment.mCurrentIndex.observe(getViewLifecycleOwner(), new Observer<String>() {
            public void onChanged(@Nullable final String newValue) {
                if(admin.getAccess()){
                    descNewCode.setVisibility(View.VISIBLE);
                    descNewAttempts.setVisibility(View.VISIBLE);
                    newCode.setVisibility(View.VISIBLE);
                    newAttempt.setVisibility(View.VISIBLE);
                    changeButton.setVisibility(View.VISIBLE);
                    loginButton.setVisibility(View.INVISIBLE);
                    message.setVisibility(View.INVISIBLE);
                }else{
                    descNewCode.setVisibility(View.INVISIBLE);
                    descNewAttempts.setVisibility(View.INVISIBLE);
                    newCode.setVisibility(View.INVISIBLE);
                    newAttempt.setVisibility(View.INVISIBLE);
                    changeButton.setVisibility(View.INVISIBLE);
                    loginButton.setVisibility(View.VISIBLE);
                }
            }
        });
        return view;
    }

    //obsługa przycisków "Zaloguj się" i "OK"
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.loginButton:
                openDialog();
                break;
            case R.id.changeButton:
                String newCodes = String.valueOf(newCode.getText());
                String newAttempts = String.valueOf(newAttempt.getText());

                if(newCodes.length()==4 && Integer.parseInt(newAttempts) > 0 && Integer.parseInt(newAttempts) < 7){
                    message.setVisibility(View.INVISIBLE);
                    connectThread.write(newCodes + ";" + newAttempts);
                    admin.setAccess(false);
                    newCode.setText("");
                    newAttempt.setText("");
                    message.setText("Poprawnie ustawiono nowe wartości");
                    message.setVisibility(View.VISIBLE);
                }else{
                    message.setText("Błędny kod lub ilość prób");
                    message.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    //otwarcie okna dialogowego, umożliwiającego wprowadzenie hasła serwisowego
    public void openDialog(){
        PasswordDialog passwordDialog = new PasswordDialog();
        passwordDialog.setTargetFragment(SettingsFragment.this,1);
        passwordDialog.show(getFragmentManager(),"Login");

    }

    @Override
    public void sendInput(String input) {
        connectThread.write(input);
    }
}