
public class JTvsBoundaryData implements Comparable<JTvsBoundaryData> {

	public int seg;
	public int offs;
	public EnumOri ori;
	public int id;
	
	JTvsBoundaryData (int seg, int offs)
	{
		this.seg = seg;
		this.offs =offs;
		this.ori = EnumOri.ORIPOS;
		this.id = 0;
	}
	
	JTvsBoundaryData (int seg, int offs, EnumOri ori, int id)
	{
		this.seg = seg;
		this.offs =offs;
		this.ori = ori;
		this.id = id;
	}
	
	public int compareTo(JTvsBoundaryData obj)
	{
		if (this.seg == obj.seg) {
			if (this.offs == obj.offs) {
				if (this.ori == obj.ori) {
					return 0;
				}
				else if (this.ori == EnumOri.ORIPOS) {
					return +1;
				}
				else {
					return -1;
				}
			}
			return this.offs - obj.offs;
		}
		return this.seg - obj.seg;
	}
	
	public String toString ()
	{
		return "\n(seg=" + seg + ", offs=" + offs + ", ori=" + ori + ", id=" + id + ")";
	}
	
	public String pr(int lvl)
	{
		String indent = "                ".substring(0, 2*lvl);
		
		return
			String.format("%s<tvsboundary seg=\"%d\" offs=\"%d\" tvsid=\"%d\" ori=\"%d\" dist=\"%d\"/>\n", indent, this.seg, this.offs, (this.ori==EnumOri.ORIPOS ? "pos" : "neg"), this.id);
	}

}
