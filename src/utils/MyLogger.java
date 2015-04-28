package utils;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**日志记录器，可以写到指定的文件中，写的格式是一行一个消息
 * @author Fanny
 *
 */
public class MyLogger {

	private Logger logger;
	public MyLogger(String filename) {
		logger = Logger.getLogger(filename);

		FileAppender appender = null;
		String pattern="%m";
		PatternLayout layout=new PatternLayout(pattern);
		try {
			appender = new FileAppender(layout , filename,false);
		} catch (Exception e) {
		}
		logger.addAppender(appender);
	}
	public void info(String info) {
		logger.info(info);
	}
	public static void main(String[] args) {
		MyLogger logger=new MyLogger("log");
		logger.info("hell\n");
		logger.info("abc");
	}
}