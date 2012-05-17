package your.packname;

public class Board {
 
 Table[] tables;
 
 
 public Board() {
  tables = new Table[4];
  for(int i = 0; i < 4; i ++) {
   tables[i] = new Table();
  }
 }
 
 public String string() {
  
  String s = "";
  
  for(int i = 0; i < 4; i++) {
   for(int j = 0; j < 4; j++) {
    s += tables[i].table[j];
   } 
  }
  return s;
 }
 
 //make swap every other turn
 
 //decide convention for translating screen to indexes
 //0-15  pos/4 is table, pos%4 is position on table
 public boolean properAdd(int pos) {
  
  if(pos < 0 || pos > 15) {
   System.out.println("improper position");
   return false;
  }
  if(tables[pos/4].table[pos%4] == 0) return true;
  else {
   System.out.println("spot taken");
   return false;
  }
 }
 
 public void add(int pos, int playerid) {
  tables[pos/4].table[pos%4] = playerid;
 }
 
 public boolean isUsefullSwap(int pos1, int pos2){
  
  if(tables[pos1/4].table[pos1%4] != 0 
    && tables[pos2/4].table[pos2%4] != 0 
    && tables[pos1/4].table[pos1%4] != tables[pos2/4].table[pos2%4])
   return true;
  else return false;
 }
 
 public void swap(int pos1, int pos2) {
  int a = tables[pos1/4].table[pos1%4];
  tables[pos1/4].table[pos1%4] = tables[pos2/4].table[pos2%4];
  tables[pos2/4].table[pos2%4] = a;
 }
 
 public boolean isKickable(int pos, int playerid) {
  
  if(pos > 15 || pos < 0) {
   System.out.println("bad position");
   return false;
  }
  
  if(!tables[pos/4].isKickable(playerid)) {
   //System.out.println("not kickable");
   return false;
  }
  return true;
 }
 
 public boolean isBoardKickable(int playerid) {
  boolean kickable = false;
  for(int i = 0; i < 16; i+=4) {
   kickable = kickable || isKickable(i, playerid);
  }
  return kickable;
 }
 
 public boolean isBoardSwapable() {
  int a = 0;
  int b = 0;
  
  for(int i = 0; i < 4; i++) {
   for(int j = 0; j < 4; j++) {
    if(a == 0 && tables[i].table[j] != 0) a = tables[i].table[j];
    if(b == 0 && tables[i].table[j] != 0 && tables[i].table[j] != a) 
     b = tables[i].table[j];
   }
  }
  return b != 0 && a != 0;
 }

 public void kick(int pos, int playerid) {
  tables[pos/4].kick(playerid);
 }
 
 public void capture(int pos) {
  if(tables[pos/4].table[pos%4] == 1) tables[pos/4].table[pos%4] = 2;
  else if(tables[pos/4].table[pos%4] == 2) tables[pos/4].table[pos%4] = 1;
  System.out.println("capture replaced by " + tables[pos/4].table[pos%4]);
 }
 
 public void print() {
  for(int i = 0; i < 4; i++) {
   for(int j = 0; j < 4; j++) {
    //System.out.println(i+ " " + j);
    System.out.print(tables[i].table[j] + " ");
   }
   System.out.println();
  }
 }
 
 
 public boolean isGameOver() {
  
  boolean done = true;
  
  for(int i = 0; i < 4; i++) {
   done = true;
   for(int j = 0; j < 3; j++) {
    if(tables[i].table[j] != tables[i].table[j+1]
                 || tables[i].table[j] == 0) done = false;
   }
   if(done) break;
  }
  
  print();

  return done;
 }
 
 public int winner() {
  
  for(int i = 0; i < 4; i++) {
   boolean winner = true;
   for(int j = 0; j < 3; j++) {
    if(tables[i].table[j] != tables[i].table[j+1] 
                 || tables[i].table[j] == 0) winner = false;
   }
   if(winner) {
    return tables[i].table[0];
   }
  }
  return 0;
 }
 
 public static void main(String[] args){
  Board b = new Board();
  b.print();
  System.out.println();
  b.add(0, 1);
  b.add(1, 1);
  //b.add(2, 1);
  b.add(3, 1);
  b.add(4, 2);
  b.add(5, 2);
  b.add(6, 2);
  b.add(7, 1);
  b.print();
  System.out.println(b.isGameOver());
  System.out.println(b.string());
  System.out.println(b.string().length());
  System.out.println(b.tables[0].toString());
 }
 
}
