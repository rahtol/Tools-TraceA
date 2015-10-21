import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SusLineParser implements TraceLineParser {

	final static Pattern p1 = Pattern.compile("I_TRA\\s..[:]SUS_GEO_COND\\s([0-9]*)'([0-9]*)'([0-9]*),\\stvs=([0-9]*).*");
	final static Pattern p2 = Pattern.compile("I_TRA\\s..[:]SUS_IDLE\\s([0-9]*)'([0-9]*)'([0-9]*).*");
	final static Pattern p3 = Pattern.compile("I_TRA\\s..[:]SUS_PROVEN\\s([0-9]*)'([0-9]*)'([0-9]*).*");
	final static Pattern p4 = Pattern.compile("I_TRA\\s..[:]DELETES\\s([0-9]*)'([0-9]*)'([0-9]*)\\stvs=([0-9]*).*");
	
	public static NavigableSet<SievingAttempt> sievingAttempts = new TreeSet<SievingAttempt>();

	@Override
	public void parseLine(String line, TraceLineIdentification lineId) throws Exception
	{
		// SUS_GEO_COND
		Matcher m1 = p1.matcher(line);
		if (m1.matches())
		{
			int seg = Integer.parseInt(m1.group(1));
			int offs = Integer.parseInt(m1.group(2));
			boolean front = TraceB.launch256parser.cmpxf(seg, offs);
			boolean rear = TraceB.launch256parser.cmpxr(seg, offs);
			if (front || rear)
			{
				SievingAttempt sa = new SievingAttempt();
				sa.state = 1;  // SUS_GEO_COND seen last, incomplete
				sa.trainId = Launch256LineParser.lastLaunch256Data.trainId;
				sa.front = front;
				sa.susGeoCond = new SievingAttempt.SusGeoCond(lineId);
				sa.susGeoCond.seg = seg;
				sa.susGeoCond.offs = offs;
				sa.susGeoCond.tvsid = Integer.parseInt(m1.group(4));
				sa.susGeoCond.opr = chkTrainId(findOpr(seg, offs, lineId.tickcount), sa.trainId);
				sievingAttempts.add(sa);
				sa.tvschange = findTvschange(sa.susGeoCond.tvsid, sa.susGeoCond.lineId.tickcount, -3000, +5000);
			}
			else
			{
				System.err.println("SUS_GEO_COND without matching LAUNCH256 in line " + lineId.lineNo);
			}
		}

		// SUS_IDLE
		Matcher m2 = p2.matcher(line);
		if (m2.matches())
		{
			int seg = Integer.parseInt(m2.group(1));
			int offs = Integer.parseInt(m2.group(2));
			boolean front = TraceB.launch256parser.cmpxf(seg, offs);
			boolean rear = TraceB.launch256parser.cmpxr(seg, offs);
			SievingAttempt sa;
			if ((front || rear) && ((sa = findSievingAttempt (Launch256LineParser.lastLaunch256Data.trainId, front, 1, lineId.tickcount - 5000)) != null))
			{
				sa.state = 10; // failed and complete, i.e. SUS_IDLE
				sa.susIdle = new SievingAttempt.SusIdle(lineId);
				sa.susIdle.seg = seg;
				sa.susIdle.offs = offs;
				sa.susIdle.opr = chkTrainId(findOpr(seg, offs, lineId.tickcount), sa.trainId);
				if (sa.tvschange == null) 
					sa.tvschange = findTvschange(sa.susGeoCond.tvsid, sa.susIdle.lineId.tickcount, -3000, +5000);
			}
			else
			{
				System.err.println("SUS_IDLE without matching SievingAttempt in line " + lineId.lineNo);
			}
		}

		// SUS_PROVEN
		Matcher m3 = p3.matcher(line);
		if (m3.matches()) {
			int seg = Integer.parseInt(m3.group(1));
			int offs = Integer.parseInt(m3.group(2));
			JOprData opr = findOpr (seg, offs, lineId.tickcount - 3000);
			boolean front = (opr != null ? opr.cmpxf(seg, offs) : false);
			boolean rear =  (opr != null ? opr.cmpxr(seg, offs) : false);
			SievingAttempt sa;
			if ((front || rear) && ((sa = findSievingAttempt (opr.trainId, front, 1, lineId.tickcount - 5000)) != null))
			{
				sa.state = 2; // successful but still incomplete, i.e. SUS_PROVEN
				sa.susProven = new SievingAttempt.SusProven(lineId);
				sa.susProven.seg = seg;
				sa.susProven.offs = offs;
				sa.susProven.opr = opr;
				if (sa.tvschange == null) 
					sa.tvschange = findTvschange(sa.susGeoCond.tvsid, sa.susProven.lineId.tickcount, -3000, +5000);
			}
			else
			{
				System.err.println("SUS_PROVEN without matching SievingAttempt in line " + lineId.lineNo);
			}
		}

		Matcher m4 = p4.matcher(line);
		if (m4.matches())
		{
			int seg = Integer.parseInt(m4.group(1));
			int offs = Integer.parseInt(m4.group(2));
			JOprData opr = findOpr (seg, offs, lineId.tickcount - 3000);
			boolean front = (opr != null ? opr.cmpxf(seg, offs) : false);
			boolean rear =  (opr != null ? opr.cmpxr(seg, offs) : false);
			SievingAttempt sa;
			if ((front || rear) && ((sa = findSievingAttempt (opr.trainId, front, 2, lineId.tickcount - 5000)) != null))
			{
				sa.state = 3; // successful and complete
				sa.deleteS = new SievingAttempt.DeleteS(lineId);
				sa.deleteS.seg = seg;
				sa.deleteS.offs = offs;
				sa.deleteS.tvsid = Integer.parseInt(m4.group(4));
				sa.deleteS.opr = opr;
				if (sa.tvschange == null) 
					sa.tvschange = findTvschange(sa.susGeoCond.tvsid, sa.deleteS.lineId.tickcount, -3000, +5000);
			}
			else
			{
				System.err.println("DELETES without matching SievingAttempt in line " + lineId.lineNo);
			}
		}

	}
	
	public SievingAttempt findSievingAttempt (int trainId, boolean front, int state, int tNotBefore)
	{
		SievingAttempt key = SievingAttempt.key(trainId, front, state);
		NavigableSet<SievingAttempt> candidates = sievingAttempts.subSet(key, true, key, true);
		if (candidates.isEmpty()) return null;
		SievingAttempt sa = candidates.first();
		return ((sa.susGeoCond != null) && (sa.susGeoCond.lineId.tickcount >= tNotBefore) ? sa : null);
	}

	public JOprData findOpr (int seg, int offs, int tNotBefore)
	{
		JOprData k = JOprData.keyB(tNotBefore, 0);
		NavigableSet<JOprData> candidates = TraceB.oprs.tailSet(k, false);
		Iterator<JOprData> i = candidates.descendingIterator();
		while (i.hasNext()) {
			JOprData opr = i.next();
			boolean front = opr.cmpxf(seg, offs);
			boolean rear = opr.cmpxr(seg, offs);
			if (front || rear) return opr;
		}
		return null;
	}
	
	public JOprData chkTrainId (JOprData opr, int trainId)
	{
		return (opr != null ? (((trainId == 0) || (opr.trainId == trainId)) ? opr : null) : null);
	}

	public JTvsChangeData findTvschange(int tvsid, int t, int dtneg, int dtpos)
	{
		JTvsChangeData k = JTvsChangeData.key(tvsid, t);
		JTvsChangeData past = TraceB.tvsChanges.floor(k);
		JTvsChangeData future = TraceB.tvsChanges.ceiling(k);
		int dtpast = (((past != null) && (past.id == tvsid) && ((past.t - t)  >= dtneg)) ? t - past.t : Integer.MAX_VALUE);
		int dtfuture = (((future != null) && (future.id == tvsid) && ((future.t - t) <= dtpos)) ? future.t - t : Integer.MAX_VALUE);
		return (dtpast < dtfuture ? past : future);
	}

}
