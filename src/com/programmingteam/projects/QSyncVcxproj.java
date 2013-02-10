package com.programmingteam.projects;

import java.io.File;
import java.util.ArrayList;

import com.programmingteam.Helpers;

public class QSyncVcxproj
{
	private String mVcxproj;
	private String mVcxprojFilters;
	private File mPwd;
	private ArrayList<QSyncImport> mImportList;
	
	QSyncVcxproj(String proj, String filters)
	{
		mVcxproj = proj;
		mVcxprojFilters = filters;
		mImportList = new ArrayList<QSyncImport>();
		
		mPwd = (new File(mVcxproj)).getParentFile();
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
	
	public File getPWD() 
	{
		return mPwd;
	}
	
	/// \brief returns relative path between current pwd (vcxproj folder) and given file
	/// Parameter file should be given as ABSOLUTE path
	public File getRelativeFile(File inFile)
	{
		String input = inFile.getAbsolutePath();
		String proj =  mVcxproj;
		
		int offset = 0;
		for(; offset<input.length() && offset<proj.length(); ++offset)
		{
			if(input.charAt(offset)!=proj.charAt(offset))
				break;
		}
		
		StringBuffer buff = new StringBuffer();
		for(int i=0; i<Helpers.countOccurances(proj.substring(offset, proj.length()), '\\'); ++i)
		{
			buff.append("..\\");
		}
		buff.append(input.substring(offset, input.length()));
		return new File(buff.toString());
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
