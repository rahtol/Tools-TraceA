
public class JTvsChangeData implements Comparable<JTvsChangeData> {
	
	public static final int CLEAR = 1;
	public static final int OCCUPIED = 2;

	public int id;
	public int state; // 1=Clear, 2=Occupied
	public int t;
	public TraceLineIdentification lineId;
	public int usageCount = 0;
	
	JTvsChangeData (int id, int t)
	{
		this.id = id;
		this.t = t;
	}

	JTvsChangeData (int id, String state, TraceLineIdentification lineId)
	{
		this.id = id;
		this.state = 0;
		this.lineId = new TraceLineIdentification(lineId);
		this.t = this.lineId.tickcount;
		
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
		return "(id=" + id + ", t=" + ((lineId!=null)?lineId.tickcount:"?") + ", state=" + state + ")";
	}
	
	public static JTvsChangeData key(int id, int t)
	{
		return new JTvsChangeData (id, t);
	}
	
	public String pr(int lvl)
	{
		String indent = "                ".substring(0, 2*lvl);
		
		return
			String.format("%s<tvschange id=\"%d\" t=\"%d\" state=\"%s\">\n", indent, this.id, this.t, prState()) +
				this.lineId.pr(lvl+1) +
			String.format("%s<\\tvschange>\n", indent);
	}
	
	public String prState()
	{
		String s;
		switch (state){
		case 1: s = "clear"; break;
		case 2: s = "occupied"; break;
		default: s = String.format("?%d?", state); break;
		}
		return s;
	}
}
