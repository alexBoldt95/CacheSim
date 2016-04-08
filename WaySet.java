import java.util.*;

public class WaySet {

	private CacheLinkedHashMap<Integer, Frame> myWays;
	
	public WaySet(int blockSize, int associativity){ 
		//ctor, creates a set which is a map that maps from tags to frames
		float loadFactor = (float) 0.75;
		myWays = new CacheLinkedHashMap<Integer, Frame>(0, loadFactor, true, associativity);
		for(int t=0; t<associativity; t++){
			myWays.put(t, new Frame(blockSize));
		}		
	}
	
	public String readHit(int tag, int blockOffset, int accessSize){ 
		//returns a string from hit read
		return ((Frame) myWays.get(tag)).readBytes(blockOffset, accessSize);
	}
	
	public String readMiss(int tag, int blockOffset, int accessSize, int blockSize, MainMemory mem, int address){  
		//fills the frame from memory and then hits the read
		int blockStart = address - blockOffset;
		String[] block = mem.giveBlock(blockStart, blockSize);
		myWays.put( tag, new Frame(blockSize));
		//System.out.println(myWays.containsKey(tag));
		((Frame) myWays.get(tag)).fill(block);
		return readHit(tag, blockOffset, accessSize);	
	}
	
	public void writeHit(int tag, int blockOffset, int accessSize, String[] data, MainMemory mem, int address){ 
		//writes to the frame as a hit and writes through to memory
		Frame block = (Frame) myWays.get(tag);
		block.writeBytes(blockOffset, accessSize, data);
		mem.writeBytes(address, accessSize, data);
	}
	
	public void writeMiss(int address, int accessSize, String[] data, MainMemory mem){ 
		//a write misss that writes to memory (write-non-allocate)
		mem.writeBytes(address, accessSize, data);
	}
	
	public String[] read(int tag, int blockOffset, int accessSize, int blockSize, MainMemory mem, int address){
		//handles reading and returns whether hit or miss in 2nd element of array
		if(myWays.containsKey(tag) && ((Frame) myWays.get(tag)).getValid() == true){
			return new String[]{readHit(tag, blockOffset, accessSize), "hit"};
		}
		else{
			return new String[]{readMiss(tag, blockOffset, accessSize, blockSize, mem, address), "miss"};
		}
	}
	
	public String write(int tag, int blockOffset, int accessSize, String[] data, MainMemory mem, int address){
		//handles writing and returns hit or miss string
		if(myWays.containsKey(tag) && ((Frame) myWays.get(tag)).getValid() == true){
			writeHit(tag, blockOffset, accessSize, data, mem, address);
			return "hit";
		}
		else{
			writeMiss(address, accessSize, data, mem);
			return "miss";
		}
	}
	
	public static void main(String[] args) {
		MainMemory mem = new MainMemory((int) Math.pow(2, 24));
		WaySet test = new WaySet(64, 2);
		String[] data = new String[]{"10101010", "00000000", "11110000", "11010011"};
		System.out.println(test.write(2, 19, 2, data, mem, 531));
		String[] ans = test.read(2, 19, 4, 64, mem, 531);
		System.out.println(Arrays.toString(ans));
		String[] ans1 = test.read(2, 19, 4, 64, mem, 531);
		System.out.println(Arrays.toString(ans1));
		
	}

}
