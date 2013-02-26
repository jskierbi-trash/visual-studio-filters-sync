package com.programmingteam.vs2010;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.programmingteam.Helpers;

///
/// \brief Visual 2010 project representation
///
public class VcxprojSync
{
	public enum SyncType { INCLUDE, COMPILE }
	
	private File mProjFile;
	private File mFilterFile;
	
	////vcxproj structure
	private ArrayList<String> mVcxHeader;
	private ArrayList<String> mVcxFooter;
	
	///vcxproj.filters structure
	private ArrayList<String> mFilterHeader;
	private ArrayList<String> mFilterFooter;
	private HashMap<String, Boolean> mFilters;

	///Shared items
	private HashMap<String, VcxprojClItem> mClIncludeItems;
	private HashMap<String, VcxprojClItem> mClCompileItems;
	
	private enum CTX { NOCTX, FILTER, INCLUDE, COMPILE }
	
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
		mFilters = new HashMap<String, Boolean>();

		parseVcxproj();
		parseFilters();
		markDeletedFiles();
	}

	private void parseVcxproj()
	{
		BufferedReader in =null;
		boolean noEndprojTag =false;
		try //Load *.vcxproj
		{
			in = new BufferedReader(new FileReader(mProjFile));
			CTX ctx = CTX.NOCTX;
			boolean flgFooter = false;
			
			String line, contextLine;
			VcxprojClItem item = new VcxprojClItem();
			int lineCount = 0;
			while((line=in.readLine())!=null)
			{
				++lineCount;
				
				//Handle closing of project
				if(line.matches(".*<Project .*/>"))
				{
					line = line.replace("/>", ">");
					noEndprojTag = true;
				}
				else if(line.matches(".*</Project>.*"))
				{
					flgFooter = true;
				}
				else if(line.matches(".*<ItemGroup>.*") && CTX.NOCTX==ctx) //Check context
				{
					contextLine = line;
					if( (line=in.readLine()) ==null)
						throw new ParseException("<ItemGroup> element not closed at line: " + lineCount, lineCount);
					
					if(line.matches(".*<ClInclude .*"))
					{
						flgFooter = true;
						item = new VcxprojClItem();
						ctx = CTX.INCLUDE;
					}
					else if(line.matches(".*<ClCompile .*"))
					{
						flgFooter = true;
						item = new VcxprojClItem();
						ctx = CTX.COMPILE;
					}
					else //No context, write to header or footer...
					{
						if(flgFooter) mVcxFooter.add(contextLine);
						else mVcxHeader.add(contextLine);
					}
				}
				else if(line.matches(".*</ItemGroup>.*") && CTX.NOCTX!=ctx) //drop context
				{
					ctx = CTX.NOCTX;
					continue;
				}
				
				if(CTX.NOCTX==ctx)
				{
					if(flgFooter) mVcxFooter.add(line);
					else mVcxHeader.add(line);
				}
				else if(CTX.INCLUDE==ctx)
				{
					if(item.addProjLine(line))
					{
						mClIncludeItems.put(item.getFilePath(), item);
						item = new VcxprojClItem();
					}
				}
				else if(CTX.COMPILE==ctx)
				{
					if(item.addProjLine(line)) //is element closed?
					{
						mClCompileItems.put(item.getFilePath(), item);
						item = new VcxprojClItem();
					}
				}
			}
		}
		catch (ParseException e) { System.err.println("Parse excepiton: " + e.getMessage()); System.exit(-1); }
		catch (FileNotFoundException e) { System.err.println("File not found: " + mProjFile); System.exit(-1); }
		catch (IOException e) { System.err.println("IOException reading file: " + mProjFile); System.exit(-1); }
		finally { try { if(in!=null) in.close(); } catch (IOException e) { System.err.println("IOException closing file"); }}
		
		if(noEndprojTag)
			mVcxFooter.add("</Project>");
		
	}
	
	private void parseFilters()
	{
		BufferedReader in =null;
		boolean noEndprojTag =false;
		try //Load *.vcxproj.filters
		{
			in = new BufferedReader(new FileReader(mFilterFile));
			CTX ctx = CTX.NOCTX;
			boolean flgFooter = false;
			
			String line, contextLine;
			VcxprojClItem item =null;
			int lineCount = 0;
			while((line=in.readLine())!=null)
			{
				++lineCount;
				
				//Handle closing of project
				if(line.matches(".*<Project .*/>"))
				{
					line = line.replace("/>", ">");
					noEndprojTag = true;
				}
				else if(line.matches(".*</Project>.*"))
				{
					flgFooter = true;
				}
				else if(line.matches(".*<ItemGroup>.*") && CTX.NOCTX==ctx) //Check context
				{
					contextLine = line;
					if( (line=in.readLine()) ==null)
						throw new ParseException("<ItemGroup> element not closed at line: " + lineCount, lineCount);
					
					if(line.matches(".*<ClInclude .*"))
					{
						flgFooter = true;
						ctx = CTX.INCLUDE;
					}
					else if(line.matches(".*<ClCompile .*"))
					{
						flgFooter = true;
						ctx = CTX.COMPILE;
					}
					else if(line.matches(".*<Filter Include=.*"))
					{
						flgFooter = true;
						ctx = CTX.FILTER;
					}
					else //No context, write to header or footer...
					{
						if(flgFooter) mFilterFooter.add(contextLine);
						else mFilterHeader.add(contextLine);
					}
				}
				else if(line.matches(".*</ItemGroup>.*") && CTX.NOCTX!=ctx) //drop context
				{
					ctx = CTX.NOCTX;
					continue;
				}
				
				if(CTX.NOCTX==ctx)
				{
					if(flgFooter) mFilterFooter.add(line);
					else mFilterHeader.add(line);
				}
				else if(CTX.FILTER==ctx)
				{
					if(line.matches(".*<Filter Include.*"))
						mFilters.put(line.substring(line.indexOf("\"")+1, line.lastIndexOf("\"")), true);
				}
				else if(CTX.INCLUDE==ctx)
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
				else if(CTX.COMPILE==ctx)
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
			}
		}
		catch (ParseException e) { System.err.println("Parse excepiton: " + e.getMessage()); System.exit(-1); }
		catch (FileNotFoundException ex) { System.err.println("File not found: " + mFilterFile); System.exit(-1); }
		catch (IOException ex) { System.err.println("IOException reading file: " + mFilterFile); System.exit(-1); }
		finally { try { if(in!=null) in.close(); } catch (IOException e) { System.err.println("IOException closing file"); }}
	
		if(noEndprojTag)
			mFilterFooter.add("</Project>");
	}
	
	private void markDeletedFiles()
	{
		final String basePath = Helpers.getPath(mProjFile.getAbsolutePath());
		
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
	
	public void syncFile(String relativeFile, String filter, SyncType type, boolean isExcluded)
	{		
		if(relativeFile.matches(".*3DS.*"))
			System.out.println("");
		
		System.out.println("Item: " + relativeFile);
		this.syncFilter(filter);

		VcxprojClItem item =null;
		if(SyncType.COMPILE==type) item = mClCompileItems.get(relativeFile);
		if(SyncType.INCLUDE==type) item = mClIncludeItems.get(relativeFile);
		
		if(item==null && !detectFileMove(relativeFile, filter, mClCompileItems, isExcluded))
		{
			System.out.println("Adding file: " + relativeFile);
			item = new VcxprojClItem();
			item.setDeleted(false);
			item.setRelativePath(relativeFile);
			item.setFilter(filter);
			if(isExcluded) item.setExcludeFromBuild();
			
			if(SyncType.COMPILE==type) mClCompileItems.put(relativeFile, item);
			if(SyncType.INCLUDE==type) mClIncludeItems.put(relativeFile, item);
		}
		else if(item!=null)
		{
			if(isExcluded) item.setExcludeFromBuild();
			item.setDeleted(false);
			item.setFilter(filter);
		}
	}
	
	private boolean detectFileMove(String file, String filter, HashMap<String, VcxprojClItem> container, boolean isExcluded)
	{
		VcxprojClItem movedItem =null;
		String movedFilter = null;
		for(Entry<String, VcxprojClItem> i: container.entrySet())
		{	
			if(i.getValue().getDeleted() && Helpers.compFiles(i.getKey(), file))
			{
				if(movedItem!=null)
				{
					System.err.println("Error! Ambigious file move:" + movedItem.getFilePath() + "");
					System.err.println("\tConflict: "+movedItem.getFilePath()+" VS. "+file+"");
					System.exit(-1);
				}

				movedFilter = i.getKey();
				System.out.println("Moving file from: " + i.getValue().getFilePath() + " to: "+file);
				movedItem = i.getValue();
				movedItem.setRelativePath(file);
				movedItem.setFilter(filter);
				movedItem.setDeleted(false);
				if(isExcluded) movedItem.setExcludeFromBuild();
			}
		}

		if(movedItem!=null)
		{
			container.remove(movedFilter);
			container.put(file, movedItem);
		}
		
		return movedItem!=null;
	}
	
	public void syncFilter(String filter)
	{
		System.out.println("Sync filter: " + filter);
		String sliceFilter = filter;
		int slashIndex;
		do
		{
			Boolean flgExists = mFilters.get(sliceFilter);
//			if(flgExists==null)
//				System.out.println("Filter added: " + sliceFilter);
			
			mFilters.put(sliceFilter, true);
			
			slashIndex = sliceFilter.lastIndexOf('\\');
			if(slashIndex>0)
				sliceFilter = sliceFilter.substring(0, slashIndex);
		}
		while( slashIndex >0 );
	}

	public void invalidateFilters(String toFilter)
	{
		for(Entry<String, Boolean> i: mFilters.entrySet())
		{
			if(i.getKey().startsWith(toFilter))
				mFilters.put(i.getKey(), false);
		}
	}
	
	public void saveVcxproj()
	{
		try
		{
			File outFile = new File(mProjFile + "");
			if(!outFile.exists()) outFile.createNewFile();
			
			// HEADER ///////
			BufferedWriter out = new BufferedWriter(new FileWriter(outFile.getAbsolutePath()));
			for(String s: mVcxHeader)
			{
				out.write(s);
				out.newLine();
			}
			
			// INCLUDES //////
			out.write("  <ItemGroup>");
			out.newLine();
			for(Entry<String, VcxprojClItem> i: mClIncludeItems.entrySet())
			{
				if(i.getValue().getDeleted()) 
				{
					System.out.println("File removed: " + i.getKey());
					continue;
				}
				
				List<String> projLines = i.getValue().getProjLines();
				if(projLines==null || projLines.size()==0)
				{
					 out.write("    <ClInclude Include=\""+i.getKey()+"\" />");
					 out.newLine();
				}
				else
				{
					out.write("    <ClInclude Include=\""+i.getKey()+"\">");
					out.newLine();
					for(String s: projLines)
					{
						out.write(s);
						out.newLine();
					}
					out.write("    </ClInclude>");
					out.newLine();
				}
			}
			out.write("  </ItemGroup>");
			out.newLine();
			
			// COMPILES //////
			out.write("  <ItemGroup>");
			out.newLine();
			for(Entry<String, VcxprojClItem> i: mClCompileItems.entrySet())
			{
				if(i.getValue().getDeleted()) continue;
				
				List<String> projLines = i.getValue().getProjLines();
				if(projLines==null || projLines.size()==0)
				{
					 out.write("    <ClCompile Include=\""+i.getKey()+"\" />");
					 out.newLine();
				}
				else
				{
					out.write("    <ClCompile Include=\""+i.getKey()+"\">");
					out.newLine();
					for(String s: projLines)
					{
						out.write(s);
						out.newLine();
					}
					out.write("    </ClCompile>");
					out.newLine();
				}
			}
			out.write("  </ItemGroup>");
			out.newLine();
			
			// FOOTER ///////
			for(String s: mVcxFooter)
			{
				out.write(s);
				out.newLine();
			}
			
			out.close();
		}
		catch (IOException e)
		{
			System.err.println("IOException while saving file! (" + e.getMessage() + ")");
			e.printStackTrace();
		}
	}
	
	public void saveVcxprojFilters()
	{
		try
		{
			File outFile = new File(mFilterFile + "");
			if(!outFile.exists()) outFile.createNewFile();
			
			// HEADER ///////
			BufferedWriter out = new BufferedWriter(new FileWriter(outFile.getAbsolutePath()));
			for(String s: mFilterHeader)
			{
				out.write(s);
				out.newLine();
			}
			
			// Filters //////
			out.write("  <ItemGroup>");
			out.newLine();
			for(Entry<String, Boolean> i: mFilters.entrySet())
			{
				if(i.getValue())
				{
					out.write("    <Filter Include=\"" + i.getKey() + "\">");
					out.newLine();
					out.write("      <UniqueIdentifier></UniqueIdentifier>");
					out.newLine();
					out.write("    </Filter>");
					out.newLine();
				}
				else
				{
					System.out.println("Filter removed: " + i.getKey());
				}
			}
			out.write("  </ItemGroup>");
			out.newLine();
			
			// Includes //////
			out.write("  <ItemGroup>");
			out.newLine();
			for(Entry<String, VcxprojClItem> i: mClIncludeItems.entrySet())
			{
				if(i.getValue().getDeleted()) continue;
				
				List<String> filterLines = i.getValue().getFilterLines();
				out.write("    <ClInclude Include=\""+i.getKey()+"\">");
				out.newLine();
				for(String s: filterLines)
				{
					out.write(s);
					out.newLine();
				}
				out.write("    </ClInclude>");
				out.newLine();
			}
			out.write("  </ItemGroup>");
			out.newLine();
			
			// Includes //////
			out.write("  <ItemGroup>");
			out.newLine();
			for(Entry<String, VcxprojClItem> i: mClCompileItems.entrySet())
			{
				if(i.getValue().getDeleted()) continue;
				
				List<String> filterLines = i.getValue().getFilterLines();
				out.write("    <ClCompile Include=\""+i.getKey()+"\">");
				out.newLine();
				for(String s: filterLines)
				{
					out.write(s);
					out.newLine();
				}
				out.write("    </ClCompile>");
				out.newLine();
			}
			out.write("  </ItemGroup>");
			out.newLine();
			
			// FOOTER ///////
			for(String s: mFilterFooter)
			{
				out.write(s);
				out.newLine();
			}
			
			out.close();
		}
		catch (IOException e)
		{
			System.err.println("IOException while saving file! (" + e.getMessage() + ")");
			e.printStackTrace();
		}
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
		for(Entry<String, Boolean> i: mFilters.entrySet()) System.out.println("["+i.getValue()+"]" +i.getKey());
		
//		System.out.println("");
//		System.out.println("ClIncludes:");
//		for(Entry<String, VcxprojClItem> i: mClIncludeItems.entrySet()) i.getValue().debugPrint();
//		
//		System.out.println("");
//		System.out.println("ClCompiles:");
//		for(Entry<String, VcxprojClItem> i: mClCompileItems.entrySet()) i.getValue().debugPrint();
		
	}
}
