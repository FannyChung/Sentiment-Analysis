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

import utils.MyLogger;
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
	private List<Map.Entry<String, Integer>> features;

	public ArrayList<String> IGSelection(ArrayList<String> features,
			int afterSize, CountNum countNum) {
		ArrayList<Integer> featureCount = countNum.getFeatureCount();// 该特征在所有文档中的出现次数
		ArrayList<Integer> cateCount = countNum.getDiffCateNum();// 不同类别的文本个数
		ArrayList<ArrayList<Integer>> countOfWordsDifCate = countNum
				.getCountOfWordsDifCate();//不同类别下不同特征的出现次数
		int totalSize = countNum.getTotalSize();

		int beforeSize = features.size();
		Map<String, Double> featureIG = new HashMap<String, Double>(beforeSize);// 不同特征的IG值

		for (int i = 0; i < features.size(); i++) { 	
			double Hcf = 0;
			double sum = 0;
			int Nf = featureCount.get(i);
			for (int k = 0; k < 2; k++) {
				if (k == 1)
					Nf = totalSize - Nf;// N_f
				double Pf = (double) Nf / totalSize;
				for (int j = 0; j < countOfWordsDifCate.size(); j++) {// 不同类别
					int Ncf = countOfWordsDifCate.get(j).get(i);// 该类别下游多少个包含该特征的文档Nc,f
					if (k == 1)
						Ncf = cateCount.get(j) - Ncf;// Nc,_f
					double Pcf = (double) Ncf / Nf;
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
		MyLogger logger = new MyLogger("特征IG.txt");
		for (int i = 0; i < afterSize; i++) {
			Entry<String, Double> entry = sortedIG.get(i);
			afterFeatures.add(entry.getKey());
			logger.info(entry + "\r\n");
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
		MyLogger logger = new MyLogger("删除高频词.txt");
		for (int i = 0; i < k; i++) {
			int last = features.size() - 1;
			logger.info(features.get(last).toString()+"\r\n");
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
		MyLogger logger = new MyLogger("删除很少出现的词.txt");
		while (features.get(i).getValue() <= num) {
			logger.info(features.get(i).toString()+"\r\n");
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
		System.out.println("del后总词数：" + k);
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
