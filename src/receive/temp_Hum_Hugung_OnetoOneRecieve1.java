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
	DatagramSocket dsSocket=null;//UDP����
	DatagramPacket sendDp=null;//���Ͱ�
	DatagramPacket recieveDp=null;//���ܰ�
	int portText=9001;//�����˿ں�
	//byteandstring bs=null;=new byteandstring();
	byteandstring bs=new byteandstring();
	dataset ds=new dataset();
	CRC crc=new CRC();
	int flag;
	
	String huStartTime="00:00:00";//����ǹ������ڿ�ʼʱ��
	String huEndTime="24:00:00";//����ǹ������ڽ���ʱ��
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
		String getP="select * from Interface";//��ȡ����ķǹ���ʱ��
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
	public void ShowMessage(String msg)//��ʾ��������
	{
		//this.showMsg.setText(showMsg.getText()+"\r\n"+msg);
		if(this.showMsg.getLineCount()>500)//���ݳ���һ�������������
		{
			this.showMsg.setText(msg);
		}
		else
		{
			this.showMsg.setText(this.showMsg.getText()+"\r\n"+msg);
		}
	}
	public void ShowSendMessage(String msg)//��ʾ��������
	{
		//this.showSendMsg.setText(showSendMsg.getText()+"\r\n"+msg);
		if(this.showSendMsg.getLineCount()>500)//���ݳ���һ�������������
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
			JOptionPane.showMessageDialog(null, "�˿�  "+portText+" ����������ռ�ã�", "�����˿ڳ�ʼ����ʾ", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		
		byte[] sendData=new byte[8];
		
		//��ȡ���е��¶Ȳ�����
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
		
		//��ȡ���еĻ��������
		String getHGdevice="select * from SampleAddress where Sample_IndexID is not null and Sample_AddressH is not null and Sample_AddressL is not null and   Sample_dataL='"+portText+"'";
		
		String Sample_ID="";//��������
		/*��Ӧ�����ڻ���ɼ��еĺ���
		String Device_Addr="";//������ַ��ΪSampleAddress���е�Sample_IndexID�ֶ�
		String Device_Feature="04";//���⴫�����豸����ֵĬ��04
		String Device_StartH="00";//���⴫������ʵ��ַĬ��00 00
		String Device_StartL="00";
		String Device_dataL="";//���⴫������ȡ���ݳ��ȣ�ΪSampleAddress���е�Sample_AddressH�ֶ�
		String Device_IP="";//Ŀ��ip��ΪSampleAddress���е�Sample_AddressL�ֶ�
		String Device_Port="9005";//Ŀ��port��ΪSampleAddress���е�Sample_dataL�ֶ�
		ResultSet rs=null;
		*/
		
		
		while(true)
		{
//////////////////////////////////////�Ƚ����¶����ݲɼ�/////////////////////////////////////////////////////////////////////
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
							/******************��ͬ�豸��������ĸ�ʽ��ͬ*****************************/
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
							crc.reset();//ÿ������������ã�����crc�����
							
							InetAddress address;
							try {
								address = InetAddress.getByName(Device_IP);
								sendDp=new DatagramPacket(sendData, sendData.length, address,Integer.parseInt(Device_Port));
							} catch (UnknownHostException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							
							//ShowSendMessage("Thread <"+flag+"> �Զ��ɼ�cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
							writeLog("Thread <"+flag+"> �Զ��ɼ�cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");//д�������־
							
							
							try 
							{
								dsSocket.send(sendDp);//�������ݰ�
								/************��������***************/
								byte[] buf = new byte[400];//�������ݵĴ�С��ע�ⲻҪ���  
								DatagramPacket dp = new DatagramPacket(buf,0,buf.length);//����һ�����յİ�  
								try
								{
									dsSocket.setSoTimeout(3000);//������ʱ
									dsSocket.receive(dp);//���������ݷ�װ������  
									
									byte[] returnData=dp.getData();//���ص���Ч���ݰ�
									int returnLenght=dp.getLength();//�������ݵ�ʵ�ʳ���
									
									String showpString="";//���յ������ݰ�������
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
									ShowMessage("�¶����ݰ���"+showpString);
									writeLog("�¶����ݰ���"+showpString);//д�������־
									
									if(returnLenght>22)//ֻ���¶ȷ������ݳ��ȴ���2ʱ�Ŵ����²����ݷ��ؿ���ΪС��2
									{
										String vid=Integer.toHexString(returnData[0]&0xff);//��Ҫץͼ������ͷid
										byte productType03_08=returnData[1];//�豸����ֵ
										if(productType03_08==0x08)//����DS8000������
										{
											
											/*******************�������ݸ�ʽ����********************************/
											//writeLog(showpString);//д�������־
											if(returnLenght!=23)
											{
												ShowMessage("�����������ݵĸ�ʽ���ԣ����ݳ��Ȳ�Ϊ23��");//д�������־
												writeLog("�����������ݵĸ�ʽ���ԣ����ݳ��Ȳ�Ϊ23��");//д�������־
											}
											else
											{
												crc.update(returnData,0,21);
										        byte test[]=crc.getCrcBytes();
												crc.reset();//ÿ������������ã�����crc�����
												if(test[0]!=returnData[21]||test[1]!=returnData[22])
												{
													ShowMessage("�����������ݵĸ�ʽ���ԣ�CRCУ���벻��ȷ��");//д�������־
													writeLog("�����������ݵĸ�ʽ���ԣ�CRCУ���벻��ȷ��");//д�������־
												}
												else
												{
													receiveDS8000(returnData,returnLenght);
												}
											}
											
											/***************************************************/
										}
										else if(productType03_08==0x03)//����DS6000������
										{
											byte productType=returnData[4];//�豸����ֵ
											/*�����豸������ѡ��ͬ����Э��Ľ�������*/
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
																		ShowMessage("�����������ݵĸ�ʽ���ԣ����Ȳ���ȷ��");
																		writeLog("�����������ݵĸ�ʽ���ԣ����Ȳ���ȷ��");//д�������־
																	}
																	
																	break;//����ͷ����
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
																		ShowMessage("�����������ݵĸ�ʽ���ԣ����Ȳ���ȷ��");
																		writeLog("�����������ݵĸ�ʽ���ԣ����Ȳ���ȷ��");//д�������־
																	}
																	
																	break;//��������
															  } 
															catch (SQLException e)
															  {
																	e.printStackTrace();
															   }
															catch (IOException e) {
																// TODO Auto-generated catch block
																e.printStackTrace();
															  }
												default:ShowMessage("�����ݸ�ʽδ��ʶ���޷���ȷ�������ݣ���ȷ�ϸ������Ƿ����ӿ�Э��ջ��");
											}
										}
										else
										{
											ShowMessage("�����������ݵĸ�ʽ���ԣ����Ȳ���ȷ��");
											writeLog("�����������ݵĸ�ʽ���ԣ����Ȳ���ȷ��");//д�������־
										}
										/*
										else if(productType03_08==0x00)//Զ��ץȡĳ������ͷ��ͼ��
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
										ShowMessage("�����������ݵĸ�ʽ���ԣ����Ȳ���ȷ��");
										writeLog("�����������ݵĸ�ʽ���ԣ����Ȳ���ȷ��");//д�������־
									}
								}
								catch (IOException e2)
								{
									System.out.println("dddd");
									e2.printStackTrace();
									
								}
								
								
								/**************************
								try {
									Thread.sleep(0);//ÿ�����������ʱ2014-08-19
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}//���ݲɼ�����
								*/
								ShowSendMessage("Thread <"+flag+"> �Զ��ɼ�cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
								//writeLog("Thread <"+flag+"> �Զ��ɼ�cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");//д�������־
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
//////////////////////////////////////��ʼ�������ݲɼ�/////////////////////////////////////////////////////////////////////
			
			Device_Addr="";
			Device_Feature="04";
			Device_StartH="00";
			Device_StartL="00";
			Device_dataL="";
			Device_IP="";
			Device_Port="9004";
			Sample_ID="";
			sendData=new byte[8];
			
			
			//���ù���ʱ�䣬���ڴ�ʱ����ڲŻ�������ץͼ�����õ�ʱ��δ����ݿ��ȡ
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
				//System.out.println("ϵͳ����ʱ���������ô���");
				ShowMessage("����ϵͳ����ʱ���������ô���<>"+cd.compare_date(imgStartTime_current,imgEndTime_current)+"<>"+"imgStartTime_current<>"+imgStartTime_current+"imgEndTime_current<>"+imgEndTime_current);
			}
			else if(cd.compare_date(imgStartTime_current,nowTime)<1&&cd.compare_date(imgEndTime_current,nowTime)>-1)
			{
				ShowMessage("����ϵͳ�ǹ���ʱ�䣡<>");
			}
			else
			{
				ShowMessage("����ϵͳ����ʱ�䣡<>");
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
								/******************��ͬ�豸��������ĸ�ʽ��ͬ*****************************/
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
								crc.reset();//ÿ������������ã�����crc�����
								
								//////////////////////////////��ʾ���͵Ļ�������//////////////////////
								String sendString="";//���յ������ݰ�������
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
								ShowSendMessage("����cmd��"+sendString);
								writeLog("����cmd��"+sendString);//д�������־
								//System.out.println("����cmd��"+sendString);
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
									dsSocket.send(sendDp);//�������ݰ�
									/************��������***************/
									byte[] buf = new byte[400];//�������ݵĴ�С��ע�ⲻҪ���  
									DatagramPacket dp = new DatagramPacket(buf,0,buf.length);//����һ�����յİ�  
									try
									{
										//System.out.println("kk");
										dsSocket.setSoTimeout(2000);//������ʱ
										dsSocket.receive(dp);//���������ݷ�װ������  
										//System.out.println("kds");
										
										//System.out.println("kdddk");
										byte[] returnData=dp.getData();//���ص���Ч���ݰ�
										int returnLenght=dp.getLength();//�������ݵ�ʵ�ʳ���
										
										String showpString="";//���յ������ݰ�������
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
										ShowMessage("�������ݰ���"+showpString);
										writeLog("�������ݰ���"+showpString);//д�������־
										//System.out.println("�������ݰ���"+showpString);
										
										if(returnLenght==40)//ֻ�л��ⷵ�����ݳ��ȱ���Ϊ40���ֽ�
										{
											//String vid=Integer.toHexString(returnData[0]&0xff);//��Ҫץͼ������ͷid
											byte productType03_08=returnData[1];//�豸����ֵ
											if(productType03_08==0x04)//����DS8000������
											{
												
												/*******************�������ݸ�ʽ����********************************/
												
												crc.update(returnData,0,(returnLenght-2));
										        byte test[]=crc.getCrcBytes();
												crc.reset();//ÿ������������ã�����crc�����
												
												//showMsg((returnLenght-2)+"<>"+Integer.toHexString(test[0]&0xff)+"<>"+Integer.toHexString(test[1]&0xff));
												
												if(test[0]!=returnData[38]||test[1]!=returnData[39])
												{
													ShowMessage("�����������ݵĸ�ʽ���ԣ�CRCУ���벻��ȷ��");//��ʾУ���������ʾ��Ϣ
													writeLog("�����������ݵĸ�ʽ���ԣ�CRCУ���벻��ȷ��");//д�������־
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
											ShowMessage("�����豸���ز���ȷ�����Ȳ���40��");//��ʾ�������ݴ�����ʾ��Ϣ
											writeLog("�����豸���ز���ȷ�����Ȳ���40��");//д�������־
										}
										System.gc();	
									}
									catch (IOException e2)
									{
										e2.printStackTrace();
									}
									/**************************
									try {
										Thread.sleep(0);//ÿ�����������ʱ2014-08-19
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}//���ݲɼ�����
									*/
									ShowSendMessage("Thread <"+flag+"> �Զ��ɼ�cmd: "+Device_IP+"<>"+Device_Port+"<>"+bs.bytesToHexString(sendData)+";");
									
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
				Thread.sleep(500);//���ݲɼ�����
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
	}

	
	
	protected  void receiveHg(byte[] returnData,int returnLenght,String Sample_ID) throws SQLException, IOException//���ջ��⴫��������Ϣ
	{
		
		ResultSet rs=null;
		//int failnum=0;//����ʧ�ܵ�����
		String device_Address="";//�����豸������ַid
		device_Address=bs.bytesToHexString(returnData[0]);
		
		///////////////��������//////////////////////
		
		int hg_low=0;//�����ܴ���
		int hg_high=0;//ǿ���ܴ���
		hg_low=(returnData[3]&0xFF);
		hg_high=(returnData[5]&0xFF);
		
		//�����һ��
		int hg_recent1_time=0;//���һ�λ��ⷢ��ʱ�����ⳤ��ʱ�䣨��λ���룩
		int hg_recent1_date=0;//���һ�λ��ⷢ��������ʱ������λ�룩
		String hg_1_date="";//���һ�λ��ⷢ������
		
		hg_recent1_time = (returnData[7] & 0xFF);
		//hg_recent1_date = (returnData[8] & 0xFF) * 16 + (returnData[9] & 0xFF);
		hg_recent1_date = ((returnData[8]<<8) & 0xFF00) | (returnData[9] & 0xFF);
		Date d = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		hg_1_date=df.format(new Date(d.getTime() - hg_recent1_date*1000));//��ǰʱ��-�����ڵ�ʱ��=��ȥ������ʱ��
		
		//System.out.println(hg_1_date+"<>"+d.getTime()+"<>"+hg_recent1_date+"<>"+(new Date(d.getTime() - hg_recent1_date)).getTime());
		//long ii=1432004959501L;
		//long iii=1432004979475L;
		//System.out.println(df.format(new Date(ii))+"<>"+(new Date(ii)).getSeconds()+"<>"+df.format(new Date(iii))+"<>"+(new Date(iii)).getSeconds());
		
		
		//����ڶ���
		int hg_recent2_time=0;//������λ��ⷢ��ʱ�����ⳤ��ʱ�䣨��λ���룩
		int hg_recent2_date=0;//������λ��ⷢ��������ʱ������λ�룩
		String hg_2_date="";//������λ��ⷢ������
		
		hg_recent2_time = (returnData[11] & 0xFF);
		hg_recent2_date = ((returnData[12]<<8) & 0xFF00) | (returnData[13] & 0xFF);//((returnData[12]<<8) & 0xFF00) | (returnData[13] & 0xFF);
		//System.out.println(hg_recent2_date);
		
		hg_2_date=df.format(new Date(d.getTime() - hg_recent2_date*1000));//��ǰʱ��-�����ڵ�ʱ��=��ȥ������ʱ��
		
		//System.out.println(hg_2_date+"<>"+d.getTime()+"<>"+hg_recent2_date+"<>"+(new Date(d.getTime() - hg_recent2_date)).getTime());
		
		
		//���������
		int hg_recent3_time=0;//������λ��ⷢ��ʱ�����ⳤ��ʱ�䣨��λ���룩
		int hg_recent3_date=0;//������λ��ⷢ��������ʱ������λ�룩
		String hg_3_date="";//������λ��ⷢ������
		
		hg_recent3_time = (returnData[15] & 0xFF);
		hg_recent3_date = ((returnData[16]<<8) & 0xFF00) | (returnData[17] & 0xFF);//(returnData[16] & 0xFF) * 16 + (returnData[17] & 0xFF);
		hg_3_date=df.format(new Date(d.getTime() - hg_recent3_date*1000));//��ǰʱ��-�����ڵ�ʱ��=��ȥ������ʱ��
		
		//System.out.println(hg_3_date+"<>"+d.getTime()+"<>"+hg_recent3_date+"<>"+(new Date(d.getTime() - hg_recent3_date)).getTime());
		
		
		//������Ĵ�
		int hg_recent4_time=0;//����Ĵλ��ⷢ��ʱ�����ⳤ��ʱ�䣨��λ���룩
		int hg_recent4_date=0;//����Ĵλ��ⷢ��������ʱ������λ�룩
		String hg_4_date="";//����Ĵλ��ⷢ������
		
		hg_recent4_time = (returnData[19] & 0xFF);
		hg_recent4_date = ((returnData[20]<<8) & 0xFF00) | (returnData[21] & 0xFF);//(returnData[20] & 0xFF) * 16 + (returnData[21] & 0xFF);
		hg_4_date=df.format(new Date(d.getTime() - hg_recent4_date*1000));//��ǰʱ��-�����ڵ�ʱ��=��ȥ������ʱ��

		//��������
		int hg_recent5_time=0;//�����λ��ⷢ��ʱ�����ⳤ��ʱ�䣨��λ���룩
		int hg_recent5_date=0;//�����λ��ⷢ��������ʱ������λ�룩
		String hg_5_date="";//�����λ��ⷢ������
		
		hg_recent5_time = (returnData[23] & 0xFF);
		hg_recent5_date = ((returnData[24]<<8) & 0xFF00) | (returnData[25] & 0xFF);//(returnData[24] & 0xFF) * 16 + (returnData[25] & 0xFF);
		hg_5_date=df.format(new Date(d.getTime() - hg_recent5_date*1000));//��ǰʱ��-�����ڵ�ʱ��=��ȥ������ʱ��

		//���������
		int hg_recent6_time=0;//������λ��ⷢ��ʱ�����ⳤ��ʱ�䣨��λ���룩
		int hg_recent6_date=0;//������λ��ⷢ��������ʱ������λ�룩
		String hg_6_date="";//������λ��ⷢ������
		
		hg_recent6_time = (returnData[27] & 0xFF);
		hg_recent6_date = ((returnData[28]<<8) & 0xFF00) | (returnData[29] & 0xFF);//(returnData[28] & 0xFF) * 16 + (returnData[29] & 0xFF);
		hg_6_date=df.format(new Date(d.getTime() - hg_recent6_date*1000));//��ǰʱ��-�����ڵ�ʱ��=��ȥ������ʱ��

		//������ߴ�
		int hg_recent7_time=0;//����λ��ⷢ��ʱ�����ⳤ��ʱ�䣨��λ���룩
		int hg_recent7_date=0;//����ߴλ��ⷢ��������ʱ������λ�룩
		String hg_7_date="";//����ߴλ��ⷢ������
		
		hg_recent7_time = (returnData[31] & 0xFF);
		hg_recent7_date = ((returnData[32]<<8) & 0xFF00) | (returnData[33] & 0xFF);//(returnData[32] & 0xFF) * 16 + (returnData[33] & 0xFF);
		hg_7_date=df.format(new Date(d.getTime() - hg_recent7_date*1000));//��ǰʱ��-�����ڵ�ʱ��=��ȥ������ʱ��

		//���������������ݲ������ݿ�
		
		//���뻡�ⱨ����¼��
		//����
		String insertHgNum_low="insert into AlarmLogArc(Sample_ID,AlarmNum,AlarmType,Odate) values('"+Sample_ID+"','"+hg_low+"','1','"+df.format(new Date())+"')";
		
		//ǿ��
		String insertHgNum_high="insert into AlarmLogArc(Sample_ID,AlarmNum,AlarmType,Odate) values('"+Sample_ID+"','"+hg_high+"','0','"+df.format(new Date())+"')";
				
		String insertRecentHg="insert into recentHg(Sample_ID,hg_recent1_time,hg_recent1_date,hg_recent2_time,hg_recent2_date,hg_recent3_time,hg_recent3_date,hg_recent4_time,hg_recent4_date,hg_recent5_time,hg_recent5_date,hg_recent6_time,hg_recent6_date,hg_recent7_time,hg_recent7_date) values('"+Sample_ID+"',"+hg_recent1_time+",'"+hg_1_date+"',"+hg_recent2_time+",'"+hg_2_date+"',"+hg_recent3_time+",'"+hg_3_date+"',"+hg_recent4_time+",'"+hg_4_date+"',"+hg_recent5_time+",'"+hg_5_date+"',"+hg_recent6_time+",'"+hg_6_date+"',"+hg_recent7_time+",'"+hg_7_date+"')";
		String updateRecentHg="update recentHg set hg_recent1_time="+hg_recent1_time+",hg_recent1_date='"+hg_1_date+"',hg_recent2_time="+hg_recent2_time+",hg_recent2_date='"+hg_2_date+"',hg_recent3_time="+hg_recent3_time+",hg_recent3_date='"+hg_3_date+"',hg_recent4_time="+hg_recent4_time+",hg_recent4_date='"+hg_4_date+"',hg_recent5_time="+hg_recent5_time+",hg_recent5_date='"+hg_5_date+"',hg_recent6_time="+hg_recent6_time+",hg_recent6_date='"+hg_6_date+"',hg_recent7_time="+hg_recent7_time+",hg_recent7_date='"+hg_7_date+"' where Sample_ID='"+Sample_ID+"'";
		
		
		//System.out.println(updateRecentHg);
		//���Ļ����豸�������¼���Ƿ����
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
		if(count==0)//��û�иû����豸�ļ�¼�������
		{
			int f3=ds.update(insertRecentHg);
		}
		else//���иû����豸�ļ�¼�������
		{
			int f3=ds.update(updateRecentHg);
		}
	
		//showMsg(insertRecentHg);
		rs.close();
		//ds.close();
		System.gc();
		
	}
	
	
	/*protected  void receiveDS8000(String vid) throws SQLException//Զ��ץȡĳ������ͷ��ͼ��
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
			ShowMessage("δ����ͼ�񱣴��ļ��У�");
		}
		else
		{
			File fileDir=new File(imgpath);
			File[]  allFiles=fileDir.listFiles();//ȡͼ���ļ����µ������ļ�
	    	String filename="";//������ͷ������ͼƬ
	    	long fTime=0;
	    	for(int i=0;i<allFiles.length;i++)
	    	{
	    		if(allFiles[i].isFile())
	    		{
	    			long t= allFiles[i].lastModified();
	    			String fName=allFiles[i].getName();
    				int k=fName.indexOf("_");
    				String index_id=fName.substring(0, k);//ȡ����ͷ���
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
				ShowMessage(vid+"����ͼ��:"+filename);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			
		}
	}	*/
	
	
	protected  void receiveDS8000(byte[] returnData,int returnLenght) throws SQLException, IOException//����DS8000����ͷ�������12��
	{
		
		ResultSet rs=null;
		int failnum=0;//����ʧ�ܵ�����
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
//		String FirstAddrString="";//���������׵�ַ
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
		int Tvalue_A=0;//A·�¶�--���13
		int Hvalue_A=0;//A·ʪ��--���14
		int Tvalue_B=0;//B·�¶�--���15
		int Hvalue_B=0;//B·ʪ��--���16
		Tvalue_A=(returnData[2]&0xFF)-40;
		Hvalue_A=(returnData[3]&0xFF);
		Tvalue_B=(returnData[4]&0xFF)-40;
		Hvalue_B=(returnData[5]&0xFF);
		
		//ShowMessage("AB��ʪ��1��"+Tvalue_A+"<>"+Hvalue_A+"<>"+Tvalue_B+"<>"+Hvalue_B);
		
		/***********�쳣���*****************/
		if(Tvalue_A>70||Tvalue_B>70)
		{
			writeLog("�������������¶ȹ����쳣��");//д�������־
		}
		/***********************/

		for(int i=13;i<17;i++)//�ֱ���A·�¶ȡ�A·ʪ�ȡ�B·�¶ȡ�B·ʪ��
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
				//System.out.println("�¶� "+(i-18)+": "+tvalue+" δ���ò��������ݽӿ���Ϣ��");
			}
			else
			{
				switch (i) {
				case 13:
						ds.execute(Tvalue_A, 0, 0,sample_id);//���ô洢���̽����յ������ݴ������ݿ�
						break;
				case 14:
					ds.executeHum(0, 0, Hvalue_A,sample_id);//���ô洢���̽����յ������ݴ������ݿ�
					break;
				case 15:
					ds.execute(Tvalue_B, 0, 0,sample_id);//���ô洢���̽����յ������ݴ������ݿ�
					break;
				case 16:
					ds.executeHum(0, 0, Hvalue_B,sample_id);//���ô洢���̽����յ������ݴ������ݿ�
					break;
				default:
					break;
				}
			}
		}
		
		
		float tvalue=0;
		for(int i=9;i<21;i++)//�ֱ�����12���¶�����
		{
			tvalue=(returnData[i]&0xFF);
			if(tvalue==255)
			{
				tvalue=0;
			}
			/***********�쳣���*****************/
			if(Tvalue_A>70||Tvalue_B>70)
			{
				writeLog("�������������¶ȹ����쳣��");//д�������־
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
				//System.out.println("�¶� "+(i-18)+": "+tvalue+" δ���ò��������ݽӿ���Ϣ��");
			}
			else
			{
				ds.execute(tvalue, 0, 0,sample_id);//���ô洢���̽����յ������ݴ������ݿ�
			}
			
//			if(tvalue>=Para_PreExceed_Tem&&tvalue<Para_Exceed_Tem)//����Ԥ���ź�
//			{
//				returnAlarmSingal(device_Address,FirstAddrString,"1");
//			}
//			else if(tvalue>=Para_Exceed_Tem)//���ر����ź�
//			{
//				returnAlarmSingal(device_Address,FirstAddrString,"0");
//			}
		}

		
		rs=null;
		//ds.close();
		//ds=null;
		//bs=null;
		ShowMessage(device_Address+"�ɹ�����  < DS8000 > ��Ч����:"+(16-failnum)+" ;ʧ��: "+failnum);
		
		//byte localTempType03_08=returnData[6];//������������
		System.gc();
		
	}

	protected  void receivePoor(byte[] returnData,int returnLenght) throws SQLException, IOException//���ս���ͷ����16*3=48
	{
		//���ص������д��±�10��ʼ��57Ϊ�¶����ݣ������ò������Ӧ�����ݵ�ַʱ��Ҫ�ӵ�ַ1��ʼ��ţ�һֱ��48.
		//System.out.println("�¶� ���ݣ�");
		//byteandstring bs=new byteandstring();
		//dataset ds=new dataset();
		ResultSet rs=null;
		int failnum=0;//����ʧ�ܵ�����
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
		String FirstAddrString="";//���������׵�ַ
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
		for(int i=10;i<57;i++)//�ֱ�����48���¶�����
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
				
				//System.out.println("�¶� "+i+": "+tvalue+"<>"+sample_id+"<>"+get_sampleid);
				if(sample_id.equals("")||sample_id==null)
				{
					//System.out.println("�¶� "+i+": "+tvalue+"<>"+sample_id+"fail");
					//failnum++;
					//System.out.println("�¶� "+(i-18)+": "+tvalue+" δ���ò��������ݽӿ���Ϣ��");
				}
				else
				{
					//System.out.println("�¶� "+i+": "+tvalue);
					failnum++;
					ds.execute(tvalue, 0, 0,sample_id);//���ô洢���̽����յ������ݴ������ݿ�
				}
				/*
				 //���ر����ź�
				if(tvalue>=Para_PreExceed_Tem&&tvalue<Para_Exceed_Tem)//����Ԥ���ź�
				{
					returnAlarmSingal(device_Address,FirstAddrString,"1");
				}
				else if(tvalue>=Para_Exceed_Tem)//���ر����ź�
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
		//ShowMessage(device_Address+"�ɹ�������Ч����:"+(48-failnum)+" ;ʧ��: "+failnum);
		ShowMessage(device_Address+"�ɹ�������Ч����:"+failnum+" ;ʧ��: "+(48-failnum));
		System.gc();
		
	}
	
	protected  void receiveHost(byte[] returnData,int returnLenght) throws SQLException //������������220�����ݣ�48*4=192��������
, IOException
	{
		//���ص������д��±�19��ʼ��210Ϊ�¶����ݣ������ò������Ӧ�����ݵ�ַʱ��Ҫ�ӵ�ַ1��ʼ��ţ�һֱ��48*4=192.

		ResultSet rs=null;
		int failnum=0;//����ʧ�ܵ�����
		String device_Address="";
		device_Address=bs.bytesToHexString(returnData[3]);

		/////////////�ɰ汾����192�������ݵĴ�����/////////////////////
		float tvalue=0;
		for(int i=19;i<211;i++)//�ֱ�����192���¶�����
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
					//System.out.println("�¶� "+(i-18)+": "+tvalue+" δ���ò��������ݽӿ���Ϣ��");
				}
				else
				{
					ds.execute(tvalue, 0, 0,sample_id);//���ô洢���̽����յ������ݴ������ݿ�
					//ShowMessage("��ȷ��������  "+i+" : "+Integer.toHexString(returnData[i]&0xff));
				}
			}
			
		}
		//System.out.println(device_Address+"�ɹ�����:"+(192-failnum)+" ;ʧ��: "+failnum);
		ShowMessage(device_Address+"�ɹ�����:"+(192-failnum)+" ;ʧ��: "+failnum);
		rs.close();
		rs=null;
		//ds.close();
		//ds=null;
		//bs=null;
		System.gc();
	}
	
	public void returnAlarmSingal(String device_Address,String nodeAddress,String signal) throws SQLException//����Ч�¶�ֵ��������ֵ�Ƿ��ر����ź�
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
			DatagramPacket sendDp=null;//���Ͱ�
			sendData=bs.hexStringToBytes(cmdString);
			InetAddress address=InetAddress.getByName(Device_IP);
			sendDp=new DatagramPacket(sendData, sendData.length, address,Integer.parseInt(Device_Port));
			DatagramSocket reds=new DatagramSocket();
			reds.send(sendDp);//�������ݰ�
			System.out.println("���ر����źţ�"+cmdString);
			ShowMessage("���ر����źţ�"+cmdString);
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
	
			     String s1 = msg;//"��� 0445218js";
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


