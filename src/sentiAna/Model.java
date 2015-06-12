/**
 * 
 */
package sentiAna;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import utils.MyLogger;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 统计特征出现的文档数、不同类别的文档数等信息
 * 
 * @author ZhongFang
 *
 */
public class Model {
	/**
	 * 不同类别的训练集
	 */
	private ArrayList<ArrayList<AnalReview>> diffCateDataSet;// 外层是预测类别，内层是评论集合
	/**
	 * 不同类别的文本个数
	 */
	private ArrayList<Integer> diffCateNum;

	/**
	 * 特征在所有文档中的出现次数
	 */
	private ArrayList<Integer> featureCount;
	
	/**
	 * 词在不同类别中的计数
	 */
	private ArrayList<ArrayList<Integer>> countOfWordsDifCate;// 外层是类别，内层是特征词
	
	private ArrayList<ArrayList<Integer>> countOfEmoWordsDifCate;
	/**
	 * 数据集的总大小
	 */
	private int totalSize;
	/**
	 * 特征词及其词频信息
	 */
	private HashMap<String, Integer> frequency;
	/**
	 * 每个特征在各个类中的概率P(f|c)
	 */
	private ArrayList<ArrayList<Double>> pOfWordInDifCate;// 外层是类别，内层是特征
	private ArrayList<ArrayList<Double>> pOfEmoWordInDifCate;
	/**
	 * 每个类的概率P(c)
	 */
	private ArrayList<Double> pOfACate;

	private MyLogger logger = new MyLogger("Pfc.txt");
	private MyLogger logger2 = new MyLogger("不同特征的文本个数.txt");
	private MyLogger logger1 = new MyLogger("不同类别的文本个数.txt");



	/**
	 * 将所有类别的评论按星级分类，统计不同类别的文档数
	 * 
	 * @param reviews
	 *            所有的评论集合
	 * @param predictLevels
	 *            分类结果与原来的评价星级的对应数组
	 */
	public void separateReviewsByLevel(ArrayList<AnalReview> reviews,
			int predictLevels[][]) {
		totalSize = reviews.size();// 数据集合的总大小
		int cateNum = predictLevels.length;// 预测类别的个数，可为3或5
		diffCateDataSet = new ArrayList<ArrayList<AnalReview>>(cateNum);// 不同类别的训练集
		for (int i = 0; i < predictLevels.length; i++) {// 初始化
			diffCateDataSet.add(new ArrayList<AnalReview>());
		}
		for (AnalReview analReview : reviews) {// 根据评论的标记类别和预测类别的对应关系将评论放入不同的diffCateDataSet
			int level = analReview.getLevel();// 标记类别
			for (int i = 0; i < predictLevels.length; i++) {
				for (int j = 0; j < predictLevels[i].length; j++) {
					if (predictLevels[i][j] == level) {// 评论的标记类别属于该预测类别，则把评论放入对应的集合
						diffCateDataSet.get(i).add(analReview);
					}
				}
			}
		}
		// 计算不同类别下的文档个数
		int n = diffCateDataSet.size();// 类别个数
		diffCateNum = new ArrayList<Integer>(n);// 不同类别的文档个数
		for (ArrayList<AnalReview> arrayList : diffCateDataSet) {// size=每个类别中的文档数
			diffCateNum.add(arrayList.size());
		}
		logger1.info(diffCateNum.toString() + "\r\n\r\n\r\n");
	}

	/**
	 * 计算给定类别的评论集合中，包含该特征词的文本数量
	 * 
	 * @param feature
	 *            特征词
	 * @param reviewsOfACate
	 *            给定的同一个类别的评论集合
	 * @return
	 */
	private int calcNumOfWordACate(String feature,
			ArrayList<AnalReview> reviewsOfACate) {
		int n = 0;
		for (AnalReview analReview : reviewsOfACate) {
			if (analReview.getFrequency().containsKey(feature))// 该文档出现了该词，则该类别的特征出现文档数+1
				n++;
		}
		return n;
	}

	/**
	 * 统计训练集的不同类别中，所有词出现的文档数 Nc,f,用二维数组countOfWordsDifCate表示，
	 * 
	 * 向量化之前使用
	 * 
	 * @param features
	 *            所有特征词
	 */
	public void countFeatureInCates(ArrayList<String> features) {
		int n = diffCateDataSet.size();// 类别数
		countOfWordsDifCate = new ArrayList<ArrayList<Integer>>(n);// 词在不同类别中的计数

		// 统计每个特征词在不同类别中出现的次数
		for (ArrayList<AnalReview> arrayList : diffCateDataSet) {// 每个类别的数据集
			ArrayList<Integer> wordOfAcate = new ArrayList<Integer>(
					features.size());// 一个类别中所有特征出现的文档个数
			for (String string : features) {// 每个特征词
				int count = calcNumOfWordACate(string, arrayList);
				wordOfAcate.add(count);
			}
			countOfWordsDifCate.add(wordOfAcate);
		}
		// 统计所有特征出现的总次数
		int f = countOfWordsDifCate.get(0).size();// 特征数
		featureCount = new ArrayList<Integer>(f);
		for (int i = 0; i < f; i++) {
			int sum = 0;
			for (int j = 0; j < n; j++) {
				sum += countOfWordsDifCate.get(j).get(i);
			}
			featureCount.add(sum);
		}
	}

	/**
	 * 统计训练集的不同类别中，所有词出现的文档数 Nc,f,用二维数组countOfWordsDifCate表示；
	 * 计算特征在所有文档中的出现次数，用featureCount表示
	 * 
	 * 文本向量化之后使用
	 */
	public void countFeatureInCates() {
		int cateNum = diffCateDataSet.size();// 类别数
		int featNum = diffCateDataSet.get(0).get(0).getFeatureVector().length;// 特征数
		featureCount = new ArrayList<Integer>(featNum);// 特征在所有文档中的出现次数
		countOfWordsDifCate = new ArrayList<ArrayList<Integer>>(cateNum);// 词在不同类别中的计数
		for (int i = 0; i < cateNum; i++) {
			ArrayList<Integer> countOfWordsAcate = new ArrayList<Integer>(
					featNum);
			for (int j = 0; j < featNum; j++) {
				countOfWordsAcate.add(0);
			}
			countOfWordsDifCate.add(countOfWordsAcate);
		}
		// 统计所有特征出现的总次数
		for (int i = 0; i < featNum; i++) {
			featureCount.add(0);
		}
		int cateIndex = 0;// 类别索引
		for (ArrayList<AnalReview> reviews : diffCateDataSet) {// 同一类别的评论集合
			for (AnalReview analReview : reviews) {// 每个评论
				boolean[] feVec = analReview.getFeatureVector();// 获取该评论的特征向量
				for (int featureIndex = 0; featureIndex < featNum; featureIndex++) {// 对每个特征
					boolean b = feVec[featureIndex];
					if (b) {// 如果对应值为1
						int bef = countOfWordsDifCate.get(cateIndex).get(
								featureIndex);
						countOfWordsDifCate.get(cateIndex).set(featureIndex,
								bef + 1);// 把对应的特征和类的文档计数+1
						bef = featureCount.get(featureIndex);
						featureCount.set(featureIndex, bef + 1);// 把对应的特征的文档计数加1
					}
				}
			}
			cateIndex++;
		}
		
		for (Integer integer : featureCount) {
			logger2.info( integer + "\r\n");
		}
		logger2.info("----------------------------------------------------------\r\n");
	}

	/**
	 * 对指定的评论集合进行分析 统计总的字数、词数、词频
	 * 
	 * @param frequency
	 */
	public void analReviews(ArrayList<AnalReview> reviews) {
		int i = 0;
		int wordSum = 0;
		int charSum = 0;
		frequency = new HashMap<String, Integer>();
		for (AnalReview analReview : reviews) {
			i++;
			wordSum += analReview.getWordsCount();// 统计总的词数
			charSum += analReview.getCharsCount();// 统计总的字数

			HashMap<String, Integer> revFreq = analReview.getFrequency();// 这一条评论的词频信息
			Iterator<Entry<String, Integer>> iter = revFreq.entrySet()
					.iterator();
			while (iter.hasNext()) {
				Entry<String, Integer> entry = (Entry<String, Integer>) iter
						.next();
				String string = entry.getKey();
				Integer sfre = entry.getValue();
				frequency.merge(string, sfre,
						(value, newValue) -> (sfre + value));
			}
		}
		System.out.println("wordsSum" + wordSum);
		System.out.println("charSum" + charSum);
		System.out.println("aveWords" + (double) wordSum / i);
		System.out.println("aveChars" + (double) charSum / i);
	}

	/**
	 * 计算所有特征在所有类别中的概率P(f|c)
	 * 
	 */
	public void calcPfc() {
		int cateSize = countOfWordsDifCate.size();// 类别数
		int featureSize = countOfWordsDifCate.get(0).size();// 特征数
		pOfWordInDifCate = new ArrayList<ArrayList<Double>>(cateSize);// 所有类别下所有特征的P(f|c),二维数组，外面是类别，里面是特征词
		for (int cateIndex = 0; cateIndex < cateSize; cateIndex++) {// 对于每个预测类别
			logger.info("\r\n\r\n" + "c" + cateIndex + ":\r\n");
			ArrayList<Double> resList = new ArrayList<Double>(featureSize);// 同一类别下所有特征的P(f|c)
			ArrayList<Integer> tmpCounts = countOfWordsDifCate.get(cateIndex);// 所有特征出现在指定类别中的文档个数
			int b = diffCateNum.get(cateIndex);// 指定类别的文档数
			for (int featureIndex = 0; featureIndex < featureSize; featureIndex++) {// 每个特征
				int a = tmpCounts.get(featureIndex);// 指定类别中的指定特征的文档数
				double p = (double) (a + 1) / (b + 2);// 指定特征在指定类别下的条件概率
				resList.add(p);
				logger.info(p + "\r\n");
			}
			pOfWordInDifCate.add(resList);
		}
	}

	/**
	 * 计算P(c),一个类别的概率
	 */
	public void calcPc() {
		int c = countOfWordsDifCate.size();// 类别数
		pOfACate = new ArrayList<Double>(c);// 所有类别的概率
		for (int i = 0; i < c; i++) {
			double p = (double) (diffCateNum.get(i) + 1) / (totalSize + c);// 指定类别的概率
			pOfACate.add(p);
		}
	}

	/**
	 * @return the featureCount 不同特征出现的文档个数
	 */
	public ArrayList<Integer> getFeatureCount() {
		return featureCount;
	}

	/**
	 * @return the diffCateNum 不同类别的文档个数
	 */
	public ArrayList<Integer> getDiffCateNum() {
		return diffCateNum;
	}

	/**
	 * @return the countOfWordsDifCate 不同类别下的不同特征的出现次数
	 */
	public ArrayList<ArrayList<Integer>> getCountOfWordsDifCate() {
		return countOfWordsDifCate;
	}

	/**
	 * @return 特征字符串集合
	 */
	public ArrayList<String> getFeatureString() {
		ArrayList<String> result = new ArrayList<String>(frequency.size());
		Iterator<Entry<String, Integer>> itr = frequency.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, Integer> entry = itr.next();
			result.add(entry.getKey());
		}
		return result;
	}

	/**
	 * @return the totalSize 数据集总大小
	 */
	public int getTotalSize() {
		return totalSize;
	}

	/**
	 * @return 词和词频统计信息
	 */
	public HashMap<String, Integer> getFrequency() {
		return frequency;
	}

	/**
	 * @param totalSize
	 *            the totalSize to set
	 */
	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
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
