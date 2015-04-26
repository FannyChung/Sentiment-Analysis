/**
 * 
 */
package textManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 统计训练集的特征出现的文档数、不同类别的文档数
 * 
 * @author hp
 *
 */
public class CountNum {
	private ArrayList<ArrayList<AnalReview>> diffCateTrainSet;// 不同类别的训练集

	private ArrayList<Integer> diffCateNum;// 不同类别的文本个数
	private ArrayList<Integer> featureCount;// 该特征在所有文档中的出现次数
	private ArrayList<ArrayList<Integer>> countOfWordsDifCate;// 词在不同类别中的计数
	/**
	 * 每个词与它的出现次数
	 */
	private HashMap<String, Integer> frequency = new HashMap<String, Integer>();

	public CountNum(TrainSet trainSet) {
		diffCateTrainSet=trainSet.getDiffCateTrainSet();
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
	public int calcNumOfWordInCate(String feature,
			ArrayList<AnalReview> reviewsOfACate) {
		int n = 0;
		for (AnalReview analReview : reviewsOfACate) {
			// n += (analReview.getFrequency().getOrDefault(feature, 0));
			if (analReview.getFrequency().containsKey(feature))// 该文档出现了该词，则该类别的特征出现文档数+1
				n++;
		}
		return n;
	}

	/**
	 * 统计训练集的不同类别中，所有词出现的文档数 Nc,f
	 * 
	 * @param features
	 *            所有特征词
	 */
	public void countAll(ArrayList<String> features) {
		int n = diffCateTrainSet.size();// 类别数
		countOfWordsDifCate = new ArrayList<ArrayList<Integer>>(n);
		for (ArrayList<AnalReview> arrayList : diffCateTrainSet) {
			ArrayList<Integer> wordOfAcate = new ArrayList<Integer>(
					features.size());
			for (String string : features) {
				int count = calcNumOfWordInCate(string, arrayList);
				wordOfAcate.add(count);
			}
			countOfWordsDifCate.add(wordOfAcate);
		}
	}

	/**
	 * 将所有的特征词在不同类别出现的次数写到表单中
	 * 
	 * @param sheet
	 *            要写入的表单
	 * @param features
	 *            所有特征词
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
				j++;
			}
			i++;
		}
	}
	
	private Integer add(Integer value, Integer addNum) {
		return value + addNum;
	}
	
	/**
	 * 对指定的评论集合进行分析 统计总的字数、词数、词频
	 * 
	 * @param frequency
	 */
	public void analAll(HashMap<String, Integer> frequency,ArrayList<AnalReview> reviews) {
		int i = 0;
		int wordSum = 0;
		int charSum = 0;
		for (AnalReview analReview : reviews) {
			i++;
			wordSum += analReview.getWordsCount();// 统计总的词数
			charSum += analReview.getCharsCount();// 统计总的字数

			HashMap<String, Integer> revFreq = analReview.getFrequency();//这一条评论的词频信息
			Iterator<Entry<String, Integer>> iter = revFreq.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String,Integer> entry = (Entry<String,Integer>) iter.next();
				String string =  entry.getKey();
				Integer sfre =  entry.getValue();
				frequency.merge(string, sfre,
						(value, newValue) -> add(value, sfre));
			}
		}
		System.out.println("wordsSum" + wordSum);
		System.out.println("charSum" + charSum);
		System.out.println("aveWords" + (double) wordSum / i);
		System.out.println("aveChars" + (double) charSum / i);
		System.out.println(frequency);
	}
	

	/**
	 * @return the featureCount
	 */
	public ArrayList<Integer> getFeatureCount() {
		int n = countOfWordsDifCate.get(0).size();// 特征数
		int c = countOfWordsDifCate.size();// 类别数
		featureCount = new ArrayList<Integer>(n);
		for (int i = 0; i < n; i++) {
			int sum = 0;
			for (int j = 0; j < c; j++) {
				sum += countOfWordsDifCate.get(j).get(i);
			}
			featureCount.add(sum);
		}
		return featureCount;
	}

	/**
	 * @return the countOfWordsDifCate 不同类别的词的文档个数
	 */
	public ArrayList<ArrayList<Integer>> getCountOfWordsDifCate() {
		return countOfWordsDifCate;
	}

	/**
	 * @return the diffCateNum 不同类别的文档个数
	 */
	public ArrayList<Integer> getDiffCateNum() {
		int n = diffCateTrainSet.size();// 类别个数
		diffCateNum = new ArrayList<Integer>(n);
		for (ArrayList<AnalReview> arrayList : diffCateTrainSet) {// size=每个类别中的文档数
			diffCateNum.add(arrayList.size());
		}
		return diffCateNum;
	}
	public ArrayList<String> getFeatureString() {
		ArrayList<String> result=new ArrayList<String>(frequency.size());
		Iterator itr=frequency.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, Integer> entry = (Entry) itr.next();
			result.add(entry.getKey());
		}
		return result;
	}
	

	public HashMap<String, Integer> getFrequency() {
		return frequency;
	}
	
	/**
	 * 将词频信息(单词+次数)写入表单中
	 * 
	 * @param sheet
	 *            要写的表单
	 * @throws WriteException
	 *             写错误
	 * @throws RowsExceededException
	 *             行错误
	 */
	public void writeFrequecy(WritableSheet sheet)
			throws RowsExceededException, WriteException {
		Label label;
		Iterator<Entry<String, Integer>> iter = frequency.entrySet().iterator();
		int k = 0;
		while (iter.hasNext()) {
			HashMap.Entry entry = iter.next();
			String string = (String) entry.getKey();
			Integer sfre = (Integer) entry.getValue();
			label = new Label(0, k, string);
			sheet.addCell(label);
			label = new Label(1, k, sfre.toString());
			sheet.addCell(label);
			k++;
		}
		System.out.println("总词数：" + k);
	}
}
