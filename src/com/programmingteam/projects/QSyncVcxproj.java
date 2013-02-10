package com.programmingteam.projects;

import java.io.File;
import java.util.ArrayList;

import com.programmingteam.Helpers;

///
/// \brief Representation of project defined in qsync file
/// Includes absolute paths to vcxproj and vcxproj.filters files
///	and list of imports to be synchronized to this project
///
public class QSyncVcxproj
{
	private String mVcxproj;
	private String mVcxprojFilters;
	private File mPwd;
	private ArrayList<QSyncImport> mImportList;
	
	///
	/// \brief constructs project abstraction
	/// \param[in] proj 	Absolute path to vcxproj file
	/// \param[in] filters	Absolute path to vcxproj.filters file
	///
	QSyncVcxproj(String proj, String filters)
	{
		mVcxproj = proj;
		mVcxprojFilters = filters;
		mImportList = new ArrayList<QSyncImport>();
		
		mPwd = (new File(mVcxproj)).getParentFile();
	}
	
	///
	/// \brief Adds import object to import list
	/// Improts are used to point out what files should be imported to visual project
	/// \param[in] QSyncImport improt object to add
	///
	void addImport(QSyncImport imp)
	{
		mImportList.add(imp);
	}
	
	///
	/// \brief returns vcxproj (absolute path)
	/// \return String 	absolute path to vcsproj file
	///
	public String getVcxproj()
	{
		return mVcxproj;
	}

	///
	/// \brief returns vcxproj.filters (absolute path)
	/// \return String 	absolute path to vcsproj.filters file
	/// 
	public String getVcxprojFilters()
	{
		return mVcxprojFilters;
	}
	
	///
	/// \brief returns absolute path to folder containing both vcxproj and filters files
	/// \return File   absolute path to folder containing both vcxproj and filters files
	///
	public File getPWD() 
	{
		return mPwd;
	}
	
	///
	/// \brief returns relative path between current pwd (vcxproj folder) and given file
	/// Parameter file should be given as ABSOLUTE path
	///
	/// \param[in] File absolute path to file to which relative path should be generated
	/// \returns File relative path to file from vcxproj folder
	///
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
	
	///
	/// \brief prints all data contained in this object
	///
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
