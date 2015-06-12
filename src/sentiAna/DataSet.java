/**
 * 
 */
package sentiAna;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 分离训练集和测试集,并向量化
 * 
 * @author ZhongFang
 *
 */
public class DataSet {
	/**
	 * 分成了k份的所有数据集，用于交叉验证
	 */
	private ArrayList<ArrayList<AnalReview>> kLists;// 将所有数据集分为k个部分

	/**
	 * 为一条评论生成普通特征的特征向量
	 * 
	 * @param analReview
	 * @param features
	 */
	private void genNmFtVecOfAReview(AnalReview analReview,
			ArrayList<String> features) {
		boolean[] feVec = new boolean[features.size()];
		HashMap<String, Integer> freTemp = analReview.getFrequency();
		for (int i = 0; i < feVec.length; i++) {
			String feature = features.get(i);
			if (freTemp.containsKey(feature)) {
				feVec[i] = true;
			} else {
				feVec[i] = (false);
			}
		}
		analReview.setFeatureVector(feVec);
	}

	private int getEmotPre(AnalReview analReview, int size, String emotionWord,
			ArrayList<String> negaWords) {
		int result = 1;
		ArrayList<String[]> wordOfSentence = analReview.getWordOfSentence();// 每句话里面的词
		for (String[] strings : wordOfSentence) {
			for (int i = 0; i < strings.length; i++) {
				String string = strings[i];
				if (string.equals(emotionWord)) {
					for (int j = Math.max(0, i - size); j < i; j++) {
						if (negaWords.contains(strings[j])) {
							result *= -1;
						}
					}
				}
			}
		}
		return result;
	}

	public void genEmoVector(AnalReview analReview,
			HashMap<String, Integer> emotionFeats, ArrayList<String> negaWords) {
		int[] emoFeatVector = new int[emotionFeats.size()];
		HashMap<String, Integer> freTemp = analReview.getFrequency();
		int i = 0;
		Iterator<Entry<String, Integer>> itr = emotionFeats.entrySet()
				.iterator();
		while (itr.hasNext()) {
			Map.Entry<String, Integer> entry = itr.next();
			String emotionWord = entry.getKey();
			if (freTemp.containsKey(emotionWord)) {
				emoFeatVector[i] = getEmotPre(analReview, 5, emotionWord,
						negaWords);// * entry.getValue()
			}
			i++;
		}
	}

	/**
	 * 把多条文本向量化
	 * 
	 * @param reviews
	 * @param features
	 */
	public void genFeatureVectors(ArrayList<AnalReview> reviews,
			ArrayList<String> features, HashMap<String, Integer> emotionFeats,
			ArrayList<String> negaWords) {
		for (AnalReview analReview : reviews) {
			genNmFtVecOfAReview(analReview, features);// 生成普通特征的特征向量
			genEmoVector(analReview, emotionFeats, negaWords);
		}
	}

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

	public void seleTrain(int k, ArrayList<AnalReview> reviews) {
		kLists = new ArrayList<ArrayList<AnalReview>>(k);
		int numOfEach = reviews.size() / k;
		reviews = genRandAnalReviews(reviews);// 打乱评论的顺序
		int revCount = 0;
		for (int i = 0; i < k; i++) {
			ArrayList<AnalReview> tmp = new ArrayList<AnalReview>(numOfEach);
			for (int j = 0; j < numOfEach; j++) {
				tmp.add(reviews.get(revCount));
				revCount++;
			}
			kLists.add(tmp);
		}
	}

	/**
	 * @return the kLists
	 */
	public ArrayList<ArrayList<AnalReview>> getkLists() {
		return kLists;
	}

	/**
	 * @param kLists
	 *            the kLists to set
	 */
	public void setkLists(ArrayList<ArrayList<AnalReview>> kLists) {
		this.kLists = kLists;
	}
}
