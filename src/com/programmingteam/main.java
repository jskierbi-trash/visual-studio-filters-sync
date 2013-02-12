package com.programmingteam;

import java.io.File;
import java.util.List;

import com.programmingteam.qsync.QSync;
import com.programmingteam.qsync.QSyncVcxproj;
import com.programmingteam.qsync.VisualVcxproj;

public class main
{
	public static void main(String[] args)
	{
		//test();
		
		File qsyncFile = new File(args[0]);
		if(args.length==1 && qsyncFile.exists())
		{
			//Read file
			QSync syncFiles = new QSync(qsyncFile);
			syncFiles.debugPrint();
			System.out.println("Qsyn created ok.");
			
			List<QSyncVcxproj> projs = syncFiles.getProjects();
			for(QSyncVcxproj p : projs)
			{
				VisualVcxproj proj = new VisualVcxproj(p.getVcxproj(), p.getVcxprojFilters());
				proj.debugPrint();
			}
		}
		else
		{
			//Run UI
			System.err.println("App does not have any UI...");
		}
	}
}
