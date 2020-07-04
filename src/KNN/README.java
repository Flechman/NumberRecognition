package KNN;

import java.io.PrintWriter;

public class README {
	
	public static void main(String []args) {
		
		PrintWriter writer;
		
		try {
			writer = new PrintWriter("README.txt", "UTF-8");
			writer.println("line 111111");
			writer.println("line 22222");
			writer.close();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
