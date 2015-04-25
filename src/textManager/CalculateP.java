/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 计算各种概率
 * 
 * @author hp
 *
 */
public class CalculateP {
	private ArrayList<ArrayList<Integer>> countOfWordsDifCate;// 特征 类别
	private HashMap<String, Integer> featureCode;

	private ArrayList<ArrayList<Double>> pOfWordInDifCate;// P(f|c)
	private ArrayList<Double> pOfACate;// P(c)
	private int sampleSize;
	private ArrayList<Integer> diffCateNum;

	public CalculateP(TrainSet trainSet, ArrayList<String> features) {
		countOfWordsDifCate = trainSet.getCountOfWordsDifCate();
		featureCode = trainSet.getFeatureCode();
		sampleSize = trainSet.getTestSet().size();
		diffCateNum = trainSet.getDiffCateNum();
		calcPfc(features);
		calcPc();
	}

	/**
	 * 计算所有特征在所有类别中的概率P(f|c)，形成矩阵,必须在预测评论之前执行
	 * 
	 * @param features
	 */
	private void calcPfc(ArrayList<String> features) {
		int c = countOfWordsDifCate.size();// 类别数
		int n = features.size();// 特征数
		pOfWordInDifCate = new ArrayList<ArrayList<Double>>(c);
		for (int i = 0; i < c; i++) {
			ArrayList<Double> resList = new ArrayList<Double>(n);
			for (String string : features) {
				int index = featureCode.get(string);
				ArrayList<Integer> tmpCounts = countOfWordsDifCate.get(i);
				double p = (double) (tmpCounts.get(index) + 1)
						/ (tmpCounts.get(tmpCounts.size() - 1) + 2);
				resList.add(p);
			}
			pOfWordInDifCate.add(resList);
		}
	}

	private void calcPc() {
		int c = countOfWordsDifCate.size();
		pOfACate = new ArrayList<Double>(c);
		for (int i = 0; i < c; i++) {
			double p = (double) (diffCateNum.get(i) + 1) / (sampleSize + c);
			pOfACate.add(p);
		}
	}

	/**
	 * @return the pOfWordInDifCate
	 */
	public ArrayList<ArrayList<Double>> getpOfWordInDifCate() {
		return pOfWordInDifCate;
	}

	/**
	 * @return the pOfACate
	 */
	public ArrayList<Double> getpOfACate() {
		return pOfACate;
	}
}
