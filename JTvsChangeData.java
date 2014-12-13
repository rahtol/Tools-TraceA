import java.io.File;


public class JTvsChangeData implements Comparable<JTvsChangeData> {
	
	public static final int CLEAR = 1;
	public static final int OCCUPIED = 2;

	public int id;
	public int t;
	public int state; // 1=Clear, 2=Occupied
	File logDataFile;
	public int lineNo;
	public int usageCount = 0;
	
	JTvsChangeData (int id, int t)
	{
		this.id = id;
		this.t = t;
	}

	JTvsChangeData (int id, int t, int state, File logDataFile, int lineNo)
	{
		this.id = id;
		this.t = t;
		this.state = state;
		this.logDataFile = logDataFile;
		this.lineNo = lineNo;
	}

	JTvsChangeData (int id, int t, String state, File logDataFile, int lineNo)
	{
		this.id = id;
		this.t = t;
		this.state = 0;
		this.logDataFile = logDataFile;
		this.lineNo = lineNo;
		
		if (state.equals("Clear")) {
			this.state = 1;
		}
		else if (state.equals("Occupied")) {
			this.state = 2;
		}
	}
	
	public int compareTo(JTvsChangeData obj)
	{
		if (this.id == obj.id) {
			return this.t - obj.t;
		}
		return this.id - obj.id;
	}
	
	public String toString ()
	{
		return "(id=" + id + ", t=" + t + ", state=" + state + ")";
	}
}
