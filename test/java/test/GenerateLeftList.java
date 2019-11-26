package test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GenerateLeftList<T> {
	
	private List<T> input = null;
	private List<List<Integer>> numList  = null;
	private int num = 0;
	
	public GenerateLeftList(List<T> input) {
		this.num = 0;
		this.input = new ArrayList<>(input);
		this.numList = new ArrayList<>();
		for (int i = 1; i < (1<<input.size()); i++) {
			this.numList.add(getPos(i));
		}
		Collections.sort(this.numList, new Comparator<List<Integer>>() {
			@Override
			public int compare(List<Integer> o1, List<Integer> o2) {
				if (o1.size() != o2.size()) return o1.size() - o2.size();
				else {
					for (int i = 0; i < o1.size(); i ++) {
						if (o1.get(i) != o2.get(i)) {
							return o1.get(i) - o2.get(i);
						}
					}
					return 0;
				}
			}
		});
	}

	private List<Integer> getPos(Integer x) {
		List<Integer> pos = new ArrayList<>();
		for (int i = 0; i < input.size(); i ++) {
			if ((x&(1 << i)) != 0) {
				pos.add(i);
			}
		}
		return pos;
	}
	
	public List<T> get() {
		List<T> res = new ArrayList<>();
		numList.get(num).forEach(i -> {
			res.add(input.get(i));
		});
		num = num + 1;
		return res;
	}
	
	public static void main(String args[]) {
		List<String> input = new ArrayList<>();
		input.addAll(Arrays.asList("1", "2", "3", "4", "5"));
		GenerateLeftList<String> create = new GenerateLeftList<String>(input);
		for (int i = 1; i < (1<<input.size()); i ++) {
			List<String> output = create.get();
			System.out.println(output);
		}
	}
}
