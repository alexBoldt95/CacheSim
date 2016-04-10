import java.util.*;

public class CacheLinkedHashMap<Integer, Frame> extends LinkedHashMap<Integer, Frame>{
	private int maxSize;
	
	public CacheLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder, int setSize){
		super(initialCapacity, loadFactor, accessOrder);
		maxSize = setSize; 
	}

	protected boolean removeEldestEntry(Map.Entry eldest) {
        return size()>maxSize;
     }
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
