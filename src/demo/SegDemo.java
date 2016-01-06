package demo;
import java.io.*;
import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreLabel;


/** This is a very simple demo of calling the Chinese Word Segmenter
 *  programmatically.  It assumes an input file in UTF8.
 *  <p/>
 *  <code>
 *  Usage: java -mx1g -cp seg.jar SegDemo fileName
 *  </code>
 *  This will run correctly in the distribution home directory.  To
 *  run in general, the properties for where to find dictionaries or
 *  normalizations have to be set.
 *
 *  @author Christopher Manning
 */

public class SegDemo {

  private static final String basedir = "/Users/chenhuajun/Application/"
  		+ "StandfordNLP/stanford-segmenter-2015-12-09/data";
 
  
  
  
  public static void main(String[] args) throws Exception {
    System.setOut(new PrintStream(System.out, true, "utf-8"));

    Properties props = new Properties();
    props.setProperty("sighanCorporaDict", basedir);
    props.setProperty("serDictionary", basedir + "/dict-chris6.ser.gz");
    //if (args.length > 0) {
      //props.setProperty("testFile", args[0]);
    //}
    
 
    props.setProperty("inputEncoding", "GBK");
    props.setProperty("sighanPostProcessing", "true");

    CRFClassifier<CoreLabel> segmenter = new CRFClassifier<>(props);
    segmenter.loadClassifierNoExceptions(basedir + "/ctb.gz", props);
   /*
    for (String filename : args) {
      segmenter.classifyAndWriteAnswers(filename);
     
    }
    */
    
    File dir = new File("./rawtxt");
    String[] files = dir.list(new FilenameFilter(){
        public boolean accept(File dirtmp,String name){
            return name.endsWith(".txt");
        }
    });
    
    for(int i=0;i<files.length;i++){
        System.out.println(files[i]);
    
    
    String sample = null;
    List<String> segmented=null;
    try {
        String encoding="GBK";
        File file=new File("./rawtxt/"+files[i]);
        if(file.isFile() && file.exists()){ //判断文件是否存在
            InputStreamReader read = new InputStreamReader(
            new FileInputStream(file),encoding);//考虑到编码格式
            BufferedReader bufferedReader = new BufferedReader(read);
            String temp=null;
            while((temp = bufferedReader.readLine()) != null){
             	sample +=temp;
                System.out.println(temp);
            }
            read.close();
        }else{
        	System.out.println("找不到指定的文件");
        }
    	} catch (Exception e) {
    		System.out.println("读取文件内容出错");
    		e.printStackTrace();	
    	}
 
    
   // sample = "我住在美国。";
    	segmented=segmenter.segmentString(sample);
    	FileWriter fw=new FileWriter("./output/"+ files[i]);
    	fw.write(segmented.toString());
    	fw.flush();
    	fw.close();
    	System.out.println(segmented);
    }
  }

}
