/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**分离训练集和测试集，并计数
 * @author hp
 *
 */
public class TrainSet {
	private ArrayList<Integer> cateCount;
	private ArrayList<AnalReview> testSet = new ArrayList<AnalReview>();
	private ArrayList<ArrayList<AnalReview>> diffCateTrainSet;
	
	private ArrayList<ArrayList<Integer>> countOfWordsDifCate;//词在不同类别中的计数
	private HashMap<String, Integer> featureCode;//对特征进行编码

	/**
	 * 统计每个类别的个数和总的个数
	 * 
	 * @param a
	 * @param reviews
	 */
	public void calCategory(int a[], ArrayList<AnalReview> reviews) {
		int n = a.length;
		cateCount = new ArrayList<Integer>(n + 1);// 最后一项代表总的个数
		for (int i = 0; i < n + 1; i++) {
			cateCount.add(0);
		}
		for (AnalReview analReview : reviews) {
			int level = analReview.getLevel();
			for (int i = 0; i < n; i++) {
				if (level == a[i]) {
					cateCount.set(i, cateCount.get(i) + 1);
					cateCount.set(n, cateCount.get(n) + 1);
					break;
				}
			}
		}
	}

	/**
	 * @param a 要选择的类别对应的星级
	 * @param numOfEach 每个类别的个数
	 * @param reviews 从给定的评论集合选择
	 */
	public void seleTrain(int a[], int numOfEach, ArrayList<AnalReview> reviews) {
		int n = a.length;
		Map<Integer, Integer> cateLevel2cateNum = new HashMap<Integer, Integer>(
				n);

		diffCateTrainSet = new ArrayList<ArrayList<AnalReview>>(n);
		for (int i = 0; i < n; i++) {
			cateLevel2cateNum.put(a[i], i);
			diffCateTrainSet.add(new ArrayList<AnalReview>(numOfEach));
		}
		for (AnalReview analReview : reviews) {
			int level = analReview.getLevel();
			int index = 0;
			if (cateLevel2cateNum.containsKey(level)) {
				index = cateLevel2cateNum.get(level);
				if (diffCateTrainSet.get(index).size() < numOfEach) {
					diffCateTrainSet.get(index).add(analReview);
				} else {
					testSet.add(analReview);
				}
			} else {
				testSet.add(analReview);
			}
		}
	}

	/** 计算给定类别的评论集合中，包含该特征词的文本数量
	 * @param feature 特征词
	 * @param reviewsOfACate 给定的同一个类别的评论集合
	 * @return
	 */
	public int calcNumOfWordInCate(String feature,
			ArrayList<AnalReview> reviewsOfACate) {
		int n = 0;
		for (AnalReview analReview : reviewsOfACate) {
//			n += (analReview.getFrequency().getOrDefault(feature, 0));
			if(analReview.getFrequency().containsKey(feature))
				n++;
		}
		return n;
	}

	/**统计不同类别中，所有词出现的词数
	 * @param features 所有特征词
	 */
	public void countAll(ArrayList<String> features) {
		int n = diffCateTrainSet.size();
		countOfWordsDifCate = new ArrayList<ArrayList<Integer>>(n);
		for (ArrayList<AnalReview> arrayList : diffCateTrainSet) {
			ArrayList<Integer> wordOfAcate = new ArrayList<Integer>(
					features.size() + 1);
			int sum = 0;
			for (String string : features) {
				int count = calcNumOfWordInCate(string, arrayList);
				wordOfAcate.add(count);
				sum += count;
			}
			wordOfAcate.add(sum);
			countOfWordsDifCate.add(wordOfAcate);
		}
	}

	/**将所有的特征词在不同类别出现的次数写到表单中
	 * @param sheet 要写入的表单
	 * @param features 所有特征词
	 * @throws RowsExceededException
	 * @throws WriteException
	 */
	public void writeCount(WritableSheet sheet, ArrayList<String> features)
			throws RowsExceededException, WriteException {
		int i = 0;
		Label label;
		for (String string : features) {
			label = new Label(0, i, string);
			sheet.addCell(label);
			i++;
		}
		i = 1;
		for (ArrayList<Integer> arrayList : countOfWordsDifCate) {
			int j = 0;
			for (Integer integer : arrayList) {
				label = new Label(i, j, integer.toString());
				sheet.addCell(label);

				label = new Label(
						i + 5,
						j,
						((double) integer / arrayList.get(arrayList.size() - 1))
								+ "");
				sheet.addCell(label);
				j++;
			}
			i++;
		}
	}

	/**为所有的特征词编码
	 * @param features
	 */
	public void makefeatureCode(ArrayList<String> features) {
		featureCode = new HashMap<String, Integer>(features.size());
		int i = 0;
		for (String string : features) {
			featureCode.put(string, i);
			i++;
		}
	}

//	private double calcP(String feature, int cate, ArrayList<String> features) {
//		int index = featureCode.get(feature);
//		ArrayList<Integer> tmpCounts = countOfWordsDifCate.get(cate);
//		double p = (double) (tmpCounts.get(index) + 1)
//				/ (tmpCounts.get(tmpCounts.size() - 1) + features.size());
//		return p;
//	}

	// 
	
	//
	// // 生成不重复的随机数，用来选择训练集
	// private Set<Integer> geneRadomSeq(int totalSize) {
	// Set<Integer> list = new HashSet<Integer>(totalSize);
	// Random rand = new Random();
	// int k = 0;
	// while (k < totalSize) {
	// int i = rand.nextInt(totalSize);
	// if (list.add(i))
	// k++;
	// }
	// return list;
	// }

	/**
	 * @return the diffCateTrainSet 不同类别的训练集
	 */
	public ArrayList<ArrayList<AnalReview>> getDiffCateTrainSet() {
		return diffCateTrainSet;
	}

	/**
	 * @return the testSet 测试集
	 */
	public ArrayList<AnalReview> getTestSet() {
		return testSet;
	}

	/**
	 * @param testSet
	 *            要设置的测试集
	 */
	public void setTestSet(ArrayList<AnalReview> testSet) {
		this.testSet = testSet;
	}
	/**
	 * @return the countOfWordsDifCate
	 */
	public ArrayList<ArrayList<Integer>> getCountOfWordsDifCate() {
		return countOfWordsDifCate;
	}

	/**
	 * @return the featureCode
	 */
	public HashMap<String, Integer> getFeatureCode() {
		return featureCode;
	}
}
