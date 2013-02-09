package com.programmingteam;

import java.io.File;

public class Helpers
{
	public static boolean isCompile(String file, String compileExt)
	{
		
		return true;
	}
	
	public static boolean isInclude(String file, String incExt)
	{
		return false;
	}
	
	public static String getRelativePath(File f, File relativeTo)
	{
		return null;
	}
	
	public static String getFileExt(String file)
	{
		int index = file.lastIndexOf('.');
		if(index!=-1 && index<file.length())
		{
			return file.substring(index+1, file.length());
		}
		else
		{
			return "";
		}
	}
	
	/// \biref resolves path - resolves env vars and fixes slashes 
	public static String resolvePath(String path)
	{
		return fixSlashes(resolveEnvVars(path));
	}
	
	
	public static String fixSlashes(String file)
	{
		String output = file.replace('/','\\');
		output = output.replace("\\\\","\\");
		output = output.replace("\\\\","\\");
		//System.out.println(output);
		
		if(output.charAt(0)=='\\') 
			output=output.substring(1, output.length());
		if(output.charAt(output.length()-1) == '\\')
			output=output.substring(0, output.length()-1);
		return output;
	}
	
	public static String resolveEnvVars(String file)
	{
		StringBuffer varBuff = null;
		StringBuffer outBuff = new StringBuffer();
		String output = null;
		boolean flgStarted = false;
		for(int i=0; i<file.length(); ++i)
		{
			if(file.charAt(i)=='$' && file.charAt(i+1)=='{')
			{
				varBuff = new StringBuffer();
				flgStarted = true;
				++i;
				continue;
			}
			
			if(file.charAt(i)=='}')
			{
				String varToResolve = varBuff.toString();
				String resolved = System.getenv(varToResolve);
				outBuff.append(resolved);
				flgStarted = false;
				continue;
			}
			
			if(flgStarted)
			{
				varBuff.append(file.charAt(i));
				continue;
			}
			
			if(!flgStarted)
			{
				outBuff.append(file.charAt(i));
			}
		}
		
		output = outBuff.toString();
		return output;
	}
}
