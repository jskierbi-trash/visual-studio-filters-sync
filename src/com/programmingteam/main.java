package com.programmingteam;

import java.io.File;

import com.programmingteam.qsync.QSync;

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
		}
		else
		{
			//Run UI
			System.err.println("App does not have any UI...");
		}
	}
}
