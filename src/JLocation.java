
public class JLocation {

	public int seg;
	public int offs;
	public EnumOri ori;

	JLocation (int seg, int offs, EnumOri ori)
	{
		this.seg = seg;
		this.offs = offs;
		this.ori = ori;
	}

	JLocation (int seg, int offs, int ori)
	{
		this.seg = seg;
		this.offs = offs;
		if (ori == 1) {
			this.ori = EnumOri.ORIPOS;
		}
		if (ori == 2) {
			this.ori = EnumOri.ORINEG;
		}
	}
}
