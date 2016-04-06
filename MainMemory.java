import java.util.Arrays;

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
	
	public static void main(String[] args) {
		MainMemory test = new MainMemory(1024);
		test.writeBytes(256, 2, new String[]{"10101010", "11110000"});
		for(String str:test.giveBlock(256, 8)){
			System.out.println(str);
		}
	}

}
