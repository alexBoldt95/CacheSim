import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;

public class cachesim {
	private static final HashMap<Character, String> hexMap;
	static
    {
        hexMap = new HashMap<Character, String>();
        hexMap.put('0', "0000"); hexMap.put('1', "0001"); hexMap.put('2', "0010"); hexMap.put('3', "0011"); hexMap.put('4', "0100"); hexMap.put('5', "0101");
        hexMap.put('6', "0110"); hexMap.put('7', "0111"); hexMap.put('8', "1000"); hexMap.put('9', "1001"); hexMap.put('a', "1010"); hexMap.put('b', "1011");
        hexMap.put('c', "1100"); hexMap.put('d', "1101"); hexMap.put('e', "1110"); hexMap.put('f', "1111");        
    }
	private static final HashMap<String, Character> revHexMap;
	static
	{
		revHexMap = new HashMap<String, Character>();
		for(Character key:hexMap.keySet()){
			revHexMap.put(hexMap.get(key), key);
		}
	}
	
	public static int log2(int arg){
		return (int) Math.ceil((Math.log((double)arg)/Math.log((double)2)));
	}
	
	public class CacheLinkedHashMap<Integer, Frame> extends LinkedHashMap<Integer, Frame>{
		private int maxSize;
		
		public CacheLinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder, int setSize){
			super(initialCapacity, loadFactor, accessOrder);
			maxSize = setSize; 
		}

		protected boolean removeEldestEntry(Map.Entry eldest) {
	        return size()>maxSize;
	     }

	}
	
	public class MainMemory {
		private String[] myData;
		
		public MainMemory(int size){
			myData = new String[size];
			Arrays.fill(myData, "00000000");
		}
		
		public void writeBytes(int startByte, int accessSize, String[] data){
			int i = startByte;
			int j = 0;
			while(j<accessSize){
				myData[i] = data[j];
				i++;
				j++;			
			}
		}
		
		public String[] giveBlock(int startByte, int blockSize){
			String[] ret = new String[blockSize];
			int i = startByte;
			int j = 0;
			while(j<blockSize){
				ret[j] = myData[i];
				i++;
				j++;			
			}
			return ret;
			
		}
	}

	
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
			//a write miss that writes to memory (write-non-allocate)
			mem.writeBytes(address, accessSize, data);
		}
		
		public String[] read(int tag, int blockOffset, int accessSize, int blockSize, MainMemory mem, int address){
			//handles reading and returns whether hit or miss in 2nd element of array
			//System.out.println(myWays.keySet());
			if(myWays.containsKey(tag) && ((Frame) myWays.get(tag)).getValid() == true){
				return new String[]{readHit(tag, blockOffset, accessSize), "hit"};
			}
			else{
				return new String[]{readMiss(tag, blockOffset, accessSize, blockSize, mem, address), "miss"};
			}
		}
		
		public String write(int tag, int blockOffset, int accessSize, String[] data, MainMemory mem, int address){
			//handles writing and returns hit or miss string
			//System.out.println(myWays.keySet());
			if(myWays.containsKey(tag) && ((Frame) myWays.get(tag)).getValid() == true){
				writeHit(tag, blockOffset, accessSize, data, mem, address);
				return "hit";
			}
			else{
				writeMiss(address, accessSize, data, mem);
				return "miss";
			}
		}
	}
	
	public class Frame{
		private String[] myBlock;
		private boolean valid;
		
		public Frame(int blockSize){
			myBlock = new String[blockSize];
			Arrays.fill(myBlock, "00000000");
			valid = false;
		}
		
		public void setValid(boolean input){
			valid = input;
		}
		
		public boolean getValid(){
			return valid;
		}
		
		public String readBytes(int startByte, int accessSize){
			String[] bytes = new String[accessSize];
			int i = startByte;
			int j = 0;
			while(j<accessSize){
				bytes[j] = myBlock[i];
				i++;
				j++;			
			}
			String ret = "";
			for(String str:bytes){
				ret = ret.concat(str);
			}
			return ret;
		}
		
		public void writeBytes(int startByte, int accessSize, String[] data){
			int i = startByte;
			int j = 0;
			while(j<accessSize){
				myBlock[i] = data[j];
				i++;
				j++;			
			}
		}
		
		public void fill(String[] data){
			if(data.length != myBlock.length){
				throw new Error("Cannot fill frame, incorrect block size!");
			}
			myBlock = data;
			valid = true;
		}
	}
	
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
	}
	


	public int[] addressParse(String hexCode, int assoc, int blockSize, int cacheCap){
		int address = Integer.decode(hexCode);
		int blockBits = log2(blockSize);
		cacheCap = cacheCap * 1024;
		int numSets = cacheCap/(assoc * blockSize);
		int setIndexBits = log2(numSets);
		//System.out.println( numSets);
		int tagBits = 24-blockBits - setIndexBits;
		int tag = address >>> blockBits + setIndexBits;
		//System.out.println(tag);
		int setIndex = address << 8 + tagBits;
		setIndex = setIndex >>> 8 + tagBits + blockBits;
		if(tagBits == 24 || tagBits + blockBits ==24){
			setIndex = 0;
		}
		//System.out.println(setIndex);
		int blockOffset = address << 8 + tagBits + setIndexBits;
		blockOffset = blockOffset >>> 8 + tagBits + setIndexBits;
		if (tagBits + setIndexBits == 24){
			blockOffset = 0;
		}
		//System.out.println(blockOffset);
		return new int[]{address, tag, setIndex, blockOffset};	
	}
	
	public String[] dataHextoByteArray(String hexCode){
		String sing = "";
		for(int i = 0; i<hexCode.length(); i++){
			char ch = hexCode.charAt(i);
			//System.out.println(ch);
			String halfByte = hexMap.get(ch);
			sing += halfByte;
		}
		//System.out.println(sing);
		if (sing.length() % 8 != 0){
			sing = "0000" + sing;
		}
		//System.out.println(sing);
		String[] result = new String[sing.length()/8];
		int k = 0;
		for(int j = 0; j<sing.length(); j+=8){
			String byteStr = sing.substring(j, j+8);
			result[k] = byteStr;
			k++;
		}		
		return result;
	}
	
	public String byteStrToHexStr(String binaryStr){
		String result = "";
		for(int i=0; i<binaryStr.length(); i+=4){
			String halfBtye = binaryStr.substring(i, i+4);
			char hex = revHexMap.get(halfBtye);
			result = result + hex;
		}
		return result;
	}
	
	public void run(Scanner source, MainMemory mem, int assoc, int blockSize, int cacheCap){
		Cache cache = new Cache(cacheCap, assoc, blockSize);
		while(source.hasNextLine()){
			String command = source.nextLine();
			String[] commandArr = command.split(" ");
			int[] aD = addressParse(commandArr[1], assoc, blockSize, cacheCap);
			int address = aD[0];
			int tag = aD[1];
			int setIndex = aD[2];
			int blockOffset = aD[3];
			boolean is_load = "load".equals(commandArr[0]);
			int accessSize = Integer.parseInt(commandArr[2]);
			if(is_load){
				String[] result = cache.load(tag, setIndex, blockOffset, accessSize, blockSize, mem, address);
				String binaryStr = result[0];
				String hexValue = byteStrToHexStr(binaryStr);
				System.out.printf("%s %s %s %s%n", commandArr[0], commandArr[1], result[1], hexValue);
			}
			else{
				String[] data = dataHextoByteArray(commandArr[3]);
				String result = cache.store(tag, setIndex, blockOffset, accessSize, data, mem, address);
				System.out.printf("%s %s %s%n", commandArr[0], commandArr[1], result);
			}			
		}
	}
	
	
	
	public static void main(String[] args) {
		cachesim cs = new cachesim();
		String fileName = args[0];
		int cacheSize = Integer.parseInt(args[1]);
		int assoc = Integer.parseInt(args[2]);
		int blockSize = Integer.parseInt(args[3]);
		MainMemory mem = cs.new MainMemory((int) Math.pow(2, 24));
		try {
			Scanner input = new Scanner(new File(fileName));
			cs.run(input, mem, assoc, blockSize, cacheSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
}