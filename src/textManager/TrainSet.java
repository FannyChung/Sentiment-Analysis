/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 分离训练集和测试集
 * 
 * @author hp
 *
 */
public class TrainSet {
	private ArrayList<AnalReview> testSet = new ArrayList<AnalReview>();// 测试集
	private ArrayList<ArrayList<AnalReview>> diffCateTrainSet;// 不同类别的训练集
	private ArrayList<AnalReview> allTrainSet;

	/**
	 * 按百分比随机选择训练集
	 * 
	 * @param a
	 *            对应分类的数组
	 * @param percent
	 *            训练集占所有数据集的百分比
	 * @param reviews
	 *            所有的数据集
	 */
	public void seleTrain(int a[], double percent, ArrayList<AnalReview> reviews) {
		int totalSize = reviews.size();
		int cateNum = a.length;
		int trainSize = (int) (totalSize * percent);
		Set<AnalReview> trainSetTmp = new HashSet<AnalReview>();
		diffCateTrainSet = new ArrayList<ArrayList<AnalReview>>(cateNum);
		for (int i = 0; i < a.length; i++) {
			diffCateTrainSet.add(new ArrayList<AnalReview>());
		}
		// TODO 使得trainSize<所有符合类别的评论的个数
		if(trainSize>reviews.size()){
			System.err.println("请使得训练集更小！");
			System.exit(0);
		}

		Random rand = new Random();
		for (int i = 0; i < trainSize;) {
			int index = rand.nextInt(totalSize);
			AnalReview review = reviews.get(index);
			for (int j = 0; j < a.length; j++) {
				if (review.getLevel() == a[j]) {
					if (trainSetTmp.add(review)) {
						i++;
						diffCateTrainSet.get(j).add(review);
					}
				}
			}
		}
		allTrainSet = new ArrayList<AnalReview>(trainSize);
		allTrainSet.addAll(trainSetTmp);

		for (AnalReview analReview : reviews) {
			if (!trainSetTmp.contains(analReview))
				testSet.add(analReview);
		}
		System.out.println("测试集大小：" + allTrainSet.size());
		System.out.println("训练集大小：" + testSet.size());
	}

	/**
	 * @param a
	 *            要选择的类别对应的星级
	 * @param numOfEach
	 *            每个类别的个数
	 * @param reviews
	 *            从给定的评论集合选择
	 */
	public void seleTrain(int a[], int numOfEach, ArrayList<AnalReview> reviews) {
		int n = a.length;
		Map<Integer, Integer> cateLevel2cateNum = new HashMap<Integer, Integer>(
				n);
		allTrainSet = new ArrayList<AnalReview>(numOfEach * n);
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
					allTrainSet.add(analReview);
				} else {
					testSet.add(analReview);
				}
			} else {
				testSet.add(analReview);
			}
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

	/**
	 * @return the allTrainSet
	 */
	public ArrayList<AnalReview> getAllTrainSet() {
		return allTrainSet;
	}
}
