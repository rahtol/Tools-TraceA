
public class JOprDataB extends JOprData {

	JOprDataB(TraceLineIdentification lineId) {
		super(lineId);
	}

	@Override
	public int compareTo(JOprData obj)
	{
		if (this.trainId == obj.trainId) {
			return this.t - obj.t;
		}
		return this.trainId - obj.trainId;
	}

}
