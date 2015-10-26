import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;


public class JTvsBoundaryTreeSet extends TreeSet<JTvsBoundaryData> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5733333599100477877L;

	public JTvsBoundaryData searchOnSeg (int seg, int offsA, int offsE, EnumOri ori_01rf)
	{
		JTvsBoundaryData rc = null;

		TreeSet<JTvsBoundaryData> resultsubset = (TreeSet<JTvsBoundaryData>) subSet(new JTvsBoundaryData(seg, offsA), new JTvsBoundaryData(seg, offsE));
		Iterator<JTvsBoundaryData> i = resultsubset.iterator();

		while (i.hasNext()) {
			JTvsBoundaryData tmp = i.next();
			if (tmp.ori == ori_01rf) {
				if (rc == null) {
					rc = tmp;
				}
				else {
					return null;
				}
			}
		}

		return rc;
	}
	
	public JTvsBoundaryData search (JLocation x0, JLocation x1, EnumOri ori_rf)
	{
		JTvsBoundaryData rc;
		JTvsBoundaryData rc1;

		if (x0.seg == x1.seg) {
			if (x0.offs < x1.offs) {
				rc = searchOnSeg (x0.seg, x0.offs, x1.offs, ori_rf.add(EnumOri.ORIPOS));
			}
			else {
				rc = searchOnSeg (x0.seg, x1.offs, x0.offs, ori_rf.add(EnumOri.ORINEG));
			}
			rc1 = rc;
		}
		else {
			JTvsBoundaryData tmp;

			if (x0.ori == EnumOri.ORIPOS) {
				rc = searchOnSeg (x0.seg, x0.offs, Integer.MAX_VALUE, ori_rf.add(EnumOri.ORIPOS));
			}
			else {
				rc = searchOnSeg (x0.seg, 0, x0.offs, ori_rf.add(EnumOri.ORINEG));
			}
			rc1 = rc;
			if (x1.ori == EnumOri.ORIPOS) {
				if ((tmp = searchOnSeg (x1.seg, 0, x1.offs, ori_rf.add(EnumOri.ORIPOS))) != null) {
					rc = tmp;
				};
			}
			else {
				if ((tmp = searchOnSeg (x1.seg, x1.offs, Integer.MAX_VALUE, ori_rf.add(EnumOri.ORINEG))) != null) {
					rc = tmp;
				};
			}
		}

		if ( rc != rc1 && rc1 != null) {
			// more than one matching boundary found
			rc = null;
		}
		
		return rc;
	}

	/**
	 * Find nearest boundary of TVS "tvsid" starting from Location "x0"
	 * 
	 * @param x0			start location, input parameter, also specifies direction to search
	 * @param tvsid			id of TVS
	 * @param maxdist		maximal distance to search
	 * @param tvsboundary	out-parameter, specifies the tvsboundary found, if any
	 * @return				distance im cm to relevant tvsboundary of Integer.Max_VALUE if none found
	 */
	public int search (JLocation x0, int tvsid, int maxdist, JTvsBoundaryData tvsboundary)
	{
		SortedSet<JTvsBoundaryData> candidates;
		Iterator<JTvsBoundaryData> i;
		JTvsBoundaryData tvsb;
		int offsA;
		int offsE;
		int dist;
		
		if(x0.ori == EnumOri.ORIPOS)
		{
			offsA = x0.offs;
			offsE = TdbProcessing.segLength.get(x0.seg);
		}
		else
		{
			offsA = 0;
			offsE = x0.offs;
		}

		candidates = subSet(new JTvsBoundaryData(x0.seg, offsA), new JTvsBoundaryData(x0.seg, offsE));
		i = candidates.iterator();
		while (i.hasNext())
		{
			tvsb = i.next();
			if ((tvsb.id == tvsid) && (tvsb.ori == x0.ori))
			{
				// found relevant boundary
				tvsboundary = tvsb;
				dist = Math.abs(tvsboundary.offs - x0.offs); // non-negative value
				return (dist <= maxdist ? dist : Integer.MAX_VALUE);
			}
		}

		// advance to corresponding end of segment
		if(x0.ori == EnumOri.ORIPOS)
		{
			dist = TdbProcessing.segLength.get(x0.seg) - x0.offs;
			x0.offs = TdbProcessing.segLength.get(x0.seg);
		}
		else
		{
			dist = x0.offs;
			x0.offs = 0;
		}
		if (dist > maxdist) return -1;  // search length exceeded
		maxdist -= dist;  //

		// recursion on adjacent segment(s)
		JLocation x0L = TdbProcessing.nxSeg(x0, SegAdjacency.LEFT);
		JLocation x0R = TdbProcessing.nxSeg(x0, SegAdjacency.RIGHT);
		int distL = (x0L.valid() ? search(x0L, tvsid, maxdist, tvsboundary) : Integer.MAX_VALUE);
		int distR = (x0R.valid() ? search(x0R, tvsid, maxdist, tvsboundary) : Integer.MAX_VALUE);
		return dist + Math.min(distL, distR);
	}
	
}

