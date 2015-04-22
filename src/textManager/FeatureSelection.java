/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * @author hp
 *
 */
public class FeatureSelection {

	private List<Map.Entry<String, Integer>> features;

	/**
	 * 按词出现的词数把词进行排序
	 * 
	 * @param frequecy
	 */
	public void sortByFreq(Map<String, Integer> frequecy) {
		features = new ArrayList<Map.Entry<String, Integer>>(
				frequecy.entrySet());
		Collections.sort(features,
				new Comparator<Map.Entry<String, Integer>>() {
					@Override
					public int compare(
							Map.Entry<String, Integer> firstMapEntry,
							Map.Entry<String, Integer> secondMapEntry) {
						return firstMapEntry.getValue()-
								secondMapEntry.getValue();
					}
				});
		System.out.println("after sort\n"+features.toString());
	}

	/**
	 * 移除频率最高的前n个词
	 * 
	 * @param k
	 */
	public void delTopK(int k) {
		Logger logger = Logger.getLogger("删除高频词");
		for (int i =0; i < k; i++) {
			int last=features.size()-1;
			logger.info(features.get(last).toString());
			features.remove(last);
		}
	}

	/**
	 * 删除出现次数<=num的词
	 * 
	 * @param num
	 *            词出现的词数
	 */
	public void delLessThan(int num) {
		int i = 0;
		Logger logger = Logger.getLogger("删除出现很少的词");
		while (features.get(i).getValue() <= num) {
			logger.info(features.get(i).toString());
			features.remove(i);
		}
	}

	public void writeFeature(WritableSheet sheet) throws RowsExceededException,
			WriteException {
		Label label;
		int k = 0;
		for (int i = 0; i < features.size(); i++) {
			Entry<String, Integer> entry = features.get(i);
			String string = entry.getKey();
			Integer sfre = entry.getValue();
			label = new Label(0, k, string);
			sheet.addCell(label);
			label = new Label(1, k, sfre.toString());
			sheet.addCell(label);
			k++;
		}
		System.out.println(features);
		System.out.println("总词数：" + k);
	}
	public List<Map.Entry<String, Integer>> getFeatures() {
		return features;
	}
}
