package utils;

import java.io.*;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class NlpirTest {

	// 定义接口CLibrary，继承自com.sun.jna.Library
	public interface CLibrary extends Library {
		// 定义并初始化接口的静态变量
		CLibrary Instance = (CLibrary) Native
				.loadLibrary(
						"./lib/NLPIR.dll",
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

		//从字符串中获取新词
		public String NLPIR_GetNewWords(String sLine);

		//从TXT文件中获取新词
		public String NLPIR_GetFileNewWords(String sTextFile);//,int nMaxKeyLimit, boolean bWeightOut

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

	public static void main(String[] args){
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

		String fileName="所有评论文本.txt";
		String readString="";
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String data = br.readLine();//一次读入一行，直到读入null为文件结束
			while( data!=null){
//				System.out.println(data);
				readString=readString.concat(data);
				data = br.readLine(); //接着读下一行
			}
		}catch (Exception e){

		}

		System.out.println(readString);
//		String words=CLibrary.Instance.NLPIR_ParagraphProcess(readString, 0);
//		System.out.println(words);

		String newWords=CLibrary.Instance.NLPIR_GetNewWords(readString);
//		String newWords=CLibrary.Instance.NLPIR_GetFileNewWords(fileName);
		System.out.println(newWords);

		String[] thewords=newWords.split("#");
		try {
			FileOutputStream out=new FileOutputStream("新词.txt");
			PrintStream p=new PrintStream(out);
			for (int i = 0; i < thewords.length; i++) {
				p.println(thewords[i]);
			}
		}catch (Exception e){

		}

		CLibrary.Instance.NLPIR_Exit();
		System.out.println("\r执行耗时 : " + (System.currentTimeMillis() - a)
				+ " ms ");
	}
}
