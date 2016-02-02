package demo;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import info.monitorenter.cpdetector.io.*;

public class TestClass {
	
	String inputDir="./rawtxt";

	CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance(); // A singleton.
	
	public TestClass() {
		// TODO Auto-generated constructor stub
		
		detector.add(JChardetFacade.getInstance());
    	detector.add(ASCIIDetector.getInstance());
    	
	}
	
	public void testLength(){
		
		 File dir = new File(this.inputDir);
		 File[] files = dir.listFiles();
		
		 for(int i=0;i<files.length;i++){
				 String sample=this.readFiles(files[i]);
				 
				 if(files[i].length()> 300000) 
				 {
					  System.out.println(files[i].getName() + ":"+ files[i].length() + ":" + sample.length());
					  continue;
				 }
		         
		         System.out.println("=======================\n\n\n");
		 
		 }	
		
	}
	
	
	public String readFiles(File filename){
		String sample=null;
		try {
	        	
	        	
	        	java.nio.charset.Charset charset = detector.detectCodepage(filename.toURL());		
	        	String encoding=charset.toString();
	        	if(encoding=="windows-1252") encoding="utf-16le";
	        	// System.out.println(filename.toString()+ "=========" + encoding + "===============\n");
	        	
		        if(filename.isFile() && filename.exists()){ //判断文件是否存在
		            //读取每个文件内容
		        	InputStreamReader read = new InputStreamReader(
		            new FileInputStream(filename),encoding);//考虑到编码格式
		            BufferedReader bufferedReader = new BufferedReader(read);
		            String temp=null;
		            while((temp = bufferedReader.readLine()) != null){
		             	sample +=temp;
		            }
		          //  byte[] tmp=sample.getBytes("windows-1252");//这里写原编码方式
		            //sample=new String(tmp,"GBK");//这里写转换后的编码方式
		            
		          //  System.out.println(sample);
		            read.close();	
		            
		        }else{
		        	System.out.println("找不到指定的文件");
		        }
		    } catch (Exception e) {
		    		System.out.println(e.getMessage());
		    		e.printStackTrace();	
		   } 	
		return sample;
	}
	
	public void testEncoding(){
		 File dir = new File(this.inputDir);
		 File[] files = dir.listFiles();
		
		 for(int i=0;i<files.length;i++){
				 this.readFiles(files[i]);
		 }	
		
		 // System.out.println(Charset.availableCharsets().keySet());
	}

	public static void main(String[] args) {
		
		File file=new File("./db.url");
        //读取每个文件内容
        InputStreamReader read;
		try {
			read = new InputStreamReader(
			         new FileInputStream(file));
		
        BufferedReader bufferedReader = new BufferedReader(read);
        String url = "jdbc:"+ bufferedReader.readLine();
        System.out.println("连接到数据库。。。。" + url);
        read.close();
    
        Properties dbprops = new Properties();
        dbprops.setProperty("user","root");
        dbprops.setProperty("password","Murren@2013");
        Connection con = DriverManager.getConnection(url, dbprops);
        
        //先删除已有sentence表格，
    	Statement st = con.createStatement();
    	String sql = "select count(*) from articles";
    	ResultSet rs = st.executeQuery(sql);
		
		
		while (rs.next())
		{
			System.out.println(rs.getInt(1));
			
		} 
		rs.close();	
		st.close();
		} catch (IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//考虑到编码格式

	}

}
