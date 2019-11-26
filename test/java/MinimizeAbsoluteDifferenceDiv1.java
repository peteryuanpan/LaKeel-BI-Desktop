
public class MinimizeAbsoluteDifferenceDiv1 {
	
	double minv;
	int[] ans;
	int[] src;
	
	double calculate(int[] a) {
		return Math.abs((1.0 * src[a[0]] / src[a[1]]) - (1.0 * src[a[2]] / src[a[3]]));
	}
	
	int compare(int[] a, int[] b) {
		for (int i = 0; i < 4; i ++) {
			if (a[i] < b[i]) return -1;
			if (a[i] > b[i]) return 1;
		}
		return 0;
	}
	
	void AA(int s, int[] a) {
		double v = calculate(a);
		if (v < minv ||
				(Math.abs(v - minv) < 1e-9 && compare(a, ans) < 0)) {
			minv = v;
			for (int i = 0; i < 4; i ++) {
				ans[i] = a[i];
			}
		}
		int t;
		for (int i = s; i < 4; i ++) {
			for (int j = i + 1; j < 4; j ++) {
				t = a[i]; a[i] = a[j]; a[j] = t;
				AA(i + 1, a);
				t = a[i]; a[i] = a[j]; a[j] = t;
			}
		}
	}
	
	void CC(int s, int t, int[] a) {
		if (t == a.length) {
			AA(0, a);
			return;
		}
		for (int i = s; i < 5; i ++) {
			a[t] = i;
			CC(i + 1, t + 1, a);
		}
	}
	
	public int[] findTuple(int[] src) {
		this.src = src;
		minv = Double.MAX_VALUE;
		ans = new int[] {0, 0, 0, 0};
		CC(0, 0, new int[] {0, 0, 0, 0});
		return ans;
	}
	
}
