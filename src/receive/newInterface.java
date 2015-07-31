package receive;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.*;
import java.io.IOException;
import java.net.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;


import com.operation.byteandstring;
import com.operation.dataset;


public class newInterface{
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		final JFrame loginFrame=new JFrame("大成电力温湿度与图像监测系统-数据采集");
		Container c=loginFrame.getContentPane();
		c.setLayout(new FlowLayout());
		
		JPanel p1=new JPanel();
		p1.setLayout(new FlowLayout());
		p1.setSize(100, 100);
		JLabel n1=new JLabel("用户名：");
		final JTextField name=new JTextField(20);
		p1.add(n1);
		p1.add(name);
		
		JPanel p2=new JPanel();
		p2.setLayout(new FlowLayout());
		JLabel n2=new JLabel("    密码：");
		final JTextField passward=new JTextField(20);
		p2.add(n2);
		p2.add(passward);
		
		JPanel p3=new JPanel();
		p2.setLayout(new FlowLayout());
		JButton submit=new JButton("登录");
		submit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub
				String is_exist="select User_Password from Users where User_Login='"+name.getText().trim()+"' ";
				dataset ds=new dataset();
				ResultSet rs=ds.select(is_exist);
				String flag="";
				if(rs!=null)
				{
					try {
						while(rs.next())
						{
							flag=rs.getString(1);
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
					ds.close();
				} 
				catch (SQLException e) 
				{
					e.printStackTrace();
				}
				if(flag.equals( passward.getText().trim()))
				{
					//用户名、密码正确，进入系统，开始数据接收进程
					try
					{
						if(true)
						{
							loginFrame.setVisible(false);

							/////////////////////////生成接口主界面//////////////////////////////
							final JTextArea showpacket=new JTextArea(30,30);//显示收到的返回数据
							final JTextArea showsend=new JTextArea(20,20);//显示发送的数据
							final JTextField datasend=new JTextField(20);//输入要发送的命令
							final JTextField IP=new JTextField(20);//目标ip
							final JTextField port=new JTextField(20);//目的端口
							JFrame frame=null;
							JButton send=null;//发送
							JButton end=null;

							//////////////////////////////////////////////////////////////////////////
							frame =new JFrame();
							Container c=frame.getContentPane();
					        //c.setLayout(new FlowLayout(FlowLayout.LEFT));new BoxLayout(rleft, BoxLayout.Y_AXIS)
							//c.setLayout(new BoxLayout(c, BoxLayout.X_AXIS));
							c.setLayout(new GridLayout(1,2));
							
					        JPanel pleft=new JPanel();
					        JPanel rleft=new JPanel();
					        
					        //showpacket = new JTextArea(30,30);
					        showpacket.setLineWrap(true);
					        showpacket.setWrapStyleWord(true);

					        JScrollPane s1=new JScrollPane(showpacket);
					        pleft.add(s1);

					        showsend.setLineWrap(true);
					        showsend.setWrapStyleWord(true);         
					        JScrollPane s2=new JScrollPane(showsend);

					        JPanel psend=new JPanel();
					        
					        JLabel n1=new JLabel("   a目标IP:");
					        //IP=new JTextField(20);
					        JPanel p1=new JPanel();
					        p1.add(n1);
					        p1.add(IP);
					        
					        JLabel n2=new JLabel("目的端口:");
					        //port=new JTextField(20);
					        JPanel p2=new JPanel();
					        p2.add(n2);
					        p2.add(port);
					        
					        JLabel n3=new JLabel("发送内容:");
					        JPanel p3=new JPanel();
					        p3.add(n3);
					        p3.add(datasend);
					        
					        send=new JButton("发送");
					        send.setSize(50, 30);
					        //send.addActionListener(this);
					        send.addActionListener(new ActionListener() {
					        	@Override
					        	public void actionPerformed(ActionEvent e) 
					        	{
					        		String cmd=datasend.getText().trim().toString();
					        		byteandstring bs=new byteandstring();
					        		byte[] datas=bs.hexStringToBytes(cmd);
					        		String ipText=IP.getText().trim().toString();
					        		int portText=Integer.parseInt(port.getText().trim().toString());
					        		DatagramSocket ds=null;
					        		DatagramPacket sendDp=null;
					        		InetAddress address;
					        		try 
					        		{
					        			address = InetAddress.getByName(ipText);
					        			sendDp=new DatagramPacket(datas, datas.length, address,portText);
					        			ds=new DatagramSocket();
					        		} 
					        		catch (UnknownHostException e2) 
					        		{
					        			e2.printStackTrace();
					        		} catch (SocketException e1) {
					        			// TODO Auto-generated catch block
					        			e1.printStackTrace();
					        		}
					        		
					        		try 
					        		{
					        			ds.send(sendDp);
					        		} 
					        		catch (IOException e1) 
					        		{
					        			e1.printStackTrace();
					        		}
					        		showsend.setText(showsend.getText()+"\r\n "+ipText+" "+portText+": "+ bs.bytesToHexString(datas)+";");
					        	}
					        });
					        psend.setLayout(new GridLayout(4,1));
					        psend.add(p1);
					        psend.add(p2);
					        psend.add(p3);
					        psend.add(send);

					        JPanel setJPanel=new JPanel(new GridLayout(2,1));
					        setJPanel.setSize(50, 30);
					        JButton setAlarm=new JButton("移动侦测报警设置");
					        setAlarm.setSize(10, 10);
					        JButton refreshAlarm=new JButton("刷新相机服务器");
					        setAlarm.setSize(50, 30);
					        setJPanel.add(setAlarm);
					        setJPanel.add(refreshAlarm);
					        
					        setAlarm.addActionListener(new ActionListener() {
					        	@Override
					        	public void actionPerformed(ActionEvent e) 
					        	{
					        		
					        	}
					        	
					        });
					        
					        refreshAlarm.addActionListener(new ActionListener() {
					        	@Override
					        	public void actionPerformed(ActionEvent e) 
					        	{
					        		
					        	}
					        	
					        });

					        //rleft.setLayout(new BoxLayout(rleft, BoxLayout.Y_AXIS));
					        rleft.setLayout(new GridLayout(3,1));
					        //rleft.add(showpicture);
					        rleft.add(s2);
					        rleft.add(psend);
					        //rleft.setSize(280, 800);
					        rleft.add(setJPanel);
					        c.add(pleft);
					        c.add(rleft);
					        frame.setSize(800, 600);
					        frame.setTitle("温度弧光安全监测温度采集系统");
					        //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
					        frame.setVisible(true);
					        frame.show();
					        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					        frame.addWindowListener(new WindowListener() {
								
								@Override
								public void windowOpened(WindowEvent arg0) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void windowIconified(WindowEvent arg0) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void windowDeiconified(WindowEvent arg0) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void windowDeactivated(WindowEvent arg0) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void windowClosing(WindowEvent arg0) {
									// TODO Auto-generated method stub
									int re=JOptionPane.showConfirmDialog(null,"数据采集接口关闭后，系统将无法接收各传感器的数据，确认要关闭数据接口吗？","关闭数据接口",JOptionPane.YES_NO_OPTION);
									if(re==JOptionPane.YES_OPTION)
									{
										System.exit(0);
									}
									else if(re==JOptionPane.CLOSED_OPTION)
									{
										//System.out.println("dddd");
										//return;
									}
								}
								
								@Override
								public void windowClosed(WindowEvent arg0) {
									// TODO Auto-generated method stub
									
								}
								
								@Override
								public void windowActivated(WindowEvent arg0) {
									// TODO Auto-generated method stub
									
								}
							});
					        
					        int[] portlist={9004};
					        for(int i=0;i<portlist.length;i++)
					        {
					        	
					        	try 
					        	{
									(new temp_Hum_Hugung_OnetoOneRecieve(showpacket,showsend,portlist[i],i)).start();//DS6000的温度+DS8000温度+湿度+弧光
								} 
					        	catch (SQLException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
					        }
		
					        String get_sub="select Substation_ID from Parameter";
							dataset dss=new dataset();
							rs=dss.select(get_sub);
							if(rs!=null)
							{
								try {
									while(rs.next())
									{
										String sid=rs.getString(1);
										if(sid!=null&&sid.trim().equals("")==false&&sid.equals(" ")==false)
										{
											(new  receiveImg2(showpacket,sid)).start();
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
								dss.close();
							} 
							catch (SQLException e) 
							{
								e.printStackTrace();
							}
					        
						}
					} 
					finally
					{
						
					}

				}
				else
				{
					JOptionPane.showMessageDialog(null, "用户名密码不正确，请重新输入！", "登录提示", JOptionPane.ERROR_MESSAGE);
				}
				//ds.close();
			}
		});
		
		JButton exit=new JButton("取消");
		exit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				System.exit(0);
			}
		});
		
		p3.add(submit);
		p3.add(exit);

		JPanel centerPanel=new JPanel();
		centerPanel.setLayout(new GridLayout(3,1));
		centerPanel.setSize(300, 30);
		//centerPanel.add(icon);
		centerPanel.add(p1);
		centerPanel.add(p2);
		centerPanel.add(p3);
		
		c.add(centerPanel);
		
		loginFrame.setSize(400, 400);
		loginFrame.setVisible(true);
		//loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		loginFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		loginFrame.pack();
		loginFrame.show();
		
		//---------------------------设置窗口居中----------------------------------------------------------
        int windowWidth = loginFrame.getWidth();                    //获得窗口宽
        int windowHeight = loginFrame.getHeight();                  //获得窗口高
        Toolkit kit = Toolkit.getDefaultToolkit();             //定义工具包
        Dimension screenSize = kit.getScreenSize();            //获取屏幕的尺寸
        int screenWidth = screenSize.width;                    //获取屏幕的宽
        int screenHeight = screenSize.height;                  //获取屏幕的高
        loginFrame.setLocation(screenWidth/2-windowWidth/2, screenHeight/2-windowHeight/2);//设置窗口居中显示
        //------------------------------------------------------------------------------------------------
	}

}
