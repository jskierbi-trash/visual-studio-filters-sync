package com.programmingteam.projects;

import java.io.File;
import java.util.ArrayList;

public class QSyncVcxproj
{
	private String mVcxproj;
	private String mVcxprojFilters;
	private ArrayList<QSyncImport> mImportList;
	
	QSyncVcxproj(String proj, String filters)
	{
		mVcxproj = proj;
		mVcxprojFilters = filters;
		mImportList = new ArrayList<QSyncImport>();
	}
	
	void addImport(QSyncImport imp)
	{
		mImportList.add(imp);
	}
	
	public String getVcxproj()
	{
		return mVcxproj;
	}

	public String getVcxprojFilters()
	{
		return mVcxprojFilters;
	}
	
	File getPWD()
	{
		return null;
	}
	
	public void debugPrint()
	{
		System.out.println("vcxproj: " + mVcxproj);
		System.out.println("vcxproj.filters: " + mVcxprojFilters);
		for(QSyncImport imp: mImportList)
		{
			imp.debugPrint();
		}
	}
}
