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

public class fileOpration
{
  public static void main(String[] args)
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
      
      String s1 = "ÄãºÃ 0445218js";
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
