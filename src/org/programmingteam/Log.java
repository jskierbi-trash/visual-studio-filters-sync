package org.programmingteam;

public class Log
{
	public static void v(String s)
	{
		if(Main.OPTS.isVerbose() && !Main.OPTS.isQuiet())
			System.out.println(s);
	}
	
	public static void d(String s)
	{
		if(!Main.OPTS.isQuiet())
			System.out.println(s);
	}
	
	public static void e(String s)
	{
		if(!Main.OPTS.isQuiet()) // TODO do we log errors in quietmode?
			System.err.println(s);
	}
}
