package com.programmingteam.projects;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.management.modelmbean.XMLParseException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.programmingteam.Helpers;

public class QSync
{
	private String mIncludes;
	private String mCompiles;
	private File mPwd;
	
	private ArrayList<QSyncVcxproj> mProjects;
	
	public QSync(File qsyncfile)
	{
		Document qsyncDoc =null;
		try
		{
			qsyncDoc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(qsyncfile);
		}
		catch(ParserConfigurationException ex) { System.err.println("QSync: error creating XML configuration"); }
		catch(SAXException ex) { System.err.println("Error reading document ("+qsyncfile.getName()+"): "+ex.getMessage()); }
		catch(IOException ex) { System.err.println("IOException reading document ("+qsyncfile.getName()+")"); }	
		
		if(qsyncDoc==null) System.exit(-1); //If file couldn't be read, kill process
		
		mPwd = qsyncfile.getAbsoluteFile().getParentFile();
		
		try
		{
			//INCLUDES
			NodeList includeExtList = qsyncDoc.getElementsByTagName("ClInclude");
			if(includeExtList.getLength() == 0) throw new XMLParseException("element <ClInclude> not found");
			if(includeExtList.getLength() >1) throw new XMLParseException("multiple <ClInclude> elements");
			
			Element includes = (Element) qsyncDoc.adoptNode(includeExtList.item(0));
			mIncludes = includes.getAttribute("ext");
			
			if(mIncludes.length()==0 || !mIncludes.matches("^[a-zA-Z0-9]*(,[a-zA-Z0-9]*)*$"))
				throw new XMLParseException("src attribute of <ClInclude> is not valid");
			
			//COMPILES
			NodeList compileExtList = qsyncDoc.getElementsByTagName("ClCompile");
			if(compileExtList.getLength() == 0) throw new XMLParseException("element <ClCompile> not found");
			if(compileExtList.getLength() >1) throw new XMLParseException("multiple <ClCompile> elements");
			
			Element compiles = (Element) qsyncDoc.adoptNode(compileExtList.item(0));
			mCompiles = compiles.getAttribute("ext");
			
			if(mCompiles.length()==0 || !mCompiles.matches("^[a-zA-Z0-9]*(,[a-zA-Z0-9]*)*$"))
				throw new XMLParseException("src attribute of <ClCompile> is not valid");
			
			//Projects
			mProjects = new ArrayList<QSyncVcxproj>();
			NodeList vcxprojList = qsyncDoc.getElementsByTagName("vcxproj");
			if(vcxprojList.getLength() == 0) throw new XMLParseException("no <vcxproj> elements found");
			for(int i=0; i<vcxprojList.getLength(); ++i)
			{
				Node projNode = vcxprojList.item(i);
				Element projElem = (Element) qsyncDoc.adoptNode(projNode);
				String projFile = projElem.getAttribute("proj");
				if(projFile.length()==0 || !projFile.matches(".*vcxproj$"))
					throw new XMLParseException("proj attribute of <vcxproj> is not valid (must be *.vcxproj)");
				
				projFile = Helpers.resolvePath(mPwd.getAbsolutePath(), projFile);
				
				QSyncVcxproj proj = new QSyncVcxproj(projFile, projFile + ".filters");
				NodeList importList = projNode.getChildNodes();
				if(importList.getLength() == 0) throw new XMLParseException("no <import> element defined for " + projFile);
				for(int j=0; j<importList.getLength(); ++j)
				{
					Node importNode = importList.item(j);
					if(!importNode.getNodeName().equals("import")) continue;

					String toFilter = importNode.getAttributes().getNamedItem("tofilter").getNodeValue();
					toFilter = Helpers.fixSlashes(toFilter);
					if(toFilter.length()==0 || !toFilter.matches("[a-zA-Z0-9 ]*(\\\\[a-zA-Z0-9 ]*)*"))
						throw new XMLParseException("tofilter attribute of <import> is not valid");
					
					QSyncImport imp = new QSyncImport(toFilter);
					
					NodeList importIncludesList = importNode.getChildNodes();
					if(importIncludesList.getLength()==0)
						throw new XMLParseException("<import> is empty (with tofilter="+toFilter+")");
					
					boolean src=false, inc=false;
					for(int n=0; n<importIncludesList.getLength(); ++n)
					{
						Node includeNode = importIncludesList.item(n);
						if(!includeNode.getNodeName().equals("include")
								   && !includeNode.getNodeName().equals("src")
								   && !includeNode.getNodeName().equals("misc"))
									continue;
						
						if(includeNode.getNodeName().equals("include"))
						{
							if(inc) throw new XMLParseException("<import tofilter="+toFilter+"> has multiple <include> elements");
							if(includeNode.getFirstChild()==null) throw new XMLParseException("<include> element is empty."); 
							inc = true;
							String includePath = includeNode.getFirstChild().getNodeValue();
							includePath = Helpers.resolvePath(mPwd.getAbsolutePath(), includePath);
							imp.setInclude(includePath);
						}
						else if(includeNode.getNodeName().equals("src"))
						{
							if(src) throw new XMLParseException("<import tofilter="+toFilter+"> has multiple <src> elements");
							if(includeNode.getFirstChild()==null) throw new XMLParseException("<src> element is empty");
							src = true;
							String srcPath = includeNode.getFirstChild().getNodeValue();
							srcPath = Helpers.resolvePath(mPwd.getAbsolutePath(), srcPath);
							imp.setSrc(srcPath);
							
						}
						else if(includeNode.getNodeName().equals("misc"))
						{
							if(includeNode.getFirstChild()==null) throw new XMLParseException("<misc> element is empty");
							String miscPath = includeNode.getFirstChild().getNodeValue();
							miscPath = Helpers.resolvePath(mPwd.getAbsolutePath(), miscPath);
							imp.addMisc(miscPath);
						}
						else
						{
							throw new XMLParseException("<import tofilter="+toFilter+"> has invalid element: " + includeNode.getNodeName());
						}
					}
					proj.addImport(imp);
				}
				mProjects.add(proj);
			}
			
		}
		catch(XMLParseException ex) { System.err.println("XMLParseExcepiton: " + ex.getMessage()); System.exit(-1); }
	}
	
	
	public File getPWD()
	{
		return mPwd;
	}
	
	public void debugPrint()
	{
		System.out.println("mIncludes: " + mIncludes);
		System.out.println("mCompiles: " + mCompiles);
		
		System.out.println("mProjects ("+mProjects.size()+"):");
		for(QSyncVcxproj proj: mProjects)
			proj.debugPrint();
	}
}
