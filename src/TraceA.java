import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


public class TraceA {

	static TdbProcessing tdbProcessing = new TdbProcessing();
	
	/**
	 * basic data identifying the currently parsed line;
	 * (a) in terms of a counting lines global or local to current file
	 * (b) in terms time relative to PCB or ATS clock 
	 */
	static TraceLineIdentification lineId = new TraceLineIdentification();
	static TvsPhyVacParser tvsPhyVacParser = new TvsPhyVacParser();
	static OprParser oprParser = new OprParser(JOprDataA.class);

	static HashMap<Integer,Integer> segLength = TdbProcessing.segLength;
	static JTvsBoundaryTreeSet tvsBoundaries = TdbProcessing.tvsBoundaries;
	static TreeSet<JTvsChangeData> tvsChanges = TvsPhyVacParser.tvsChanges;
	static NavigableSet<JOprData> oprs = OprParser.oprs;
	static LinkedList<JMeasurement> measurementsClear = new LinkedList<JMeasurement>();
	static LinkedList<JMeasurement> measurementsOccupied = new LinkedList<JMeasurement>();
	
	public static void main(String[] args) {

//		System.out.println("TraceA v1.01, 19.11.2014");
//		System.out.println("TraceA v1.02, 16.10.2015");
		System.out.println("TraceA v1.03, 18.10.2015");
		
		if (args.length < 2) {
			System.err.println ("usage: TraceA <TDB-XML-File> <OSA-Log-File-Wildcard> ...\n");
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
				// TODO: order of logfiles[] is undefined !!!
				for (final File logfile : logfiles) {
					processLog(logfile);
				}
			}
			evaluate();
		} catch (Exception e) {
			System.err.println("Excption: " + e.getMessage());
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
			lineId.parseLine(zeile, null);
			
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
		// evaluate the oprs collected
		Iterator<JOprData> i = oprs.iterator();
		JOprData opr0 = null;
		JOprData opr1 = null;
		while (i.hasNext()) {
			opr0 = opr1;
			opr1 = i.next();
			if ((opr0 != null) && (opr0.iSourceId == opr1.iSourceId)) {
				checkSubsequentOprs (opr0, opr1, tvsChanges);
			}
		}

		// System.out.println("tvsBoundaries=" + tvsBoundaries);
		FileWriter outf;
		try {
			outf = new FileWriter("TraceA.out");
	       	outf.write(measurementsClear + "\n" + measurementsOccupied + "\n");
	       	outf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static void checkSubsequentOprs (JOprData opr0, JOprData opr1, TreeSet<JTvsChangeData> tvsChanges)
	{
		int dt = opr1.t - opr0.t;
		if ((dt > 0) && (dt < 2500) && (opr1.iTrainSpeed > 400) && (opr0.iTrainSpeed > 400)) {
			JTvsBoundaryData tvscleared = tvsBoundaries.search(opr0.xr, opr1.xr, EnumOri.ORINEG);
			if (tvscleared != null) {
				TreeSet<JTvsChangeData> candidates = (TreeSet<JTvsChangeData>) tvsChanges.subSet(new JTvsChangeData(tvscleared.id, opr0.t - 1000), new JTvsChangeData(tvscleared.id, opr1.t + 5000));
				Iterator<JTvsChangeData> i = candidates.iterator();
				while (i.hasNext()) {
					JTvsChangeData tmp = i.next();
					if (tmp.state == JTvsChangeData.CLEAR) {
						measurementsClear.add(new JMeasurement (opr0, opr1, tvscleared, tmp));
						break;
					}
				}
			}
			JTvsBoundaryData tvsoccupied = tvsBoundaries.search(opr0.xf, opr1.xf, EnumOri.ORIPOS);
			if (tvsoccupied != null) {
				TreeSet<JTvsChangeData> candidates = (TreeSet<JTvsChangeData>) tvsChanges.subSet(new JTvsChangeData(tvsoccupied.id, opr0.t - 1000), new JTvsChangeData(tvsoccupied.id, opr1.t + 5000));
				Iterator<JTvsChangeData> i = candidates.iterator();
				while (i.hasNext()) {
					JTvsChangeData tmp = i.next();
					if (tmp.state == JTvsChangeData.OCCUPIED) {
						measurementsOccupied.add(new JMeasurement (opr0, opr1, tvsoccupied, tmp));
						break;
					}
				}
			}
		}
		
	}  // of checkSubsequentOprs
	
}

