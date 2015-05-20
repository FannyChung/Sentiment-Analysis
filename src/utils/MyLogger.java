package utils;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.SimpleLayout;

/**
 * 日志记录器，可以写到指定的文件中，写的格式是一行一个消息
 * 
 * @author ZhongFang
 *
 */
public class MyLogger {

	private Logger logger;

	/**
	 * 建立一个可以把字符打印到文件的logger对象
	 * 
	 * @param filename文件名
	 */
	public MyLogger(String filename) {
		logger = Logger.getLogger("./output/" + filename);

		FileAppender appender = null;
		String pattern = "%m";// 输出的只有指定文本，没有其他信息
		PatternLayout layout = new PatternLayout(pattern);
		logger.setAdditivity(false);// 不在控制台输出
		try {
			appender = new FileAppender(layout, filename, false);
		} catch (Exception e) {
		}
		logger.addAppender(appender);
	}

	/**
	 * 打印信息到文件
	 * 
	 * @param info
	 */
	public void info(String info) {
		logger.info(info);
	}
}