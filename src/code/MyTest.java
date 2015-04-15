package code;

import java.util.ArrayList;

import code.NlpirTest.CLibrary;

public class MyTest {
	/**
	 * 处理一个表单里的评论
	 */
	public void dealSheetRev() {
		
	}
	public static void main(String[] args) {
		String argu = "C:/Users/hp/Desktop/ICTCLAS2015/sample/JnaTest_NLPIR/";
		// String system_charset = "GBK";//GBK----0
		String system_charset = "UTF-8";
		int charset_type = 1;
		
		int init_flag = CLibrary.Instance.NLPIR_Init(argu, 1, "0");
		String nativeBytes = null;

		if (0 == init_flag) {
			nativeBytes = CLibrary.Instance.NLPIR_GetLastErrorMsg();
			System.err.println("初始化失败！fail reason is "+nativeBytes);
			return;
		}
		
		String sInput = "购买荣耀6近2月，\n1、卖家是亚马逊，包装好，标牌齐全，买大件还是自营得靠谱。\n2、手机用着比较流畅，系统较好，不用再装其他软件。\n3、照相功能很好用，强光下能够看清屏幕，是一个优点。\n4、电池待机时间长，基本上用3天，不玩游戏。";
		nativeBytes = CLibrary.Instance.NLPIR_ParagraphProcess(sInput, 0);
		System.out.print("关键词提取结果是：" + nativeBytes+'\n');
		CLibrary.Instance.NLPIR_Exit();
		nativeBytes=nativeBytes.replaceAll("[、,;。， ]+", " ");
		nativeBytes = nativeBytes.replaceAll("\\s+", " ");
//		ArrayList<String> abc=new ArrayList<String>();
		String[] A=nativeBytes.split(" ");
		int i=0;
		for (String string : A) {
			i++;
			System.out.println(i+string);
		}
	}
}
