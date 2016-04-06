import java.util.Arrays;

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

	public static void main(String[] args) {
		Frame test = new Frame(4);
		test.writeBytes(2, 2, new String[]{"10101010", "00001111"});
		System.out.println(test.readBytes(2, 2));
		//test.fill(new String[]{"01010101", "", ""});

	}

}
