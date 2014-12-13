import java.io.File;


public class JOprData implements Comparable<JOprData> {

	public int t;
	File logDataFile;
	public int lineNo;

	public int iSourceId;
	public int iSourceTs;
	public int iLocIsSecure;
	public int iLocUncertainty;
	public int iLocIntegrityIsOk;
	public JLocation xf;
	public JLocation xr;
	public int iOperationLevel;
	public int iOperationMode;
	public int iTrainLength;
	public int iTrainType;
	public int iTrainSpeed;
	public int iTrainStandstill;
	
	JOprData (int t, File logDataFile, int lineNo)
	{
		this.t = t;
		this.logDataFile = logDataFile;
		this.lineNo = lineNo;
	}

	public int compareTo(JOprData obj)
	{
		if (this.iSourceId == obj.iSourceId) {
			return this.t - obj.t;
		}
		return this.iSourceId - obj.iSourceId;
	}
	
	public String toString ()
	{
		return "\n(iSourceId=" + iSourceId + ", t=" + t + ")";
	}
}
