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
		//System.out.println(setIndex);
		int blockOffset = address << 8 + tagBits + setIndexBits;
		blockOffset = blockOffset >>> 8 + tagBits + setIndexBits;
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
		MainMemory mem = new MainMemory((int) Math.pow(2, 24));
		try {
			Scanner input = new Scanner(new File(fileName));
			cs.run(input, mem, assoc, blockSize, cacheSize);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}
}