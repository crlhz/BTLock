package com.example.btlock;


//  1.  Określenie dostępu do trybu serwisowego


public class Admin {

    private boolean access;

    //true -> dostęp do trybu serwisowego
    public void setAccess(boolean access){
        this.access = access;
    }

    public boolean getAccess(){
        return this.access;
    }

}

