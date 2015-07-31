package com.operation;

public class byteandstring {
	/*
	* @param src byte[] data   
	 * @return hex string   
	 */  
	public static String bytesToHexString(byte[] src){   
	    StringBuilder stringBuilder = new StringBuilder("");   
	    if (src == null || src.length <= 0) {   
	        return null;   
	    }   
	    for (int i = 0; i < src.length; i++) {   
	        int v = src[i] & 0xFF;   
	        String hv = Integer.toHexString(v);   
	        if (hv.length() < 2) {   
	            stringBuilder.append(0);   
	        }   
	        stringBuilder.append(hv);   
	    }   
	    return stringBuilder.toString();   
	}  
	
	public static String bytesToHexString(byte src){    
	    String hv = Integer.toHexString(src & 0xFF);   
        if (hv.length() < 2) {   
        	hv="0"+hv;   
        }    
	    return hv;   
	}   
	
	/**  
	 * Convert hex string to byte[]  
	 * @param hexString the hex string  
	 * @return byte[]  
	 */  
	public static byte[] hexStringToBytes(String hexString) {   
	    if (hexString == null || hexString.equals("")) {   
	        return null;   
	    }   
	    hexString = hexString.toUpperCase();   
	    int length = hexString.length() / 2;   
	    char[] hexChars = hexString.toCharArray();   
	    byte[] d = new byte[length];   
	    for (int i = 0; i < length; i++) {   
	        int pos = i * 2;   
	        d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));   
	    }   
	    return d;   
	}   
	
	public static byte hexStringTobytes(String hexString) {   
	    if (hexString == null || hexString.equals("")) {   
	        return (byte)(0xF0);   
	    }   
	    hexString = hexString.toUpperCase();   
	    char[] hexChars = hexString.toCharArray();    
	     
	    byte d = (byte) (charToByte(hexChars[0]) << 4 | charToByte(hexChars[1]));   
	    return d;   
	}   
	
	/**  
	 * Convert char to byte  
	 * @param c char  
	 * @return byte  
	 */  
	 private static byte charToByte(char c) {   
	    return (byte) "0123456789ABCDEF".indexOf(c);   
	}  

}
