package com.example.btlock;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;


//  1.  Utworzenie widoku i jego modyfikacja na podstawie parametrów Admin() i Lock().
//  2.  Modyfikacja wywoływana jest zmianą wartości danych odbieranych przez Bluetooth (MutableLiveData())


public class CodeFragment extends Fragment implements View.OnClickListener {

    public static MutableLiveData<String> mCurrentIndex = new MutableLiveData<>();
    private View view;
    private final Lock lock;
    private final MyBluetoothService.ConnectThread connectThread;

    private Button sendCode;
    private ImageView checkMark;
    private ImageView lockMark;
    private ImageView wrongMark;
    private TextView blockade;
    private TextView editCode;

    public CodeFragment(Lock lock, MyBluetoothService.ConnectThread connectThread) {
        this.lock = lock;
        this.connectThread = connectThread;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_code, container, false);

        sendCode = (Button) view.findViewById(R.id.sendCode);
        sendCode.setOnClickListener(this);

        checkMark = (ImageView) view.findViewById(R.id.checkmark);
        lockMark = (ImageView) view.findViewById(R.id.lockmark);
        wrongMark = (ImageView) view.findViewById(R.id.wrongmark);
        blockade = (TextView) view.findViewById(R.id.blockade);
        editCode = (TextView) view.findViewById(R.id.editCode);

        checkMark.setVisibility(View.INVISIBLE);
        lockMark.setVisibility(View.VISIBLE);
        wrongMark.setVisibility(View.INVISIBLE);
        blockade.setVisibility(View.INVISIBLE);

        //modyfikacja wyświetlanych grafik w zależności od parametrów Lock() i Admin()
        //wywoływane wyłącznie w momencie zmiany wartości wysyłanej przez zamek
        mCurrentIndex.observe(getViewLifecycleOwner(), new Observer<String>() {
            public void onChanged(@Nullable final String newValue) {

                if(lock.getOpen()){
                    wrongMark.setVisibility(View.INVISIBLE);
                    lockMark.setVisibility(View.INVISIBLE);
                    checkMark.setVisibility(View.VISIBLE);
                    editCode.setText("");
                }

                if(!lock.getOpen()){
                    wrongMark.setVisibility(View.INVISIBLE);
                    checkMark.setVisibility(View.INVISIBLE);
                    lockMark.setVisibility(View.VISIBLE);
                    blockade.setVisibility(View.INVISIBLE);
                    sendCode.setEnabled(true);
                    editCode.setText("");
                }

                if(!lock.getCorrectCode() && !lock.getBlockade()){
                    checkMark.setVisibility(View.INVISIBLE);
                    lockMark.setVisibility(View.INVISIBLE);
                    wrongMark.setVisibility(View.VISIBLE);
                    editCode.setText("");
                }

                if(lock.getBlockade()){
                    checkMark.setVisibility(View.INVISIBLE);
                    lockMark.setVisibility(View.INVISIBLE);
                    wrongMark.setVisibility(View.INVISIBLE);
                    blockade.setVisibility(View.VISIBLE);
                    sendCode.setEnabled(false);
                    editCode.setText("");
                }
            }
        });

        return view;
    }

    //obsługa przycisku "OK"
    @Override
    public void onClick(View v) {
        TextView editCode = (TextView) view.findViewById(R.id.editCode);
        String code = String.valueOf(editCode.getText());
        if (code.length() == 4) {
            connectThread.write(code);
        } else {
            Snackbar.make(v, "Kod musi mieć minimum 8 znaków", Snackbar.LENGTH_LONG).show();
        }
    }
}