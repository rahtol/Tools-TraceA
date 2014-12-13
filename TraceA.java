import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class TraceA {

	final static Pattern p1 = Pattern.compile("Input[:]\\sCycle\\s0x([0-9a-f]*)\\s.*@(\\d*)[:].*");
	final static Pattern p2 = Pattern.compile("I_TRA\\s..[:]TVSPHYVAC\\stvs[=]([0-9]*)\\sphyVac[=](Clear|Occupied).*");
	final static Pattern p3 = Pattern.compile("Dispatcher[:]\\stime[:].*201h[,]\\ss[:]\\s60[,]\\sd[:]\\s62[,]\\sch[:].*data[:]\\s..\\s..\\s..\\s..\\s..\\sf6\\s00\\s80((\\s[0-9a-f][0-9a-f])*)");

	
	/**
	 * current line number in log file;
	 */
	static int lineNo = 0;

	/** 
	 * current time in log file, resolution is [ms]; 
	 * windows tickcount at start of current PCB cycle; 
	 * extracted from input-cycle traceline
	*/
	static int tickcount = 0;

	static HashMap<Integer,Integer> segLength = new HashMap<Integer,Integer>();
	static JTvsBoundaryTreeSet tvsBoundaries = new JTvsBoundaryTreeSet();
	static LinkedList<JMeasurement> measurementsClear = new LinkedList<JMeasurement>();
	static LinkedList<JMeasurement> measurementsOccupied = new LinkedList<JMeasurement>();
	
	public static void main(String[] args) {

		System.out.println("TraceA v1.01, 19.11.2014");
		
		if (args.length < 2) {
			System.err.println ("usage: TraceA <TDB-XML-File> <OSA-Log-File-Wildcard> ...\n");
			System.exit(-1);
		};
		
		final File tdb = new File (args[0]);
		
		try {
			processTdb (tdb);
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
		} catch (IOException e) {
			System.err.println("file not found: "+args[1]);
			e.printStackTrace();
			System.exit(-1);
		}
		
		// System.out.println("tvsBoundaries=" + tvsBoundaries);
       	System.out.println(measurementsClear + "\n" + measurementsOccupied);
       	System.out.println("ready.\n");
	}

	static void processTdb (final File tdbXmlFile) throws IOException, ParserConfigurationException, SAXException
	{
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(tdbXmlFile);
		
		NodeList nList = doc.getElementsByTagName("NetworkSection");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNodeNetworkSection = nList.item(temp);
			// System.err.println("Current Element: " + nNodeNetworkSection.getNodeName());
			Element eElementNetworkSection = (Element) nNodeNetworkSection;
			int networksectionId = Integer.parseInt(eElementNetworkSection.getElementsByTagName("networksection_id").item(0).getTextContent());
			// System.err.println ("network section id: " + networksectionId);
			Node nNodeSegmentDataList = eElementNetworkSection.getElementsByTagName("SegmentDataList").item(0);
			Element eElementSegmentDataList = (Element) nNodeSegmentDataList;
			NodeList nListItem = eElementSegmentDataList.getChildNodes();
			for (int j = 0; j < nListItem.getLength(); j++) {
				Node nNodeItem = nListItem.item(j);
				if (nNodeItem.getNodeType() == Node.ELEMENT_NODE) {
					Element eElementItem = (Element) nNodeItem;
					int segmentsubid = Integer.parseInt(eElementItem.getElementsByTagName("segment_subid").item(0).getTextContent());
					int seg = networksectionId * 256 + segmentsubid;
					int seglen = Integer.parseInt(eElementItem.getElementsByTagName("SegmentLength").item(0).getTextContent().trim());
					segLength.put(seg, seglen);
					int segmentidrightdown = Integer.parseInt(eElementItem.getElementsByTagName("SegmentIdRightDown").item(0).getTextContent().trim());
					int segmentidrightup = Integer.parseInt(eElementItem.getElementsByTagName("SegmentIdRightUp").item(0).getTextContent().trim());
					int segmentendtypedown = Integer.parseInt(eElementItem.getElementsByTagName("SegmentEndTypeDown").item(0).getTextContent().trim());
					int segmentendtypeup = Integer.parseInt(eElementItem.getElementsByTagName("SegmentEndTypeUp").item(0).getTextContent().trim());
					int segmentdirectionrightdown = Integer.parseInt(eElementItem.getElementsByTagName("SegmentDirectionRightDown").item(0).getTextContent().trim());
					int segmentdirectionrightup = Integer.parseInt(eElementItem.getElementsByTagName("SegmentDirectionRightUp").item(0).getTextContent().trim());
					EnumOri segOriDown = EnumOri.fromSegmentDirection (segmentdirectionrightdown);
					EnumOri segOriUp = EnumOri.fromSegmentDirection (segmentdirectionrightup);
					// System.out.println ("seg=" + seg + ", segmentidrightdown=" + segmentidrightdown  + ", segmentidrightup=" + segmentidrightup + ", segmentendtypedown=" + segmentendtypedown + ", segmentendtypeup=" + segmentendtypeup + ", seglen=" + seglen);
					Node nNodeTrackVacancySectionList = eElementItem.getElementsByTagName("TrackVacancySectionList").item(0);
					Element eElementTrackVacancySectionList = (Element) nNodeTrackVacancySectionList;
					NodeList nListItemTvs = eElementTrackVacancySectionList.getElementsByTagName("Item");
					for (int k = 0; k < nListItemTvs.getLength(); k++) {
						Node nNodeItemTvs = nListItemTvs.item(k);
						Element eElementItemTvs = (Element) nNodeItemTvs;
						int id = Integer.parseInt(eElementItemTvs.getElementsByTagName("logical_element_id").item(0).getTextContent());
						int offs = Integer.parseInt(eElementItemTvs.getElementsByTagName("distance").item(0).getTextContent());
						int len = Integer.parseInt(eElementItemTvs.getElementsByTagName("length").item(1).getTextContent());
						// System.out.println ("seg=" + seg + ", offs=" + offs + " len=" + len + " id=" + id);
						if ((offs != 0) || (isTvsBoundaryAtSegLimit(segmentendtypedown, segmentidrightdown, id, segOriDown.invert()))) {
							tvsBoundaries.add(new JTvsBoundaryData(seg, offs, EnumOri.ORIPOS, id));
						}
						if ((offs + len != seglen) || (isTvsBoundaryAtSegLimit(segmentendtypeup, segmentidrightup, id, segOriUp))) {
							tvsBoundaries.add(new JTvsBoundaryData(seg, offs + len, EnumOri.ORINEG, id));
						}
					}
				}
			}
		}
	}  // of processTdb
	
	static void processLog (final File logDataFile) throws IOException
	{
		System.out.println("Processing: \"" + logDataFile.getAbsolutePath() + "\"");
		
		FileReader logDataFr = new FileReader (logDataFile);
		BufferedReader logDataBr = new BufferedReader(logDataFr);
		String zeile = "";

		TreeSet<JTvsChangeData> tvsChanges = new TreeSet<JTvsChangeData>();
		TreeSet<JOprData> oprs = new TreeSet<JOprData>();
		
		// check for "Input: Cycle ... " lines in order to extract tickcount values
		while ((zeile = logDataBr.readLine()) != null)
		{
			lineNo++;
			
			// check for "Input: Cycle ..."
			Matcher m1 = p1.matcher(zeile);
			if (m1.matches()) {
				int t = Integer.parseInt(m1.group(2));
				tickcount = t;
				// System.out.println(lineNo + "  " + t + "  " + m1.group(1) + "  " + m1.group(2) + " :: " + zeile);
			}
			
			// check for TVSPHYVAC lines in order to store tvsChanges
			Matcher m2 = p2.matcher(zeile);
			if (m2.matches()) {
				int tvsid = Integer.parseInt(m2.group(1));
				String tvsstate = m2.group(2);
				tvsChanges.add(new JTvsChangeData (tvsid, tickcount, tvsstate, logDataFile, lineNo));
				// System.out.println(lineNo + "  " + tickcount + "  " + tvsid + "  " + tvsstate + " :: " + zeile);
			}
			
			// check for position report
			Matcher m3 = p3.matcher(zeile);
			if (m3.matches()) {
				String hexstr = m3.group(1);
				JHexDump oprmsg = new JHexDump (" f6 00 80" + hexstr);
				JOprData oprdata = new JOprData (tickcount, logDataFile, lineNo);
				oprdata.iSourceId = oprmsg.getUInt16 (5);
				oprdata.iSourceTs = oprmsg.getUInt32 (9);
				oprdata.iLocIsSecure = oprmsg.getUInt8 (26);
				oprdata.iLocUncertainty = oprmsg.getUInt16 (27);
				oprdata.iLocIntegrityIsOk = oprmsg.getUInt8 (29);
				oprdata.xf = new JLocation(oprmsg.getUInt16 (30), oprmsg.getUInt32 (32), oprmsg.getUInt8 (36));
				oprdata.xr = new JLocation(oprmsg.getUInt16 (37), oprmsg.getUInt32 (39), oprmsg.getUInt8 (43));
				oprdata.iOperationLevel = oprmsg.getUInt8 (47);
				oprdata.iOperationMode = oprmsg.getUInt8 (48);
				oprdata.iTrainLength = oprmsg.getUInt16 (50);
				oprdata.iTrainType = oprmsg.getUInt8 (52);
				oprdata.iTrainSpeed = oprmsg.getUInt16 (53);
				oprdata.iTrainStandstill = oprmsg.getUInt16 (55);
				if ((oprdata.iLocIsSecure == 1) && (oprdata.iLocIntegrityIsOk == 1))
				{
					oprs.add(oprdata);
				}
			}
			
		}
		
		logDataBr.close();

		// System.out.println(tvsChanges);
		// System.out.println(oprs);
		
		// evaluate the oprs collected
		Iterator<JOprData> i = oprs.iterator();
		while (i.hasNext()) {
			JOprData opr0 = i.next();
			JOprData opr1;
			while (i.hasNext() && (opr1 = i.next()).iSourceId == opr0.iSourceId) {
				checkSubsequentOprs (opr0, opr1, tvsChanges);
				opr0 = opr1;
			}
		}
	}  // of processLog
	
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
	
	static boolean isTvsBoundaryAtSegLimit (int endtype, int segAdj, int id, EnumOri ori)
	{
		if (endtype > 1) {
			// no tvsBoundary at points, i.e. endtypes converging or diverging
			return false;
		}
		else {
			if (endtype == 0) {
				// there is always a tvsBoundary at end of chaining
				return true;
			}
			else {
				// endtype == 1: 1:1 segment chaining
				// search segAdj for id: return search result and remove entry
				boolean found = false;
				TreeSet<JTvsBoundaryData> segAdjsubset = (TreeSet<JTvsBoundaryData>) tvsBoundaries.subSet(new JTvsBoundaryData(segAdj, 0), new JTvsBoundaryData(segAdj, Integer.MAX_VALUE));
				Iterator<JTvsBoundaryData> i = segAdjsubset.iterator();
	
				while (i.hasNext()) {
					JTvsBoundaryData tmp = i.next();
					if (tmp.id == id && tmp.ori == ori) {
						found = true;
						i.remove();
					}
				}
				return !found;
			}
		}
	}
}

