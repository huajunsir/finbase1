package cn.edu.zju.finbase;
import info.monitorenter.cpdetector.io.*;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.sql.*;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;


public class SegmenterDIR {
	
	String inputDir="./inputtxt";
	String outputDir;
	Connection con=null;
	
	private static String basedir = "./nlp-tool/stanford-segmenter-2015-12-09/data";
	CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance(); // A singleton.
	//detector.add(new ParsingDetector(false));
	
	/*
	 * input: input dir
	 * output: output dir
	 */
	public SegmenterDIR() {
		init();
	}
	
    private void init(){	
    	//初始化中文字符编码格式探测器。
    	detector.add(JChardetFacade.getInstance());
    	detector.add(ASCIIDetector.getInstance());
    	
	    //初始化数据库连接
		try {
			
			File file=new File("./db.url");
            //读取每个文件内容
            InputStreamReader read = new InputStreamReader(
                     new FileInputStream(file));//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String url = "jdbc:"+ bufferedReader.readLine();
            System.out.println("连接到数据库。。。。" + url);
            read.close();
        
	        Properties dbprops = new Properties();
	        //dbprops.setProperty("user","boboss");
	        //dbprops.setProperty("password","");
	        con = DriverManager.getConnection(url, dbprops);
	        
	        //先删除已有sentence表格，
	    	Statement st = con.createStatement();
	    	String sql = "DROP TABLE IF EXISTS articles";
			st.executeUpdate(sql);
		
			//再创建新的空表。
			sql = "CREATE TABLE articles(" +
	  	          "article_id serial,"+
				  "text text," +
	  	          "file_name text" +
				  ")";
			st.executeUpdate(sql);
			st.close();
			
	    } catch (Exception e) {
	    		System.out.println(e.getMessage());
	    		e.printStackTrace();	
	   }		
	}
	

	
	public String readFiles(File filename){
		String sample=null;
		try {
	        	
	        	
	        	java.nio.charset.Charset charset = detector.detectCodepage(filename.toURL());		
	        	String encoding=charset.toString();
	        	if(encoding=="windows-1252") encoding="utf-16le";
	        	System.out.println(filename.toString()+ "=========" + encoding + "===============\n");
	        	
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
		            
		            System.out.println(sample);
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
	
	public void segmentDir() {
		
		    //basedir=System.getenv("HOME") + "/" + basedir;
		    
		    //System.out.println(basedir);
		    //创建分词器，建立分词模型
		    Properties props = new Properties();
		    props.setProperty("sighanCorporaDict", basedir);
		    props.setProperty("serDictionary", basedir + "/dict-chris6.ser.gz");
		  
		    props.setProperty("inputEncoding", "GBK");
		    props.setProperty("sighanPostProcessing", "true");

		    CRFClassifier<CoreLabel> segmenter = new CRFClassifier<>(props);
		    segmenter.loadClassifierNoExceptions(basedir + "/ctb.gz", props);
		    
		    //读取待分词文本文件
		    File dir = new File(this.inputDir);
		    File[] files = dir.listFiles();

		     //逐一处理待分词文件，并写入postgresql数据库
		    for(int i=0;i<files.length;i++){
		        System.out.println("---------对文件" + files[i].toString() + "进行分词------------------------");       
	
		        try{
		        	
		        		java.nio.charset.Charset charset = detector.detectCodepage(files[i].toURL());// 检测文本的编码格式，可能是gb2312, window-1252等。	
		        		String encoding=charset.toString();
		        		if(encoding=="windows-1252") encoding="utf-16le"; //检测到的windows-1252编码实际上是utf-16le编码
		        	    
		        		String fileContents = IOUtils.slurpFile(files[i].toString(),encoding); //基于给定的编码读取文本内容。
				        //System.out.println(fileContents);
		        		//byte[] tmp=fileContents.getBytes(encoding);
		        		//fileContents=new String(tmp,"GBK");
		        		fileContents=fileContents.replaceAll("", " ");
		        		
		        		//System.out.println(fileContents);
				        //完成分词
		        		if(fileContents!=null && fileContents.trim().length()>30){// 过滤掉太短的文本
					        List<String> segmented=segmenter.segmentString(fileContents);
					        Iterator<String> it=segmented.iterator();
					        String temp=" ";
					        while(it.hasNext()) temp += (String) it.next() +" "; 
					        //分词结果写入数据库 
					        
					        Statement st = con.createStatement();
						    
							//最后插入数据库
							String sql = "insert into articles(text, file_name) values('"+ 
										temp + "','"+ files[i].getName() +"')";
							
							//System.out.println(sql);
							st.executeUpdate(sql);
							st.close();
		        		}
				        
			    } catch (Exception e) {
			    		System.out.println(e.getMessage());
			    		e.printStackTrace();	
			   } 	
		    }
		    
		    /*
		    //逐一处理待分词文件，并写入结果文件
		    for(int i=0;i<files.length;i++){
		        System.out.println("对文件" +files[i] + "进行分词");       
	
		        try{
		        	    String fileContents = IOUtils.slurpFile(this.inputDir+"/"+files[i],"GBK");
				        System.out.println(fileContents);
				        //完成分词
				        List<String> segmented=segmenter.segmentString(fileContents);
				        Iterator it=segmented.iterator();
				        String temp=" ";
				        while(it.hasNext()) temp += (String) it.next() +" "; 
				        //分词结果写入文件  
				        FileWriter fw=new FileWriter(this.outputDir + "/" + files[i]);
				    	fw.write(temp);
				    	fw.flush();
				    	fw.close();
				    	System.out.println(temp);	
				    	
			    } catch (Exception e) {
			    		System.out.println("写入文件内容出错");
			    		e.printStackTrace();	
			   } 	
		    }*/
		  }

	
	
	public static void main(String[] args) {
		/*
		String input=null;
		String output=null;
		
		if(args.length==0){
			input ="./";
			output="./";
		}
		else if (args.length==1){
			input = args[0];
			output="./";
		}
		else if (args.length==2){
			input=args[0];
			output=args[1];
		}
		else {
			System.out.println("请使用正确的调用格式：java Segmenter -inputdir -outputdir");
		}
		*/
		
	    SegmenterDIR sg=new SegmenterDIR();
	    //sg.testEncoding();
	    sg.segmentDir();

	}

}
