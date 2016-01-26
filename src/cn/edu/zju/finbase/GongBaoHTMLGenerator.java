package cn.edu.zju.finbase;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class GongBaoHTMLGenerator {

	public GongBaoHTMLGenerator() {
		// TODO Auto-generated constructor stub
	}

	public void creatHTMLForJiaoyi() throws FileNotFoundException{
		
		
		
		StringBuilder sb = new StringBuilder();  
	  
	    PrintStream printStream = new PrintStream(new FileOutputStream("report.html"));  
	    sb.append("<html>");  
	    sb.append("<head>");  
	    sb.append("<title>交易类公报</title>");  
	    sb.append("</head>");  
	    sb.append("<body bgcolor=\"#FFF8DC\">");  
	    sb.append("<br/>");  
	    sb.append("<br/>");  
	    
	    

	    sb.append("<br/><br/>");  
	    sb.append("</body></html>");  
        printStream.println(sb.toString());  
        printStream.flush();
        printStream.close();
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
