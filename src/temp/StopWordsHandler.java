package temp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理停用词
 * 
 * @author hp
 *
 */
public class StopWordsHandler

{
	private List<Map.Entry<String, Integer>> lists;

	private void initLists(HashMap<String, Integer> frequency) {
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
		for (int i = 0; i < ReadWords.stopWordsList.size(); ++i) {
			if (word.equalsIgnoreCase(ReadWords.stopWordsList.get(i)))
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
		for (int i = 0; i < ReadWords.emotionWords.size(); ++i) {
			if (word.equalsIgnoreCase(ReadWords.emotionWords.get(i)))
				return false;
		}
		return true;
	}

	/**
	 * @return the lists
	 */
	public List<Map.Entry<String, Integer>> getLists() {
		return lists;
	}
}