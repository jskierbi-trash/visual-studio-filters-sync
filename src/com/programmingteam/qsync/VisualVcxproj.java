package com.programmingteam.qsync;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

///
/// \brief Visual 2010 project representation
///
public class VisualVcxproj
{
	ArrayList<String> mProjLines;
	ArrayList<String> mFilterLines;
	
	File mProjFile;
	File mFilterFile;
	
	public VisualVcxproj(String vcxproj, String vcxprojFilters)
	{
		mProjLines = new ArrayList<String>();
		mFilterLines = new ArrayList<String>();
		
		mProjFile = new File(vcxproj);
		mFilterFile = new File(vcxprojFilters);

		try //Load *.vcxproj
		{
			BufferedReader in = new BufferedReader(new FileReader(mProjFile));
			String line;
			while((line=in.readLine())!=null) 
				mProjLines.add(line);
			in.close();
		}
		catch (FileNotFoundException ex) { System.err.println("File not found: " + vcxproj); System.exit(-1); }
		catch (IOException ex) { System.err.println("IOException reading file: " + vcxproj); System.exit(-1); }
		
		try //Load *.vcxproj.filters
		{
			BufferedReader in = new BufferedReader(new FileReader(mFilterFile));
			String line;
			while((line=in.readLine())!=null)
				mFilterLines.add(line);
			
			in.close();
		}
		catch (FileNotFoundException ex) { System.err.println("File not found: " + vcxproj); System.exit(-1); }
		catch (IOException ex) { System.err.println("IOException reading file: " + vcxproj); System.exit(-1); }
	}

	void addFile(File f, boolean flgIsHeader)
	{
		//Add file to sources/headers list
	}
	
	void deleteFile(File f, boolean flgIsHeader)
	{
		//Delete file from project/filters
	}
	
	void cleanFilters(String filterRoot)
	{
		//Clean filters
	}
	
	void updateFilter(File f)
	{
		//Make sure that filter exists
	}
	
	void moveFile(File oldFile, File newFile)
	{
		//Moves file in structure (1. find old file, 2. change location to new)
	}
	
	public void debugPrint()
	{
		System.out.println(">>> VCXPROJ <<<");
		for(String s: mProjLines)
		{
			System.out.println(s);
		}
		
		System.out.println("");
		System.out.println(">>> VCXPROJ.FILTERS <<<");
		for(String s: mFilterLines)
		{
			System.out.println(s);
		}
	}
}
