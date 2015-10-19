
public class JOprDataB extends JOprData {

	JOprDataB(TraceLineIdentification lineId) {
		super(lineId);
	}
	
	protected JOprDataB(int t, int trainId) {
		this.t = t;
		this.trainId = trainId;
	}

	@Override
	public int compareTo(JOprData obj)
	{
		if (this.t == obj.t) {
			return this.trainId - obj.trainId;
		}
		return this.t - obj.t;
	}

}
