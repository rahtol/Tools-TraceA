import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SusLineParser implements TraceLineParser {

	final static Pattern p1 = Pattern.compile("I_TRA\\s..[:]SUS_GEO_COND\\s([0-9]*)'([0-9]*)'([0-9]*),\\stvs=([0-9]*).*");
	final static Pattern p2 = Pattern.compile("I_TRA\\s..[:]SUS_IDLE\\s([0-9]*)'([0-9]*)'([0-9]*).*");
	final static Pattern p3 = Pattern.compile("I_TRA\\s..[:]SUS_PROVEN\\s([0-9]*)'([0-9]*)'([0-9]*).*");
	final static Pattern p4 = Pattern.compile("I_TRA\\s..[:]DELETES\\s([0-9]*)'([0-9]*)'([0-9]*),\\stvs=([0-9]*).*");
	
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
				sievingAttempts.add(sa);
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
			int seg = Integer.parseInt(m1.group(1));
			int offs = Integer.parseInt(m1.group(2));
			boolean front = TraceB.launch256parser.cmpxf(seg, offs);
			boolean rear = TraceB.launch256parser.cmpxr(seg, offs);
			SievingAttempt sa;
			if ((front || rear) && ((sa = findSievingAttempt (Launch256LineParser.lastLaunch256Data.trainId, front, 1, lineId.tickcount - 5000)) != null))
			{
				sa.state = 10; // failed and complete, i.e. SUS_IDLE
				sa.susIdle = new SievingAttempt.SusIdle(lineId);
				sa.susIdle.seg = seg;
				sa.susIdle.offs = offs;
			}
		}

		// SUS_PROVEN
		Matcher m3 = p3.matcher(line);
		if (m3.matches()) {
			int seg = Integer.parseInt(m1.group(1));
			int offs = Integer.parseInt(m1.group(2));
			JOprData opr = findOpr (seg, offs, lineId.tickcount - 3000);
			boolean front = opr.cmpxf(seg, offs);
			boolean rear = opr.cmpxr(seg, offs);
			SievingAttempt sa;
			if ((front || rear) && ((sa = findSievingAttempt (opr.trainId, front, 1, lineId.tickcount - 5000)) != null))
			{
				sa.state = 2; // successful but still incomplete, i.e. SUS_PROVEN
				sa.susProven = new SievingAttempt.SusProven(lineId);
				sa.susProven.seg = seg;
				sa.susProven.offs = offs;
			}
		}

		Matcher m4 = p4.matcher(line);
		if (m4.matches()) {
		}

	}
	
	public SievingAttempt findSievingAttempt (int trainId, boolean front, int state, int tNotBefore)
	{
		// TODO
		return null;
	}

	public JOprData findOpr (int seg, int offs, int tNotBefore)
	{
		// TODO
		return null;
	}
	
}
