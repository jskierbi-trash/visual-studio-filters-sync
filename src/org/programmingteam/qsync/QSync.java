package org.programmingteam.qsync;

import java.io.File;
import java.io.IOException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.ArrayList;

import javax.management.modelmbean.XMLParseException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.programmingteam.Helpers;
import org.programmingteam.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


///
/// \brief qsync XML file representation
/// Contains of basic config (include ext, compile ext) and list of defined projects.
///
public class QSync
{
	private String mIncludes;
	private String mCompiles;
	private File mPwd;
	
	private ArrayList<QSyncVcxproj> mProjects;

	///
	/// \brief reads xml file (qsync format) and creates object hierarchy
	/// In case of read or XML file format error, this method prints message
	/// and make application exti with code -1
	///
	/// \param [in] qsyncfile 	path to file to read
	///
	public QSync(File qsyncfile)
	{
		Document qsyncDoc =null;
		try
		{
			qsyncDoc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(qsyncfile);
		}
		catch(ParserConfigurationException ex) { Log.e("QSync: error creating XML configuration"); }
		catch(SAXException ex) { Log.e("Error reading document ("+qsyncfile.getName()+"): "+ex.getMessage()); }
		catch(IOException ex) { Log.e("IOException reading document ("+qsyncfile.getName()+")"); }	
		
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
			//Log.d("vcxproj length: " + vcxprojList.getLength());
			for(int i=vcxprojList.getLength()-1; i>=0; --i)
			{
				//Log.d("Project!");
				Node projNode = vcxprojList.item(i);
				Element projElem = (Element) qsyncDoc.adoptNode(projNode);
				String projFile = projElem.getAttribute("proj");
				if(projFile.length()==0 || !projFile.matches(".*vcxproj$"))
					throw new XMLParseException("proj attribute of <vcxproj> is not valid (must be *.vcxproj)");
				
				projFile = Helpers.resolvePath(mPwd.getAbsolutePath(), projFile);
				
				QSyncVcxproj proj = new QSyncVcxproj(projFile, projFile + ".filters");
				NodeList importList = projNode.getChildNodes();
				if(importList.getLength() == 0) throw new XMLParseException("no <import> element defined for " + projFile);
				for(int j= importList.getLength()-1; j>=0; --j)
				{
					Node importNode = importList.item(j);
					if(!importNode.getNodeName().equals("import")) continue;

					Node importNodeAttrs;
					if( (importNodeAttrs = importNode.getAttributes().getNamedItem("tofilter")) == null)
						throw new XMLParseException("<import> has no tofilter attribute");
					String toFilter =importNodeAttrs.getNodeValue();
					toFilter = Helpers.fixSlashes(toFilter);
					toFilter = Helpers.stripSlashes(toFilter);
					if(toFilter.length()==0 || !toFilter.matches("[a-zA-Z0-9 ]*(\\\\[a-zA-Z0-9 ]*)*"))
						throw new XMLParseException("tofilter attribute of <import> is not valid");
					
					QSyncImport imp = new QSyncImport(toFilter);
					
					if( (importNodeAttrs = importNode.getAttributes().getNamedItem("includeEmptyDirs")) != null)
						imp.setIncludeEmptyDirs(importNodeAttrs.getNodeValue().toLowerCase().equals("true"));
					
					NodeList importIncludesList = importNode.getChildNodes();
					if(importIncludesList.getLength()==0)
						throw new XMLParseException("<import> is empty (with tofilter="+toFilter+")");
					
					boolean src=false, inc=false;
					for(int n=importIncludesList.getLength()-1; n>=0; --n)
					{
						Node includeNode = importIncludesList.item(n);
						if(!includeNode.getNodeName().equals("include")
								   && !includeNode.getNodeName().equals("src")
								   && !includeNode.getNodeName().equals("misc"))
									continue;
						
						if(includeNode.getNodeName().equals("include"))
						{
							if(inc) throw new XMLParseException("<import tofilter="+toFilter+"> has multiple <include> elements");
							
							Node attrNode;
							if( (attrNode=includeNode.getAttributes().getNamedItem("accept"))!=null )
								imp.setRegexpInclude(attrNode.getNodeValue());
							if( (attrNode=includeNode.getAttributes().getNamedItem("exclude"))!=null )
								imp.setExcludeInc(attrNode.getNodeValue());
							
							if( (attrNode=includeNode.getAttributes().getNamedItem("dir"))!=null )
							{
								String includePath = attrNode.getNodeValue().trim();
								includePath = Helpers.resolvePath(mPwd.getAbsolutePath(), includePath);
								imp.setInclude(includePath);
								inc = true;
							}
							else
								throw new XMLParseException("<include> has no dir attribute");
						}
						else if(includeNode.getNodeName().equals("src"))
						{
							if(src) throw new XMLParseException("<import tofilter="+toFilter+"> has multiple <src> elements");
						
							Node attrNode;
							if( (attrNode=includeNode.getAttributes().getNamedItem("accept"))!=null)
								imp.setRegexpSrc(attrNode.getNodeValue());
							if( (attrNode=includeNode.getAttributes().getNamedItem("exclude"))!=null )
								imp.setExcludeSrc(attrNode.getNodeValue());
							
							if( (attrNode=includeNode.getAttributes().getNamedItem("dir"))!=null )
							{
								String srcPath = attrNode.getNodeValue().trim();
								srcPath = Helpers.resolvePath(mPwd.getAbsolutePath(), srcPath);
								imp.setSrc(srcPath);
								src = true;
							}
							else
								throw new XMLParseException("<src> has no dir attribute");
							
						}
						else if(includeNode.getNodeName().equals("misc"))
						{
							// TODO set regexp
							throw new UnsupportedAddressTypeException();
							
//							if(includeNode.getFirstChild()==null) throw new XMLParseException("<misc> element is empty");
//							String miscPath = includeNode.getFirstChild().getNodeValue();
//							miscPath = Helpers.resolvePath(mPwd.getAbsolutePath(), miscPath);
//							imp.addMisc(miscPath);
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
		catch(XMLParseException ex) { Log.e("XMLParseExcepiton: " + ex.getMessage()); System.exit(-1); }
	}
	
	///
	/// \brief returns absolute path to dir containing loaded config file
	/// \return File	absolute path to dir containing loaded config file
	///
	public File getPWD()
	{
		return mPwd;
	}

	///
	/// \brief returns list of projects from config file
	/// \return list of projects
	///
	public ArrayList<QSyncVcxproj> getProjects()
	{
		return mProjects;
	}
	
	public String getIncludeExt()
	{
		return mIncludes;
	}
	
	public String getCompileExt()
	{
		return mCompiles;
	}
	
	///
	/// \brief Prints all data loaded from config 
	///
	public void debugPrint()
	{
		Log.v(">>>> QSync <<<<");
		Log.v("mIncludes: " + mIncludes);
		Log.v("mCompiles: " + mCompiles);
		
		Log.v("mProjects ("+mProjects.size()+"):");
		for(QSyncVcxproj proj: mProjects)
			proj.debugPrint();
		Log.v("^^^^^^^^^^");
	}
}
