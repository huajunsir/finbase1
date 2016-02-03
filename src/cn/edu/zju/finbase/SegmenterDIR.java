package cn.edu.zju.finbase;
import info.monitorenter.cpdetector.io.*;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;


public class SegmenterDIR {
	private String db_url="";
	private String inputDir=""; //分词的文本目录
	private String basedir = "./nlp-tool/stanford-segmenter-2015-12-09/data";
	private String article_type="";
	//private String publish_date="2015-01-01";
	private boolean init_table=false;
	
	int max_text_length=300000; //控制文件大小
	
	Connection con=null;
	CodepageDetectorProxy detector = CodepageDetectorProxy.getInstance(); // A singleton.
	
	/*
	 * input: input dir
	 * output: output dir
	 */
	public SegmenterDIR() {
		
	}
	
    private void init(){	
    	//初始化中文字符编码格式探测器。
    	detector.add(JChardetFacade.getInstance());
    	detector.add(ASCIIDetector.getInstance());
    	
	    //初始化数据库连接
		try {
			
			String url = "jdbc:"+ db_url;
		    
	        //read.close();
	      
	        Properties dbprops = new Properties();
	        //dbprops.setProperty("user","root");
	        //dbprops.setProperty("password","");
	        con = DriverManager.getConnection(url, dbprops);
	        System.out.println("成功连接到数据库--------" + url);
			 
	        if(init_table==true){
		        //先删除已有sentence表格，
		    	Statement st = con.createStatement();
		    	String sql = "DROP TABLE IF EXISTS articles";
				st.executeUpdate(sql);
			
				//再创建新的空表。
				sql = "CREATE TABLE articles(" +
		  	          "article_id serial,"+
					  "text text," +
		  	          "file_name text," +
					  "article_type text," + 
		  	          "publish_date text," +
					  "publish_year text" +
					  ")";
				st.executeUpdate(sql);
				st.close();
	        }
			
	    } catch (Exception e) {
	    		System.out.println(e.getMessage());
	    		e.printStackTrace();	
	   }		
	}
	

	
	public String readFiles(File filename){
		String sample="";
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
		            String temp="";
		            while((temp = bufferedReader.readLine()) != null){
		             	sample +=temp + "\n";
		            }
		          
		            //byte[] tmp=sample.getBytes("windows-1252");//这里写原编码方式
		            //sample=new String(tmp,"GBK");//这里写转换后的编码方式
		            
		           // System.out.println(sample);
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
	
	
	public String[] getPublishDate(String article_content) throws IOException{
		    String publish_date=null;
		    String publish_year=null;
			  
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
	
			
			String[] tmp= {publish_date, publish_year};
			return tmp;	
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
		        System.out.println("-对文件" + files[i].toString() + "-进行分词-文件大小:"+files[i].length()+"已处理"+i+"个文件--");       
	
		        if(files[i].length() > max_text_length) continue; //不处理过大的文本文件。
		           
		        try{
		        	
		        		java.nio.charset.Charset charset = detector.detectCodepage(files[i].toURL());// 检测文本的编码格式，可能是gb2312, window-1252等。	
		        		String encoding=charset.toString();
		        		if(encoding=="windows-1252") encoding="utf-16le"; //检测到的windows-1252编码实际上是utf-16le编码
		        	    
		        		String fileContents = IOUtils.slurpFile(files[i].toString(),encoding); //基于给定的编码读取文本内容。
				
		        		fileContents=fileContents.replaceAll("", " ");
		        		//fileContents=fileContents.replaceAll("", " ");
		        
				        //完成分词
		        		if(fileContents!=null && fileContents.trim().length()>30){// 过滤掉太短的文本
					        List<String> segmented=segmenter.segmentString(fileContents);
					        Iterator<String> it=segmented.iterator();
					        String temp=" ";
					        
					        String[] publish_time=this.getPublishDate(fileContents);
					        
					        while(it.hasNext()) temp += (String) it.next() +" "; 
					        //分词结果写入数据库 
					        
					        Statement st = con.createStatement();
						    
							//最后插入数据库
							String sql = "insert into articles(text, file_name, article_type, publish_date,publish_year) values('"+ 
										temp + "','"+ files[i].getName() +"','"+ article_type+"','"+ publish_time[0]+"','"+ publish_time[1] +"')";
							
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

		
	    SegmenterDIR sg=new SegmenterDIR();
	    if(args.length<6) {
			System.out.println("请输入至少三个参数（按顺序）：数据库的连接字符串，文档类型，输入文件目录，分词器目录，最大文件大小，是否重新初始化数据库");
		}else{
			sg.setDb_url(args[0]);
			sg.setArticle_type(args[1]);
			sg.setInputDir(args[2]);
			sg.setBasedir(args[3]);
			sg.setMax_text_length(Integer.valueOf(args[4]));
			sg.setInit_table(Boolean.valueOf(args[5]));
			sg.init();
			sg.segmentDir();
		}

	}

	public String getDb_url() {
		return db_url;
	}

	public void setDb_url(String db_url) {
		this.db_url = db_url;
	}

	public String getInputDir() {
		return inputDir;
	}

	public void setInputDir(String inputDir) {
		this.inputDir = inputDir;
	}

	public String getBasedir() {
		return basedir;
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public String getArticle_type() {
		return article_type;
	}

	public void setArticle_type(String article_type) {
		this.article_type = article_type;
	}

	public boolean isInit_table() {
		return init_table;
	}

	public void setInit_table(boolean init_table) {
		this.init_table = init_table;
	}

	public int getMax_text_length() {
		return max_text_length;
	}

	public void setMax_text_length(int max_text_length) {
		this.max_text_length = max_text_length;
	}
}
