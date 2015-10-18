
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.NavigableSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class TraceB {

	static TdbProcessing tdbProcessing = new TdbProcessing();
	
	/**
	 * basic data identifying the currently parsed line;
	 * (a) in terms of a counting lines global or local to current file
	 * (b) in terms time relative to PCB or ATS clock 
	 */
	static TraceLineIdentification lineId = new TraceLineIdentification();
	static TvsPhyVacParser tvsPhyVacParser = new TvsPhyVacParser();
	static OprParser oprParser = new OprParser(JOprDataB.class);

	static HashMap<Integer,Integer> segLength = TdbProcessing.segLength;
	static JTvsBoundaryTreeSet tvsBoundaries = TdbProcessing.tvsBoundaries;
	static NavigableSet<JTvsChangeData> tvsChanges = TvsPhyVacParser.tvsChanges;
	static NavigableSet<JOprData> oprs = OprParser.oprs;
	
	public static void main(String[] args) {

		System.out.println("TraceB v1.00, 18.10.2015");
		
		if (args.length < 2) {
			System.err.println ("usage: TraceB <TDB-XML-File> <OSA-Log-File-Wildcard> ...\n");
			System.exit(-1);
		};
		
		final File tdb = new File (args[0]);
		
		try {
			TdbProcessing.processTdb (tdb);
		} catch (Exception e) {
			System.err.println("Error in TDB-XML: '" + e.getMessage() + "', in file: " + args[0]);
			
			e.printStackTrace();
			System.exit(-1);
		}
		

		try {
			for (int i=1; i < args.length; i++) {
				File argi0 = new File (args[i]);
				String s0 = argi0.getAbsolutePath();
	 			File argi = new File (s0);
				String wildcard = argi.getName();
				File logfiledir = argi.getParentFile();
				JFnPatternMatcher patternmatcher = new JFnPatternMatcher (wildcard);
				File logfiles[] = logfiledir.listFiles(patternmatcher);
				for (final File logfile : logfiles) {
					processLog(logfile);
				}
			}
			evaluate();
		} catch (Exception e) {
			System.err.println("file not found: "+args[1]);
			e.printStackTrace();
			System.exit(-1);
		}
		
       	System.out.println("ready.\n");
	}

	static void processLog (final File logDataFile) throws Exception
	{
		System.out.println("Processing: \"" + logDataFile.getAbsolutePath() + "\"");
		
		ZipFile zf = null;
		BufferedReader logDataBr;
		
		if (logDataFile.getName().endsWith(".zip"))
		{
			zf = new ZipFile(logDataFile);
			int len = logDataFile.getName().length();
			String s0 = logDataFile.getName();
			String s1 = s0.substring(0, len-4); 
			String s2 = s1 + ".txt";
			InputStream in = zf.getInputStream(new ZipEntry(s2));
			InputStreamReader inr = new InputStreamReader (in);
			logDataBr = new BufferedReader(inr);
		}
		else
		{
			FileReader logDataFr = new FileReader (logDataFile);
			logDataBr = new BufferedReader(logDataFr);
		}
		
		String zeile = "";
		lineId.nextFile (logDataFile);

		
		// check for "Input: Cycle ... " lines in order to extract tickcount values
		while ((zeile = logDataBr.readLine()) != null)
		{
			lineId.parseLine(zeile, lineId);
			
			// check for TVSPHYVAC lines in order to store tvsChanges
			tvsPhyVacParser.parseLine(zeile, lineId);
			
			// check for position report
			oprParser.parseLine(zeile, lineId);
		}
		
		logDataBr.close();
		if (zf != null) zf.close();

		// System.out.println(tvsChanges);
		// System.out.println(oprs);
		
	}  // of processLog
	
	static void evaluate()
	{
		// TODO
		
		FileWriter outf;
		try {
			outf = new FileWriter("TraceB.out");
			// TODO
//	       	outf.write(measurementsClear + "\n" + measurementsOccupied + "\n");
	       	outf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

