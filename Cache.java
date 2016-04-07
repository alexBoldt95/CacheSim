import java.util.Arrays;

public class Cache {
	
	WaySet[] mySets;
	
	public Cache(int cacheCap, int assoc, int blockSize){
		cacheCap = cacheCap * 1024;
		int numSets = cacheCap/(assoc * blockSize);
		mySets = new WaySet[numSets];
		for(int i=0; i<numSets; i++){
			mySets[i] = new WaySet(blockSize, assoc);
		}
	}
	
	public String[] load(int tag, int setIndex, int blockOffset, int accessSize, int blockSize, MainMemory mem, int address){
		return mySets[setIndex].read(tag, blockOffset, accessSize, blockSize, mem, address);
	}
	
	public String store(int tag, int setIndex, int blockOffset, int accessSize, String[] data, MainMemory mem, int address){
		return mySets[setIndex].write(tag, blockOffset, accessSize, data, mem, address);
	}
	

	public static void main(String[] args) {
		Cache test = new Cache(1024, 4, 64);
		MainMemory mem = new MainMemory((int) Math.pow(2, 24));
		String[] data = new String[]{"10101010", "00000000", "11110000", "11010011"};
		System.out.println(test.store(2, 0, 19, 4, data, mem, 531));
		String[] ans = test.load(2, 0, 19, 2, 64, mem, 531);
		System.out.println(Arrays.toString(ans));
		String[] ans1 = test.load(2, 0, 19, 4, 64, mem, 531);
		System.out.println(Arrays.toString(ans1));
		String[] data1 = new String[]{"11111111"};
		System.out.println(test.store(2, 1, 0, 1, data1, mem, 576));
		String[] ans2 = test.load(2, 1, 0, 4, 64, mem, 576);
		System.out.println(Arrays.toString(ans2));
		String[] ans3 = test.load(2, 1, 0, 1, 64, mem, 576);
		System.out.println(Arrays.toString(ans3));

		
	}

}
