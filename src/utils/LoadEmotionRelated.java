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
	private String filePath = "./HowNet/";
	private String fileName[] = { "正面情感词语（中文）.txt", "负面情感词语（中文）.txt",
			"正面评价词语（中文）.txt", "负面评价词语（中文）.txt", "程度级别词语（中文）.txt",
			"主张词语（中文）.txt", "自定义否定词.txt" };
	private ArrayList<String> emotionalWordsP = new ArrayList<String>();// 正面情感
	private ArrayList<String> emotionalWordsN = new ArrayList<String>();
	private ArrayList<String> evaluWordsP = new ArrayList<String>();// 正面评价
	private ArrayList<String> evaluWordsN = new ArrayList<String>();
	private ArrayList<String> degreeWords = new ArrayList<String>();// 程度
	private ArrayList<Integer> degreeWordsGroupCount;
	private ArrayList<String> viewWords = new ArrayList<String>();// 主张
	private ArrayList<String> nagWords = new ArrayList<String>();// 否定
	private ArrayList<String> allEmotionRelated = new ArrayList<String>();

	public LoadEmotionRelated() {
		int n = fileName.length;
		int countEach[] = new int[n];
		int i = 0;
		for (; i < n - 1; i++) {
			File file = new File(filePath + fileName[i]);
			boolean degreeTag = false;
			int degreeCount = 0;
			if (fileName[i].equals("程度级别词语（中文）.txt")) {
				degreeTag = true;
				degreeWordsGroupCount = new ArrayList<Integer>();
			}

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
		// String[] tmp=(String[]) allEmotionRelated.toArray();
		// emotionalWordsP = allEmotionRelated;
		// emotionalWordsN = allEmotionRelated;
		// evaluWordsP = allEmotionRelated;
		// evaluWordsN = allEmotionRelated;
		// degreeWords = allEmotionRelated;
		// viewWords = allEmotionRelated;
		File file = new File(filePath + fileName[i]);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(file));
			String tempString;
			while ((tempString = reader.readLine()) != null) {
				tempString = tempString.trim();
				if (tempString.length() > 0)
					nagWords.add(tempString);
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

	public void regenWordLists(ArrayList<String> features) {// 只保留特征词里面有的情感词
		allEmotionRelated.retainAll(features);
	}

	/**
	 * @return the emotionalWordsP
	 */
	public ArrayList<String> getEmotionalWordsP() {
		return emotionalWordsP;
	}

	/**
	 * @param emotionalWordsP
	 *            the emotionalWordsP to set
	 */
	public void setEmotionalWordsP(ArrayList<String> emotionalWordsP) {
		this.emotionalWordsP = emotionalWordsP;
	}

	/**
	 * @return the emotionalWordsN
	 */
	public ArrayList<String> getEmotionalWordsN() {
		return emotionalWordsN;
	}

	/**
	 * @param emotionalWordsN
	 *            the emotionalWordsN to set
	 */
	public void setEmotionalWordsN(ArrayList<String> emotionalWordsN) {
		this.emotionalWordsN = emotionalWordsN;
	}

	/**
	 * @return the evaluWordsP
	 */
	public ArrayList<String> getEvaluWordsP() {
		return evaluWordsP;
	}

	/**
	 * @param evaluWordsP
	 *            the evaluWordsP to set
	 */
	public void setEvaluWordsP(ArrayList<String> evaluWordsP) {
		this.evaluWordsP = evaluWordsP;
	}

	/**
	 * @return the evaluWordsN
	 */
	public ArrayList<String> getEvaluWordsN() {
		return evaluWordsN;
	}

	/**
	 * @param evaluWordsN
	 *            the evaluWordsN to set
	 */
	public void setEvaluWordsN(ArrayList<String> evaluWordsN) {
		this.evaluWordsN = evaluWordsN;
	}

	/**
	 * @return the degreeWords
	 */
	public ArrayList<String> getDegreeWords() {
		return degreeWords;
	}

	/**
	 * @param degreeWords
	 *            the degreeWords to set
	 */
	public void setDegreeWords(ArrayList<String> degreeWords) {
		this.degreeWords = degreeWords;
	}

	/**
	 * @return the viewWords
	 */
	public ArrayList<String> getViewWords() {
		return viewWords;
	}

	/**
	 * @param viewWords
	 *            the viewWords to set
	 */
	public void setViewWords(ArrayList<String> viewWords) {
		this.viewWords = viewWords;
	}

	/**
	 * @return the allEmotionRelated
	 */
	public ArrayList<String> getAllEmotionRelated() {
		return allEmotionRelated;
	}

	/**
	 * @param allEmotionRelated
	 *            the allEmotionRelated to set
	 */
	public void setAllEmotionRelated(ArrayList<String> allEmotionRelated) {
		this.allEmotionRelated = allEmotionRelated;
	}

	/**
	 * @return the nagWords
	 */
	public ArrayList<String> getNagWords() {
		return nagWords;
	}

	/**
	 * @param nagWords
	 *            the nagWords to set
	 */
	public void setNagWords(ArrayList<String> nagWords) {
		this.nagWords = nagWords;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		LoadEmotionRelated loadEmotionRelated = new LoadEmotionRelated();
		for (String string : loadEmotionRelated.getAllEmotionRelated()) {
			System.out.println(string);
		}
	}

}
