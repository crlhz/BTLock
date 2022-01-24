package com.example.btlock;

//  1.  Określenie stanu zamku -> otwarty/zamknięty zamek, poprawny/niepoprawny kod, zablokowany/odblokowany zamek.

public class Lock {
    private boolean open;
    private boolean correctCode;
    private boolean blockade;

    //true -> zamek otwarty
    public void setOpen(boolean open){
        this.open = open;
    }

    //true -> poprawny kod
    public void setCorrectCode(boolean correctCode){
        this.correctCode = correctCode;
    }

    //true -> zamek zablokowany
    public void setBlockade(boolean blockade){
        this.blockade = blockade;
    }

    public boolean getOpen(){
        return this.open;
    }

    public boolean getCorrectCode(){
        return this.correctCode;
    }

    public boolean getBlockade(){
        return this.blockade;
    }
}
