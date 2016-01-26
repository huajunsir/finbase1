package cn.edu.zju.finbase;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

public class GongBaoHTMLGenerator {

	int startYear=2015;
	int endYear=2015;
	int startMonth=1;
	int endMonth=10;
	
	String head = "http://xinpi.cs.com.cn/new/search.html?t=b&";
	
	public GongBaoHTMLGenerator() {
		// TODO Auto-generated constructor stub
	}

	public void creatHTMLForJiaoyi() throws FileNotFoundException{
		
		String tail="&c=&q=&m=012001&s=0117";
		
		
		StringBuilder sb = new StringBuilder();  
	  
	    PrintStream printStream = new PrintStream(new FileOutputStream("data/spider/jiaoyi_hushi.html"));  
	    sb.append("<html>");  
	    sb.append("<head>");  
	    sb.append("<meta http-equiv=\"Content-type\" content=\"text/html; charset=utf-8\" />");
	    sb.append("<title>沪市交易</title>");  
	    sb.append("</head>");  
	    sb.append("<body bgcolor=\"#FFF8DC\">");  
	    sb.append("<br/>");  
	    sb.append("<br/>");  
	    for(int i=startYear;i<=endYear;i++)
	    	for(int j=startMonth;j<=endMonth;j++)
	    		for(int k=1;k<=31;k++){
	    			//http://xinpi.cs.com.cn/new/search.html?t=b&st=2016-01-25&et=2016-01-27&c=&q=&m=012001&s=0117
	    			//<li><a target="_blank" href="http://m.weibo.com/web/cellphone.php#iphone"  suda-uatrack="key=index_new_menu&value=sina_apps_list_click">新浪微博</a></li>
	    		   
	    			String url=head+ "st=" + i + "-" + j + "-" + k + "&et=" + i + "-" + j + "-" + k + tail;
	    			sb.append("<li><a href=\""+url+"\">");
	    			sb.append(url);
	    			sb.append("</a> </li>");
	    		}
	   
	    sb.append("<br/><br/>");  
	    sb.append("</body></html>");
	    
	    System.out.println(sb.toString());
        printStream.println(sb.toString());  
        printStream.flush();
        printStream.close();
	}
	
	public static void main(String[] args) {
		GongBaoHTMLGenerator gbhg=new GongBaoHTMLGenerator();
		try {
			gbhg.creatHTMLForJiaoyi();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
