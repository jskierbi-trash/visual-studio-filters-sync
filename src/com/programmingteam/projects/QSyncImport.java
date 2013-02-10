package com.programmingteam.projects;

import java.util.ArrayList;
import java.util.List;

///
/// \brief representation of single import to vcxproj/filters files
/// Single import contains of:
/// 	* destination filter path (Visual Studio specific)
///		* path to include files (used in create class for storing .h file)
/// 	* path to source files	(used in create class for storing .cpp file)
/// 	* list of misc folder to be included
///
public class QSyncImport
{
	private String mToFilter;
	private String mInclude;
	private String mSrc;
	private List<String> mMisc;
	
	///
	/// \brief constructs QSyncImport
	///
	QSyncImport(String toFilter)
	{
		mToFilter = toFilter;
		mMisc = new ArrayList<String>();
	}

	///
	/// \brief sets absolute path to include folder
	///
	public void setInclude(String inc)
	{
		this.mInclude = inc;
	}
	
	///
	/// \brief sets absolute path to source folder
	/// \param[in] String absolute path to src folder to be imported
	///
	public void setSrc(String src)
	{
		this.mSrc = src;
	}

	///
	/// \biref sets absolute path to misc folder
	/// \param[in] String absolute path to include folder to be imported
	///
	public void addMisc(String misc)
	{
		mMisc.add(misc);
	}

	///
	/// \brief ToFilter is a root for folder structure in visual studio project 
	/// \returns String toFilter path
	///
	public String getToFilter()
	{
		return mToFilter;
	}
	
	///
	/// \brief returns include absolute path
	///
	public String getInclude()
	{
		return mInclude;
	}

	///
	/// \brief returns sources absolute path
	///
	public String getSrc()
	{
		return mSrc;
	}
	
	///
	/// \brief prints all data contained in this object
	///
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
