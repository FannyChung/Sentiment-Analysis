/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import utils.AnalReview;
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
	private ArrayList<ArrayList<AnalReview>> kLists;//将所有数据集分为k个部分

	

	/**
	 * 生成一个随机序列
	 * 
	 * @param range
	 *            随机序列中数字的范围
	 * @param size
	 *            随机序列的个数
	 * @return 随机序列
	 */
	private ArrayList<Integer> randomSet(int range, int size) {
		ArrayList<Integer> integers = new ArrayList<Integer>(size);
		Random rand = new Random();
		boolean[] exits = new boolean[range];
		int randInt = 0;
		for (int i = 0; i < size; i++) {
			do {
				randInt = rand.nextInt(range);
			} while (exits[randInt]);
			integers.add(randInt);
			exits[randInt] = true;
		}
		return integers;
	}

	/**
	 * 生成随机序列的评论集合
	 * 
	 * @param reviews
	 *            输入的评论集合
	 * @return 输出的评论集合，顺序是随机的
	 */
	private ArrayList<AnalReview> genRandAnalReviews(
			ArrayList<AnalReview> reviews) {
		int totalSize = reviews.size();
		ArrayList<Integer> integers = randomSet(totalSize, totalSize);// 打乱reviews的排序
		ArrayList<AnalReview> tmpAnalReviews = new ArrayList<AnalReview>(
				totalSize);
		Iterator<Integer> itr = integers.iterator();
		while (itr.hasNext()) {
			Integer i = itr.next();
			tmpAnalReviews.add(reviews.get(i));
		}
		return tmpAnalReviews;
	}
	
	
	public void seleTrain(int k,int b[],ArrayList<AnalReview> reviews){
		kLists=new ArrayList<ArrayList<AnalReview>>(k);
		int numOfEach=reviews.size()/k;
		reviews = genRandAnalReviews(reviews);// 打乱评论的顺序
		int revCount=0;
		for (int i = 0; i < k; i++) {
			ArrayList<AnalReview> tmp=new ArrayList<AnalReview>(numOfEach);
			for (int j = 0; j < numOfEach; j++) {
				tmp.add(reviews.get(revCount));
				revCount++;
			}
			kLists.add(tmp);
		}
	}
	/**
	 * 按百分比随机选择训练集
	 * 
	 * @param b
	 *            预测分类的数组
	 * @param percent
	 *            训练集占所有数据集的百分比
	 * @param reviews
	 *            所有的数据集
	 */
	public void seleTrain(int b[], double percent, ArrayList<AnalReview> reviews) {
		int totalSize = reviews.size();
		int cateNum = b.length;
		int trainSize = (int) (totalSize * percent);
		reviews = genRandAnalReviews(reviews);// 打乱评论的顺序
		System.out.println(reviews.size());
		diffCateTrainSet = new ArrayList<ArrayList<AnalReview>>(cateNum);
		allTrainSet = new ArrayList<AnalReview>(trainSize);

		for (int i = 0; i < cateNum; i++) {
			diffCateTrainSet.add(new ArrayList<AnalReview>());
		}
		int i = 0;
		int currentSize = 0;
		for (; currentSize < trainSize && i < totalSize; i++) {// 前面指定百分比且指定类别的评论加入到训练集
			AnalReview review = reviews.get(i);
			boolean added = false;
			for (int j = 0; j < cateNum; j++) {// 判断属于哪个类别，加入到对应的集合中
				if (review.getLevel() == b[j]) {
					diffCateTrainSet.get(j).add(review);
					allTrainSet.add(review);
					currentSize++;
					added = true;
				}
			}
			if (!added)
				testSet.add(review);
		}

		for (; i < totalSize; i++) {// 后面剩下的加入测试集
			testSet.add(reviews.get(i));
		}
		System.out.println("训练集大小：" + allTrainSet.size());
		System.out.println("测试集大小：" + testSet.size());
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
		reviews = genRandAnalReviews(reviews);// 打乱评论的顺序

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
		System.out.println("训练集大小：" + allTrainSet.size());
		System.out.println("测试集大小：" + testSet.size());
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
	 * @return the allTrainSet 所有训练集（不分类别）
	 */
	public ArrayList<AnalReview> getAllTrainSet() {
		return allTrainSet;
	}
	
	/**
	 * @return the kLists
	 */
	public ArrayList<ArrayList<AnalReview>> getkLists() {
		return kLists;
	}

	/**
	 * @param kLists the kLists to set
	 */
	public void setkLists(ArrayList<ArrayList<AnalReview>> kLists) {
		this.kLists = kLists;
	}
}
