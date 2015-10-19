
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
	
	JOprData (TraceLineIdentification lineId)
	{
		this.lineId = new TraceLineIdentification(lineId);
		this.t = lineId.tickcount;
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

}
