package utils;

/**
 * 
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 读取情感词典
 * 
 * @author ZhongFang
 *
 */
public class LoadEmotionRelated {
	private String filePath = "./myresource/HowNet/";
	private String fileName[] = { "正面情感词语（中文）.txt", "负面情感词语（中文）.txt",
			"正面评价词语（中文）.txt", "负面评价词语（中文）.txt", "程度级别词语（中文）.txt",
			"主张词语（中文）.txt" };
	private String nageFile = "自定义否定词.txt";
	private ArrayList<String> allEmotionRelated = new ArrayList<String>();

	private HashMap<String, Integer> emotionFeats = new HashMap<String, Integer>();
	private ArrayList<String> negaWords = new ArrayList<String>();

	/**
	 * 构造函数，读取所有的情感相关的词
	 */
	public LoadEmotionRelated() {
		int n = fileName.length;
		int countEach[] = new int[n];
		int i = 0;
		for (; i < n - 1; i++) {
			File file = new File(filePath + fileName[i]);
			int value = 0;
			boolean emofTag = false;
			if (fileName[i] == "正面情感词语（中文）.txt"
					|| fileName[i] == "正面评价词语（中文）.txt") {
				value = 1;
				emofTag = true;
			} else if (fileName[i] == "负面情感词语（中文）.txt"
					|| fileName[i] == "正面评价词语（中文）.txt") {
				value = -1;
				emofTag = true;
			}
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String tempString;
				while ((tempString = reader.readLine()) != null) {
					tempString = tempString.trim();
					if (!tempString.startsWith("#") && tempString.length() > 0) {
						tempString = tempString.replace(" ... ", "*");
						allEmotionRelated.add(tempString);
						if (emofTag)
							emotionFeats.put(tempString, value);

						countEach[i]++;
					}
				}
				reader.close();
			} catch (FileNotFoundException e) {
				System.err.println("找不到文件" + fileName[i]);
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("读文件" + fileName[i] + "错误");
			}
		}
		File file = new File(nageFile);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString;
			while ((tempString = reader.readLine()) != null) {
				tempString = tempString.trim();
				if (tempString.length() > 0)
					negaWords.add(tempString);
			}
		} catch (FileNotFoundException e) {
			System.err.println("找不到文件" + fileName[i]);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("读文件" + fileName[i] + "错误");
		}
	}

	/**
	 * 只保留特征词里面有的情感词，以减少需要遍历的个数
	 * 
	 * @param features
	 *            特征词
	 */
	public void regenWordLists(ArrayList<String> features) {
		allEmotionRelated.retainAll(features);
		Iterator<Entry<String, Integer>> itr = emotionFeats.entrySet()
				.iterator();
		HashMap<String, Integer> temp = new HashMap<String, Integer>();
		while (itr.hasNext()) {
			Map.Entry<String, Integer> entry = itr.next();
			String emotionWord = entry.getKey();
			if (features.contains(emotionWord)) {
				temp.put(emotionWord, entry.getValue());
			}
		}
		emotionFeats = temp;
	}

	/**
	 * @return the allEmotionRelated
	 */
	public ArrayList<String> getAllEmotionRelated() {
		return allEmotionRelated;
	}

	/**
	 * @return the emotionFeats
	 */
	public HashMap<String, Integer> getEmotionFeats() {
		return emotionFeats;
	}

	/**
	 * @return the negaWords
	 */
	public ArrayList<String> getNegaWords() {
		return negaWords;
	}
}
