package cn.edu.zju.finbase;
import java.sql.*;
import java.io.*;
import java.util.*;

import edu.stanford.nlp.dcoref.CorefChain;
import edu.stanford.nlp.dcoref.CorefCoreAnnotations;
import edu.stanford.nlp.io.*;
import edu.stanford.nlp.ling.*;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.*;



/**
 * 从article表中读取一个分好词的文档，
 * 将该文档文本利用StandfordCoreNLP进行切句子，实体标记，pos处理，语法依赖分析等工作，
 * 再将结果插入到sentence表中。
 * 
 * @author boboss
 *
 */

public class FinbaseNLPPipeline {

	StanfordCoreNLP pipeline;
	Connection con=null;
	String parse_maxlen="150";
	int max_text_length = 100000;
	int min_sentence_length =50;
	
	// For debug only  
	PrintWriter out ;
	
	public FinbaseNLPPipeline() {
		init();//init db connection.
	}
	
	/**
	 * 读db.url文件，连接到PostgreSQL数据库
	 */
	private void init(){	
		
		
		// 初始化parser的各种属性，包括中文模型的选定。
	    Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	    props.put("ner.model", "edu/stanford/nlp/models/ner/chinese.misc.distsim.crf.ser.gz");
	    props.put("ner.useSUTime", false);
	    props.put("ner.applyNumericClassifiers", false);
	    props.put("pos.model", "edu/stanford/nlp/models/pos-tagger/chinese-distsim/chinese-distsim.tagger");
	    props.put("ssplit.newlineIsSentenceBreak", "always");
	    props.put("ssplit.boundaryTokenRegex", "。");
	    props.put("parse.model", "edu/stanford/nlp/models/lexparser/xinhuaPCFG.ser.gz");
	    props.put("parse.maxlen", parse_maxlen);
	    //props.put("ssplit.tokenPatternsToDiscard", ",");
	   
	    //创建文本处理流
	     pipeline = new StanfordCoreNLP(props);
		
	     
	    //初始化数据库连接
		try {
			//for debug only
			out=new PrintWriter("./debug_output");
			
        	File file=new File("./db.url");
	            //读取每个文件内容
	        InputStreamReader read = new InputStreamReader(
	                     new FileInputStream(file));//考虑到编码格式
	        BufferedReader bufferedReader = new BufferedReader(read);
	        String url = "jdbc:"+ bufferedReader.readLine();
	        System.out.println("连接到数据库。。。。" + url);
	        read.close();
	      
	        Properties dbprops = new Properties();
	       // props.setProperty("user","boboss");
	       // props.setProperty("password","");
	        con = DriverManager.getConnection(url, dbprops);
	        
	        
	       //先删除已有sentence表格，
	    	Statement st = con.createStatement();
	    	String sql = "DROP TABLE IF EXISTS sentences";
			st.executeUpdate(sql);
		
			//再创建新的空表。
			sql = "CREATE TABLE sentences(" +
	  	          "document_id text,"+
				  "sentence text,"+
				  "words text[],"+
				  "lemma text[],"+
				  "pos_tags text[],"+
				  "dependencies text[],"+
				  "ner_tags text[],"+
				  "sentence_offset bigint,"+
				  "sentence_id text"+
				  ")";
			st.executeUpdate(sql);
			st.close();
			
	    } catch (Exception e) {
	    		System.out.println(e.getMessage());
	    		e.printStackTrace();	
	   }		
	}
	
	
	
	/*
	 * 函数用于写入Sentence表。
	 * 
	 * CREATE TABLE sentences(
  			  document_id text,
			  sentence text,
			  words text[],
			  lemma text[],
			  pos_tags text[],
			  dependencies text[],
			  ner_tags text[],
			  sentence_offset bigint,
			  sentence_id text -- unique identifier for sentences
			  );
	 * 
	 * 
	 */
	private void insertSentences(String document_id,
								String sentence, 
								String words,
								String lemma, 
								String pos_tags, 
								String dependencies,
								String ner_tags, 
								int sentence_offset,
								String sentence_id){
		
			
	    try {
	    	
	    	Statement st = con.createStatement();
	    
			//最后插入数据库
			String sql = "insert into sentences values('"+ 
						document_id +"','"+ 
						sentence +"','"+ 
						words +"','"+ 
						lemma +"','"+ 
						pos_tags +"','"+ 
						dependencies +"','"+ 
						ner_tags +"','"+ 
						sentence_offset +"','" +
						sentence_id+ "')";
			
			out.println(sql);
			st.executeUpdate(sql);
			st.close();
	    } catch (SQLException e) {
	    	
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	/***
	 * 
	 * 读取article表中的每一列，进行解析，然后调用insertSentences;
	 * @param anno
	 */
	public void annotateAllArticles(){	
		Statement st;
		try {
			st = con.createStatement();
		
			String sql = " select * from articles " ;
			ResultSet rs = st.executeQuery(sql);
			
			while (rs.next())
			{
				
				String article_id=rs.getString(1);
				String text=rs.getString( 2 );
				String file_name=rs.getString(3);
				
				if(text.length()>max_text_length) continue; // 不处理过大的文本。 
				
				System.out.println("Processing article:" + article_id + ":"+file_name);
				try {
					this.annotateOneArticle(article_id, text);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} 
			rs.close();	
			st.close();
			out.flush(); 
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	private void annotateOneArticle(String article_id, String text) throws FileNotFoundException{
		//把所有的","换成中文的逗号，避免转换成数组之后发生符号冲突。
		text=text.replaceAll(",","");
		
		String document_id=article_id;
		String sentence=null;
		String words="";
		String lemma="";
		String pos_tags=""; 
		String dependencies="";
		String ner_tags="";
		int sentence_offset=0;
		String sentence_id= "";
		
	    out.println("Start to annotate:" + article_id + "...................");
		//out.println(text);
		
		// Initialize an Annotation with some text to be annotated. The text is the argument to the constructor.
		Annotation annotation=new Annotation(text);
		
		pipeline.annotate(annotation);

		//读取每一个sentence，获取词列表、POS 列表、NER列表、语法分析树等。
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		for(CoreMap sentence_map:sentences){
			    
			    sentence=sentence_map.toString();
			    
			    if(sentence.length()<min_sentence_length) continue; //不处理长度小于5的句子。
			    //out.println("Process new sentence -----");
		    	//out.println(sentence);
		    	
 		    	List<String> word_list =new ArrayList<String>();
 		    	List<String> lemma_list=new ArrayList<String>();
 		    	List<String> pos_list=new ArrayList<String>();
 		    	List<String> ner_list=new ArrayList<String>();
		    	for(CoreMap token : sentence_map.get(CoreAnnotations.TokensAnnotation.class)){
		    		// generate words:	
		    		word_list.add(token.get(CoreAnnotations.TextAnnotation.class));
		    		lemma_list.add(token.get(CoreAnnotations.LemmaAnnotation.class));
		    		pos_list.add(token.get(CoreAnnotations.PartOfSpeechAnnotation.class));
		    		ner_list.add(token.get(CoreAnnotations.NamedEntityTagAnnotation.class));		    			
		    	}
		    	
		    	words = "{" + word_list.toString().replaceAll("[\\[\\]]", "")+ "}"; // "[\\[\\]]" 是为了去掉数组toString()后在头尾产生的[和]符号，下同。
		    	
		    	
		    	lemma="{" +lemma_list.toString().replaceAll("[\\[\\]]", "")+ "}";
		    	pos_tags="{" +pos_list.toString().replaceAll("[\\[\\]]", "")+ "}";
		    	ner_tags="{" +ner_list.toString().replaceAll("[\\[\\]]", "")+ "}";
		    	sentence_id = document_id + "@" + sentence_offset;	
		    	
		    	//generate dependences;
		    	SemanticGraph graph = sentence_map.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
		    	dependencies="{\"" +graph.toList()+ "\"}";
		        CharSequence cs1= "\n";
		        CharSequence cs2="\",\"";
		        dependencies= dependencies.replace(cs1,cs2); //把graph.toList()产生的换行符\n替换成","，原因是数据库对应字段是text []数组。
		        cs1="\",\"\"";
		        cs2="\"";
		    	dependencies= dependencies.replace(cs1,cs2);//去掉尾部多余的\",\""符号
		       
		       
		    	System.out.println(dependencies);
		    		
		    
		    	//out.println("words:" + words);
		    	//out.println("lemma:" +lemma);
		    	//out.println("pos:"+pos_tags);
		    	//out.println("dependence:" + dependencies);
		    	//out.println("ner:"+ner_tags);
		    	//out.println();
		    	
		    	this.insertSentences(document_id, sentence, words, lemma, pos_tags, dependencies, ner_tags, sentence_offset, sentence_id);
			
		    	sentence_offset++;
		}
   
		//out.println();
		//out.println(); 	    
	}

	public static void main(String[] args) {
		FinbaseNLPPipeline fp=new FinbaseNLPPipeline();
		fp.annotateAllArticles();
	}

}
