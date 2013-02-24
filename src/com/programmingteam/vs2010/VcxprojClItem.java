package com.programmingteam.vs2010;

import java.util.ArrayList;

public class VcxprojClItem
{
	ArrayList<String> mFilterLines;
	ArrayList<String> mProjLines;
	
	String mFileRelativePath;
	
	boolean mFlgDeleted;
	
	enum Ctx { PROJ, FILTER }
	
	public VcxprojClItem()
	{
		mFlgDeleted = false;
		mProjLines = new ArrayList<String>();
		mFilterLines = new ArrayList<String>();
	}
	
	public boolean addProjLine(String line)
	{
		if(line.matches(".*<ClInclude .*") || line.matches(".*<ClCompile .*"))
		{
			mFileRelativePath = line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""));
			return line.matches(".*/>.*");
		}
		else if(line.matches(".*</ClInclude>.*") || line.matches(".*</ClCompile>.*"))
		{
			return true;
		}
		else
		{
			mProjLines.add(line);
			return false;
		}
	}
	
	public boolean addFilterLine(String line)
	{
		if(line.matches(".*<ClInclude .*") || line.matches(".*<ClCompile .*"))
		{
			if( mFileRelativePath!=null && 
				!mFileRelativePath.equals(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""))))
			{
				System.err.println("Error: filter path not matches proj path!");
				System.exit(-1);
			}
			return line.matches(".*/>.*");
		}
		else if(line.matches(".*</ClInclude>.*") || line.matches(".*</ClCompile>.*"))
		{
			return true;
		}
		else
		{
			mFilterLines.add(line);
			return false;
		}
	}
	
	public String getFilePath()
	{
		return mFileRelativePath;
	}
	
	public void setRelativePath(String path)
	{
		mFileRelativePath = path;
	}
	
	public void setFilter(String filter)
	{
		for(int i=0; i<mFilterLines.size(); ++i)
		{
			String s = mFilterLines.get(i);
			if(s.matches(".*<Filter>.*"))
			{
				s = s.substring(0, s.indexOf('>')+1);
				s = s + filter + "</Filter>";
				mFilterLines.set(i, s);
				return;
			}
		}
		
		//Filter not added till this line - put new line to filters
		mFilterLines.add("      <Filter>"+filter+"</Filter>");
	}
	
	public void setDeleted(boolean flgDeleted)
	{
		this.mFlgDeleted = flgDeleted;
	}
	
	public boolean getDeleted()
	{
		return mFlgDeleted;
	}
	
	public void debugPrint()
	{
		System.out.println("  <ClInclude Include=\""+ mFileRelativePath +"\">");
		for(String s: mProjLines) System.out.println(s);
		for(String s: mFilterLines) System.out.println(s);
	}
}
