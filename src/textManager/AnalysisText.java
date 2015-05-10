package textManager;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import utils.AnalReview;
import utils.NlpirTest.CLibrary;
import jxl.Sheet;
import jxl.write.Label;
import jxl.write.WritableSheet;
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

	private ArrayList<AnalReview> reviews = new ArrayList<AnalReview>();
	private ArrayList<String> allEmotionRelatedWords;
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

	private Integer addOne(Integer value) {
		return value + 1;
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
	private void analysis(String text, int level, String title) {
		text += ("。 " + title);
		AnalReview review = new AnalReview(text, level);
		// 统计字数
		int charsCount = 0;
		int wordsCount = 0;
//		if (text.length() > GUESS_LEN)
//			text = filt(text, allEmotionRelatedWords, review);

		// 分词，并统计词数和词出现的词数
		String[] analText = wordSeg(text);
		for (String string : analText) {
			review.getFrequency().merge(string, 1,
					(value, newValue) -> addOne(value));
			charsCount += string.length();// 统计字数
			wordsCount++;// 统计词数
		}
		review.setWordsCount(wordsCount);
		review.setCharsCount(charsCount);
		reviews.add(review);
	}

	private String filt(String inString, ArrayList<String> emotionWords,
			AnalReview review) {
		String[] subStrings = inString.split("[。？！?.!]+");
		String filtedString = "";
		for (String string : subStrings) {// 每个句子
			boolean emotionTag = false;
			String[] words = wordSeg(string);
			for (String word : words) {// 每个词
				if (allEmotionRelatedWords.contains(word)) {
					emotionTag = true;
					break;
				}
			}
			if (!emotionTag) {
				filtedString = filtedString.concat(string);
				inString = inString.replace(string, " ");
			}
		}
		review.setFiltedText(filtedString);
		return inString;
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
		System.out.println("总评论个数：" + i);
		System.out.println();
		System.out.println();
		System.out.println();
	}

	/**
	 * 初始化分词工具
	 */
	public void initNlpri() {
		String argu = "./";
		// String system_charset = "GBK";//GBK----0
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
	 * @param reivewText
	 *            输入的文本
	 * @return 分词后的单词的数组
	 */
	public String[] wordSeg(String reivewText) {
		try {
			reivewText = full2HalfChange(reivewText);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		reivewText = reivewText.replaceAll("[^\u4e00-\u9fa5?!？！]+",// 只保留中英文数字和?!
																	// 0-9a-zA-Z
				" ");// 去除特殊字符，保留感叹号和问号
		reivewText = reivewText.replaceAll("\\s+", " ");
		nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(reivewText, 0);
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

	public ArrayList<AnalReview> getReviews() {
		return reviews;
	}

	/**
	 * @param allEmotionRelatedWords
	 *            the allEmotionRelatedWords to set
	 */
	public void setAllEmotionRelatedWords(
			ArrayList<String> allEmotionRelatedWords) {
		this.allEmotionRelatedWords = allEmotionRelatedWords;
	}
}
