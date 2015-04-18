package code;

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
import java.util.Vector;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import code.NlpirTest.CLibrary;

public class MyTest {
	/**
	 * 每个词与它的出现次数
	 */
	HashMap<String, Integer> frequency = new HashMap<String, Integer>();

	ArrayList<AnalReview> reviews = new ArrayList<AnalReview>();

	Vector<String> emotionWords=new Vector<String>();
	String nativeBytes;
	final int GUESS_LEN = 50;

	/**
	 * 处理一个表单里的评论
	 */
	public void dealSheetRev(Sheet sheet) {
		for (int i = 0; i < sheet.getRows(); i++) {
			String text = sheet.getCell(0, i).getContents();
			int level = Integer.parseInt(sheet.getCell(1, i).getContents());
			analysis(text, level);
		}
	}

	/**
	 * 全角转半角的 转换函数
	 * 
	 * @param QJstr
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public final String full2HalfChange(String QJstr)
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
	 * 对文本进行筛选，去除不含情感的句子
	 * 
	 * @param 筛选前的文本
	 * @return 筛选后的文本
	 */
	public String filtText(String text) {
		String aftText = "";
		String sens[] = aftText.split("[\n。.?!]");// 分割句子，用换行符或。（中英文）
		for (String sentence : sens) {
			for (String emoWord : emotionWords) {
				if (sentence.contains(emoWord)){//检查是否含有情感词，如果有，则将该句子加入最后的文本
					aftText += sentence;
					break;
				}
			}
		}
		return aftText;
	}

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
			while ((line = bw.readLine()) != null ) {
				line=line.trim();
				if (line.isEmpty()) {
					continue;
				}
				if (!line.startsWith("#")) {
					emotionWords.addElement(line);
				}
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 分析一条评论 包括字数、词数和词的出现次数
	 * 
	 * @param text
	 * @param level
	 */
	public void analysis(String text, int level) {
		AnalReview review = new AnalReview(text, level);
		// 统计字数
		int charsCount = 0;
		// 如果大概的词数>估计的平均长度,则进行文本筛选
		if (text.length() > GUESS_LEN)
			text = filtText(text);

		// 分词，并统计词数和词出现的词数
		String[] analText = analText(text);
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
	 * 对所有评论进行分析，统计总的字数、词数、词频
	 */
	public void analAll() {
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
		System.out.println("aveWords" + wordSum / i);
		System.out.println("aveChars" + charSum / i);
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
	 * 对一条评论文本分词
	 */
	public String[] analText(String sInput) {
		try {
			sInput = full2HalfChange(sInput);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		sInput = sInput.replaceAll(
		// "[`~@#$%^&*()+=|{}':;',//[//].<>/~@#￥%……&*（）——+|{}【】‘；：”“’。，、\"%&'()*+,-—--丶]",
		// "[`~@#$%^&*()+=|{}':;',//[//].<>/~！@#￥%……&*（）——+|{}【】‘；：”“’。，、∩》「」《﹏①②③④⑤⊙≧≦←↓_]+",
				"[^0-9a-zA-Z\u4e00-\u9fa5?!]+",// 只保留中英文数字和?!
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
	 * @throws WriteException
	 * @throws RowsExceededException
	 */
	public void printReviews(WritableSheet sheet) throws RowsExceededException,
			WriteException {
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
			i++;
		}
	}

	public static void main(String[] args) {
		MyTest test = new MyTest();
		test.initNlpri();
		test.readHowNet("C:\\Users\\hp\\Desktop\\HowNet.txt");
		try {
			InputStream stream = new FileInputStream(
					"C:\\Users\\hp\\Desktop\\MyData.xls");
			Workbook wb = Workbook.getWorkbook(stream);

			WritableWorkbook book = Workbook.createWorkbook(new File(
					"result.xls"), wb);
			WritableSheet sheet;

			for (int i = 0; i < 4; i++) {
				sheet = book.getSheet(i);
				test.dealSheetRev(sheet);
			}
			test.printRes();
			test.analAll();
			// 将词频信息写入表单中
			Label label;
			sheet = book.createSheet("总词频", 4);
			Iterator<Entry<String, Integer>> iter = test.frequency.entrySet()
					.iterator();
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
			// 写每条评论的词频和星级等信息
			sheet = book.createSheet("所有评论", 5);
			test.printReviews(sheet);
			System.out.println("总词数：" + k);
			book.write();
			book.close();
		} catch (BiffException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}
		test.exitNlpir();
	}
}
