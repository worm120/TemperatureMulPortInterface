package receive;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;


import com.operation.compareData;
import com.operation.dataset;
import com.operation.picOpration;


public class receiveImg2  extends Thread
{

	JTextArea showMsg=null;
	String picurl="";
	int picFrequence=10;
	String sid="none";
	
	String imgStartTime="00:00:00";
	String imgEndTime="24:00:00";
	compareData cd=new compareData();
	picOpration po=new picOpration();
	
	
	public static void main(String[] args) {
		
	}
	
	public receiveImg2(JTextArea showJTextArea) throws SQLException
	{
		dataset ds=new dataset();
		String getP="select * from Interface";
		ResultSet rs=ds.select(getP);
		if(rs!=null)
		{
			while(rs.next())
			{
				this.picurl=rs.getString("Imagepath");
				this.imgStartTime=rs.getString("imgStartTime");
				this.imgEndTime=rs.getString("imgEndTime");
			}
			rs.close();
		}
		String getF="select Para_Photo_Rate from Parameter";
		
		if(this.sid.equals("none")==false)
		{
			getF="select Para_Photo_Rate from Parameter Substation_ID='"+this.sid+"'";
		}
		
		rs=ds.select(getF);
		if(rs!=null)
		{
			while(rs.next())
			{
				if(rs.getInt("Para_Photo_Rate")>0)
				{
					this.picFrequence=rs.getInt("Para_Photo_Rate");
				}
			}
			rs.close();
		}

		ds.close();
		if(this.picurl.equals(""))
		{
			showMsg("未设置图像保存文件夹！");
		}
		this.showMsg=showJTextArea;
	}

	public receiveImg2(JTextArea showJTextArea,String sid) throws SQLException
	{
		dataset ds=new dataset();
		this.sid=sid;
		String getP="select * from Interface";
		ResultSet rs=ds.select(getP);
		if(rs!=null)
		{
			while(rs.next())
			{
				this.picurl=rs.getString("Imagepath");
				this.imgStartTime=rs.getString("imgStartTime");
				this.imgEndTime=rs.getString("imgEndTime");
			}
			rs.close();
		}
		String getF="select Para_Photo_Rate from Parameter";
		
		if(this.sid.equals("none")==false)
		{
			getF="select Para_Photo_Rate from Parameter where Substation_ID='"+this.sid+"'";
		}
		
		rs=ds.select(getF);
		if(rs!=null)
		{
			while(rs.next())
			{
				if(rs.getInt("Para_Photo_Rate")>0)
				{
					this.picFrequence=rs.getInt("Para_Photo_Rate");
				}
				
			}
			rs.close();
		}

		ds.close();
		if(this.picurl.equals(""))
		{
			showMsg("未设置图像保存文件夹！");
		}
		this.showMsg=showJTextArea;
		this.sid=sid;
	}
	
	
	public void showMsg(String msg)
	{
		if(this.showMsg.getLineCount()>500)
		{
			this.showMsg.setText(msg);
		}
		else
		{
			this.showMsg.setText(this.showMsg.getText()+"\r\n"+msg);
		}
	}
	
	public void run()
	{
		while (true)
		{
			Date dt=new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dString=df.format(dt);
			String nowTime=dff.format(dt);
			String imgStartTime_current=dString+" "+imgStartTime;		
			String imgEndTime_current=dString+" "+imgEndTime;
			if(cd.compare_date(imgStartTime_current,imgEndTime_current)==0)
			{
			}
			else if(cd.compare_date(imgStartTime_current,nowTime)<1&&cd.compare_date(imgEndTime_current,nowTime)>-1)
			{
				String imgpath=picurl+"photo";
				String picid="";
				dataset ds=new dataset();
				String get_sampleid="select Sample_IndexID,Sample_AddressH,Sample_AddressL from SampleAddress,Sample where Sample.Sample_ID=SampleAddress.Sample_ID and Sample_Type='00' and Sample_AddressH is not null and Sample_AddressL is not null";
				if(this.sid.equals("none")==false)
				{
					get_sampleid="select Sample_IndexID,Sample_AddressH,Sample_AddressL from SampleAddress,Sample where Sample.Sample_ID=SampleAddress.Sample_ID and Sample_Type='00' and Sample_AddressH is not null and Sample_AddressL is not null and Substation_ID='"+this.sid+"'";
				}
				ResultSet rs=ds.select(get_sampleid);
				if(rs!=null)
				{
					try 
					{
						while(rs.next())
						{
							String Sample_AddressH=rs.getString("Sample_AddressH");
							String Sample_AddressL=rs.getString("Sample_AddressL");
							picid=rs.getString("Sample_IndexID");
							
							if(Sample_AddressH!=null&&Sample_AddressH.trim().equals("")==false&&Sample_AddressL!=null&&Sample_AddressL.trim().equals("")==false&&picid!=null&&picid.trim().equals("")==false)
							{
								Date time=new Date();
								DateFormat format = new SimpleDateFormat("yyMMddHHmmss");  
								imgpath=this.picurl+"photo\\"+picid+"_"+format.format(time)+".jpg";
								String picname=picid+"_"+format.format(time)+".jpg";
								boolean k=po.getImg(Sample_AddressH,Sample_AddressL,picurl,imgpath);
								if(k)
								{
									picSQL(picid,picname,picurl);
								}
							}
						}
					}
					catch (SQLException e) 
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				try
				{
					rs.close();
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
				ds.close();
				
			}
			else if(cd.compare_date(imgStartTime_current,nowTime)==1)
			{
			}
			else if(cd.compare_date(imgEndTime_current,nowTime)==-1)
			{
			}

			try
			{
				Thread.sleep(this.picFrequence*1000*60*60);
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}
	

	public void picSQL2(String picid,String picname,String picurl) throws SQLException 
	{
		String getPath=picurl;
		String imagepath=getPath+"photo\\"+picname;
		int flag2=0;
		
    	File fileTC=new File(imagepath);
    	if(fileTC.exists())
    	{
    		if(fileTC.length()>0)
    		{
    			flag2=1;
    		}
    	}
    	
    	if(flag2==1)
    	{
    		Date time=new Date();
    		DateFormat format = new SimpleDateFormat("yyMMddHHmmss");  
    		dataset ds=new dataset();
    		ResultSet rs=null;
    		String device_Address="";
    		String sample_id="none";
    		String photoid="";

    		String get_sampleid="select Device_Address,SampleAddress.Sample_ID from SampleAddress,Sample where Sample.Sample_ID=SampleAddress.Sample_ID and Sample_Type='00' and Sample_IndexID='"+picid+"'";
    		
    		rs=ds.select(get_sampleid);
    		try 
    		{
    			if(rs!=null)
    			{
    				while(rs.next())
    				{
    					//System.out.println("kkkk");
    					sample_id=rs.getString(2);
    					device_Address=rs.getString(1);
    				}
    			}
    		} 
    		catch (SQLException e) 
    		{
    			e.printStackTrace();
    		}
    		System.out.println("sample_id:"+sample_id);
    		
    		if(sample_id.equals("none")||sample_id==null)
    		{
    			showMsg("摄像头 "+picid+": 未设置采样点数据接口信息！");
    		}
    		else
    		{
    			String insertPic="insert into Photo(Sample_ID,Photo_Name,Photo_Location,Date) values('"+sample_id+"','"+picname+"','../images/photo/"+picname+"','"+time.toLocaleString()+"')";
    			
    			int k=ds.update(insertPic);
    			if(k>0)
    			{
    				showMsg("图像存入数据库sucess！");
    				
    			}
    			
    		}
    		
    		try
    		{
    			rs.close();
    		}
    		catch (SQLException e) 
    		{
    			e.printStackTrace();
    		}
    		ds.close();
    	}

	}
	
	
	public void picSQL(String picid,String picname,String picurl) throws SQLException 
	{
		String getPath=picurl;
		String imagepath=getPath+"photo\\"+picname;
		int flag2=0;
    	File fileTC=new File(imagepath);
    	if(fileTC.exists())
    	{
    		if(fileTC.length()>0)
    		{
    			flag2=1;
    		}
    	}
    	
    	if(flag2==1)
    	{
    		Date time=new Date();
    		DateFormat format = new SimpleDateFormat("yyMMddHHmmss");  
    		dataset ds=new dataset();
    		ResultSet rs=null;
    		String device_Address="";
    		String sample_id="none";
    		String photoid="";

    		String get_sampleid="select Device_Address,SampleAddress.Sample_ID from SampleAddress,Sample where Sample.Sample_ID=SampleAddress.Sample_ID and Sample_Type='00' and Sample_IndexID='"+picid+"'";
    		
    		rs=ds.select(get_sampleid);
    		try 
    		{
    			if(rs!=null)
    			{
    				while(rs.next())
    				{
    					//System.out.println("kkkk");
    					sample_id=rs.getString(2);
    					device_Address=rs.getString(1);
    				}
    			}
    		} 
    		catch (SQLException e) 
    		{
    			e.printStackTrace();
    		}
    		System.out.println("sample_id:"+sample_id);
    		
    		if(sample_id.equals("none")||sample_id==null)
    		{
    			showMsg("摄像头 "+picid+": 未设置采样点数据接口信息！");
    		}
    		else
    		{
    			String insertPic="insert into Photo(Sample_ID,Photo_Name,Photo_Location,Date) values('"+sample_id+"','"+picname+"','../images/photo/"+picname+"','"+time.toLocaleString()+"')";
    			
    			int k=ds.update(insertPic);
    			if(k>0)
    			{
    				showMsg("图像存入数据库sucess！");
    				String getid="select Photo_Id from Photo where sample_id='"+sample_id+"' and Date='"+time.toLocaleString()+"'";
    				rs=ds.select(getid);
    				while (rs.next()) 
    				{
    					photoid= rs.getString(1);
    				}
    			}
    		}
    		try
    		{
    			rs.close();
    		}
    		catch (SQLException e) 
    		{
    			e.printStackTrace();
    		}
    		ds.close();
    	}

	}
}
