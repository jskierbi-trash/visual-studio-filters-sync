package com.programmingteam;

import java.io.File;

import com.programmingteam.projects.QSync;

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
	
	public static void test()
	{
		System.out.println("Hello World!");
		
		String s = "${BOX2D_3DS_DIR}/include/";
		s = Helpers.resolveEnvVars(s);
		s = Helpers.fixSlashes(s);
		System.out.println("Fixed: " + s);
		
		String file = "${BOX2D_3DS_DIR}/include/a.cpp";
		System.out.println("Extension: " + Helpers.getFileExt(file));
	}
}
