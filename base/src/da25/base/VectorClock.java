package da25.base;

import java.io.Serializable;
import java.util.HashMap;

public class VectorClock implements Serializable {
	private static final long serialVersionUID = 1L;
	private HashMap<Integer, Integer> vector;

	public VectorClock() {
		vector = new HashMap<>();
	}
	
	public VectorClock(VectorClock vectorClock) {
		vector = new HashMap<>(vectorClock.vector);
	}
	
	synchronized public void reset() {
		vector.clear();
	}

	synchronized public int get(int index) {
		try {
			return vector.get(index).intValue();
		} catch (NullPointerException e) {
			return 0;
		}
	}

	synchronized public void increase(int index) {
		vector.put(index, get(index) + 1);
	}

	synchronized public void decrease(int index) {
		vector.put(index, get(index) - 1);
	}

	synchronized public boolean greaterEqual(VectorClock otherClock) {
		for (int key : otherClock.vector.keySet()) {
			if (get(key) < otherClock.get(key)) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	synchronized public String toString() {
		int largestId = 0;
		for (int key : vector.keySet()) {
			if (key > largestId) {
				largestId = key;
			}
		}
		
		StringBuilder bld = new StringBuilder();
		bld.append("(");
		
		for (int i = 1; i <= largestId; i++) {
			bld.append(get(i));
			bld.append(",");
		}
		
		if (bld.length() > 1) {
			bld.deleteCharAt(bld.length() - 1);
		}
		
		bld.append(")");
		return bld.toString();
	}
}
