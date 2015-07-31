package com.operation;

import java.io.PrintStream;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class dataset
{
  String url = "jdbc:sqlserver://localhost:1433;DatabaseName=SDB";
  String username = "sa";
  String password = "qiye016700";
  Connection conn = null;
  Statement stmt = null;
  ResultSet rs = null;
  
  public dataset() {}
  
  public dataset(String url_re, String password_re)
  {
    this.url = url_re;
    this.password = password_re;
  }
  
  Connection getCon()
  {
    try
    {
      if ((this.conn == null) || (this.conn.isClosed())) {
        try
        {
          Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
          this.conn = DriverManager.getConnection(this.url, this.username, this.password);
        }
        catch (Exception ex)
        {
          ex.printStackTrace();
        }
      }
      return this.conn;
    }
    catch (SQLException e)
    {
      e.printStackTrace();
    }
	return null;
  }
  
  public ResultSet select(String sql)
  {
    try
    {
      this.conn = getCon();
      this.stmt = this.conn.createStatement(1004, 1007);
      this.rs = this.stmt.executeQuery(sql);
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return this.rs;
  }
  
  public int update(String sql)
  {
    int i = 0;
    try
    {
      this.conn = getCon();
      this.stmt = this.conn.createStatement();
      i = this.stmt.executeUpdate(sql);
      if (i == 0) {
        System.out.print(sql + ": 未成功更新");
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
    return i;
  }
  
  public void execute(float Tvalue, float Vvalue, float Hvalue, String nodeid)
  {
    int i = 0;
    try
    {
      this.conn = getCon();
      CallableStatement c = this.conn.prepareCall("{call insertTemperature(?,?,?,?)}");
      c.setFloat(1, Tvalue);
      c.setFloat(2, Vvalue);
      c.setFloat(3, Hvalue);
      c.setString(4, nodeid);
      c.execute();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public void executeHum(float Tvalue, float Vvalue, float Hvalue, String nodeid)
  {
    int i = 0;
    try
    {
      this.conn = getCon();
      CallableStatement c = this.conn.prepareCall("{call insertHumidity(?,?,?,?)}");
      c.setFloat(1, Tvalue);
      c.setFloat(2, Vvalue);
      c.setFloat(3, Hvalue);
      c.setString(4, nodeid);
      c.execute();
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  public void close()
  {
    try
    {
      if (this.rs != null) {
        this.rs.close();
      }
      if (this.stmt != null) {
        this.stmt.close();
      }
      if (this.conn != null) {
        this.conn.close();
      }
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
    }
  }
}
