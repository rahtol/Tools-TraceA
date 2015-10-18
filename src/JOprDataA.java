
public class JOprDataA extends JOprData {

	JOprDataA(TraceLineIdentification lineId) {
		super(lineId);
	}

	public int compareTo(JOprData obj)
	{
		if (this.iSourceId == obj.iSourceId) {
			return this.t - obj.t;
		}
		return this.iSourceId - obj.iSourceId;
	}
}
