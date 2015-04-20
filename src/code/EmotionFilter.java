package code;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

public class EmotionFilter {
	Vector<String> emotionWords = new Vector<String>();
	static MyTest test = new MyTest();

	/**
	 * 从指定路径获取所有的情感词、评价词、程度词、主张词等
	 * 
	 * @param path文本路径
	 * @return 词的集合
	 */
	public void readHowNet(String path) {
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
					emotionWords.addElement(line);
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void printWords() {
		for (String string : emotionWords) {
			System.out.println(string);
		}
	}

	/**
	 * 对文本进行筛选，去除不含情感的句子
	 * 
	 * @param 筛选前的文本
	 * @return 筛选后的文本
	 */
	public String filtText(String text) {
		String aftText = "";
		String filtedText = "";
		String sens[] = text.split("[。？，！]");// 分割句子，用换行符或。（中英文）
		// System.out.println(sens.length);
		for (String sentence : sens) {
			boolean emoSen = false;
			if (!(sentence.trim().length() > 1))
				continue;
			// System.out.println("sent: " + sentence);
			String[] words = test.analText(sentence);// 分词
			for (String emoWord : emotionWords) {
				for (String word : words) {
					if (word.equalsIgnoreCase(emoWord)) {// 检查是否含有情感词，如果有，则将该句子加入最后的文本
						aftText += (sentence + ".");
						System.out.println(sentence + "\temo\t" + emoWord
								+ "\t" + word);
						emoSen = true;
						break;
					}
				}
				if (emoSen)
					break;
			}
			if (!emoSen)
				filtedText += (sentence + "\n");
		}
		if (filtedText.length() > 1)
			System.out.println("fl:\n" + filtedText + "fi");
		return aftText;
	}

	public static void main(String[] args) {
		test.initNlpri();
		EmotionFilter t = new EmotionFilter();
		t.readHowNet("C:\\Users\\hp\\Desktop\\HowNet.txt");
		// t.printWords();
		// String
		// text="屏幕有坏点。。安兔兔测评灰色时两侧有擦痕。亚马逊一直很相信呢。。居然卖翻新机？着急回家过年，给老人玩的就这样吧。心理真别扭。之前很期待的像素，和华为后200前30一样，前摄影头用照相软件就会上下反转。。。";
		String text = "说说4G的信号吧。我住的地方在市中心的地方，在室外和路上4G信号基本都是满格的，但是一旦进入室内，4G信号就瞬间掉下来，我在房子里还能收到4G信号，但是在等电梯的时候就收不到4G信号，直接掉落到3G了，在比较狭小的房间里面也收不到4G信号，就连3G信号都很不好。这里要表扬下联通，不仅在城里的3G信号杠杠的，而且我回老家农村里在房间里面都是有3G信号的。";
		System.out.println("af\n" + t.filtText(text));
		test.exitNlpir();
	}
}
