package com.programmingteam.projects;

import java.util.ArrayList;
import java.util.List;

public class QSyncImport
{
	private String mToFilter;
	private String mInclude;
	private String mSrc;
	private List<String> mMisc;

	public QSyncImport(String toFilter)
	{
		mToFilter = toFilter;
		mMisc = new ArrayList<String>();
	}

	public void setInclude(String inc)
	{
		this.mInclude = inc;
	}
	
	public void setSrc(String src)
	{
		this.mSrc = src;
	}

	public void addMisc(String misc)
	{
		mMisc.add(misc);
	}

	public String getToFilter()
	{
		return mToFilter;
	}
	
	public String getInclude()
	{
		return mInclude;
	}

	public String getSrc()
	{
		return mSrc;
	}
	
	public void debugPrint()
	{
		System.out.println("mToFilter: " + mToFilter);
		System.out.println("\tmInclude: " + mInclude);
		System.out.println("\tmSrc: " + mSrc);
		for(String s: mMisc)
		{
			System.out.println("\tMisc: " + s);
		}
	}
}
