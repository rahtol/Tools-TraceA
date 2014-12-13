
public class JMeasurement {

		public JOprData opr0;
		public JOprData opr1;
		public JTvsBoundaryData tvsBoundary;
		public JTvsChangeData tvsChange;
		
		private int speed;
		private int uncertainty;
		private int overhang;
		private JLocation boundaryLocation;
		private int tInterpolated;
		private int dtUncertainty;
		private int dtOverhang;
		private int dtCorrection;
		private int dtNotCorrected;
		private int dtCorrected;
		
		
		JMeasurement (JOprData opr0, JOprData opr1, JTvsBoundaryData tvsBoundary, JTvsChangeData tvsChange)
		{
			this.opr0 = opr0;
			this.opr1 = opr1;
			this.tvsBoundary = tvsBoundary;
			this.tvsChange = tvsChange;
			
			tvsChange.usageCount++;
			
			speed = (this.opr0.iTrainSpeed + this.opr1.iTrainSpeed) / 2;
			uncertainty = (this.opr0.iLocUncertainty + this.opr1.iLocUncertainty) / 2;
			overhang = 200;
			boundaryLocation = new JLocation(this.tvsBoundary.seg, this.tvsBoundary.offs, this.tvsBoundary.ori);
			
			// v = s/t => t = s / v
			dtUncertainty = (500 * uncertainty) / speed;  // only half of uncertainty counts on each extremity
			dtOverhang = (1000 * overhang) / speed;
			dtCorrection = dtUncertainty + dtOverhang;
			
			if (tvsChange.state == JTvsChangeData.CLEAR) {
				tInterpolated = interpolate (opr0.t, opr1.t, opr0.xr, opr1.xr, boundaryLocation, speed);
				dtNotCorrected = tvsChange.t - tInterpolated;
				dtCorrected = dtNotCorrected + dtCorrection;
			}
			else {
				tInterpolated = interpolate (opr0.t, opr1.t, opr0.xf, opr1.xf, boundaryLocation, speed);
				dtNotCorrected = tvsChange.t - tInterpolated;
				dtCorrected = dtNotCorrected - dtCorrection;
			}
		}
		
		private int interpolate(int t0, int t1, JLocation x0, JLocation x1, JLocation x, int v)
		{
			int t = t0;
			
			if (x0.seg == x.seg) {
				int dx = Math.abs(x0.offs - x.offs);
				int dt = (1000 * dx) / v;
				t = t0 + dt;
			}
			else if (x1.seg == x.seg) {
				int dx = Math.abs(x1.offs - x.offs);
				int dt = (1000 * dx) / v;
				t = t1 - dt;
			}
			
			return t;
		}
		
		public String stateToStr (int state)
		{
			if (state == 1) {
				return "CLEAR";
			}
			else if (state == 2) {
				return "OCCUPIED";
			}
			else {
				return "???";
			}
		}
		
		public String toString ()
		{
			return "\n(TVS id=" + tvsChange.id + ", transistion to " + stateToStr (tvsChange.state) + ", delay=" + dtCorrected + "ms, correction=" + dtCorrection + "ms)";
		}
		
}
