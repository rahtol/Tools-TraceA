import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TvsPhyVacParser implements TraceLineParser {

	final static Pattern p2 = Pattern.compile("I_TRA\\s..[:]TVSPHYVAC\\stvs[=]([0-9]*)\\sphyVac[=](Clear|Occupied).*");

	public static TreeSet<JTvsChangeData> tvsChanges = new TreeSet<JTvsChangeData>();
	
	@Override
	public void parseLine(String line, TraceLineIdentification lineId) {
		Matcher m2 = p2.matcher(line);
		if (m2.matches()) {
			int tvsid = Integer.parseInt(m2.group(1));
			String tvsstate = m2.group(2);
			tvsChanges.add(new JTvsChangeData (tvsid, tvsstate, lineId));
		}

	}

}
