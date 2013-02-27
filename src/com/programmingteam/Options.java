package com.programmingteam;

public class Options
{
	private boolean pretend;
	private boolean verbose;
	private boolean quiet;
	private String output;
	private String file;
	
	Options(String[] args)
	{		
		pretend = false;
		verbose = false;
		quiet   = false;
		output  = null;
		
		for(int i=0; i<args.length; ++i)
		{
			if(args[i].equals("-p") || args[i].equals("--pretend"))
			{
				pretend = true;
				continue;
			}
			
			if(args[i].equals("-v") || args[i].equals("--verbose"))
			{
				verbose = true;
				continue;
			}
			
			if(args[i].equals("-q") || args[i].equals("--quiet"))
			{
				quiet = true;
				continue;
			}
			
			if(args[i].equals("-h") || args[i].equals("--help"))
			{
				printUsage();
				System.exit(0);
			}
			
			if(args[i].equals("-o") || args[i].equals("--output"))
			{
				if(++i>=args.length || args[i].matches("-.*"))
					commandError("No file specified after output option");
				
				output = args[i];
				continue;
			}
			
			if(args[i].matches("-.*"))
				commandError("Unrecognized option: " + args[i]);
			
			file = args[i];
			if(i!=args.length-1)
			{
				Log.d("Invalid options after input file");
				printUsage();
				System.exit(-1);
			}
		}
		
		if(file==null)
			commandError("Invalid options.");
		
		if(verbose && quiet)
			commandError("Invalid options: both verbose and silent specified");
		
		if(pretend && output!=null)
			commandError("Invalid options: both pretend and output specified");
	}
	
	public boolean isPretend()
	{
		return pretend;
	}

	public boolean isVerbose()
	{
		return verbose;
	}

	public boolean isQuiet()
	{
		return quiet;
	}

	public String getOutput()
	{
		return output;
	}

	public String getFile()
	{
		return file;
	}

	void commandError(String msg)
	{
		Log.d(msg);
		printUsage();
		System.exit(-1);
	}
	
	
	void printUsage()
	{
		Log.d("Usage: QubicSync [OPTION]... FILE");
		Log.d("Synchronizes Visual Studio project files (*.vcxproj and ");
		Log.d(" *.vcxproj.filters) with filesystem using config file.");
		Log.d("");
		Log.d("  -o, --output <file>  generates output to specified file");
		Log.d("  -p, --pretend        create no output file, only synchronization logs");
		Log.d("  -q, --quiet          disable console output");
		Log.d("  -v, --verbose        show diagnostic logs");
		Log.d("  -h, --help           shows this help");
		Log.d("");
	}
}
