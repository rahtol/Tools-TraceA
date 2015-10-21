
public class SievingAttempt implements Comparable<SievingAttempt> {
	
	public static class SusGeoCond {
		public TraceLineIdentification lineId;
		public int seg;
		public int offs;
		public int tvsid;
		public JOprData opr;
		
		SusGeoCond (TraceLineIdentification lineId)
		{
			this.lineId = new TraceLineIdentification(lineId);
		}
		
		public String pr(int lvl)
		{
			String indent = "                ".substring(0, 2*lvl);
			
			return
				String.format("%s<susgeocond seg=\"%d\" offs=\"%d\" tvsid=\"%d\">\n", indent, this.seg, this.offs, this.tvsid) +
					this.lineId.pr(lvl+1) +
					(opr != null ? opr.pr(lvl+1) : "") +
				String.format("%s<\\susgeocond>\n", indent);
		}
	}
	
	public static class SusIdle {
		public TraceLineIdentification lineId;
		public int seg;
		public int offs;
		public JOprData opr;
		
		SusIdle (TraceLineIdentification lineId)
		{
			this.lineId = new TraceLineIdentification(lineId);
		}
		
		public String pr(int lvl)
		{
			String indent = "                ".substring(0, 2*lvl);
			
			return
				String.format("%s<susidle seg=\"%d\" offs=\"%d\">\n", indent, this.seg, this.offs) +
					this.lineId.pr(lvl+1) +
					(opr != null ? opr.pr(lvl+1) : "") +
				String.format("%s<\\susidle>\n", indent);
		}
	}
	
	public static class SusProven {
		public TraceLineIdentification lineId;
		public int seg;
		public int offs;
		public JOprData opr;
		
		SusProven (TraceLineIdentification lineId)
		{
			this.lineId = new TraceLineIdentification(lineId);
		}
		
		public String pr(int lvl)
		{
			String indent = "                ".substring(0, 2*lvl);
			
			return
				String.format("%s<susproven seg=\"%d\" offs=\"%d\">\n", indent, this.seg, this.offs) +
					this.lineId.pr(lvl+1) +
					(opr != null ? opr.pr(lvl+1) : "") +
				String.format("%s<\\susproven>\n", indent);
		}
	}
	
	public static class DeleteS {
		public TraceLineIdentification lineId;
		public int seg;
		public int offs;
		public int tvsid;
		public JOprData opr;
		
		DeleteS (TraceLineIdentification lineId)
		{
			this.lineId = new TraceLineIdentification(lineId);
		}
		
		public String pr(int lvl)
		{
			String indent = "                ".substring(0, 2*lvl);
			
			return
				String.format("%s<deletes seg=\"%d\" offs=\"%d\" tvsid=\"%d\">\n", indent, this.seg, this.offs, this.tvsid) +
					this.lineId.pr(lvl+1) +
				String.format("%s<\\deletes>\n", indent);
		}
	}
	
	public int trainId;
	public boolean front; // otherwise sieving in rear
	public int state;

	public SusGeoCond susGeoCond;
	public SusIdle susIdle;
	public SusProven susProven;
	public DeleteS deleteS;
	
	public JTvsChangeData tvschange;
	
	public SievingAttempt ()
	{
	}
	
	private SievingAttempt (int trainId, boolean front, int state)
	{
		this.trainId = trainId;
		this.front = front;
		this.state = state;
	}
	
	public static SievingAttempt key (int trainId, boolean front, int state)
	{
		return new SievingAttempt(trainId, front, state);
	}

	@Override
	public int compareTo(SievingAttempt obj) {
		// keys are: trainId, front, state 
		if (this.trainId == obj.trainId) {
			if (this.front == obj.front) {
				return this.state - obj.state;
			}
			return (this.front ? +1 : -1);
		}
		return this.trainId - obj.trainId;
	}

	public String pr(int lvl)
	{
		String indent = "                ".substring(0, 2*lvl);
		
		return
			String.format("%s<sievingattempt state=\"%d\" trainid=\"%d\" frontrear=\"%s\" timeofday=\"%s\">\n", indent, this.state, this.trainId, (this.front ? "F" : "R"), this.susGeoCond.lineId.timeOfDay()) +
				(this.susGeoCond != null ? this.susGeoCond.pr(lvl+1) : "") +
				(this.susIdle != null ? this.susIdle.pr(lvl+1) : "") +
				(this.susProven != null ? this.susProven.pr(lvl+1) : "") +
				(this.deleteS != null ? this.deleteS.pr(lvl+1) : "") +
				(this.tvschange != null ? this.tvschange.pr(lvl+1) : "") +
			String.format("%s<\\sievingattempt>\n", indent);
	}
}
