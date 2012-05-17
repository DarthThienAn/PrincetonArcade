package your.packname;


public class Table {
	
	int[] table;
	
	public Table() {
		
		table = new int[4];	
	}
	
	
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
	
	public boolean isFull() {
		boolean full = true;
		
		for(int j = 0; j < 4; j ++) {
			if(table[j] == 0) full = false;
		}	
		return full;
	}
	
	public boolean isKickable(int playerid) {
		
		int i = 0;
		
		for(int j = 0; j < 4; j ++) {
			if(table[j] == playerid) i++;
		}
		return i == 3 && isFull();	
	}
	
	public void kick(int playerid) {
		
		for(int i = 0; i < 4; i++) {
			if(table[i] != playerid) table[i] = 0;
		}
	}
	
	public static void main(String[] args) {
		
		Table t = new Table();
		
		t.add(1, 14);
		t.add(5, 14);
		//if(t.isFull()) System.out.println("its full");
		t.add(3, 24);
		t.add(0,14);
		t.kick(14);
		t.add(2,14);
		t.kick(14);
		
		
		
		
	}

}
