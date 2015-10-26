import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class TdbProcessing {

	static HashMap<Integer,Integer> segLength = new HashMap<Integer,Integer>();
	static JTvsBoundaryTreeSet tvsBoundaries = new JTvsBoundaryTreeSet();
	static NavigableSet<SegAdjacency> segAdj = new TreeSet<SegAdjacency>();
	
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
					int segmentidleftdown = Integer.parseInt(eElementItem.getElementsByTagName("SegmentIdLeftDown").item(0).getTextContent().trim());
					int segmentidleftup = Integer.parseInt(eElementItem.getElementsByTagName("SegmentIdLeftUp").item(0).getTextContent().trim());
					if (segmentidrightdown != 0) segAdj.add(new SegAdjacency(seg, SegAdjacency.DOWN, SegAdjacency.RIGHT, segmentidrightdown));
					if (segmentidrightup != 0) segAdj.add(new SegAdjacency(seg, SegAdjacency.UP, SegAdjacency.RIGHT, segmentidrightup));
					if (segmentidleftdown != 0) segAdj.add(new SegAdjacency(seg, SegAdjacency.DOWN, SegAdjacency.LEFT, segmentidleftdown));
					if (segmentidleftup != 0) segAdj.add(new SegAdjacency(seg, SegAdjacency.UP, SegAdjacency.LEFT, segmentidleftup));
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
	
	public static boolean isTvsBoundaryAtSegLimit (int endtype, int segAdj, int id, EnumOri ori)
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
	
	/**
	 * Calculate a JLocation on the adjacent segment with distance zero to given location "loc"
	 *  
	 * @param loc	starting location; prerequisite: must be on segment limit, i.e. offset zero or seglength
	 * @param rl	selects adjacent segment: SegAdjacency.RIGHT or SegAdjacency.LEFT
	 * @return		a JLocation object on the adjacent segment specifying same location, i.e. dist=0 or an invalid location if segment does not exist.
	 */
	public static JLocation nxSeg(JLocation loc, int rl)
	{
		int ud = -1;
		if (loc.offs == 0) ud = SegAdjacency.DOWN;
		if (loc.offs == segLength.get(loc.seg)) ud = SegAdjacency.UP;
		if (ud < 0) return new JLocation(JLocation.INVALIDSEGID,-1,-1);
		
		SegAdjacency k = new SegAdjacency(loc.seg, ud, rl, 0);
		NavigableSet<SegAdjacency> candidates = segAdj.subSet(k,true,k,true);
		if (candidates.isEmpty()) return new JLocation(JLocation.INVALIDSEGID,-1,-1);
		int adjseg = candidates.first().adjSegId;
		int ud2 = findAdjSeg (adjseg, loc.seg);
		if (ud2 < 0) return new JLocation(JLocation.INVALIDSEGID,-2,-1);
		
		int offs = (ud2 == SegAdjacency.DOWN ? 0 : segLength.get(adjseg));
		EnumOri ori = (ud != ud2 ? loc.ori : loc.ori.invert());
		return new JLocation (adjseg, offs, ori);
		
	}
	
	private static int findAdjSeg(int srcSegId, int seg)
	{
		SegAdjacency k0 = new SegAdjacency(srcSegId, 0, 0, 0);
		SegAdjacency k1 = new SegAdjacency(srcSegId, 1, 1, 0);
		NavigableSet<SegAdjacency> candidates = segAdj.subSet(k0, true, k1, true);
		Iterator<SegAdjacency> i = candidates.iterator();
		while(i.hasNext())
		{
			SegAdjacency segi = i.next();
			if (segi.adjSegId == seg)
			{
				return segi.ud;
			}
		}
		return -1;
	}

	/**
	 * Is there a path from location "from" to location "to".
	 * @param from		start location of path, input only
	 * @param to		target location of path. direction irrelevant on input, updated on output
	 * @param maxdist	maximum distance to be searched
	 * @return			length in cm of path, negative if there is no path shorter that maxdist
	 */
	public static int dist(JLocation from, JLocation to, int maxdist)
	{
		// TODO
		return -1;
	}

}
