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
	
	private String fileName="StopWords.txt";
	private ArrayList<String> stopWords;

	/**
	 * 
	 */
	public LoadStopWords() {
		stopWords=new ArrayList<String>();
		File file = new File(fileName);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String tempString;
			while ((tempString = reader.readLine()) != null) {
				tempString=tempString.trim();
				if (!tempString.startsWith("#")
						&& tempString.length() > 0 &&tempString.matches("[\u4e00-\u9fa5]+")) {
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
	 * @param stopWords the stopWords to set
	 */
	public void setStopWords(ArrayList<String> stopWords) {
		this.stopWords = stopWords;
	}
	
	public static void main(String[] args) {
		LoadStopWords loadStopWords=new LoadStopWords();
		for (String string : loadStopWords.getStopWords()) {
			System.out.println(string);
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+loadStopWords.getStopWords().size());
		LoadEmotionRelated loadEmotionRelated=new LoadEmotionRelated();
		loadStopWords.filtEmotionWords(loadEmotionRelated.getAllEmotionRelated());
		for (String string : loadStopWords.getStopWords()) {
			System.out.println(string);
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~"+loadStopWords.getStopWords().size());
	}
}
