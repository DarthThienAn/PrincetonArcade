package com.Tabletop;

import android.content.Context;
import android.widget.ImageView;
import android.widget.Toast;

import com.link.R;

public class GameTile extends ImageView {
	
	int p1pic = R.drawable.p1button;
	int p2pic = R.drawable.p2button;
	int unselect = R.drawable.megusta;
	
	int tracker;
	String testobj;
	int index;
	GameState state;
	//boolean selected;
	Context mContext;
	
	public GameTile(Context c) {
		super(c);
		mContext = c;
		tracker = 0;
		//selected = false;
	}
	
	public void setIndex(int i) {
		index = i;
	}
	
	public void setState(GameState g) {
		state = g;
	}
	

	public GameState clickLogic() {
		
		
		if(!state.used[index/4][index%4]){
			if(!state.selected[index]) {
				if(state.nUnused < 1 && state.nUsed < 1) {
					state.selected[index] = true;
					if(state.playerturn == 1) this.setImageResource(p1pic);
					if(state.playerturn == 2) this.setImageResource(p2pic);
					if(state.choice1 != -1 && state.choice2 == -1) state.choice2 = index;
					if(state.choice1 == -1)	state.choice1 = index;
					state.nUnused++;
				}
				else {
					Toast.makeText(mContext, "Must unselect everything to pick this", Toast.LENGTH_SHORT).show();
				}
			}
			else {
				state.selected[index] = false;
				this.setImageResource(unselect);
				if(state.choice1 == index) state.choice1 = -1;
				if(state.choice2 == index) state.choice2 = -1;
				state.nUnused--;
			}
		}
		else {
			if(state.nUnused < 2) {
				if(!state.selected[index]) {
					if(state.nUsed < 2 && state.nUnused < 1) {
						state.selected[index] = true;
						if(state.board.tables[index/4].table[index%4] == 1) this.setImageResource(p2pic);
						if(state.board.tables[index/4].table[index%4] == 2) this.setImageResource(p1pic);
						if(state.choice1 != -1 && state.choice2 == -1) state.choice2 = index;
						if(state.choice1 == -1)	state.choice1 = index;
						state.nUsed++;
					}
					else {
						if(state.nUnused <= 1) Toast.makeText(mContext, "Must unselect a used to pick this", Toast.LENGTH_SHORT).show();
						if(state.nUsed <= 2) Toast.makeText(mContext, "Must unselect an unused to pick this", Toast.LENGTH_SHORT).show();
					}
				}
				else {
					state.selected[index] = false;
					//Toast.makeText(mContext, "reaches here " + state.choice1 + " " + state.choice2, Toast.LENGTH_SHORT).show();
					if(state.board.tables[index/4].table[index%4] == 1) this.setImageResource(p1pic);
					if(state.board.tables[index/4].table[index%4] == 2) this.setImageResource(p2pic);
					if(state.choice1 == index) state.choice1 = -1;
					if(state.choice2 == index) state.choice2 = -1;
					state.nUsed--;
				}
			}
			else Toast.makeText(mContext, "Must unselect an unused to pick this", Toast.LENGTH_SHORT).show();
		}
		
		System.out.println(state.string());
		//System.out.println("choice1 " + state.choice1 + "choice2 " + state.choice2 + );
		
		return state; 
	}
	
}
