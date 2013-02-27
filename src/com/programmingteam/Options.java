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
				commandError("Invalid options after input file");
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
		System.out.println(msg);
		printUsage();
		System.exit(-1);
	}
	
	
	void printUsage()
	{
		System.out.println("Usage: QubicSync [OPTION]... FILE");
		System.out.println("Synchronizes Visual Studio project files (*.vcxproj and ");
		System.out.println(" *.vcxproj.filters) with filesystem using config file.");
		System.out.println("");
		System.out.println("  -o, --output <file>  generates output to specified file");
		System.out.println("  -p, --pretend        create no output file, only synchronization logs");
		System.out.println("  -q, --quiet          disable console output");
		System.out.println("  -v, --verbose        show diagnostic logs");
		System.out.println("  -h, --help           shows this help");
		System.out.println("");
	}
}
