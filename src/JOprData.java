
public abstract class JOprData implements Comparable<JOprData> {
	
	public final int MAX_TRAIN_CONSIST_VEHICLES = 5;
	
	public class T_sVehicle {
		public int iOBCU_UnitID_1;
		public int iOBCU_UnitID_2;
		public int iVehicle_Status;
	}

	public int t;
	public TraceLineIdentification lineId;

	public int trainId; // TRA compatible trainId
	
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
	public int iTrainConsistId;
	public int iNumOfVehiclesInTrainConsist;
	public T_sVehicle asVehicles[] = new T_sVehicle [MAX_TRAIN_CONSIST_VEHICLES];
	
	protected JOprData ()
	{
	}
	
	JOprData (TraceLineIdentification lineId)
	{
		this.lineId = new TraceLineIdentification(lineId);
		this.t = lineId.tickcount;
	}
	
	public static JOprData keyA (int iSourceId, int t)
	{
		return new JOprDataA(iSourceId, t);
	}

	public static JOprData keyB (int t, int trainId)
	{
		return new JOprDataB(t, trainId);
	}

	public String toString ()
	{
		return "\n(iSourceId=" + iSourceId + ", t=" + t + ")";
	}

	public boolean cmpxf (int seg, int offs)
	{
		return (seg == xf.seg) && (offs == xf.offs);
	}

	public boolean cmpxr (int seg, int offs)
	{
		return (seg == xr.seg) && (offs == xr.offs);
	}

	public String pr(int lvl)
	{
		String indent = "                ".substring(0, 2*lvl);
		
		return
			String.format("%s<opr xfseg=\"%d\" xfoffs=\"%d\" xfori=\"%s\" xrseg=\"%d\" xroffs=\"%d\" xrori=\"%s\" speed=\"%.1f\" uncertainty=\"%d\">\n", indent, this.xf.seg, this.xf.offs, (this.xf.ori==EnumOri.ORIPOS ? "pos" : "neg"), this.xr.seg, this.xr.offs, (this.xr.ori==EnumOri.ORIPOS ? "pos" : "neg"),  this.iTrainSpeed*3.6/100, this.iLocUncertainty) +
				this.lineId.pr(lvl+1) +
			String.format("%s</opr>\n", indent);
	}
}

