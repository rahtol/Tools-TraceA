import java.util.NavigableSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class OprParser implements TraceLineParser {

	final static Pattern p3 = Pattern.compile("Dispatcher[:]\\stime[:].*201h[,]\\ss[:]\\s60[,]\\sd[:]\\s62[,]\\sch[:].*data[:]\\s..\\s..\\s..\\s..\\s..\\sf6\\s00\\s80((\\s[0-9a-f][0-9a-f])*)");

	public static NavigableSet<JOprData> oprs = new TreeSet<JOprData>();
	Class<? extends JOprData> jOprDataX;
	
	public OprParser (Class<? extends JOprData> jOprDataX)
	{
		this.jOprDataX = jOprDataX;
//		jOprDataX.getClass();
	}

	@Override
	public void parseLine(String line, TraceLineIdentification lineId) throws Exception {
		Matcher m3 = p3.matcher(line);
		if (m3.matches()) {
			String hexstr = m3.group(1);
			JHexDump oprmsg = new JHexDump (" f6 00 80" + hexstr);
			JOprData oprdata = jOprDataX.getDeclaredConstructor(TraceLineIdentification.class).newInstance(lineId);
			oprdata.iSourceId = oprmsg.getUInt16 (5);
			oprdata.iSourceTs = oprmsg.getUInt32 (9);
			oprdata.iLocIsSecure = oprmsg.getUInt8 (26);
			oprdata.iLocUncertainty = oprmsg.getUInt16 (27);
			oprdata.iLocIntegrityIsOk = oprmsg.getUInt8 (29);
			oprdata.xf = new JLocation(oprmsg.getUInt16 (30), oprmsg.getUInt32 (32), oprmsg.getUInt8 (36));
			oprdata.xr = new JLocation(oprmsg.getUInt16 (37), oprmsg.getUInt32 (39), oprmsg.getUInt8 (43));
			oprdata.iOperationLevel = oprmsg.getUInt8 (47);
			oprdata.iOperationMode = oprmsg.getUInt8 (48);
			oprdata.iTrainLength = oprmsg.getUInt16 (50);
			oprdata.iTrainType = oprmsg.getUInt8 (52);
			oprdata.iTrainSpeed = oprmsg.getUInt16 (53);
			oprdata.iTrainStandstill = oprmsg.getUInt8 (55);
//			oprdata.iPsdOpenAuth = oprmsg.getUInt8 (56);
//			oprdata.iMgfCommand = oprmsg.getUInt8 (57);
//			oprdata.iStopAssure = oprmsg.getUInt8 (58);
//			oprdata.sVitalMal = = new JLocation(oprmsg.getUInt16 (59), oprmsg.getUInt32 (61), oprmsg.getUInt8 (65));
//			oprdata.iVitalMalType = oprmsg.getUInt8 (66);
//			oprdata.iSignalId = oprmsg.getUInt16 (67);
//			oprdata.iReversalStatus = oprmsg.getUInt8 (69);
//			oprdata.iDdiActive = oprmsg.getUInt8 (70);
			// T_sVTrainDatDispl	sVTrainDatDispl 71-84 (14 Byte)
			oprdata.iTrainConsistId = oprmsg.getUInt16 (82);
			oprdata.iNumOfVehiclesInTrainConsist = oprmsg.getUInt8 (84);
			int minUnitId = Integer.MAX_VALUE;
			for (int i=0; i<oprdata.iNumOfVehiclesInTrainConsist; i++)
			{
				oprdata.asVehicles[i] = oprdata.new T_sVehicle();
				oprdata.asVehicles[i].iOBCU_UnitID_1 = oprmsg.getUInt16 (85+i*5);
				oprdata.asVehicles[i].iOBCU_UnitID_2 = oprmsg.getUInt16 (87+i*5);
				oprdata.asVehicles[i].iVehicle_Status = oprmsg.getUInt8 (89+i*5);
				if (oprdata.asVehicles[i].iOBCU_UnitID_1 < minUnitId) minUnitId = oprdata.asVehicles[i].iOBCU_UnitID_1;
				if (oprdata.asVehicles[i].iOBCU_UnitID_2 < minUnitId) minUnitId = oprdata.asVehicles[i].iOBCU_UnitID_2;
			}

			oprdata.trainId = oprdata.iNumOfVehiclesInTrainConsist*2048 + (minUnitId & 0xfff)/2;
			
			if ((oprdata.iLocIsSecure == 1) && (oprdata.iLocIntegrityIsOk == 1))
			{
				oprs.add(oprdata);
			}
		}
	}

}
