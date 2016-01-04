package cn.edu.zju.finbase;
import java.io.*;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;

public class SegmenterDIR {
	
	String inputDir;
	String outputDir;
	
	private static final String basedir = "/Users/boboss/Application/"
	  		+ "StandfordNLP/stanford-segmenter-2015-12-09/data";

	/*
	 * input: input dir
	 * output: output dir
	 */
	public SegmenterDIR(String input, String output) {
		inputDir=input;
		outputDir=output;
	}
	
	public String readFiles(String filename){
		String sample=null;
		try {
	           
	        	String encoding="GBK";
	        	File file=new File(this.inputDir+"/"+ filename);
		        if(file.isFile() && file.exists()){ //判断文件是否存在
		            //读取每个文件内容
		        	InputStreamReader read = new InputStreamReader(
		            new FileInputStream(file),encoding);//考虑到编码格式
		            BufferedReader bufferedReader = new BufferedReader(read);
		            String temp=null;
		            while((temp = bufferedReader.readLine()) != null){
		             	sample +=temp;
		                //System.out.println(temp);
		            }
		            read.close();	
		            
		        }else{
		        	System.out.println("找不到指定的文件");
		        }
		    } catch (Exception e) {
		    		System.out.println("读取文件内容出错");
		    		e.printStackTrace();	
		   } 	
		return sample;
	}
	
	public void segmentDir() {
		
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
		    String[] files = dir.list(new FilenameFilter(){
		        public boolean accept(File dirtmp,String name){
		            return name.endsWith(".txt");
		        }
		    });
		    
		    //逐一处理待分词文件，并写入结果文件
		    for(int i=0;i<files.length;i++){
		        System.out.println("对文件" +files[i] + "进行分词");       
	
		        try{
		        	    String fileContents = IOUtils.slurpFile(this.inputDir+"/"+files[i],"GBK");
				        System.out.println(fileContents);
				        //完成分词
				        List<String> segmented=segmenter.segmentString(fileContents);
				       
				        //分词结果写入文件  
				        FileWriter fw=new FileWriter(this.outputDir + "/" + files[i]);
				    	fw.write(segmented.toString());
				    	fw.flush();
				    	fw.close();
				    	System.out.println(segmented);	
				    	
			    } catch (Exception e) {
			    		System.out.println("写入文件内容出错");
			    		e.printStackTrace();	
			   } 	
		    }
		  }

	
	
	public static void main(String[] args) {
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
			
	    SegmenterDIR sg=new SegmenterDIR(input, output);
	    sg.segmentDir();

	}

}