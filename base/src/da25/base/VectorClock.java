package da25.base;

import java.io.Serializable;

public class VectorClock implements Serializable {
	private static final long serialVersionUID = 1L;
	private int[] vector;
	
	public VectorClock(int size){
		vector = new int[size];
	}
	
	public void reset(){
		for(int i = 0; i< vector.length; i++){
			vector[i] = 0;
		}
	}
	
	public int get(int index){
		return vector[index];
	}
	
	public void increase(int index){
		vector[index]++;
	}
	
	public void decrease(int index){
		vector[index]--;
	}
	
	public boolean greaterEqual(VectorClock otherClock){
		for (int i = 0; i < vector.length; i++) {
			if (vector[i] < otherClock.get(i)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public String toString(){
		StringBuilder bld = new StringBuilder();
		for (int i = 0; i < vector.length; i++) {
			bld.append(vector[i]);
			bld.append(",");
		}
		bld.deleteCharAt(bld.length()-1);
		return bld.toString();
	}
}
