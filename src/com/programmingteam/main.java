package com.programmingteam;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.programmingteam.qsync.QSync;
import com.programmingteam.qsync.QSyncImport;
import com.programmingteam.qsync.QSyncVcxproj;
import com.programmingteam.vs2010.VcxprojSync;

public class Main
{
	public static void main(String[] args)
	{		
		File qsyncFile = new File(args[0]);
		
		if(args.length==1 && qsyncFile.exists())
		{
			//Read file
			QSync qsync = new QSync(qsyncFile);
			qsync.debugPrint();
			System.out.println("Qsyn created ok.");
			
			List<QSyncVcxproj> qsyncProjs = qsync.getProjects();
			for(QSyncVcxproj qsyncProj : qsyncProjs)
			{
				VcxprojSync vcxprojSync = new VcxprojSync(qsyncProj.getVcxproj(), qsyncProj.getVcxprojFilters());
				for(QSyncImport imp: qsyncProj.getImportList())
				{
					ArrayList<File> dirList = new ArrayList<File>();
					dirList.add(new File(imp.getInclude()));
					dirList.add(new File(imp.getSrc()));
					
					while(dirList.size()>0)
					{
						File dir = dirList.get(0);
						dirList.remove(0);
						File listFiles[] = dir.listFiles();
						for(int i=0; i<listFiles.length; ++i)
						{
							if(listFiles[i].isDirectory())
								dirList.add(listFiles[i]);
							else
							{
								if( Helpers.isCompile(listFiles[i], qsync.getCompileExt()) ||
									Helpers.isInclude(listFiles[i], qsync.getIncludeExt()))
								{
									String toFilter = listFiles[i].getAbsolutePath()
											.replace(imp.getInclude(), imp.getToFilter())
											.replace(imp.getSrc(), imp.getToFilter());
									toFilter = Helpers.getPath(toFilter);
									toFilter = Helpers.stripSlashes(toFilter);
									vcxprojSync.syncFile(qsyncProj.getRelativeFile(listFiles[i]), toFilter);
								}
							}
						}
					}
					
					System.out.println("To Filter: " + imp.getToFilter());
					System.out.println("include: " + imp.getInclude());
					System.out.println("src: " + imp.getSrc());
				}
			}
		}
		else
		{
			//Run UI
			System.err.println("App does not have any UI...");
		}
	}
}
