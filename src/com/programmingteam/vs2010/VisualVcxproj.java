package com.programmingteam.vs2010;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

///
/// \brief Visual 2010 project representation
///
public class VisualVcxproj
{

	////VCX project structure
	private ArrayList<String> mVcxHeader;
	private ArrayList<String> mVcxFooter;
	
	private ArrayList<String> mFilterHeader;
	private ArrayList<String> mFilterFooter;
	

	///Shared items
	private HashMap<String, ClItem> mClIncludeItems;
	private HashMap<String, ClItem> mClCompileItems;
	private HashSet<String> mFilters;
	
	private enum ParseContext { HEADER, FILTER, INCLUDE, COMPILE, FOOTER }
	
	File mProjFile;
	File mFilterFile;
	
	public VisualVcxproj(String vcxproj, String vcxprojFilters)
	{		
		mProjFile = new File(vcxproj);
		mFilterFile = new File(vcxprojFilters);
		
		mVcxHeader = new ArrayList<String>();
		mVcxFooter = new ArrayList<String>();
		
		mFilterHeader = new ArrayList<String>();
		mFilterFooter = new ArrayList<String>();
		
		mClIncludeItems = new HashMap<String, ClItem>();
		mClCompileItems = new HashMap<String, ClItem>();
		mFilters = new HashSet<String>();

		BufferedReader in =null;
		try //Load *.vcxproj
		{
			in = new BufferedReader(new FileReader(mProjFile));
			ParseContext context = ParseContext.HEADER;
			String line, contextLine;
			ClItem item = new ClItem();
			int lineCount = 0;
			while((line=in.readLine())!=null)
			{
				++lineCount;
				if(line.matches(".*<ItemGroup.*")) //Start context
				{
					contextLine = line;
					line = in.readLine();
					if(line!=null)
					{
						if(line.matches(".*<ClInclude .*"))
						{
							context = ParseContext.INCLUDE;
							System.out.println("*** Context: ClInclude");
						}
						else if(line.matches(".*<ClCompile .*"))
						{
							context = ParseContext.COMPILE;
							System.out.println("*** Context: ClCompile");
						}
						else
						{
							if(ParseContext.INCLUDE==context || ParseContext.COMPILE==context)
							{
								context = ParseContext.FOOTER;
								mVcxFooter.add(contextLine);
							}
							else
							{
								mVcxHeader.add(contextLine);
							}
							
							System.out.println("*** No context change");
						}
					}
					else
						throw new ParseException("<ItemGroup> element not closed at line: " + lineCount, lineCount);
				}
				
				if(ParseContext.HEADER==context)
					mVcxHeader.add(line);
				else if(ParseContext.FOOTER==context)
					mVcxFooter.add(line);
				else if(ParseContext.COMPILE==context)
				{
					if(item.addProjLine(line)) //is element closed?
					{
						mClCompileItems.put(item.getFilePath(), item);
						item = new ClItem();
					}
				}
				else if(ParseContext.INCLUDE==context)
				{
					if(item.addProjLine(line))
					{
						mClIncludeItems.put(item.getFilePath(), item);
						item = new ClItem();
					}
				}
			}
		}
		catch (ParseException e) { System.err.println("Parse excepiton: " + e.getMessage()); System.exit(-1); }
		catch (FileNotFoundException e) { System.err.println("File not found: " + vcxproj); System.exit(-1); }
		catch (IOException e) { System.err.println("IOException reading file: " + vcxproj); System.exit(-1); }
		finally { try { if(in!=null) in.close(); } catch (IOException e) { System.err.println("IOException closing file"); }}
		
		System.out.println(">>> Filters...");
		
		in =null;
		try //Load *.vcxproj.filters
		{
			in = new BufferedReader(new FileReader(mFilterFile));
			ParseContext context = ParseContext.HEADER;
			String line, contextLine;
			ClItem item = null;
			int lineCount = 0;
			while((line=in.readLine())!=null)
			{
				++lineCount;
				if(line.matches(".*<ItemGroup.*")) //Start context
				{
					contextLine = line;
					line = in.readLine();
					if(line!=null)
					{
						if(line.matches(".*<ClInclude .*"))
						{
							context = ParseContext.INCLUDE;
							System.out.println("*** Context: ClInclude");
						}
						else if(line.matches(".*<ClCompile .*"))
						{
							context = ParseContext.COMPILE;
							System.out.println("*** Context: ClCompile");
						}
						else if(line.matches(".*<Filter.*"))
						{
							context = ParseContext.FILTER;
							System.out.println("*** Context: Filter");
						}
						else
						{
							if(ParseContext.INCLUDE==context || ParseContext.COMPILE==context)
							{
								context = ParseContext.FOOTER;
								mFilterFooter.add(contextLine);
							}
							else
							{
								mFilterHeader.add(contextLine);
							}
							
							System.out.println("*** No context change");
						}
					}
					else
						throw new ParseException("<ItemGroup> element not closed at line: " + lineCount, lineCount);
				}
				
				if(ParseContext.HEADER==context)
					mFilterHeader.add(line);
				else if(ParseContext.FOOTER==context)
					mFilterFooter.add(line);
				else if(ParseContext.FILTER==context)
				{
					if(line.matches(".*<Filter Include.*"))
						mFilters.add(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
				}
				else if(ParseContext.COMPILE==context && !line.matches(".*</ItemGroup>.*"))
				{
					if(item==null) //find item
					{
						item = mClCompileItems.get(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
						if(item==null)
						{
							System.err.println("Could not find ClItem element for filter file!");
							System.exit(-1);
						}
					}
					if(item.addFilterLine(line)) //is element closed?
						item = null;
				}
				else if(ParseContext.INCLUDE==context && !line.matches(".*</ItemGroup>.*"))
				{
					if(item==null) //find item
					{
						item = mClIncludeItems.get(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")));
						if(item==null)
						{
							System.err.println("Could not find ClItem element for filter file!");
							System.exit(-1);
						}
					}
					if(item.addFilterLine(line)) //is element closed?
						item = null;
				}
			}
		}
		catch (ParseException e) { System.err.println("Parse excepiton: " + e.getMessage()); System.exit(-1); }
		catch (FileNotFoundException ex) { System.err.println("File not found: " + vcxproj); System.exit(-1); }
		catch (IOException ex) { System.err.println("IOException reading file: " + vcxproj); System.exit(-1); }
		finally { try { if(in!=null) in.close(); } catch (IOException e) { System.err.println("IOException closing file"); }}
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
		System.out.println(">>> VCX HEADER <<<");
		for(String s: mVcxHeader) System.out.println(s);
		System.out.println("");
		System.out.println(">>> VCX FOOTER <<<");
		for(String s: mVcxFooter) System.out.println(s);
		
		System.out.println("");
		System.out.println(">>> Filter HEADER <<<");
		for(String s: mFilterHeader) System.out.println(s);
		System.out.println("");
		System.out.println(">>> Filter FOOTER <<<");
		for(String s: mFilterFooter) System.out.println(s);
		
		System.out.println("");
		System.out.println(">>> Filters <<<");
		for(String s: mFilters) System.out.println(s);
		
		System.out.println("");
		System.out.println("ClIncludes:");
		for(Entry<String, ClItem> i: mClIncludeItems.entrySet()) i.getValue().debugPrint();
		
		System.out.println("");
		System.out.println("ClCompiles:");
		for(Entry<String, ClItem> i: mClIncludeItems.entrySet()) i.getValue().debugPrint();
		
	}
}
