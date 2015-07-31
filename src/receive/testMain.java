package receive;

import java.text.SimpleDateFormat;
import java.util.Date;

public class testMain {
  public static void main(String[] args) {
	  SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd");
	  System.out.println(dfDate.format(new Date()));
}
}
