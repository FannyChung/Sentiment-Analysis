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
	private ArrayList<String> allEmotionRelated = new ArrayList<String>();

	/**
	 * 构造函数，读取所有的情感相关的词
	 */
	public LoadEmotionRelated() {
		int n = fileName.length;
		int countEach[] = new int[n];
		int i = 0;
		for (; i < n - 1; i++) {
			File file = new File(filePath + fileName[i]);
			try {
				BufferedReader reader = new BufferedReader(new FileReader(file));
				String tempString;
				while ((tempString = reader.readLine()) != null) {
					tempString = tempString.trim();
					if (!tempString.startsWith("#") && tempString.length() > 0) {
						allEmotionRelated.add(tempString.replace(" ... ", "*"));
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
	}

	/**
	 * 只保留特征词里面有的情感词，以减少需要遍历的个数
	 * 
	 * @param features
	 *            特征词
	 */
	public void regenWordLists(ArrayList<String> features) {
		allEmotionRelated.retainAll(features);
	}

	/**
	 * @return the allEmotionRelated
	 */
	public ArrayList<String> getAllEmotionRelated() {
		return allEmotionRelated;
	}
}
