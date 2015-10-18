
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
}
