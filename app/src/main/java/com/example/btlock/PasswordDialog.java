package com.example.btlock;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


//  1.  Widok okna dialogowego umożliwiającego wpisanie hasła.
//  2.  Przekazanie hasła do SettingsFragment()


public class PasswordDialog extends DialogFragment {

    private EditText password;
    private TextView mActionOk, mActionCancel;

    public interface OnInputSelected{
        void sendInput(String input);
    }

    public OnInputSelected mOnInputSelected;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_dialog,container,false);

        mActionOk = view.findViewById(R.id.action_ok);
        mActionCancel = view.findViewById(R.id.action_cancel);
        password = view.findViewById(R.id.editPassword);

        mActionCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        mActionOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String input = password.getText().toString();
                if(!input.equals("")){
                    mOnInputSelected.sendInput(input);
                    getDialog().dismiss();
                }
            }
        });
        return view;

    }

    @Override
    public void onAttach(Context context){
        super.onAttach(context);
        try{
            mOnInputSelected = (OnInputSelected) getTargetFragment();
        } catch(ClassCastException e){
            Log.e("Error","onAttach error");
        }
    }
}
