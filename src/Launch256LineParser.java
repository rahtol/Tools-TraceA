import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Launch256LineParser implements TraceLineParser {

	final static Pattern p1 = Pattern.compile("I_TRA\\s..[:]LAUNCH\\sid=([0-9]*),.*trainId=([0-9]*),\\sxf=([0-9]*)'([0-9]*)'([12]),\\sxr=([0-9]*)'([0-9]*)'([12]).*");

	public static Launch256Data lastLaunch256Data = new Launch256Data();
	
	@Override
	public void parseLine(String line, TraceLineIdentification lineId) throws Exception
	{
		Matcher m1 = p1.matcher(line);
		if (m1.matches()) {
			lastLaunch256Data.lineId = new TraceLineIdentification(lineId);
			lastLaunch256Data.id = Integer.parseInt(m1.group(1));
			lastLaunch256Data.trainId = Integer.parseInt(m1.group(2));
			lastLaunch256Data.xf = new JLocation(Integer.parseInt(m1.group(3)), Integer.parseInt(m1.group(4)), Integer.parseInt(m1.group(5)));
			lastLaunch256Data.xr = new JLocation(Integer.parseInt(m1.group(6)), Integer.parseInt(m1.group(7)), Integer.parseInt(m1.group(8)));
		}
	}
	
	public boolean cmpxf (int seg, int offs)
	{
		return (seg == lastLaunch256Data.xf.seg) && (offs == lastLaunch256Data.xf.offs);
	}

	public boolean cmpxr (int seg, int offs)
	{
		return (seg == lastLaunch256Data.xr.seg) && (offs == lastLaunch256Data.xr.offs);
	}

}
