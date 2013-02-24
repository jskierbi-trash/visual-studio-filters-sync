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

import com.programmingteam.Helpers;

///
/// \brief Visual 2010 project representation
///
public class VcxprojSync
{
	private File mProjFile;
	private File mFilterFile;
	
	////vcxproj structure
	private ArrayList<String> mVcxHeader;
	private ArrayList<String> mVcxFooter;
	
	///vcxproj.filters structure
	private ArrayList<String> mFilterHeader;
	private ArrayList<String> mFilterFooter;
	private HashSet<String> mFilters;

	///Shared items
	private HashMap<String, VcxprojClItem> mClIncludeItems;
	private HashMap<String, VcxprojClItem> mClCompileItems;
	
	private enum ParseContext { HEADER, FILTER, INCLUDE, COMPILE, FOOTER }
	
	public VcxprojSync(String vcxproj, String vcxprojFilters)
	{		
		mProjFile = new File(vcxproj);
		mFilterFile = new File(vcxprojFilters);
		
		mVcxHeader = new ArrayList<String>();
		mVcxFooter = new ArrayList<String>();
		
		mFilterHeader = new ArrayList<String>();
		mFilterFooter = new ArrayList<String>();
		
		mClIncludeItems = new HashMap<String, VcxprojClItem>();
		mClCompileItems = new HashMap<String, VcxprojClItem>();
		mFilters = new HashSet<String>();

		parseVcxproj();
		parseFilters();
		markDeletedFiles();
	}

	private void parseVcxproj()
	{
		BufferedReader in =null;
		try //Load *.vcxproj
		{
			in = new BufferedReader(new FileReader(mProjFile));
			ParseContext context = ParseContext.HEADER;
			String line, contextLine;
			VcxprojClItem item = new VcxprojClItem();
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
						item = new VcxprojClItem();
					}
				}
				else if(ParseContext.INCLUDE==context)
				{
					if(item.addProjLine(line))
					{
						mClIncludeItems.put(item.getFilePath(), item);
						item = new VcxprojClItem();
					}
				}
			}
		}
		catch (ParseException e) { System.err.println("Parse excepiton: " + e.getMessage()); System.exit(-1); }
		catch (FileNotFoundException e) { System.err.println("File not found: " + mProjFile); System.exit(-1); }
		catch (IOException e) { System.err.println("IOException reading file: " + mProjFile); System.exit(-1); }
		finally { try { if(in!=null) in.close(); } catch (IOException e) { System.err.println("IOException closing file"); }}
		
	}
	
	private void parseFilters()
	{
		BufferedReader in =null;
		try //Load *.vcxproj.filters
		{
			in = new BufferedReader(new FileReader(mFilterFile));
			ParseContext context = ParseContext.HEADER;
			String line, contextLine;
			VcxprojClItem item = null;
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
		catch (FileNotFoundException ex) { System.err.println("File not found: " + mFilterFile); System.exit(-1); }
		catch (IOException ex) { System.err.println("IOException reading file: " + mFilterFile); System.exit(-1); }
		finally { try { if(in!=null) in.close(); } catch (IOException e) { System.err.println("IOException closing file"); }}
	}
	
	private void markDeletedFiles()
	{
		/// Get project file dir!
		final String basePath = Helpers.getPath(mProjFile.getAbsolutePath());
//		System.out.println("BasePath: " + basePath);
		
		for(Entry<String, VcxprojClItem> i: mClIncludeItems.entrySet())
		{
			File f = new File(basePath + i.getKey());
			if(!f.exists())
			{
				System.out.println("File not exists: " + f);
				i.getValue().setDeleted(true);
			}
		}
		
		for(Entry<String, VcxprojClItem> i: mClCompileItems.entrySet())
		{
			File f = new File(basePath + i.getKey());
			if(!f.exists())
			{
				System.out.println("File not exists: " + f);
				i.getValue().setDeleted(true);
			}
		}
	}
	
	public void syncFile(String relativeFile, String filter)
	{
		System.out.println("Sync file: " + relativeFile + " (filter: " + filter + ")");
		
		
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
		for(Entry<String, VcxprojClItem> i: mClIncludeItems.entrySet()) i.getValue().debugPrint();
		
		System.out.println("");
		System.out.println("ClCompiles:");
		for(Entry<String, VcxprojClItem> i: mClIncludeItems.entrySet()) i.getValue().debugPrint();
		
	}
}
