/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import utils.MyLogger;

/**
 * 计算各种概率
 * 
 * @author hp
 *
 */
public class CalculateP {
	private ArrayList<ArrayList<Integer>> countOfWordsDifCate;// 特征 类别计数
	private int sampleSize;
	private ArrayList<Integer> diffCateNum;// 不同类别的文本个数,Nc

	private ArrayList<ArrayList<Double>> pOfWordInDifCate;// P(f|c)
	private ArrayList<Double> pOfACate;// P(c)

	public CalculateP(TrainSet trainSet, CountNum countNum) {
		countOfWordsDifCate = countNum.getCountOfWordsDifCate();
		sampleSize = trainSet.getAllTrainSet().size();
		diffCateNum = countNum.getDiffCateNum();
	}

	/**
	 * 计算所有特征在所有类别中的概率P(f|c)，形成矩阵
	 * 
	 * @param features
	 */
	public void calcPfc() {
		int cateSize = countOfWordsDifCate.size();// 类别数
		int featureSize = countOfWordsDifCate.get(0).size();// 特征数
		MyLogger logger = new MyLogger("Pfc.txt");
		pOfWordInDifCate = new ArrayList<ArrayList<Double>>(cateSize);//二维数组，外面是类别，里面是特征词
		for (int cateIndex = 0; cateIndex < cateSize; cateIndex++) {
			logger.info("\r\n\r\n" + "c" + cateIndex + ":\r\n");
			ArrayList<Double> resList = new ArrayList<Double>(featureSize);
			ArrayList<Integer> tmpCounts = countOfWordsDifCate.get(cateIndex);// 所有特征出现在一个类别中的文档个数
			for (int featureIndex = 0; featureIndex < featureSize; featureIndex++) {
				int a = tmpCounts.get(featureIndex);
				int b = diffCateNum.get(cateIndex);
				double p = (double) (a + 1) / (b + 2);
				resList.add(p);
				logger.info(p + "\r\n");
			}
			// logger.info(resList.toString()+"\r\n");
			pOfWordInDifCate.add(resList);
		}
	}

	/**
	 * 计算P(c),一个类别的概率
	 */
	public void calcPc() {
		int c = countOfWordsDifCate.size();// 类别数
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
