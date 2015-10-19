
public class SievingAttempt implements Comparable<SievingAttempt> {
	
	public static class SusGeoCond {
		public TraceLineIdentification lineId;
		public int seg;
		public int offs;
		public int tvsid;
		
		SusGeoCond (TraceLineIdentification lineId)
		{
			this.lineId = new TraceLineIdentification(lineId);
		}
	}
	
	public static class SusIdle {
		public TraceLineIdentification lineId;
		public int seg;
		public int offs;
		
		SusIdle (TraceLineIdentification lineId)
		{
			this.lineId = new TraceLineIdentification(lineId);
		}
	}
	
	public static class SusProven {
		public TraceLineIdentification lineId;
		public int seg;
		public int offs;
		
		SusProven (TraceLineIdentification lineId)
		{
			this.lineId = new TraceLineIdentification(lineId);
		}
	}
	
	public static class DeleteS {
		public TraceLineIdentification lineId;
		public int seg;
		public int offs;
		public int tvsid;
		
		DeleteS (TraceLineIdentification lineId)
		{
			this.lineId = new TraceLineIdentification(lineId);
		}
	}
	
	public int trainId;
	public boolean front; // otherwise sieving in rear
	public int state;

	public SusGeoCond susGeoCond;
	public SusIdle susIdle;
	public SusProven susProven;
	public DeleteS deletes;

	@Override
	public int compareTo(SievingAttempt o) {
		// keys are: trainId, front, state 
		// TODO Auto-generated method stub
		return 0;
	}

}
