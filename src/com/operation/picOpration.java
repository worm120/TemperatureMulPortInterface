package com.operation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class picOpration
{
  public static void main(String[] args) {}
  
  public static boolean getImg(String ip, String port, String path, String imageName)
  {
    boolean flag = false;
    Runtime rn = Runtime.getRuntime();
    try
    {
      Process p = null;
      

      String imgName = imageName;
      String exePath = path + "Debug\\testsdk.exe";
      String cmd = exePath + " " + ip + " " + port + " " + imgName;
      p = rn.exec(cmd);
      p.waitFor();
      p.destroy();
      flag = true;
    }
    catch (Exception e)
    {
      return flag;
    }
    return flag;
  }
  
  public void writeLog(String msg)
  {
    try
    {
      File dir = new File("e:\\logs");
      if (!dir.exists()) {
        dir.mkdir();
      }
      Date time = new Date();
      DateFormat format = new SimpleDateFormat("yyMMdd");
      String svgpath = "e:\\logs\\" + format.format(time) + ".txt";
      
      File file = new File(svgpath);
      if (!file.exists()) {
        file.createNewFile();
      }
      FileOutputStream fos = new FileOutputStream(file, true);
      OutputStreamWriter osw = new OutputStreamWriter(fos);
      BufferedWriter bw = new BufferedWriter(osw);
      
      String s1 = msg;
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
