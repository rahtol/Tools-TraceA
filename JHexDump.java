
public class JHexDump {

	private String hex;
	
	JHexDump (String hex)
	{
		this.hex = hex;
	}
	
	public int getUInt8 (int idx)
	{
		String b0 = hex.substring (idx*3 + 1, idx*3 + 3);
		return Integer.parseInt(b0, 16);
	}

	public int getUInt16 (int idx)
	{
		String b0 = hex.substring (idx*3 + 1, idx*3 + 3);
		String b1 = hex.substring (idx*3 + 4, idx*3 + 6);
		return Integer.parseInt(b1+b0, 16);
	}

	public int getUInt32 (int idx)
	{
		String b0 = hex.substring (idx*3 + 1, idx*3 + 3);
		String b1 = hex.substring (idx*3 + 4, idx*3 + 6);
		String b2 = hex.substring (idx*3 + 7, idx*3 + 9);
		String b3 = hex.substring (idx*3 + 10, idx*3 + 12);
		return Integer.parseInt(b3+b2+b1+b0, 16);
	}
}
