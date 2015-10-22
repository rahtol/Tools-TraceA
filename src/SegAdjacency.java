
public class SegAdjacency implements Comparable<SegAdjacency> {
	
	final public static int UP = 0;
	final public static int DOWN = 1;
	final public static int RIGHT = 0;
	final public static int LEFT = 1;
	
	public int srcSegId;
	public int ud;
	public int rl;
	public int adjSegId;

	public SegAdjacency (int srcSegId, int ud, int rl, int adjSegId)
	{
		this.srcSegId = srcSegId;
		this.ud = ud;
		this.rl = rl;
		this.adjSegId = adjSegId;
	}
	
	@Override
	public int compareTo(SegAdjacency o) {
		if (srcSegId == o.srcSegId)
		{
			if (ud == o.ud)
			{
				return rl - o.rl;
			}
			return ud - o.ud;
		}
		return srcSegId - o.srcSegId;
	}

}
