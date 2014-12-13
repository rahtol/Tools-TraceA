
public enum EnumOri {
	ORIPOS(0),
	ORINEG(1);

    private final int ori;
    
	EnumOri (int ori)
	{
		this.ori = ori;
	}
	
	private static EnumOri fromInt (int ori)
	{
		if(ori==1) {
			return ORINEG;
		} else {
			return ORIPOS;
		}
	}

	public static EnumOri fromSegmentDirection (int segdir)
	{
		if(segdir==2) {
			return ORINEG;
		} else { // segdir == 1
			return ORIPOS;
		}
	}
	
	public EnumOri invert ()
	{
		EnumOri rc;
		if(ori==1) {
			rc = ORIPOS;
		} else {
			rc = ORINEG;
		}
		
		return rc;
	}
	
	public EnumOri add (EnumOri ori2)
	{
		return EnumOri.fromInt ((this.ori + ori2.ori) % 2);
	}
}

