/**
 * 
 */
package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * 获取情感词表和停用词表
 * 
 * @author hp
 *
 */
public class GetWords {
	public static Vector<String> emotionWords = new Vector<String>();
	public static Vector<String> stopWordsList=new Vector<String>();

	static {
		readHowNet("C:\\Users\\hp\\Desktop\\HowNet.txt");
		readStopWd("C:\\Users\\hp\\Desktop\\StopWords.txt");
	}

	/**
	 * 从指定路径获取所有的情感词、评价词、程度词、主张词等
	 * 
	 * @param path文本路径
	 * @return 词的集合
	 */
	public static void readHowNet(String path) {
		File file = new File(path);
		try {
			BufferedReader bw = new BufferedReader(new InputStreamReader(
					new FileInputStream(path), "utf-8"));
			String line = null;
			// 因为不知道有几行数据，所以先存入list集合中
			while ((line = bw.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				if (!line.contains("#")) {
					GetWords.emotionWords.addElement(line);
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printWords() {
		for (String string : GetWords.emotionWords) {
			System.out.println(string);
		}
	}

	public static void readStopWd(String path) {
		File file = new File(path);
		try {
			BufferedReader bw = new BufferedReader(new InputStreamReader(
					new FileInputStream(path), "utf-8"));
			String line = null;
			// 因为不知道有几行数据，所以先存入list集合中
			while ((line = bw.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				GetWords.stopWordsList.addElement(line);
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
