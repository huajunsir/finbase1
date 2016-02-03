package demo;

import java.io.*;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat; 
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.io.IOUtils;
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

	public void testRemoteConnection(){
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

	public void testGetPublishDate() throws MalformedURLException, IOException{
		  String publish_date=null;
		  String publish_year=null;
		  File dir = new File("./inputtxt");
		  File[] files = dir.listFiles();
		  
		  CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance(); // A singleton.
		 //初始化中文字符编码格式探测器。
	    	detector.add(JChardetFacade.getInstance());
	    	detector.add(ASCIIDetector.getInstance());
	    	
	    	int count=0;
	    	
	    	StringBuffer sb=new StringBuffer();
		  for(int i=1;i<files.length;i++){
			  publish_date=null;
			  publish_year=null;
			  System.out.println("-对文件" + files[i].toString() + "-进行分词-文件大小:"+files[i].length()+"已处理"+i+"个文件--");       
				
		      java.nio.charset.Charset charset = detector.detectCodepage(files[i].toURL());// 检测文本的编码格式，可能是gb2312, window-1252等。	
			  
			  String encoding=charset.toString();
			  if(encoding=="windows-1252") encoding="utf-16le"; //检测到的windows-1252编码实际上是utf-16le编码
    	      if(encoding=="void" || encoding==null)continue;
    	      
			  String article_content = IOUtils.slurpFile(files[i].toString(),encoding); //基于给定的编码读取文本内容。
	
			  //过滤空的或过小的文档
			  if(article_content==null || article_content.trim().length()<30)continue; 
			  
			  //匹配年月日，以最后出现的年月日为准
			  Pattern pattern = Pattern.compile("[\\d\\s零O0○〇一二三四五六七八九]{4,8}\\s{0,3}年\\s{0,3}[\\d零0O〇元正○一二三四五六七八九]{1,2}\\s{0,3}月\\s{0,3}[\\d零0O〇初○一二三四五六七八九]{1,2}\\s{0,3}日");
			  Matcher matcher = pattern.matcher(article_content);
			  while(matcher.find()) {
			    	publish_date=matcher.group();
			  }
			  
	
			  //匹配年
			  pattern = Pattern.compile("([\\d零O○〇一二三四五六七八九]{4,8}\\s{0,3}年)|(20[\\d]{2})");
			  matcher = pattern.matcher(article_content);
			  while(matcher.find()) {
			    	publish_year=matcher.group();
			  }
				
			  if(publish_date==null){
				  publish_date=publish_year;
				  
			  }
			  else{
				    publish_date=publish_date.trim().replaceAll(" ", "");
				    publish_date=publish_date.replaceAll("[年月]", "-");
				    publish_date=publish_date.replaceAll("日", "");
				    publish_date=publish_date.replaceAll("[零O○〇]", "0");
					publish_date=publish_date.replaceAll("一", "1");
					publish_date=publish_date.replaceAll("二", "2");
					publish_date=publish_date.replaceAll("三", "3");
					publish_date=publish_date.replaceAll("四", "4");
					publish_date=publish_date.replaceAll("五", "5");
					publish_date=publish_date.replaceAll("六", "6");
					publish_date=publish_date.replaceAll("七", "7");
					publish_date=publish_date.replaceAll("八", "8");
					publish_date=publish_date.replaceAll("九", "9");
				 
					publish_year=publish_date.substring(0, 4);
			  }
				
			  if(publish_year==null){
					publish_date=publish_year="3000";//没有找到匹配，则设置为3000.
					count++;
					sb.append(files[i].toString());
			  }
			
				
			  publish_date=publish_date.trim().replaceAll(" ", "");
			    publish_date=publish_date.replaceAll("[年月]", "-");
			    publish_date=publish_date.replaceAll("日", "");
			    publish_date=publish_date.replaceAll("[零O○〇]", "0");
				publish_date=publish_date.replaceAll("一", "1");
				publish_date=publish_date.replaceAll("二", "2");
				publish_date=publish_date.replaceAll("三", "3");
				publish_date=publish_date.replaceAll("四", "4");
				publish_date=publish_date.replaceAll("五", "5");
				publish_date=publish_date.replaceAll("六", "6");
				publish_date=publish_date.replaceAll("七", "7");
				publish_date=publish_date.replaceAll("八", "8");
				publish_date=publish_date.replaceAll("九", "9");
	
				publish_year=publish_year.trim().replaceAll(" ", "");
				publish_year=publish_year.replaceAll("年", "");
				publish_year=publish_year.replaceAll("[零O○〇]", "0");
				publish_year=publish_year.replaceAll("一", "1");
				publish_year=publish_year.replaceAll("二", "2");
				publish_year=publish_year.replaceAll("三", "3");
				publish_year=publish_year.replaceAll("四", "4");
				publish_year=publish_year.replaceAll("五", "5");
				publish_year=publish_year.replaceAll("六", "6");
				publish_year=publish_year.replaceAll("七", "7");
				publish_year=publish_year.replaceAll("八", "8");
				publish_year=publish_year.replaceAll("九", "9");
				
				System.out.println(publish_date+"==="+publish_year);
				
		  }
		  
		  System.out.println(sb.toString());
		  System.out.println(count);
		  

		//  Pattern pattern = Pattern.compile("href=\"(.+?)\"");
		 // Matcher matcher = pattern.matcher("<a href=\"index.html\">主页</a>");
		 // if(matcher.find())
		 //   System.out.println(matcher.group(0));

	}
	public static void main(String[] args) {
		TestClass tc=new TestClass();
		try {
			tc.testGetPublishDate();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
