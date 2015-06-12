/**
 * 
 */
package sentiAna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utils.MyLogger;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 特征类
 * 
 * @author ZhongFang
 *
 */
public class Feature {
	/**
	 * 特征的字符串以及对应的词频组成的集合
	 */
	private List<Map.Entry<String, Integer>> featuresFreq;
	/**
	 * 特征的字符串集合
	 */
	private ArrayList<String> featureStrings;

	/**
	 * 根据现有的字符串更新featuresFreq
	 */
	public void updateFeatFre() {
		List<Map.Entry<String, Integer>> tmp = new ArrayList<Map.Entry<String, Integer>>();
		for (Entry<String, Integer> string2Int : featuresFreq) {
			if (featureStrings.contains(string2Int.getKey()))
				tmp.add(string2Int);
		}
		featuresFreq = tmp;
	}

	/**
	 * 进行信息增益特征选择
	 * 
	 * @param features
	 *            特征集
	 * @param afterSize
	 *            指定的选择后的大小
	 * @param model
	 *            包含特征概率信息的模型
	 * @return 过滤后的特征字符串集合
	 */
	public ArrayList<String> IGSelection(ArrayList<String> features,
			int afterSize, Model model) {
		int beforeSize = features.size();
		if (afterSize >= beforeSize) {// IG选择后的规模应该小于当前规模
			System.err.println("IG选择后的规模应该小于当前规模" + beforeSize);
			return features;
		}
		ArrayList<Integer> featureCount = model.getFeatureCount();// 该特征在所有文档中的出现次数
		ArrayList<Integer> cateCount = model.getDiffCateNum();// 不同类别的文本个数
		ArrayList<ArrayList<Integer>> countOfWordsDifCate = model
				.getCountOfWordsDifCate();// 不同类别下不同特征的出现次数
		int totalSize = model.getTotalSize();// 总共评论个数

		// 计算每个特征的IG
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
					sum += Pcf * (Math.log(Pcf + Double.MIN_VALUE));// 避免出现log(0)
				}
				Hcf += Pf * sum;
			}
			featureIG.put(features.get(i), Hcf);
		}
		// 排序，value值大的排在前面
		List<Map.Entry<String, Double>> sortedIG = new ArrayList<Map.Entry<String, Double>>(
				featureIG.entrySet());
		Comparator<Map.Entry<String, Double>> cp = new Comparator<Map.Entry<String, Double>>() {
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
		};
		Collections.sort(sortedIG, cp);

		// 选择前afterSize个并返回
		ArrayList<String> afterFeatures = new ArrayList<String>(afterSize);
		for (int i = 0; i < afterSize; i++) {
			Entry<String, Double> entry = sortedIG.get(i);
			afterFeatures.add(entry.getKey());
		}
		return afterFeatures;
	}

	/**
	 * 根据停用词过滤，过滤词频是前20%并且在停用词表的词
	 * 
	 * @param stopWords
	 *            停用词表
	 */
	public void removeStopWords(ArrayList<String> stopWords) {// 前20%高频词&&在停用词表里
		int firstSize = (int) (featureStrings.size() * 0.2);
		int totalSize = featureStrings.size();
		MyLogger logger = new MyLogger("删除停用词.txt");
		for (int i = totalSize - 1, c = 0; c < firstSize; c++, i--) {
			if (stopWords.contains(featureStrings.get(i))) {
				logger.info(featuresFreq.get(i).toString() + "\r\n");
				featuresFreq.remove(i);
				featureStrings.remove(i);
			}
		}
	}

	/**
	 * 按词出现的词数把词进行排序
	 * 
	 * @param frequecy
	 *            待排序的词与词频的映射
	 */
	public void sortByFreq(Map<String, Integer> frequecy) {
		featuresFreq = new ArrayList<Map.Entry<String, Integer>>(
				frequecy.entrySet());
		Collections.sort(featuresFreq,
				new Comparator<Map.Entry<String, Integer>>() {
					@Override
					public int compare(
							Map.Entry<String, Integer> firstMapEntry,
							Map.Entry<String, Integer> secondMapEntry) {
						return firstMapEntry.getValue()
								- secondMapEntry.getValue();
					}
				});

		featureStrings = new ArrayList<String>(featuresFreq.size());
		for (int i = 0; i < featuresFreq.size(); i++) {
			featureStrings.add(featuresFreq.get(i).getKey());
		}
	}

	/**
	 * 根据DF过滤，删除文档频率太小的词
	 * 
	 * @param featureCount
	 *            词出现的文档数
	 * @param minDF
	 *            指定的最小文档
	 */
	public void removeByDF(ArrayList<Integer> featureCount, int minDF) {
		MyLogger logger = new MyLogger("删除DF.txt");
		int removeCount = 0;
		for (int i = 0; i < featureCount.size(); i++) {
			if (featureCount.get(i) <= minDF) {
				logger.info(featuresFreq.get(i - removeCount).toString()
						+ "\r\n");
				featuresFreq.remove(i - removeCount);
				featureStrings.remove(i - removeCount);
				removeCount++;
			}
		}
	}

	/**
	 * 移除频率最高的前n个词
	 * 
	 * @param k
	 */
	public void delTopK(int k) {
		MyLogger logger = new MyLogger("删除高频词.txt");
		for (int i = 0; i < k; i++) {
			int last = featuresFreq.size() - 1;
			logger.info(featuresFreq.get(last).toString() + "\r\n");
			featuresFreq.remove(last);
			featureStrings.remove(last);
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
		while (featuresFreq.get(i).getValue() <= num) {
			logger.info(featuresFreq.get(i).toString() + "\r\n");
			featuresFreq.remove(i);
			featureStrings.remove(i);
		}
	}

	/**
	 * 在指定表格中打印词及其词频
	 * 
	 * @param sheet
	 *            指定表格
	 * @throws RowsExceededException
	 *             行错误
	 * @throws WriteException
	 *             写错误
	 */
	public void writeFeature(WritableSheet sheet) throws RowsExceededException,
			WriteException {
		Label label;
		int k = 0;
		for (int i = 0; i < featuresFreq.size(); i++) {
			Entry<String, Integer> entry = featuresFreq.get(i);
			String string = entry.getKey();
			Integer sfre = entry.getValue();
			label = new Label(0, k, string);
			sheet.addCell(label);
			label = new Label(1, k, sfre.toString());
			sheet.addCell(label);
			k++;
		}
	}

	/**
	 * @return 词及其词频映射集合
	 */
	public List<Map.Entry<String, Integer>> getFeaturesFreq() {
		return featuresFreq;
	}

	/**
	 * @return 特征的字符串集合
	 */
	public ArrayList<String> getFeatureStrings() {
		return featureStrings;
	}

	/**
	 * 设置特征的字符串集合
	 * 
	 * @param featureStrings
	 *            特征的字符串集合
	 */
	public void setFeatureStrings(ArrayList<String> featureStrings) {
		this.featureStrings = featureStrings;
	}
}
