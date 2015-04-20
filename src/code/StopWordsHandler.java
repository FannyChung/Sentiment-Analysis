package code;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 停用词处理器
 * 
 */

public class StopWordsHandler

{
	static List<Map.Entry<String, Integer>> lists;

	private static void initLists(HashMap<String, Integer> frequency) {
		lists = new ArrayList<Map.Entry<String, Integer>>(frequency.entrySet());
		Collections.sort(lists, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				// return (o2.getValue() - o1.getValue());
				return (o1.getKey()).toString().compareTo(o2.getKey());
			}
		});
	}

	public boolean IsStopWord(String word) {
		for (int i = 0; i < GetWords.stopWordsList.size(); ++i) {
			if (word.equalsIgnoreCase(GetWords.stopWordsList.get(i)))
				return true;
		}
		return false;
	}

	public boolean isHighFreq(String word) {
		int size = lists.size();
		double fir_2per = size * 0.02;
		// 将词频表按降序排序，查看是否属于前2%
		for (int i = 0; i < fir_2per; i++) {
			if (word.equals(lists.get(i))) {
				return true;
			}
		}
		return false;
	}

	public boolean notEmotion(String word) {
		for (int i = 0; i < GetWords.emotionWords.size(); ++i) {
			if (word.equalsIgnoreCase(GetWords.emotionWords.get(i)))
				return false;
		}
		return true;
	}
}