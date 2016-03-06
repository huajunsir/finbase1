package cn.edu.zju.finbase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.UUID;




public class ArticleUUID {
	private String db_url="";
	private String user_name="root";
	private Connection con=null;
	
	 private void init(){	   	
		    //初始化数据库连接
			try {
				String url = "jdbc:"+ db_url;	      
		        Properties dbprops = new Properties();
		        dbprops.setProperty("user",user_name);
		        dbprops.setProperty("password","");
		        con = DriverManager.getConnection(url, dbprops);
		        System.out.println("成功连接到数据库--------" + url);				
		    } catch (Exception e) {
		    		System.out.println(e.getMessage());
		    		e.printStackTrace();	
		   }		
		}
	
	public ArticleUUID() {
		// TODO Auto-generated constructor stub
		//init();
	}
	
	public void updateArticleIDWithUUID() throws SQLException{
	        
	        Statement st = con.createStatement();
		    Statement st2=con.createStatement();
			//最后插入数据库
			String sql = "select * from articles";
			
			ResultSet rs=st.executeQuery(sql);
			
			while(rs.next()){
				String id=rs.getString(1);
				String uuid= UUID.randomUUID().toString();
				
				String usql=" update articles set id='"+uuid+"' where id='"+id+"'";
				st2.executeUpdate(usql);
			}
						
			//System.out.println(sql);
			//st.executeUpdate(sql);
			st.close();
			st=null;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ArticleUUID fp=new ArticleUUID();
		if(args.length<2) {
			System.out.println("请输入至少两个参数（按顺序）：数据库的连接字符串，登录用户名");
		
		}else{
			fp.setDb_url(args[0]);
			fp.setUser_name(args[1]);
			fp.init();
			try {
				fp.updateArticleIDWithUUID();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println(args[0]);
		}

	}

	public String getDb_url() {
		return db_url;
	}

	public void setDb_url(String db_url) {
		this.db_url = db_url;
	}

	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

}
