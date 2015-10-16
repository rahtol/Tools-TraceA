import java.util.Iterator;
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
}

