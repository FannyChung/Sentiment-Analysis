/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * @author hp
 *
 */
public class TrainSet {
	private ArrayList<Integer> cateCount;
	private ArrayList<AnalReview> testSet = new ArrayList<AnalReview>();
	private ArrayList<ArrayList<AnalReview>> diffCateTrainSet;
	private ArrayList<ArrayList<Integer>> countOfWordsDifCate;
	private HashMap<String, Integer> featureCode;

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

	public void seleTrain(int a[], int numOfEach, ArrayList<AnalReview> reviews) {
		int n = a.length;
		ArrayList<Integer> countEach = new ArrayList<Integer>(n);// 每个类别已经获取的个数
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
				if (diffCateTrainSet.get(index).size() < numOfEach)
					diffCateTrainSet.get(index).add(analReview);
				else {
					testSet.add(analReview);
				}
			} else {
				testSet.add(analReview);
			}
		}
	}

	// 计算给定类别的评论集合中，该词的出现次数
	public int calcNumOfWordInCate(String feature,
			ArrayList<AnalReview> reviewsOfACate) {
		int n = 0;
		for (AnalReview analReview : reviewsOfACate) {
			n += (analReview.getFrequency().getOrDefault(feature, 0));
		}
		return n;
	}

	// 统计不同类别中，所有词出现的词数
	public void calcAll(ArrayList<String> features) {
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
				
				label=new Label(i+5,j,((double)integer/arrayList.get(arrayList.size()-1))+"");
				sheet.addCell(label);
				j++;
			}
			i++;
		}
	}

	public void makefeatureCode(ArrayList<String> features) {
		featureCode = new HashMap<String, Integer>(features.size());
		int i = 0;
		for (String string : features) {
			featureCode.put(string, i);
			i++;
		}
	}

	public double calcP(String feature, int cate, ArrayList<String> features) {
		int index = featureCode.get(feature);
		ArrayList<Integer> tmpCounts = countOfWordsDifCate.get(cate);
		double p = (double) (tmpCounts.get(index) + 1)
				/ (tmpCounts.get(tmpCounts.size() - 1) + features.size());
		return p;
	}

	public int predict(AnalReview review, ArrayList<String> features) {
		int n = diffCateTrainSet.size();
		int resultIndex = 0;
		double tmpP = 0;
		for (int i = 0; i < n; i++) {
			double p = 1;
			HashMap<String, Integer> reviewWords = review.getFrequency();
			for (String string : features) {
				if (reviewWords.containsKey(string)) {
					p *= calcP(string, i, features);
				} else {
					p *= (1 - calcP(string, i, features));
				}
			}
			if (tmpP < p) {
				tmpP = p;
				resultIndex = i;
			}
		}
		return resultIndex;
	}

	public ArrayList<Integer> predictRevs(ArrayList<AnalReview> reviews,
			ArrayList<String> features) {
		ArrayList<Integer> results = new ArrayList<Integer>();
		for (AnalReview analReview : reviews) {
			results.add(predict(analReview, features));
		}
		return results;
	}

	public void writePredResult(ArrayList<AnalReview> reviews,
			WritableSheet sheet, ArrayList<String> features, int a[])
			throws RowsExceededException, WriteException {
		int i = 0;
		Label label;
		for (AnalReview analReview : reviews) {
			label = new Label(0, i, analReview.getText());
			sheet.addCell(label);

			int oriLevel = analReview.getLevel();
			label = new Label(1, i, oriLevel + "");
			sheet.addCell(label);

			int predLevel = a[predict(analReview, features)];
			label = new Label(2, i, predLevel + "");
			sheet.addCell(label);

			int diff = Math.abs(oriLevel - predLevel);
			label = new Label(3, i, diff + "");
			sheet.addCell(label);

			i++;
		}
	}

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
}
