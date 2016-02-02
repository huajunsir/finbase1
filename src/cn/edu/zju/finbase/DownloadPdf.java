package cn.edu.zju.finbase;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadPdf {

	public static void main(String[] args) {
		//new DownloadPdf().analyse("http://www1.ap.dell.com/content/topics/global.aspx/services/service-contracts/service-contracts?c=cn&l=zh&s=dhs&cs=cndhs1&~ck=anavml&redirect=1");
		new DownloadPdf().analyse("http://xinpi.cs.com.cn/new/file/bulletin/2015/12/");
		//http://www1.ap.dell.com/content/topics/global.aspx/services/service-contracts/service-contracts?c=cn&l=zh&s=dhs&cs=cndhs1&~ck=anavml&redirect=1
	}
	
	private void analyse(String site) {
		// ������ʽ��������ҳ��<a href="http://*****.pdf"��ʽ���ļ�
		String regex = "<A HREF=\"(/new/file/bulletin/2016/1/[0-9]*.PDF)\">";
	   //  String regex = "<a href=\\\"/new/file/bulletin/2016/1/\\\d+\\\.pdf\\\">";
	  //String regex = "\\<a[^\\<|^\\>]*href=[\\'|\\\"]([^\\<|^\\>]*\\.pdf)[\\'|\\\"][^\\<|^\\>]*[\\>|\\/\\>]";
		Pattern p = Pattern.compile(regex);
		
		List<String> pdfList = new ArrayList<String>();
		
		try {
			URL url = new URL(site);
			
			InputStream is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				Matcher m = p.matcher(line); 
				while(m.find()){
		  // System.out.println(m.group(1));
					// �ѽ����õ�pdf�����ص�ַ�ŵ�list��
					pdfList.add(m.group(1));
				}
			}
			br.close();
			is.close();
			
			String dir = "d:/2016.1/";
			File file = new File(dir);
			if (!file.exists()) {
				file.mkdirs();
			}
			
			// ����pdf��ַ��list���������
			for (String pdf : pdfList) {
				URL u = new URL("http://xinpi.cs.com.cn/"+pdf);
				InputStream i = u.openStream();
				byte[] b = new byte[1024*1024];
				int len;
				String fileName = pdf.substring(pdf.lastIndexOf("/"));
				OutputStream bos = new FileOutputStream(new File(dir + fileName));
				while ((len = i.read(b)) != -1) {
					bos.write(b, 0, len);
				}
				bos.flush();
				bos.close();
				i.close();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}