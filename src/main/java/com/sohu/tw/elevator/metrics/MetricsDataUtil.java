package com.sohu.tw.elevator.metrics;

import java.text.DecimalFormat;

public class MetricsDataUtil {
	private static DecimalFormat df = new DecimalFormat("#.00");
	public static String parseThroughPut(long size){
		float f = size/1024F;
		if ( f<1)
			return df.format(size)+"B";
		else{
			f = f/1024F;
			if( f<1)
				return  df.format(size/1024F)+"K";
			else{
				f=f/1024F;
				if(f<1)
					return df.format(size/1024F/1024F)+"M";
				else{
					f=f/1024F;
					if(f<1)
						return df.format(size/1024F/1024F/1024F)+"G";
				}
			}
		}
		return "";
	}

}
