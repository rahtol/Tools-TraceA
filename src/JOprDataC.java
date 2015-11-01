
public class JOprDataC extends JOprData {

	JOprDataC(TraceLineIdentification lineId) {
		super(lineId);
	}
	
	protected JOprDataC(int t, int trainId) {
		this.t = t;
		this.trainId = trainId;
	}

	@Override
	public int compareTo(JOprData obj) {
		if (this.trainId == obj.trainId) {
			return this.t - obj.t;
		}
		return this.trainId - obj.trainId;
	}

}
