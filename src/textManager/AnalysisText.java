package textManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import textManager.NlpirTest.CLibrary;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * 从excel表中读取信息 分析评论文本 写入excel表中
 * 
 * @author hp
 *
 */
/**
 * @author hp
 *
 */
public class AnalysisText {
	/**
	 * 每个词与它的出现次数
	 */
	private HashMap<String, Integer> frequency = new HashMap<String, Integer>();

	private ArrayList<AnalReview> reviews = new ArrayList<AnalReview>();

	private String nativeBytes;
	private final int GUESS_LEN = 50;

	/**
	 * 处理一个表单里的评论
	 * 
	 * @param sheet
	 *            要处理的表单
	 */
	public void dealSheetRev(Sheet sheet) {
		for (int i = 0; i < sheet.getRows(); i++) {
			String text = sheet.getCell(0, i).getContents();
			int level = Integer.parseInt(sheet.getCell(1, i).getContents());
			String title = sheet.getCell(2, i).getContents();
			analysis(text, level, title);
		}
	}

	/**
	 * 全角转半角的 转换函数
	 * 
	 * @param QJstr
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private final String full2HalfChange(String QJstr)
			throws UnsupportedEncodingException {
		StringBuffer outStrBuf = new StringBuffer("");
		String Tstr = "";
		byte[] b = null;
		for (int i = 0; i < QJstr.length(); i++) {
			Tstr = QJstr.substring(i, i + 1);
			// 全角空格转换成半角空格
			if (Tstr.equals("　")) {
				outStrBuf.append(" ");
				continue;
			}
			b = Tstr.getBytes("unicode");
			// 得到 unicode 字节数据
			if (b[2] == -1) {
				// 表示全角？
				b[3] = (byte) (b[3] + 32);
				b[2] = 0;
				outStrBuf.append(new String(b, "unicode"));
			} else {
				outStrBuf.append(Tstr);
			}
		} // end for.
		return outStrBuf.toString();
	}

	private Integer add(Integer value, Integer addNum) {
		return value + addNum;
	}

	/**
	 * 对一条评论字符进行分词处理 统计评论的字数、词数
	 * 
	 * @param text
	 *            评论文本
	 * @param level
	 *            评论星级
	 * @param title
	 *            评论标题
	 */
	public void analysis(String text, int level, String title) {
		text += (" " + title);
		AnalReview review = new AnalReview(text, level);
		// 统计字数
		int charsCount = 0;
		// TODO 先进行文本筛选，过滤客观句子
		// if(text.length()>GUESS_LEN)
		// text=filtText(text,review);

		// 分词，并统计词数和词出现的词数
		String[] analText = analText(review.getText());
		int wordsCount = 0;
		for (String string : analText) {
			review.getFrequency().merge(string, 1,
					(value, newValue) -> add(value, 1));
			charsCount += string.length();// 统计字数
			wordsCount++;// 统计词数
		}
		review.setWordsCount(wordsCount);
		review.setCharsCount(charsCount);
		reviews.add(review);
	}

	/**
	 * 对指定的评论集合进行分析 统计总的字数、词数、词频
	 * 
	 * @param frequency
	 */
	public void analAll(HashMap<String, Integer> frequency) {
		int i = 0;
		int wordSum = 0;
		int charSum = 0;
		for (AnalReview analReview : reviews) {
			i++;
			wordSum += analReview.getWordsCount();// 统计总的词数
			charSum += analReview.getCharsCount();// 统计总的字数

			HashMap<String, Integer> revFreq = analReview.getFrequency();
			Iterator iter = revFreq.entrySet().iterator();
			while (iter.hasNext()) {
				HashMap.Entry entry = (HashMap.Entry) iter.next();
				String string = (String) entry.getKey();
				Integer sfre = (Integer) entry.getValue();
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
	 * 在控制台打印所有分析过的评论信息输出
	 */
	public void printRes() {
		int i = 0;
		for (AnalReview review : reviews) {
			System.out.println(review);
			i++;
		}
		System.out.println(i);
		System.out.println();
		System.out.println();
		System.out.println();
	}

	/**
	 * 初始化分词工具
	 */
	public void initNlpri() {
		// TODO Auto-generated method stub
		String argu = "C:/Users/hp/Desktop/ICTCLAS2015/sample/JnaTest_NLPIR/";
		// String system_charset = "GBK";//GBK----0
		String system_charset = "UTF-8";
		int charset_type = 1;

		int init_flag = CLibrary.Instance.NLPIR_Init(argu, 1, "0");

		if (0 == init_flag) {
			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			System.err.println("初始化失败！fail reason is " + nativeBytes);
			return;
		}
	}

	/**
	 * 利用分词器对一段文本分词
	 * 
	 * @param sInput
	 *            输入的文本
	 * @return 分词后的单词的数组
	 */
	public String[] analText(String sInput) {
		try {
			sInput = full2HalfChange(sInput);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		sInput = sInput.replaceAll("[^\u4e00-\u9fa5?!？！]+",// 只保留中英文数字和?! 0-9a-zA-Z
				" ");// 去除特殊字符，保留感叹号和问号
		sInput = sInput.replaceAll("\\s+", " ");
		nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 0);
		nativeBytes = nativeBytes.replaceAll("\\s+", " ");
		String[] A = nativeBytes.split(" ");
		return A;
	}

	/**
	 * 关闭Nlpir分词工具
	 */
	public void exitNlpir() {
		CLibrary.Instance.NLPIR_Exit();
	}

	/**
	 * 在表格中打印每条评论的统计信息
	 * 
	 * @param sheet
	 *            要写的表单
	 * @throws WriteException
	 *             写错误
	 * @throws RowsExceededException
	 *             行错误
	 */
	public void writeReviews(WritableSheet sheet, ArrayList<AnalReview> reviews)
			throws RowsExceededException, WriteException {
		Label label;
		int i = 0;
		for (AnalReview review : reviews) {
			label = new Label(0, i, review.getText());
			sheet.addCell(label);
			label = new Label(1, i, review.getLevel() + "");
			sheet.addCell(label);
			label = new Label(2, i, review.getCharsCount() + "");
			sheet.addCell(label);
			label = new Label(3, i, review.getWordsCount() + "");
			sheet.addCell(label);
			label = new Label(4, i, review.getFrequency().toString());
			sheet.addCell(label);
			label = new Label(5, i, review.getFiltedText());
			sheet.addCell(label);
			i++;
		}
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

	public HashMap<String, Integer> getFrequency() {
		return frequency;
	}

	public ArrayList<AnalReview> getReviews() {
		return reviews;
	}

}
