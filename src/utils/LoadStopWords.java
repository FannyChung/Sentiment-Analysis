/**
 * 
 */
package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 加载停用词表
 * 
 * @author ZhongFang
 *
 */
public class LoadStopWords {

	private String fileName = "./myresource/StopWords.txt";
	private ArrayList<String> stopWords;

	/**
	 * 构造函数，导入停用词
	 */
	public LoadStopWords() {
		stopWords = new ArrayList<String>();
		File file = new File(fileName);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String tempString;
			while ((tempString = reader.readLine()) != null) {
				tempString = tempString.trim();
				if ( tempString.length() > 0) {
					stopWords.add(tempString);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.err.println("找不到文件" + fileName);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("读文件" + fileName + "错误");
		}
	}

	/**
	 * 停用词中不能包含情感词
	 * 
	 * @param emotionWords
	 *            情感词集合
	 */
	public void filtEmotionWords(ArrayList<String> emotionWords) {
		stopWords.removeAll(emotionWords);
	}

	/**
	 * @return the stopWords
	 */
	public ArrayList<String> getStopWords() {
		return stopWords;
	}

	/**
	 * @param stopWords
	 *            the stopWords to set
	 */
	public void setStopWords(ArrayList<String> stopWords) {
		this.stopWords = stopWords;
	}
}
