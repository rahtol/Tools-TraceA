
public class JOprDataA extends JOprData {

	JOprDataA(TraceLineIdentification lineId) {
		super(lineId);
	}

	protected JOprDataA(int iSourceId, int t) {
		this.iSourceId = iSourceId;
		this.t = t;
	}

	public int compareTo(JOprData obj)
	{
		if (this.iSourceId == obj.iSourceId) {
			return this.t - obj.t;
		}
		return this.iSourceId - obj.iSourceId;
	}
}
