package utils;

import java.io.*;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class NlpirTest {

	// 定义接口CLibrary，继承自com.sun.jna.Library
	public interface CLibrary extends Library {
		// 定义并初始化接口的静态变量
		CLibrary Instance = (CLibrary) Native.loadLibrary("./lib/NLPIR.dll",
				CLibrary.class);

		public int NLPIR_Init(String sDataPath, int encoding,
				String sLicenceCode);

		public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

		public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
				boolean bWeightOut);

		public String NLPIR_GetFileKeyWords(String sLine, int nMaxKeyLimit,
				boolean bWeightOut);

		public int NLPIR_AddUserWord(String sWord);// add by qp 2008.11.10

		public int NLPIR_DelUsrWord(String sWord);// add by qp 2008.11.10

		public int NLPIR_ImportUserDict(String sPath);

		// 从字符串中获取新词
		public String NLPIR_GetNewWords(String sLine);

		// 从TXT文件中获取新词
		public String NLPIR_GetFileNewWords(String sTextFile);// ,int
																// nMaxKeyLimit,
																// boolean
																// bWeightOut

		public String NLPIR_GetLastErrorMsg();

		public void NLPIR_Exit();
	}

	public static String transString(String aidString, String ori_encoding,
			String new_encoding) {
		try {
			return new String(aidString.getBytes(ori_encoding), new_encoding);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		String argu = "./";
		// String system_charset = "GBK";//GBK----0
		int init_flag = CLibrary.Instance.NLPIR_Init(argu, 1, "0");
		String nativeBytes;
		if (0 == init_flag) {
			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			System.err.println("初始化失败！fail reason is " + nativeBytes);
			return;
		}
		long a = System.currentTimeMillis();

		String allReviewsFileName = "所有评论文本.txt";
		String newWordsFileName = "新词.txt";

		// 导入新词文件
		String newWords = CLibrary.Instance.NLPIR_GetFileNewWords(allReviewsFileName);
		System.out.println(newWords);

		String[] thewords = newWords.split("#");
		System.out.println(thewords.length);
		try {
			FileOutputStream writerStream = new FileOutputStream("新词.txt",
					false);
			OutputStreamWriter osw = (new OutputStreamWriter(writerStream,
					"UTF-8"));
			for (int i = 0; i < thewords.length; i++) {
				osw.write(thewords[i].split("/")[0] + "\n");
			}
			osw.flush();
			osw.close();
		} catch (Exception e) {

		}

		// 使用新词
		String str = "新词.txt";
		int nCount = CLibrary.Instance.NLPIR_ImportUserDict(str);
		System.out.println("Import User Dictionary entries: " + nCount);

		String reivewText = "先说手机，呵呵，充电端口坏了，不能充电，坑！！！作为备用机,充电器跟电池都是旧的，很明显，我另一个同学也是刚买的这个，都不一样他的都是新的，坑！！！商家态度，给呵呵了，一个电话都不敢打过来是几个意思啊！！！不是嫌退货麻烦，还有贴了公司的通行证，肯定退货了，第一次在亚马逊遇到这么坑的商家！！！！给跪了。 简直坑爹";
		nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(reivewText, 1);
		System.out.println(nativeBytes);
		CLibrary.Instance.NLPIR_Exit();
		System.out.println("\r执行耗时 : " + (System.currentTimeMillis() - a)
				+ " ms ");
	}
}
