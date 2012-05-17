package com.Tabletop;


public class Table {
    
    int[] table;
    
    //public constructor 
    public Table() {
        table = new int[4]; 
    }
    
    //adds an integer to this table
    public boolean add(int loc, int playerid) {
        
        if(loc < 4 && loc > -1) {
            table[loc] = playerid;
            return true;
        }
        else {
            //System.out.println("bad location");
            return false;
        }
    }
    
    //checks if this table is full - debugging
    public boolean isFull() {
        boolean full = true;
        
        for(int j = 0; j < 4; j ++) {
            if(table[j] == 0) full = false;
        } 
        return full;
    }
    
   
    
    public static void main(String[] args) {
        
        Table t = new Table();
        
        t.add(1, 14);
        t.add(5, 14);
        //if(t.isFull()) System.out.println("its full");
        t.add(3, 24);
        t.add(0,14);
        
        t.add(2,14);
        
        
        
        
        
    }
    
}
