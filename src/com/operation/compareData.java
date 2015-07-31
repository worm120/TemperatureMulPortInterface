package com.operation;

import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class compareData
{
//  public static void main(String[] args)
//  {
//    String t1 = "06:00:00";
//    String t2 = "16:30:00";
//    
//    Date dt = new Date();
//    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//    String dString = df.format(dt);
//    System.out.println(dString);
//    
//    t1 = dString + " " + t1;
//    t2 = dString + " " + t2;
//    
//    int i = compare_date(t1, t2);
//    System.out.println("i==" + i);
//  }
  
  public static int compare_date(String DATE1, String DATE2)
  {
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try
    {
      Date dt1 = df.parse(DATE1);
      Date dt2 = df.parse(DATE2);
      if (dt1.getTime() > dt2.getTime()) {
        return 1;
      }
      if (dt1.getTime() < dt2.getTime()) {
        return -1;
      }
      return 0;
    }
    catch (Exception exception)
    {
      exception.printStackTrace();
    }
    return 0;
  }
}
