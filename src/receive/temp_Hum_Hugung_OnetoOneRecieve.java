package receive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.operation.CRC;
import com.operation.byteandstring;
import com.operation.compareData;
import com.operation.dataset;
import com.operation.picOpration;


public class temp_Hum_Hugung_OnetoOneRecieve extends Thread{

	JTextArea showMsg=null;
	JTextArea showSendMsg=null;
	DatagramSocket dsSocket=null;
	DatagramPacket sendDp=null;
	DatagramPacket recieveDp=null;
	int portText=9001;
	
	byteandstring bs=new byteandstring();
	dataset ds=new dataset();
	CRC crc=new CRC();
	int flag;
	
	String huStartTime="00:00:00";
	String huEndTime="24:00:00";
	compareData cd=new compareData();
	picOpration po=new picOpration();
	
	
	Date currentDay;  //获取当前日期
	//统计计数，确保弧光1分钟统计，而温度5分钟统计一次
	int timeCount = 0;

	
	public temp_Hum_Hugung_OnetoOneRecieve(JTextArea showJTextArea,JTextArea showSendMsg,int port,int flag) throws SQLException
	{
		this.flag=flag;
		this.showSendMsg=showSendMsg;
		this.showMsg=showJTextArea;
		this.portText=port;
		bs=new byteandstring();
		
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		
		currentDay = Calendar.getInstance().getTime();

		String getP="select * from Interface";
		ResultSet rs=ds.select(getP);
		if(rs!=null)
		{
			while(rs.next())
			{
				this.huStartTime=rs.getString("huStartTime");
				this.huEndTime=rs.getString("huEndTime");
			}
			rs.close();
		}
//		String getHGdeviceCount="select * from SampleAddress where Sample_IndexID is not null and Sample_AddressH is not null and Sample_AddressL is not null and   Sample_dataL='"+portText+"'";
//		rs=ds.select(getHGdeviceCount);
//		if(rs!=null){
//			try {
//				if(rs.next())
//				{					
//					int HgDeviceCount=rs.getInt(1);
//					if(HgDeviceCount!=0){
//						HgFlag = new int[HgDeviceCount+1];
//						for(int i= 0;i<HgFlag.length;i++)
//						{
//							HgFlag[i]=0;//为每一台弧光设备添加一个标志位，该标志位被后面的弧光次数统计所用
//						}
//					}
//				}
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
		rs=null;
		ds.close();
		
	}
	
	public void ShowMessage(String msg)
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
	
	public void ShowSendMessage(String msg)
	{
		
		if(this.showSendMsg.getLineCount()>500)
		{
			this.showSendMsg.setText(msg);
		}
		else
		{
			this.showSendMsg.setText(this.showSendMsg.getText()+"\r\n"+msg);
		}
	}

	public void run()
	{	
		//每天将前一天的temperatureHistory表中的数据更新到temperatureHistory3H中
		if(!(new SimpleDateFormat("yyyy-MM-dd")).format(currentDay).
				 	equals((new SimpleDateFormat("yyyy-MM-dd")).format(Calendar.getInstance().getTime())))
		{
			currentDay= Calendar.getInstance().getTime();
			ds.executeUpdateTempHistory3H();
		}
		try 
		{
			dsSocket=new DatagramSocket(portText);
		}
		catch (SocketException e) 
		{
			JOptionPane.showMessageDialog(null, "端口  "+portText+" 被其他程序占用！", "监听端口初始化提示", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		byte[] sendData=new byte[8];
		//获取所有的温度采样点
		String getdevice="select * from  DeviceSet where Device_Addr is not null and Device_IP is not null and Device_Port='"+portText+"'";
		String Device_Addr="";
		String Device_Feature="";
		String Device_StartH="";
		String Device_StartL="";
		String Device_dataL="";
		String Device_IP="";
		String Device_Port="9004";
		ResultSet rs=null;
		//获取所有的弧光采样点
		String getHGdevice="select * from SampleAddress where Sample_IndexID is not null and Sample_AddressH is not null and Sample_AddressL is not null and   Sample_dataL='"+portText+"'";
		
		String Sample_ID="";

		while(true)
		{

				try
				{
					rs=ds.select(getdevice);
					if(rs!=null)
					{
						while(rs.next())
						{  
							Device_Addr=rs.getString("Device_Addr");
							Device_StartH=rs.getString("Device_StartH");
							Device_StartL=rs.getString("Device_StartL");
							Device_dataL=rs.getString("Device_dataL");
							Device_Feature=rs.getString("Device_Feature");
							
							Device_IP=rs.getString("Device_IP");
							Device_Port=rs.getString("Device_Port");
							if(Device_Addr!=null&&Device_Addr.equals("null")==false&&Device_IP!=null&&Device_IP.equals("null")==false)
							{//Device_Addr!=null&&Device_Addr.equals("null")==false&&
								//ds=new DatagramSocket();
								int length=0;
	
								sendData[0]=byteandstring.hexStringTobytes(Device_Addr);
								sendData[1]=byteandstring.hexStringTobytes(Device_Feature);
								sendData[2]=bs.hexStringTobytes(Device_StartH);
								sendData[3]=bs.hexStringTobytes(Device_StartL);
								if(Device_dataL.length()>2)
								{
									byte[] le=new byte[2];
									le=byteandstring.hexStringToBytes(Device_dataL);
									sendData[4]=le[0];
									sendData[5]=le[1];
								}
								else
								{
									sendData[4]=0x00;
									sendData[5]=bs.hexStringTobytes(Device_dataL);
								}
								
								byte[] data= new byte[6];
								for(int j=0;j<6;j++)
								{
									data[j]=sendData[j];
									//System.out.print(Integer.toHexString(data[j]&0xFF)+" ");
								}
								crc.update(data,0,6);
						        byte d[]=crc.getCrcBytes();
						        sendData[6]=d[0];
						        sendData[7]=d[1];
								crc.reset();
								
								InetAddress address;
								try {
									address = InetAddress.getByName(Device_IP);
									sendDp=new DatagramPacket(sendData, sendData.length, address,Integer.parseInt(Device_Port));
								} catch (UnknownHostException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								//ShowSendMessage("Thread <"+flag+"> 自动采集cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
								po.writeLog("Thread <"+flag+"> 自动采集cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");//写入接收日志
						
								try 
								{
									dsSocket.send(sendDp);
									byte[] buf = new byte[400];
									DatagramPacket dp = new DatagramPacket(buf,0,buf.length);
									try
									{
										dsSocket.setSoTimeout(3000);
										dsSocket.receive(dp);
										
										byte[] returnData=dp.getData();
										int returnLenght=dp.getLength();
										
										String showpString="";
										for(int i=0;i<returnLenght;i++)
									    {
									    	String dataString=Integer.toHexString(returnData[i]&0xff);
									    	if(dataString.length()<2)
									    	{
									    		dataString="0"+dataString;
									    	}
									    	showpString=showpString.trim()+dataString;
									    }
										showpString=showpString+"\r\n";
										ShowMessage("温度数据包："+showpString);
										po.writeLog("温度数据包："+showpString);
										
										if(returnLenght>22)
										{
											String vid=Integer.toHexString(returnData[0]&0xff);
											byte productType03_08=returnData[1];
											if(productType03_08==0x03)
											{
												byte productType=returnData[4];
												if(returnLenght==59)
												{
													receivePoor(returnData,returnLenght);
												}
												else
												{
													ShowMessage("该条返回数据的格式不对：长度不正确！");
													po.writeLog("该条返回数据的格式不对：长度不正确！");
												}
											}
											else
											{
												ShowMessage("该条返回数据的格式不对：长度不正确！");
												po.writeLog("该条返回数据的格式不对：长度不正确！");
											}
										}
										else
										{
											ShowMessage("该条返回数据的格式不对：长度不正确！");
											po.writeLog("该条返回数据的格式不对：长度不正确！");
										}
									}
									catch (IOException e2)
									{
										System.out.println("dddd");
										e2.printStackTrace();
										
									}
									
									ShowSendMessage("Thread <"+flag+"> 自动采集cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
									
								} 
								catch (IOException e) 
								{
									e.printStackTrace();
								}
							}
							
						}
						rs.close();
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					ds.close();
				}
			
			
			System.gc();	
			

			Device_Addr="";
			Device_Feature="04";
			Device_StartH="00";
			Device_StartL="00";
			Device_dataL="";
			Device_IP="";
			Device_Port="9004";
			Sample_ID="";
			sendData=new byte[8];
			
		
			Date dt=new Date();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
			DateFormat dff = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dString=df.format(dt);
			String nowTime=dff.format(dt);
			//System.out.println(nowTime);
			
			String imgStartTime_current=dString+" "+huStartTime;		
			String imgEndTime_current=dString+" "+huEndTime;
			if(cd.compare_date(imgStartTime_current,imgEndTime_current)==0)
			{
				//System.out.println("系统工作时间区间设置错误！");
				ShowMessage("弧光系统工作时间区间设置错误！<>"+cd.compare_date(imgStartTime_current,imgEndTime_current)+"<>"+"imgStartTime_current<>"+imgStartTime_current+"imgEndTime_current<>"+imgEndTime_current);
			}
			else if(cd.compare_date(imgStartTime_current,nowTime)<1&&cd.compare_date(imgEndTime_current,nowTime)>-1)
			{
				ShowMessage("弧光系统非工作时间！<>");
			}
			else
			{
				ShowMessage("弧光系统工作时间！<>");
				try
				{
					rs=ds.select(getHGdevice);
					//System.out.println(getHGdevice);
					if(rs!=null)
					{
						while(rs.next())
						{							
							Sample_ID=rs.getString("Sample_ID");
							Device_Addr=rs.getString("Sample_IndexID");
							Device_dataL=rs.getString("Sample_AddressH");
							Device_IP=rs.getString("Sample_AddressL");
							Device_Port=rs.getString("Sample_dataL");
							if(Device_Addr!=null&&Device_Addr.equals("null")==false&&Device_IP!=null&&Device_IP.equals("null")==false)
							{
								int length=0;
								sendData[0]=byteandstring.hexStringTobytes(Device_Addr);
								sendData[1]=byteandstring.hexStringTobytes(Device_Feature);
								sendData[2]=bs.hexStringTobytes(Device_StartH);
								sendData[3]=bs.hexStringTobytes(Device_StartL);
								if(Device_dataL.length()>2)
								{
									byte[] le=new byte[2];
									le=byteandstring.hexStringToBytes(Device_dataL);
									sendData[4]=le[0];
									sendData[5]=le[1];
								}
								else
								{
									sendData[4]=0x00;
									sendData[5]=bs.hexStringTobytes(Device_dataL);
								}
								
								byte[] data= new byte[6];
								for(int j=0;j<6;j++)
								{
									data[j]=sendData[j];
								
								}
								crc.update(data,0,6);
						        byte d[]=crc.getCrcBytes();
						        sendData[6]=d[0];
						        sendData[7]=d[1];
								crc.reset();

								String sendString="";
								for(int i=0;i<8;i++)
							    {
							    	String dataString=Integer.toHexString(sendData[i]&0xff);
							    	if(dataString.length()<2)
							    	{
							    		dataString="0"+dataString;
							    	}
							    	sendString=sendString.trim()+dataString;
							    }
								sendString=sendString+"\r\n";
								ShowSendMessage("弧光cmd："+sendString);
								po.writeLog("弧光cmd："+sendString);
								
								InetAddress address;
								try {
									address = InetAddress.getByName(Device_IP);
									sendDp=new DatagramPacket(sendData, sendData.length, address,Integer.parseInt(Device_Port));
								} catch (UnknownHostException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								
								try 
								{
									dsSocket.send(sendDp);
									byte[] buf = new byte[400];
									DatagramPacket dp = new DatagramPacket(buf,0,buf.length);
									try
									{
										//System.out.println("kk");
										dsSocket.setSoTimeout(2000);
										dsSocket.receive(dp);
										//System.out.println("kds");
										
										//System.out.println("kdddk");
										byte[] returnData=dp.getData();
										int returnLength=dp.getLength();
										
										String showpString="";
										for(int i=0;i<returnLength;i++)
									    {
									    	String dataString=Integer.toHexString(returnData[i]&0xff);
									    	if(dataString.length()<2)
									    	{
									    		dataString="0"+dataString;
									    	}
									    	showpString=showpString.trim()+dataString;
									    }
										showpString=showpString+"\r\n";
										ShowMessage("弧光数据包："+showpString);
										po.writeLog("弧光数据包："+showpString);
										//System.out.println("弧光数据包："+showpString);
										
										if(returnLength==40)
										{
											
											byte productType03_08=returnData[1];
											if(productType03_08==0x04)
											{
							
												crc.update(returnData,0,(returnLength-2));
										        byte test[]=crc.getCrcBytes();
												crc.reset();
												if(test[0]!=returnData[38]||test[1]!=returnData[39])
												{
													ShowMessage("该条返回数据的格式不对：CRC校验码不正确！");
													po.writeLog("该条返回数据的格式不对：CRC校验码不正确！");
												}
												else
												{
													receiveHg(returnData,returnLength,Sample_ID);
												}
											}
										}
										else
										{
											ShowMessage("弧光设备返回不正确，长度不是40！");
											po.writeLog("弧光设备返回不正确，长度不是40！");
										}
										System.gc();	
									}
									catch (IOException e2)
									{
										e2.printStackTrace();
									}
									
									ShowSendMessage("Thread <"+flag+"> 自动采集cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
									
								} 
								catch (IOException e) 
								{
									e.printStackTrace();
								}
							}
							else
							{
								
							}
						}
					}
					rs.close();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					ds.close();
				}
			}
			
			ds.close();	
			
			System.gc();
			try 
			{
				Thread.sleep(60000);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	protected  void receiveHg(byte[] returnData,int returnLenght,String Sample_ID) throws SQLException, IOException
	{
		ResultSet rs=null;
		String device_Address="";
		device_Address=bs.bytesToHexString(returnData[0]);
		int hg_count=0;
		int hg_high=0;
		int hg_low=0;   

		SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
		hg_count=(returnData[3]&0xFF);    //总次数		
		hg_high=(returnData[5]&0xFF);   //强弧次数				             
		hg_low=hg_count-hg_high;        //弱弧次数
		

		
		int hg_recent1_time=0;
		int hg_recent1_date=0;
		String hg_1_date="";
		hg_recent1_time = (returnData[7] & 0xFF);
		hg_recent1_date = ((returnData[8]<<8) & 0xFF00) | (returnData[9] & 0xFF);
		Date d = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		hg_1_date=df.format(new Date(d.getTime() - hg_recent1_date*1000));    
		int hg_recent2_time=0;
		int hg_recent2_date=0;
		String hg_2_date="";
		hg_recent2_time = (returnData[11] & 0xFF);
		hg_recent2_date = ((returnData[12]<<8) & 0xFF00) | (returnData[13] & 0xFF);
		hg_2_date=df.format(new Date(d.getTime() - hg_recent2_date*1000));
		int hg_recent3_time=0;
		int hg_recent3_date=0;
		String hg_3_date="";
		hg_recent3_time = (returnData[15] & 0xFF);
		hg_recent3_date = ((returnData[16]<<8) & 0xFF00) | (returnData[17] & 0xFF);
		hg_3_date=df.format(new Date(d.getTime() - hg_recent3_date*1000));
		int hg_recent4_time=0;
		int hg_recent4_date=0;
		String hg_4_date="";
		hg_recent4_time = (returnData[19] & 0xFF);
		hg_recent4_date = ((returnData[20]<<8) & 0xFF00) | (returnData[21] & 0xFF);
		hg_4_date=df.format(new Date(d.getTime() - hg_recent4_date*1000));
		int hg_recent5_time=0;
		int hg_recent5_date=0;
		String hg_5_date="";
		hg_recent5_time = (returnData[23] & 0xFF);
		hg_recent5_date = ((returnData[24]<<8) & 0xFF00) | (returnData[25] & 0xFF);
		hg_5_date=df.format(new Date(d.getTime() - hg_recent5_date*1000));
		int hg_recent6_time=0;
		int hg_recent6_date=0;
		String hg_6_date="";
		hg_recent6_time = (returnData[27] & 0xFF);
		hg_recent6_date = ((returnData[28]<<8) & 0xFF00) | (returnData[29] & 0xFF);
		hg_6_date=df.format(new Date(d.getTime() - hg_recent6_date*1000));
		int hg_recent7_time=0;
		int hg_recent7_date=0;
		String hg_7_date="";
		
		
		hg_recent7_time = (returnData[31] & 0xFF);
		hg_recent7_date = ((returnData[32]<<8) & 0xFF00) | (returnData[33] & 0xFF);
		hg_7_date=df.format(new Date(d.getTime() - hg_recent7_date*1000));
//		String insertHgNum_count="insert into AlarmLogArc(Sample_ID,AlarmNum,AlarmType,Odate) values('"
//								+Sample_ID+"','"+hg_count+"','0','"+df.format(new Date())+"')";
//		String insertHgNum_high="insert into AlarmLogArc(Sample_ID,AlarmNum,AlarmType,Odate) values('"
//							+Sample_ID+"','"+hg_high+"','1','"+df.format(new Date())+"')";
	
		String insertRecentHg="insert into recentHg(Sample_ID,hg_recent1_time,hg_recent1_date,"
							+"hg_recent2_time,hg_recent2_date,hg_recent3_time,hg_recent3_date,"
							+"hg_recent4_time,hg_recent4_date,hg_recent5_time,hg_recent5_date,"
							+"hg_recent6_time,hg_recent6_date,hg_recent7_time,hg_recent7_date) "
							+"values('"+Sample_ID+"',"+hg_recent1_time+",'"+hg_1_date+"',"
							+hg_recent2_time+",'"+hg_2_date+"',"+hg_recent3_time+",'"
							+hg_3_date+"',"+hg_recent4_time+",'"+hg_4_date+"',"
							+hg_recent5_time+",'"+hg_5_date+"',"+hg_recent6_time+",'"+hg_6_date
							+"',"+hg_recent7_time+",'"+hg_7_date+"')";
		String updateRecentHg="update recentHg set hg_recent1_time="+hg_recent1_time
							+",hg_recent1_date='"+hg_1_date+"',hg_recent2_time="+hg_recent2_time
							+",hg_recent2_date='"+hg_2_date+"',hg_recent3_time="+hg_recent3_time
							+",hg_recent3_date='"+hg_3_date+"',hg_recent4_time="+hg_recent4_time
							+",hg_recent4_date='"+hg_4_date+"',hg_recent5_time="+hg_recent5_time
							+",hg_recent5_date='"+hg_5_date+"',hg_recent6_time="+hg_recent6_time
							+",hg_recent6_date='"+hg_6_date+"',hg_recent7_time="+hg_recent7_time
							+",hg_recent7_date='"+hg_7_date+"' where Sample_ID='"+Sample_ID+"'";
		String checkRecentHg="select count(*) from recentHg where Sample_ID='"+Sample_ID+"'";
//		String getRecentHg= "select hg_recent1_time,hg_recent1_date from recentHg where Sample_ID = '"+Sample_ID+"'";
		
		
//		int f1=ds.update(insertHgNum_count);
//		int f2=ds.update(insertHgNum_high);
		//更新最近弧光
		int countRecentHg=0;
		if(hg_recent1_time!=0)
		{
			rs=ds.select(checkRecentHg);
			if(rs!=null)
			{
				while(rs.next())
				{
					countRecentHg=rs.getInt(1);
				}
			}
			
			if(countRecentHg==0)
			{
				ds.update(insertRecentHg);
			}
			else
			{
//				rs=ds.select(getRecentHg);
//				if(rs!=null)
//				{
//					if(rs.next())
//					{
//						int recentHgTimeOld1 = rs.getInt(1);
//						if(recentHgTimeOld1 != hg_recent1_time)ds.update(updateRecentHg);
//					}
//				}
				if(hg_count!=0)
				{
					
					ds.update(updateRecentHg);
				}
				
			}
		}

//		System.out.println("step into updateHgcishu");
		updateHgcishu(Sample_ID,hg_high,hg_low);
//		System.out.println("step out updateHgcishu");
		//rs.close();
		System.gc();
	}
	
	protected  void receivePoor(byte[] returnData,int returnLenght) throws SQLException, IOException
	{
		ResultSet rs=null;
		int failnum=0;
		String device_Address="";
		device_Address=bs.bytesToHexString(returnData[3]);
		float Para_PreExceed_Tem=40;
		float Para_Exceed_Tem=60;
		String getAlartmT="select Para_PreExceed_Tem,Para_Exceed_Tem from  Parameter where Substation_ID in(select Substation_ID from DeviceSet,Device where Device.Device_ID=DeviceSet.Device_ID and Device_Addr='"+device_Address+"')";
		rs=ds.select(getAlartmT);
		if(rs!=null)
		{
			while (rs.next()) 
			{
				Para_PreExceed_Tem=rs.getFloat(1);
				Para_Exceed_Tem=rs.getFloat(2);
			}
		}
		/*String FirstAddrString="";
		String getFirstAddrString="select Device_StartH,Device_StartL from DeviceSet where Device_Addr='"+device_Address+"'";
		rs=ds.select(getFirstAddrString);
		if(rs!=null)
		{
			while (rs.next()) 
			{
				if(rs.getString(1)!=null&&rs.getString(1).trim().equals("")&&rs.getString(1).trim().equals("null"))
				{
					FirstAddrString=FirstAddrString+rs.getString(1);
				}
				else
				{
					FirstAddrString="00";
				}
				if(rs.getString(2)!=null&&rs.getString(2).trim().equals("")&&rs.getString(2).trim().equals("null"))
				{
					FirstAddrString=FirstAddrString+rs.getString(2);
				}
				else
				{
					FirstAddrString=FirstAddrString+"00";
				}
			}
		}*/
		float tvalue=0;
		for(int i=10;i<57;i++)
		{
			tvalue=(returnData[i]&0xFF);
			if(tvalue==255)
			{
				tvalue=0;
			}
			else
			{
				String sample_id="";
				String get_sampleid="select Sample_ID from SampleAddress where Device_Address='"+device_Address+"' and Sample_IndexID='"+(i-9)+"'";
				rs=ds.select(get_sampleid);
				if(rs!=null)
				{
					while(rs.next())
					{
						sample_id=rs.getString(1);
					}
				}
				rs.close();
				if(sample_id.equals("")||sample_id==null)
				{
				}
				else
				{
					failnum++;
					ds.execute(tvalue, 0, 0,sample_id);
				}
			}
		}
		rs=null;
		
		ShowMessage(device_Address+"成功接收有效数据:"+failnum+" ;失败: "+(48-failnum));
		System.gc();
	}
	
	//更新弧光次数
	//获取数据库中是否有当天的弧光次数
	protected void updateHgcishu(String Sample_ID,int hg_high,int hg_low) throws SQLException
	{
		
		int hg_count = hg_high+hg_low;
		SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
		String checkHgCount="select count(*) from Hgcishu where Datetime = CONVERT(varchar(100), GETDATE(), 23) and Sample_ID = '"
								+Sample_ID+"'";
	
		//查询数据库中的弧光次数
		String getHgCount="select Qianghu,Ruohu,Total,HgFlag from Hgcishu where Sample_ID = '"+ Sample_ID 
							+"' and Datetime = CONVERT(varchar(100), GETDATE(), 23)";

		//获取数据库中标志位
//		String getFlag = "select HgFlag from Hgcishu where Sample_ID = '"+ Sample_ID 
//				+"' and Datetime = CONVERT(varchar(100), GETDATE(), 23)";
		ResultSet rs = null;
		rs = ds.select(checkHgCount);
		int count=0;
		if(rs!=null)
		{
			if(rs.next())
				count=rs.getInt(1);
		}
		


		if(count==0)
		{
			//向数据库中添加一条当天的弧光次数记录
			String insertHgCount="insert into Hgcishu(Sample_ID,Qianghu,Ruohu,Total,Datetime) values('"
							+Sample_ID+"',"+hg_high+","+hg_low+","+hg_count+",'"+dfDate.format(new Date())+"')";
			ds.update(insertHgCount);

		}else{
			if(hg_count!=0){
				rs = ds.select(getHgCount);
				if(rs.next())
				{
					int hg_high_old = rs.getInt(1);
					int hg_low_old = rs.getInt(2);
					int hg_count_old = rs.getInt(3);
//					System.out.println(hg_count_old);
//					System.out.println(hg_count);
					hg_high += hg_high_old;
					hg_low += hg_low_old;
					hg_count += hg_count_old;
//					System.out.println(hg_count);
					//更新数据库中的弧光次数
					String updateHgCount="update Hgcishu set Qianghu ="+hg_high+",Ruohu="+hg_low+",Total="
										+hg_count+" where Sample_ID = '"+ Sample_ID 
										+ "' and Datetime = CONVERT(varchar(100), GETDATE(), 23)";
					ds.update(updateHgCount);
				}	
			}
		}
	}	
}


