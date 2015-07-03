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
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import com.opration.CRC;
import com.opration.byteandstring;
import com.opration.dataset;
import send.sendcmd;
import com.opration.compareData;

public class temp_Hum_Hugung_OnetoOneRecieve1 extends Thread{

	JTextArea showMsg=null;
	JTextArea showSendMsg=null;
	DatagramSocket dsSocket=null;//UDP链接
	DatagramPacket sendDp=null;//发送包
	DatagramPacket recieveDp=null;//接受包
	int portText=9001;//监听端口号
	//byteandstring bs=null;=new byteandstring();
	byteandstring bs=new byteandstring();
	dataset ds=new dataset();
	CRC crc=new CRC();
	int flag;
	
	String huStartTime="00:00:00";//弧光非工作周期开始时间
	String huEndTime="24:00:00";//弧光非工作周期结束时间
	compareData cd=new compareData();
	
	

	//public dataRecieve(JTextArea showJTextArea,JTextArea showSendMsg,int port,String picurl)
	public temp_Hum_Hugung_OnetoOneRecieve1(JTextArea showJTextArea,JTextArea showSendMsg,int port,int flag) throws SQLException
	{
		this.flag=flag;
		this.showSendMsg=showSendMsg;
		this.showMsg=showJTextArea;
		this.portText=port;
		bs=new byteandstring();
		
		//dataset ds=new dataset();
		String getP="select * from Interface";//获取弧光的非工作时间
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
		rs=null;
		ds.close();
		
	}
	public void ShowMessage(String msg)//显示接收数据
	{
		//this.showMsg.setText(showMsg.getText()+"\r\n"+msg);
		if(this.showMsg.getLineCount()>500)//数据超过一定的行数就清空
		{
			this.showMsg.setText(msg);
		}
		else
		{
			this.showMsg.setText(this.showMsg.getText()+"\r\n"+msg);
		}
	}
	public void ShowSendMessage(String msg)//显示发送数据
	{
		//this.showSendMsg.setText(showSendMsg.getText()+"\r\n"+msg);
		if(this.showSendMsg.getLineCount()>500)//数据超过一定的行数就清空
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
		//while(true)
		
		//获取所有的弧光采样点
		String getHGdevice="select * from SampleAddress where Sample_IndexID is not null and Sample_AddressH is not null and Sample_AddressL is not null and   Sample_dataL='"+portText+"'";
		
		String Sample_ID="";//采样点编号
		/*相应变量在弧光采集中的含义
		String Device_Addr="";//主机地址，为SampleAddress表中的Sample_IndexID字段
		String Device_Feature="04";//弧光传感器设备特征值默认04
		String Device_StartH="00";//弧光传感器其实地址默认00 00
		String Device_StartL="00";
		String Device_dataL="";//弧光传感器读取数据长度，为SampleAddress表中的Sample_AddressH字段
		String Device_IP="";//目标ip，为SampleAddress表中的Sample_AddressL字段
		String Device_Port="9005";//目标port，为SampleAddress表中的Sample_dataL字段
		ResultSet rs=null;
		*/
		
		
		while(true)
		{
//////////////////////////////////////先进行温度数据采集/////////////////////////////////////////////////////////////////////
			try
			{
				rs=ds.select(getdevice);
				if(rs!=null)
				{
					while(rs.next())
					{  
						//System.out.println("wendu start");
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
							/******************不同设备发送命令的格式不同*****************************/
							//sendData[1]=0x03;
							sendData[1]=byteandstring.hexStringTobytes(Device_Feature);
							/***********************************************/
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
							crc.reset();//每次用完必须重置，否则crc码错误
							
							InetAddress address;
							try {
								address = InetAddress.getByName(Device_IP);
								sendDp=new DatagramPacket(sendData, sendData.length, address,Integer.parseInt(Device_Port));
							} catch (UnknownHostException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							//ShowSendMessage("Thread <"+flag+"> 自动采集cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
							writeLog("Thread <"+flag+"> 自动采集cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");//写入接收日志
							
							
							try 
							{
								dsSocket.send(sendDp);//发送数据包
								/************接收数据***************/
								byte[] buf = new byte[400];//接受内容的大小，注意不要溢出  
								DatagramPacket dp = new DatagramPacket(buf,0,buf.length);//定义一个接收的包  
								try
								{
									dsSocket.setSoTimeout(3000);//阻塞超时
									dsSocket.receive(dp);//将接受内容封装到包中  
									
									byte[] returnData=dp.getData();//返回的有效数据包
									int returnLenght=dp.getLength();//返回数据的实际长度
									
									String showpString="";//接收到的数据包的内容
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
									writeLog("温度数据包："+showpString);//写入接收日志
									
									if(returnLenght>22)//只有温度返回数据长度大于2时才处理，下层数据返回可能为小于2
									{
										String vid=Integer.toHexString(returnData[0]&0xff);//需要抓图的摄像头id
										byte productType03_08=returnData[1];//设备特征值
										if(productType03_08==0x08)//接受DS8000的数据
										{
											
											/*******************进行数据格式检验********************************/
											//writeLog(showpString);//写入接收日志
											if(returnLenght!=23)
											{
												ShowMessage("该条返回数据的格式不对：数据长度不为23！");//写入接收日志
												writeLog("该条返回数据的格式不对：数据长度不为23！");//写入接收日志
											}
											else
											{
												crc.update(returnData,0,21);
										        byte test[]=crc.getCrcBytes();
												crc.reset();//每次用完必须重置，否则crc码错误
												if(test[0]!=returnData[21]||test[1]!=returnData[22])
												{
													ShowMessage("该条返回数据的格式不对：CRC校验码不正确！");//写入接收日志
													writeLog("该条返回数据的格式不对：CRC校验码不正确！");//写入接收日志
												}
												else
												{
													receiveDS8000(returnData,returnLenght);
												}
											}
											
											/***************************************************/
										}
										else if(productType03_08==0x03)//接受DS6000的数据
										{
											byte productType=returnData[4];//设备特征值
											/*根据设备特征码选择不同数据协议的接受数据*/
											switch(productType)
											{
												case 0x01:  try 
															  {
																	if(returnLenght==59)
																	{
																		receivePoor(returnData,returnLenght);
																	}
																	else
																	{
																		ShowMessage("该条返回数据的格式不对：长度不正确！");
																		writeLog("该条返回数据的格式不对：长度不正确！");//写入接收日志
																	}
																	
																	break;//接收头数据
															  } 
															catch (SQLException e) 
															  {
																e.printStackTrace();
															  } 
															catch (IOException e) {
																// TODO Auto-generated catch block
																e.printStackTrace();
															}
												case 0x02:  try
															  {
																	if(returnLenght>209)
																	{
																		receiveHost(returnData,returnLenght);
																	}
																	else
																	{
																		ShowMessage("该条返回数据的格式不对：长度不正确！");
																		writeLog("该条返回数据的格式不对：长度不正确！");//写入接收日志
																	}
																	
																	break;//主机数据
															  } 
															catch (SQLException e)
															  {
																	e.printStackTrace();
															   }
															catch (IOException e) {
																// TODO Auto-generated catch block
																e.printStackTrace();
															  }
												default:ShowMessage("该数据格式未能识别，无法正确接受数据，请确认该数据是否加入接口协议栈！");
											}
										}
										else
										{
											ShowMessage("该条返回数据的格式不对：长度不正确！");
											writeLog("该条返回数据的格式不对：长度不正确！");//写入接收日志
										}
										/*
										else if(productType03_08==0x00)//远程抓取某个摄像头的图像
										{
											if(vid!=null)
											{
												receiveDS8000(vid);
											}
										}
										*/
									}
									else
									{
										ShowMessage("该条返回数据的格式不对：长度不正确！");
										writeLog("该条返回数据的格式不对：长度不正确！");//写入接收日志
									}
								}
								catch (IOException e2)
								{
									System.out.println("dddd");
									e2.printStackTrace();
									
								}
								
								
								/**************************
								try {
									Thread.sleep(0);//每条命令发送做延时2014-08-19
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}//数据采集周期
								*/
								ShowSendMessage("Thread <"+flag+"> 自动采集cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
								//writeLog("Thread <"+flag+"> 自动采集cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");//写入接收日志
								//System.out.println("cmdTemp "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
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
			}
			System.gc();	
			ds.close();
//////////////////////////////////////开始弧光数据采集/////////////////////////////////////////////////////////////////////
			
			Device_Addr="";
			Device_Feature="04";
			Device_StartH="00";
			Device_StartL="00";
			Device_dataL="";
			Device_IP="";
			Device_Port="9004";
			Sample_ID="";
			sendData=new byte[8];
			
			
			//设置工作时间，不在此时间段内才会周期性抓图，设置的时间段从数据库获取
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
							//System.out.println("huguang start");
							Sample_ID=rs.getString("Sample_ID");
							Device_Addr=rs.getString("Sample_IndexID");
							Device_dataL=rs.getString("Sample_AddressH");
							Device_IP=rs.getString("Sample_AddressL");
							Device_Port=rs.getString("Sample_dataL");
							if(Device_Addr!=null&&Device_Addr.equals("null")==false&&Device_IP!=null&&Device_IP.equals("null")==false)
							{
								int length=0;

								sendData[0]=byteandstring.hexStringTobytes(Device_Addr);
								/******************不同设备发送命令的格式不同*****************************/
								sendData[1]=byteandstring.hexStringTobytes(Device_Feature);
								/***********************************************/
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
								crc.reset();//每次用完必须重置，否则crc码错误
								
								//////////////////////////////显示发送的弧光命令//////////////////////
								String sendString="";//接收到的数据包的内容
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
								writeLog("弧光cmd："+sendString);//写入接收日志
								//System.out.println("弧光cmd："+sendString);
								///////////////////////////////////////////////////////////////////////////
								
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
									dsSocket.send(sendDp);//发送数据包
									/************接收数据***************/
									byte[] buf = new byte[400];//接受内容的大小，注意不要溢出  
									DatagramPacket dp = new DatagramPacket(buf,0,buf.length);//定义一个接收的包  
									try
									{
										//System.out.println("kk");
										dsSocket.setSoTimeout(2000);//阻塞超时
										dsSocket.receive(dp);//将接受内容封装到包中  
										//System.out.println("kds");
										
										//System.out.println("kdddk");
										byte[] returnData=dp.getData();//返回的有效数据包
										int returnLenght=dp.getLength();//返回数据的实际长度
										
										String showpString="";//接收到的数据包的内容
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
										ShowMessage("弧光数据包："+showpString);
										writeLog("弧光数据包："+showpString);//写入接收日志
										//System.out.println("弧光数据包："+showpString);
										
										if(returnLenght==40)//只有弧光返回数据长度必须为40个字节
										{
											//String vid=Integer.toHexString(returnData[0]&0xff);//需要抓图的摄像头id
											byte productType03_08=returnData[1];//设备特征值
											if(productType03_08==0x04)//接受DS8000的数据
											{
												
												/*******************进行数据格式检验********************************/
												
												crc.update(returnData,0,(returnLenght-2));
										        byte test[]=crc.getCrcBytes();
												crc.reset();//每次用完必须重置，否则crc码错误
												
												//showMsg((returnLenght-2)+"<>"+Integer.toHexString(test[0]&0xff)+"<>"+Integer.toHexString(test[1]&0xff));
												
												if(test[0]!=returnData[38]||test[1]!=returnData[39])
												{
													ShowMessage("该条返回数据的格式不对：CRC校验码不正确！");//显示校验码错误提示信息
													writeLog("该条返回数据的格式不对：CRC校验码不正确！");//写入接收日志
												}
												else
												{
													receiveHg(returnData,returnLenght,Sample_ID);
												}
											}
											/***************************************************/
										}
										else
										{
											ShowMessage("弧光设备返回不正确，长度不是40！");//显示返回数据错误提示信息
											writeLog("弧光设备返回不正确，长度不是40！");//写入接收日志
										}
										System.gc();	
									}
									catch (IOException e2)
									{
										e2.printStackTrace();
									}
									/**************************
									try {
										Thread.sleep(0);//每条命令发送做延时2014-08-19
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}//数据采集周期
									*/
									ShowSendMessage("Thread <"+flag+"> 自动采集cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
									
									//System.out.println("cmdTemp "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
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
				Thread.sleep(500);//数据采集周期
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	
	
	protected  void receiveHg(byte[] returnData,int returnLenght,String Sample_ID) throws SQLException, IOException//接收弧光传感器的信息
	{
		
		ResultSet rs=null;
		//int failnum=0;//接受失败的数据
		String device_Address="";//弧光设备主机地址id
		device_Address=bs.bytesToHexString(returnData[0]);
		
		///////////////解析数据//////////////////////
		
		int hg_low=0;//弱弧总次数
		int hg_high=0;//强弧总次数
		hg_low=(returnData[3]&0xFF);
		hg_high=(returnData[5]&0xFF);
		
		//最近第一次
		int hg_recent1_time=0;//最近一次弧光发生时，弧光长度时间（单位毫秒）
		int hg_recent1_date=0;//最近一次弧光发生到现在时长（单位秒）
		String hg_1_date="";//最近一次弧光发生日期
		
		hg_recent1_time = (returnData[7] & 0xFF);
		//hg_recent1_date = (returnData[8] & 0xFF) * 16 + (returnData[9] & 0xFF);
		hg_recent1_date = ((returnData[8]<<8) & 0xFF00) | (returnData[9] & 0xFF);
		Date d = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		hg_1_date=df.format(new Date(d.getTime() - hg_recent1_date*1000));//当前时间-到现在的时间=过去发生的时间
		
		//System.out.println(hg_1_date+"<>"+d.getTime()+"<>"+hg_recent1_date+"<>"+(new Date(d.getTime() - hg_recent1_date)).getTime());
		//long ii=1432004959501L;
		//long iii=1432004979475L;
		//System.out.println(df.format(new Date(ii))+"<>"+(new Date(ii)).getSeconds()+"<>"+df.format(new Date(iii))+"<>"+(new Date(iii)).getSeconds());
		
		
		//最近第二次
		int hg_recent2_time=0;//最近二次弧光发生时，弧光长度时间（单位毫秒）
		int hg_recent2_date=0;//最近二次弧光发生到现在时长（单位秒）
		String hg_2_date="";//最近二次弧光发生日期
		
		hg_recent2_time = (returnData[11] & 0xFF);
		hg_recent2_date = ((returnData[12]<<8) & 0xFF00) | (returnData[13] & 0xFF);//((returnData[12]<<8) & 0xFF00) | (returnData[13] & 0xFF);
		//System.out.println(hg_recent2_date);
		
		hg_2_date=df.format(new Date(d.getTime() - hg_recent2_date*1000));//当前时间-到现在的时间=过去发生的时间
		
		//System.out.println(hg_2_date+"<>"+d.getTime()+"<>"+hg_recent2_date+"<>"+(new Date(d.getTime() - hg_recent2_date)).getTime());
		
		
		//最近第三次
		int hg_recent3_time=0;//最近三次弧光发生时，弧光长度时间（单位毫秒）
		int hg_recent3_date=0;//最近三次弧光发生到现在时长（单位秒）
		String hg_3_date="";//最近三次弧光发生日期
		
		hg_recent3_time = (returnData[15] & 0xFF);
		hg_recent3_date = ((returnData[16]<<8) & 0xFF00) | (returnData[17] & 0xFF);//(returnData[16] & 0xFF) * 16 + (returnData[17] & 0xFF);
		hg_3_date=df.format(new Date(d.getTime() - hg_recent3_date*1000));//当前时间-到现在的时间=过去发生的时间
		
		//System.out.println(hg_3_date+"<>"+d.getTime()+"<>"+hg_recent3_date+"<>"+(new Date(d.getTime() - hg_recent3_date)).getTime());
		
		
		//最近第四次
		int hg_recent4_time=0;//最近四次弧光发生时，弧光长度时间（单位毫秒）
		int hg_recent4_date=0;//最近四次弧光发生到现在时长（单位秒）
		String hg_4_date="";//最近四次弧光发生日期
		
		hg_recent4_time = (returnData[19] & 0xFF);
		hg_recent4_date = ((returnData[20]<<8) & 0xFF00) | (returnData[21] & 0xFF);//(returnData[20] & 0xFF) * 16 + (returnData[21] & 0xFF);
		hg_4_date=df.format(new Date(d.getTime() - hg_recent4_date*1000));//当前时间-到现在的时间=过去发生的时间

		//最近第五次
		int hg_recent5_time=0;//最近五次弧光发生时，弧光长度时间（单位毫秒）
		int hg_recent5_date=0;//最近五次弧光发生到现在时长（单位秒）
		String hg_5_date="";//最近五次弧光发生日期
		
		hg_recent5_time = (returnData[23] & 0xFF);
		hg_recent5_date = ((returnData[24]<<8) & 0xFF00) | (returnData[25] & 0xFF);//(returnData[24] & 0xFF) * 16 + (returnData[25] & 0xFF);
		hg_5_date=df.format(new Date(d.getTime() - hg_recent5_date*1000));//当前时间-到现在的时间=过去发生的时间

		//最近第六次
		int hg_recent6_time=0;//最近六次弧光发生时，弧光长度时间（单位毫秒）
		int hg_recent6_date=0;//最近六次弧光发生到现在时长（单位秒）
		String hg_6_date="";//最近六次弧光发生日期
		
		hg_recent6_time = (returnData[27] & 0xFF);
		hg_recent6_date = ((returnData[28]<<8) & 0xFF00) | (returnData[29] & 0xFF);//(returnData[28] & 0xFF) * 16 + (returnData[29] & 0xFF);
		hg_6_date=df.format(new Date(d.getTime() - hg_recent6_date*1000));//当前时间-到现在的时间=过去发生的时间

		//最近第七次
		int hg_recent7_time=0;//最近次弧光发生时，弧光长度时间（单位毫秒）
		int hg_recent7_date=0;//最近七次弧光发生到现在时长（单位秒）
		String hg_7_date="";//最近七次弧光发生日期
		
		hg_recent7_time = (returnData[31] & 0xFF);
		hg_recent7_date = ((returnData[32]<<8) & 0xFF00) | (returnData[33] & 0xFF);//(returnData[32] & 0xFF) * 16 + (returnData[33] & 0xFF);
		hg_7_date=df.format(new Date(d.getTime() - hg_recent7_date*1000));//当前时间-到现在的时间=过去发生的时间

		//将解析出来的数据插入数据库
		
		//插入弧光报警记录表
		//弱弧
		String insertHgNum_low="insert into AlarmLogArc(Sample_ID,AlarmNum,AlarmType,Odate) values('"+Sample_ID+"','"+hg_low+"','1','"+df.format(new Date())+"')";
		
		//强弧
		String insertHgNum_high="insert into AlarmLogArc(Sample_ID,AlarmNum,AlarmType,Odate) values('"+Sample_ID+"','"+hg_high+"','0','"+df.format(new Date())+"')";
				
		String insertRecentHg="insert into recentHg(Sample_ID,hg_recent1_time,hg_recent1_date,hg_recent2_time,hg_recent2_date,hg_recent3_time,hg_recent3_date,hg_recent4_time,hg_recent4_date,hg_recent5_time,hg_recent5_date,hg_recent6_time,hg_recent6_date,hg_recent7_time,hg_recent7_date) values('"+Sample_ID+"',"+hg_recent1_time+",'"+hg_1_date+"',"+hg_recent2_time+",'"+hg_2_date+"',"+hg_recent3_time+",'"+hg_3_date+"',"+hg_recent4_time+",'"+hg_4_date+"',"+hg_recent5_time+",'"+hg_5_date+"',"+hg_recent6_time+",'"+hg_6_date+"',"+hg_recent7_time+",'"+hg_7_date+"')";
		String updateRecentHg="update recentHg set hg_recent1_time="+hg_recent1_time+",hg_recent1_date='"+hg_1_date+"',hg_recent2_time="+hg_recent2_time+",hg_recent2_date='"+hg_2_date+"',hg_recent3_time="+hg_recent3_time+",hg_recent3_date='"+hg_3_date+"',hg_recent4_time="+hg_recent4_time+",hg_recent4_date='"+hg_4_date+"',hg_recent5_time="+hg_recent5_time+",hg_recent5_date='"+hg_5_date+"',hg_recent6_time="+hg_recent6_time+",hg_recent6_date='"+hg_6_date+"',hg_recent7_time="+hg_recent7_time+",hg_recent7_date='"+hg_7_date+"' where Sample_ID='"+Sample_ID+"'";
		
		
		//System.out.println(updateRecentHg);
		//检查改弧光设备最近弧记录光是否存在
		String checkRecentHg="select count(*) from recentHg where Sample_ID='"+Sample_ID+"'";
		
		
		
		int f1=ds.update(insertHgNum_low);
		int f2=ds.update(insertHgNum_high);
		
		int count=0;
		rs=ds.select(checkRecentHg);
		if(rs!=null)
		{
			while(rs.next())
			{
				count=rs.getInt(1);
			}
		}
		if(count==0)//若没有该弧光设备的记录，则插入
		{
			int f3=ds.update(insertRecentHg);
		}
		else//若有该弧光设备的记录，则更新
		{
			int f3=ds.update(updateRecentHg);
		}
	
		//showMsg(insertRecentHg);
		rs.close();
		//ds.close();
		System.gc();
		
	}
	
	
	/*protected  void receiveDS8000(String vid) throws SQLException//远程抓取某个摄像头的图像
	{
		String imgpath="";
		//dataset ds=new dataset();
		String getP="select * from Interface";
		ResultSet rs=ds.select(getP);
		if(rs!=null)
		{
			while(rs.next())
			{
				imgpath=rs.getString("Imagepath");
			}
			rs.close();
		}
		//ds.close();
		if(imgpath.equals(""))
		{
			ShowMessage("未设置图像保存文件夹！");
		}
		else
		{
			File fileDir=new File(imgpath);
			File[]  allFiles=fileDir.listFiles();//取图像文件夹下的所有文件
	    	String filename="";//该摄像头的最新图片
	    	long fTime=0;
	    	for(int i=0;i<allFiles.length;i++)
	    	{
	    		if(allFiles[i].isFile())
	    		{
	    			long t= allFiles[i].lastModified();
	    			String fName=allFiles[i].getName();
    				int k=fName.indexOf("_");
    				String index_id=fName.substring(0, k);//取摄像头编号
	    			if(vid.equals(index_id))
	    			{
	    				if(t>fTime)
	    				{
	    					filename=fName;
	    				}
	    			}
	    		}
	    	}
	    	
			try 
			{
				recieveImg re=new recieveImg();
				re.picSQL(vid,filename,imgpath);
				ShowMessage(vid+"接收图像:"+filename);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			
		}
	}	*/
	
	
	protected  void receiveDS8000(byte[] returnData,int returnLenght) throws SQLException, IOException//接收DS8000接收头数据最多12个
	{
		
		ResultSet rs=null;
		int failnum=0;//接受失败的数据
		String device_Address="";
		device_Address=bs.bytesToHexString(returnData[0]);
		//////////////////////////////////////////////
//		float Para_PreExceed_Tem=40;
//		float Para_Exceed_Tem=60;
//		
//		String getAlartmT="select Para_PreExceed_Tem,Para_Exceed_Tem from  Parameter where Substation_ID in(select Substation_ID from DeviceSet,Device where Device.Device_ID=DeviceSet.Device_ID and Device_Addr='"+device_Address+"')";
//		rs=ds.select(getAlartmT);
//		if(rs!=null)
//		{
//			while (rs.next()) 
//			{
//				Para_PreExceed_Tem=rs.getFloat(1);
//				Para_Exceed_Tem=rs.getFloat(2);
//			}
//		}
//		String FirstAddrString="";//主机数据首地址
//		String getFirstAddrString="select Device_StartH,Device_StartL from DeviceSet where Device_Addr='"+device_Address+"'";
//		rs=ds.select(getFirstAddrString);
//		if(rs!=null)
//		{
//			while (rs.next()) 
//			{
//				if(rs.getString(1)!=null&&rs.getString(1).trim().equals("")&&rs.getString(1).trim().equals("null"))
//				{
//					FirstAddrString=FirstAddrString+rs.getString(1);
//				}
//				else
//				{
//					FirstAddrString="00";
//				}
//				if(rs.getString(2)!=null&&rs.getString(2).trim().equals("")&&rs.getString(2).trim().equals("null"))
//				{
//					FirstAddrString=FirstAddrString+rs.getString(2);
//				}
//				else
//				{
//					FirstAddrString=FirstAddrString+"00";
//				}
//			}
//		}
		////////////////////////////////////////////////////////
		int Tvalue_A=0;//A路温度--序号13
		int Hvalue_A=0;//A路湿度--序号14
		int Tvalue_B=0;//B路温度--序号15
		int Hvalue_B=0;//B路湿度--序号16
		Tvalue_A=(returnData[2]&0xFF)-40;
		Hvalue_A=(returnData[3]&0xFF);
		Tvalue_B=(returnData[4]&0xFF)-40;
		Hvalue_B=(returnData[5]&0xFF);
		
		//ShowMessage("AB温湿度1："+Tvalue_A+"<>"+Hvalue_A+"<>"+Tvalue_B+"<>"+Hvalue_B);
		
		/***********异常捡测*****************/
		if(Tvalue_A>70||Tvalue_B>70)
		{
			writeLog("该条返回数据温度过高异常！");//写入接收日志
		}
		/***********************/

		for(int i=13;i<17;i++)//分别处理A路温度、A路湿度、B路温度、B路湿度
		{
			String sample_id="";
			String get_sampleid="select Sample_ID from SampleAddress where Device_Address='"+device_Address+"' and Sample_IndexID='"+i+"'";
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
				failnum++;
				//System.out.println("温度 "+(i-18)+": "+tvalue+" 未设置采样点数据接口信息！");
			}
			else
			{
				switch (i) {
				case 13:
						ds.execute(Tvalue_A, 0, 0,sample_id);//调用存储过程将接收到的数据存入数据库
						break;
				case 14:
					ds.executeHum(0, 0, Hvalue_A,sample_id);//调用存储过程将接收到的数据存入数据库
					break;
				case 15:
					ds.execute(Tvalue_B, 0, 0,sample_id);//调用存储过程将接收到的数据存入数据库
					break;
				case 16:
					ds.executeHum(0, 0, Hvalue_B,sample_id);//调用存储过程将接收到的数据存入数据库
					break;
				default:
					break;
				}
			}
		}
		
		
		float tvalue=0;
		for(int i=9;i<21;i++)//分别处理其12个温度数据
		{
			tvalue=(returnData[i]&0xFF);
			if(tvalue==255)
			{
				tvalue=0;
			}
			/***********异常捡测*****************/
			if(Tvalue_A>70||Tvalue_B>70)
			{
				writeLog("该条返回数据温度过高异常！");//写入接收日志
			}
			/****************************/
			
			String sample_id="";
			String get_sampleid="select Sample_ID from SampleAddress where Device_Address='"+device_Address+"' and Sample_IndexID='"+(i-8)+"'";
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
				failnum++;
				//System.out.println("温度 "+(i-18)+": "+tvalue+" 未设置采样点数据接口信息！");
			}
			else
			{
				ds.execute(tvalue, 0, 0,sample_id);//调用存储过程将接收到的数据存入数据库
			}
			
//			if(tvalue>=Para_PreExceed_Tem&&tvalue<Para_Exceed_Tem)//返回预警信号
//			{
//				returnAlarmSingal(device_Address,FirstAddrString,"1");
//			}
//			else if(tvalue>=Para_Exceed_Tem)//返回报警信号
//			{
//				returnAlarmSingal(device_Address,FirstAddrString,"0");
//			}
		}

		
		rs=null;
		//ds.close();
		//ds=null;
		//bs=null;
		ShowMessage(device_Address+"成功接收  < DS8000 > 有效数据:"+(16-failnum)+" ;失败: "+failnum);
		
		//byte localTempType03_08=returnData[6];//本机测温类型
		System.gc();
		
	}

	protected  void receivePoor(byte[] returnData,int returnLenght) throws SQLException, IOException//接收接收头数据16*3=48
	{
		//返回的数据中从下标10开始至57为温度数据，在设置采样点对应的数据地址时需要从地址1开始标号，一直到48.
		//System.out.println("温度 数据！");
		//byteandstring bs=new byteandstring();
		//dataset ds=new dataset();
		ResultSet rs=null;
		int failnum=0;//接受失败的数据
		String device_Address="";
		device_Address=bs.bytesToHexString(returnData[3]);
		//////////////////////////////////////////////
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
		String FirstAddrString="";//主机数据首地址
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
		}
		////////////////////////////////////////////////////////
		float tvalue=0;
		for(int i=10;i<57;i++)//分别处理其48个温度数据
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
				
				//System.out.println("温度 "+i+": "+tvalue+"<>"+sample_id+"<>"+get_sampleid);
				if(sample_id.equals("")||sample_id==null)
				{
					//System.out.println("温度 "+i+": "+tvalue+"<>"+sample_id+"fail");
					//failnum++;
					//System.out.println("温度 "+(i-18)+": "+tvalue+" 未设置采样点数据接口信息！");
				}
				else
				{
					//System.out.println("温度 "+i+": "+tvalue);
					failnum++;
					ds.execute(tvalue, 0, 0,sample_id);//调用存储过程将接收到的数据存入数据库
				}
				/*
				 //返回报警信号
				if(tvalue>=Para_PreExceed_Tem&&tvalue<Para_Exceed_Tem)//返回预警信号
				{
					returnAlarmSingal(device_Address,FirstAddrString,"1");
				}
				else if(tvalue>=Para_Exceed_Tem)//返回报警信号
				{
					returnAlarmSingal(device_Address,FirstAddrString,"0");
				}
				*/
			}
			
		}
		//
		rs=null;
		//ds.close();
		//ds=null;
		//bs=null;
		//ShowMessage(device_Address+"成功接收有效数据:"+(48-failnum)+" ;失败: "+failnum);
		ShowMessage(device_Address+"成功接收有效数据:"+failnum+" ;失败: "+(48-failnum));
		System.gc();
		
	}
	
	protected  void receiveHost(byte[] returnData,int returnLenght) throws SQLException //接收主机数据220个数据，48*4=192个采样点
, IOException
	{
		//返回的数据中从下标19开始至210为温度数据，在设置采样点对应的数据地址时需要从地址1开始标号，一直到48*4=192.

		ResultSet rs=null;
		int failnum=0;//接受失败的数据
		String device_Address="";
		device_Address=bs.bytesToHexString(returnData[3]);

		/////////////旧版本处理192个点数据的处理方法/////////////////////
		float tvalue=0;
		for(int i=19;i<211;i++)//分别处理其192个温度数据
		{
			tvalue=(returnData[i]&0xFF);
			if(tvalue==255)
			{
				//tvalue=0;
				failnum++;
			}
			else
			{
				String sample_id="";
				//String get_sampleid="select Sample_ID from SampleAddress where Device_Address='"+device_Address+"' and Sample_AddressH='"+(i-18)+"'";
				String get_sampleid="select Sample_ID from SampleAddress where Device_Address='"+device_Address+"' and Sample_IndexID='"+(i-18)+"'";
				rs=ds.select(get_sampleid);
				if(rs!=null)
				{
					while(rs.next())
					{
						sample_id=rs.getString(1);
					}
				}
				
				if(sample_id.equals("")||sample_id==null)
				{
					failnum++;
					//System.out.println("温度 "+(i-18)+": "+tvalue+" 未设置采样点数据接口信息！");
				}
				else
				{
					ds.execute(tvalue, 0, 0,sample_id);//调用存储过程将接收到的数据存入数据库
					//ShowMessage("正确接收数据  "+i+" : "+Integer.toHexString(returnData[i]&0xff));
				}
			}
			
		}
		//System.out.println(device_Address+"成功接收:"+(192-failnum)+" ;失败: "+failnum);
		ShowMessage(device_Address+"成功接收:"+(192-failnum)+" ;失败: "+failnum);
		rs.close();
		rs=null;
		//ds.close();
		//ds=null;
		//bs=null;
		System.gc();
	}
	
	public void returnAlarmSingal(String device_Address,String nodeAddress,String signal) throws SQLException//当有效温度值超过报警值是返回报警信号
	, IOException
		{
			String getUDPAddress="select Device_IP,Device_Port  from DeviceSet where Device_Addr='"+device_Address+"'";
			//dataset ds=new dataset();
			ResultSet rs=ds.select(getUDPAddress);
			String Device_IP="";
			String Device_Port="";
			if(rs!=null)
			{
				while (rs.next())
				{
					Device_IP = rs.getString("Device_IP");
					Device_Port = rs.getString("Device_Port");
				}
			}
			rs.close();
			//ds.close();
			rs=null;
			//ds=null;
			
			String cmdString=device_Address+"03"+device_Address+nodeAddress+signal;
			byte[] sendData=null;
			DatagramPacket sendDp=null;//发送包
			sendData=bs.hexStringToBytes(cmdString);
			InetAddress address=InetAddress.getByName(Device_IP);
			sendDp=new DatagramPacket(sendData, sendData.length, address,Integer.parseInt(Device_Port));
			DatagramSocket reds=new DatagramSocket();
			reds.send(sendDp);//发送数据包
			System.out.println("返回报警信号："+cmdString);
			ShowMessage("返回报警信号："+cmdString);
			sendData=null;
			sendDp=null;
			address=null;
			reds=null;
			System.gc();
		}
	
	public void writeLog(String msg)
	{
		try
		{
				 File dir=new File("e:\\logs");
				 if (!dir.exists()) 
			     {
					 dir.mkdir();  
				 }
				 
				 Date time=new Date();
				 DateFormat format = new SimpleDateFormat("yyMMdd");  
				 String svgpath="e:\\logs\\"+format.format(time)+".txt";

			     File file = new File(svgpath);
			     if (!file.exists()) 
			     {
						file.createNewFile();  
				 }  

			     FileOutputStream fos = new FileOutputStream(file,true);
			     OutputStreamWriter osw = new OutputStreamWriter(fos);
			     BufferedWriter bw = new BufferedWriter(osw);
	
			     String s1 = msg;//"你好 0445218js";
			     bw.write(s1);
			     bw.newLine();
			     
			     bw.flush();
			     bw.close();
			     osw.close();
			     fos.close();
		}
	    catch (FileNotFoundException e1)
	    {
	     e1.printStackTrace();
	    } 
		catch (IOException e2)
	    {
	     e2.printStackTrace();
	    }
	}

}


