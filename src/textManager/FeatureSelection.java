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
 * 特征选择
 * 
 * @author hp
 *
 */
public class FeatureSelection {
	private ArrayList<Integer> featureCount;// 该特征在所有文档中的出现次数
	private ArrayList<Integer> diffCateNum;// 不同类别的文本个数
	private ArrayList<ArrayList<Integer>> countOfWordsDifCate;// 词在不同类别中的计数
	private int trainSize;

	private List<Map.Entry<String, Integer>> features;

	public FeatureSelection(TrainSet trainSet,CountNum countNum) {
		featureCount=countNum.getFeatureCount();
		diffCateNum=countNum.getDiffCateNum();
		countOfWordsDifCate=countNum.getCountOfWordsDifCate();
		trainSize=trainSet.getAllTrainSet().size();
	}
	
	public ArrayList<String> IGSelection(ArrayList<String> features,
			int afterSize) {
		int beforeSize = features.size();
		Map<String, Double> featureIG = new HashMap<String, Double>(beforeSize);// 不同特征的IG值
		for (int i = 0; i < features.size(); i++) {
			double Hcf = 0;
			double sum = 0;
			for (int k = 0; k < 2; k++) {
				int Nf = featureCount.get(i);
				if (k == 1)
					Nf = 1 - Nf;
				double Pf = (double) Nf / trainSize;
				for (int j = 0; j < countOfWordsDifCate.size(); j++) {// 不同类别
					int Nc = diffCateNum.get(j);
					double Pcf = (double) Nc / Nf;
					sum += Pcf * (Math.log(Pcf + Double.MIN_VALUE));
				}
				Hcf += Pf * sum;
			}
			featureIG.put(features.get(i), Hcf);
		}
		// 排序，value值大的排在前面
		List<Map.Entry<String, Double>> sortedIG = new ArrayList<Map.Entry<String, Double>>(
				featureIG.entrySet());
		Collections.sort(sortedIG, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Map.Entry<String, Double> firstMapEntry,
					Map.Entry<String, Double> secondMapEntry) {
				double res = secondMapEntry.getValue()
						- firstMapEntry.getValue();
				if (res > 0)
					return 1;
				else if (res < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		ArrayList<String> afterFeatures = new ArrayList<String>(afterSize);
		for (int i = 0; i < afterSize; i++) {
			afterFeatures.add(sortedIG.get(i).getKey());
		}
		return afterFeatures;
	}

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
						return firstMapEntry.getValue()
								- secondMapEntry.getValue();
					}
				});
		System.out.println("after sort\n" + features.toString());
	}

	/**
	 * 移除频率最高的前n个词
	 * 
	 * @param k
	 */
	public void delTopK(int k) {
		Logger logger = Logger.getLogger("删除高频词");
		for (int i = 0; i < k; i++) {
			int last = features.size() - 1;
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

	public ArrayList<String> getFeatureString() {
		ArrayList<String> arrayList = new ArrayList<String>(features.size());
		for (int i = 0; i < features.size(); i++) {
			arrayList.add(features.get(i).getKey());
		}
		return arrayList;
	}

	/**
	 * @param features
	 *            the features to set
	 */
	public void setFeatures(List<Map.Entry<String, Integer>> features) {
		this.features = features;
	}
}
