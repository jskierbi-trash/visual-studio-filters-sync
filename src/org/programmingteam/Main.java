package org.programmingteam;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.programmingteam.qsync.QSync;
import org.programmingteam.qsync.QSyncImport;
import org.programmingteam.qsync.QSyncVcxproj;
import org.programmingteam.vs2010.VcxprojSync;


public class Main
{
	public static Options OPTS;
	
	public static void main(String[] args)
	{	
		OPTS = new Options(args);
		
		File qsyncFile = new File(OPTS.getFile());
		if(!qsyncFile.exists())
		{
			Log.e("Configuration file not found (" + OPTS.getFile() + ")");
			System.exit(-1);
		}
		
		//Read file
		QSync qsync = new QSync(qsyncFile);
		qsync.debugPrint();
		Log.v("Qsyn created ok.");
		
		List<QSyncVcxproj> qsyncProjs = qsync.getProjects();
		for(QSyncVcxproj qsyncProj : qsyncProjs)
		{
			Log.d(">>> Sync: " + qsyncProj.getVcxproj() + "");
			VcxprojSync vcxprojSync = new VcxprojSync(qsyncProj.getVcxproj(), qsyncProj.getVcxprojFilters());
			for(QSyncImport imp: qsyncProj.getImportList())
			{
				Log.d("Parsing <import tofilter=\"" + imp.getToFilter() + "\">");
				vcxprojSync.invalidateFilters(imp.getToFilter());
				
				ArrayList<File> dirList = new ArrayList<File>();
				if(imp.getInclude()!=null) dirList.add(new File(imp.getInclude()));
				if(imp.getSrc()!=null) dirList.add(new File(imp.getSrc()));
				
				while(dirList.size()>0)
				{
					File dir = dirList.get(0);
					dirList.remove(0);
					File listFiles[] = dir.listFiles();
					if(listFiles==null)
					{
						Log.e("Directory does not exist! " + dir);
						System.exit(-1);
					}
					for(int i=0; i<listFiles.length; ++i)
					{
						if(listFiles[i].isDirectory())
						{
							dirList.add(listFiles[i]);
							
							if(imp.isIncludeEmptyDirs())
							{
								String toFilter = imp.getFileFilterPath(listFiles[i].getAbsolutePath()); 
								toFilter = Helpers.stripSlashes(toFilter);
								vcxprojSync.syncFilter(toFilter);
							}
						}
						else
						{
							//TODO add handling misc
							boolean include =false;
							if( Helpers.isCompile(listFiles[i], qsync.getCompileExt()) ||
								(include=Helpers.isInclude(listFiles[i], qsync.getIncludeExt())))
							{
								if(include && !imp.matchesInclue(listFiles[i].getName()))
								{
									Log.v("Skipping file: "+listFiles[i]+" (not matching regexp)");
									continue;
								}
								if(!include && !imp.matchesSrc(listFiles[i].getName()))
								{
									Log.v("Skipping file: "+listFiles[i]+" (not matching regexp)");
									continue;
								}
								
								boolean isExcludedFromBuild = false;
								if(include)
									isExcludedFromBuild = imp.isExcludedInc(""+listFiles[i].getName());
								else
									isExcludedFromBuild = imp.isExcludedSrc(""+listFiles[i].getName());
								
								VcxprojSync.SyncType syncType = VcxprojSync.SyncType.COMPILE;
								if(include) syncType = VcxprojSync.SyncType.INCLUDE;
								
								try
								{
									String toFilter = imp.getFileFilterPath(listFiles[i].getAbsolutePath());
									toFilter = Helpers.getPath(toFilter);
									toFilter = Helpers.stripSlashes(toFilter);
									vcxprojSync.syncFile(
											qsyncProj.getRelativeFile(listFiles[i].getCanonicalPath()), 
											toFilter, 
											syncType, 
											isExcludedFromBuild);
								}
								catch(IOException e)
								{
									Log.e("Error obtaining folder cannonical path!" + listFiles[i]); 
								}
							}
						}
					}
				}
				vcxprojSync.printLog(imp.getToFilter());
			}
				
			//TODO save files!
			if(vcxprojSync.getNumChanges()==0)
			{
				Log.d("Pretend option: skipping file save...");
			}
			else if(OPTS.isPretend())
			{
				Log.d("0 modifications detected, skipping file save...");
			}
			else
			{
				Log.d(vcxprojSync.getNumChanges() + " modifications detected, saving file...");
				vcxprojSync.saveVcxproj(OPTS.getOutput());
				vcxprojSync.saveVcxprojFilters(OPTS.getOutput());
			}
			Log.d("Done.");
		}

	}
}
