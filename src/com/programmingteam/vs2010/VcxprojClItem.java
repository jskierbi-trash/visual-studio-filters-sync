package com.programmingteam.vs2010;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.programmingteam.Log;

public class VcxprojClItem
{
	ArrayList<String> mFilterLines;
	ArrayList<String> mProjLines;
	
	File mFileRelativePath;
	
	boolean mFlgDeleted;
	boolean mFlgFilterLines;
	
	enum Ctx { PROJ, FILTER }
	
	public VcxprojClItem()
	{
		mFlgDeleted = false;
		mFlgFilterLines = false;
		mProjLines = new ArrayList<String>();
		mFilterLines = new ArrayList<String>();
	}
	
	public boolean addProjLine(String line)
	{
		if(line.matches(".*<ClInclude .*") || line.matches(".*<ClCompile .*"))
		{
			mFileRelativePath = new File(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
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
		mFlgFilterLines = true;
		if(line.matches(".*<ClInclude .*") || line.matches(".*<ClCompile .*"))
		{
			if( mFileRelativePath!=null && 
				!mFileRelativePath.getPath().equals(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\""))))
			{
				Log.e("Error: filter path not matches proj path!");
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
	
	public File getFile()
	{
		return mFileRelativePath;
	}
	
	public void setRelativePath(String path)
	{
		mFileRelativePath = new File(path);
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
	
	public String getFilter()
	{
		for(int i=0; i<mFilterLines.size(); ++i)
		{
			String s = mFilterLines.get(i);
			if(s.matches(".*<Filter>.*</Filter>.*"))
			{
				return s.substring(s.indexOf('>')+1, s.lastIndexOf('<'));
			}
		}
		
		return null;
	}
	
	public void setExcludeFromBuild(ArrayList<String> log)
	{
		for(String s: mProjLines)
		{
			if(s.matches(".*<ExcludedFromBuild.*"))
			{
				//Log.d(" File already excluded!");
				return;
			}
		}
		//Log.d("Auto-exclude from build (by regexp): " + mFileRelativePath);
		log.add("Auto-exclude: " + mFileRelativePath);
		mProjLines.add("      <ExcludedFromBuild>true</ExcludedFromBuild>");
	}
	
	public boolean getDeleted()
	{
		return mFlgDeleted;
	}
	
	public List<String> getProjLines()
	{
		return mProjLines;
	}
	
	public List<String> getFilterLines()
	{
		return mFilterLines;
	}
	
	public void debugPrint()
	{
		Log.d("  <ClInclude Include=\""+ mFileRelativePath +"\">");
		for(String s: mProjLines) Log.d(s);
		for(String s: mFilterLines) Log.d(s);
	}
	
	public boolean isFilterLines()
	{
		return mFlgFilterLines;
	}
}
