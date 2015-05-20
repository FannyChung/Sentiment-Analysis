package spider;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import utils.FileDealer;
import utils.MyLogger;

public class ReivewWebDriver {
	/**
	 * 
	 */
	private HtmlUnitDriver driver = new HtmlUnitDriver();
	/**
	 * 
	 */
	private ArrayList<Review> reviews = new ArrayList<Review>(); // 只需要不断增加，所以使用vector
	private MyLogger logger = new MyLogger("商品名称和url.txt");
	private int maxLoadTime = 3;

	/**
	 * 处理评论信息并获取下一页评论
	 *
	 * @param thisPage
	 *            当前页
	 */
	public void dealReviewPage(String thisPage) {
		String nextp = null;
		String reviewReg = "table[id=productReviews]>tbody>tr>td>div";// 评论信息的选择规则
		String textReg = ".reviewText"; // 评论文本的选择规则
		String levelReg = "span";// 评论等级
		String titleReg = "b"; // 评论标题
		String timeReg = "nobr"; // 评论时间
		String dateFormat = "yyyy年MM月dd日"; // 时间格式

		get(thisPage);

		// 处理评论信息
		List<WebElement> ele1 = driver.findElementsByCssSelector(reviewReg);
		for (WebElement element : ele1) {// 获取评论元素，放入review中，每个元素有评论、作者、时间等
			Review review = new Review();
			WebElement textElements = element.findElement(By
					.cssSelector(textReg));// 获取评论文本
			String cString = textElements.getText();
			System.out.println(cString);
			review.setText(cString);

			WebElement levelElements = element.findElement(By
					.cssSelector(levelReg));// 获取星级信息
			cString = levelElements.getText();
			int level = cString.charAt(2) - '0';
			review.setLevel(level);

			WebElement titleEle = element.findElement(By.cssSelector(titleReg));
			cString = titleEle.getText(); // 标题
			review.setReTitle(cString);

			WebElement timeEle = element.findElement(By.cssSelector(timeReg));
			cString = timeEle.getText(); // 获取日期
			SimpleDateFormat s = new SimpleDateFormat(dateFormat);
			Date d = null;
			try {
				long t = s.parse(cString).getTime();
				d = new Date(t);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			review.setTime(d);

			reviews.add(review);
		}

		// 获取下一页评论链接
		WebElement element = null;
		try {
			element = driver.findElementByPartialLinkText("下一页");
		} catch (Exception e) {
			System.err.println("当前：" + thisPage + "没有下一页\n");
			return;
		}
		nextp = element.getAttribute("href");
		System.out.println("下一页\t" + nextp);

		// 递归调用，分析下一页
		dealReviewPage(nextp);
	}

	/**
	 * 从商品页面获取对应评论的第一页
	 *
	 * @param productPage
	 *            商品页面url
	 * @return 对应评论的第一页
	 */
	public String getReviewPage(String productPage) {
		String reviewString = "div[id=revF]>div>a";// 商品页面中对应的评论页面的元素选择规则
		HtmlUnitDriver driver = new HtmlUnitDriver();
		driver.get(productPage);// 跳转到商品页面
		logger.info("Page title is:" + driver.getTitle() + " url: "
				+ driver.getCurrentUrl() + "\r\n");
		WebElement element = driver.findElementByCssSelector(reviewString);
		element.click();// 点击评论链接，跳转到评论页面
		String url = driver.getCurrentUrl();// 获取评论页面的url
		driver.close();
		return url;
	}

	/**
	 * 地址跳转方法，使用WebDriver原生get方法，加入失败重试的次数定义。
	 * 
	 * @param url
	 *            想要打开的网址
	 * @param actionCount
	 *            指定如果超时后的重试次数
	 * @throws RuntimeException
	 */
	private void get(String url, int actionCount) {
		boolean inited = false;
		int index = 0, timeout = 10;
		while (!inited && index < actionCount) {
			timeout = (index == actionCount - 1) ? maxLoadTime : 10;// 最后一次跳转使用最大的默认超时时间
			inited = navigateAndLoad(url, timeout);
			index++;
		}
		if (!inited && index == actionCount) {// 最终跳转失败则抛出运行时异常，退出运行
			throw new RuntimeException("can not get the url [" + url
					+ "] after retry " + actionCount + "times!");
		}
	}

	/**
	 * 地址跳转。默认加载超重试1次。
	 * 
	 * @param url
	 *            想要打开的网址
	 */
	private void get(String url) {
		get(url, 2);
	}

	/**
	 * 跳转到指定的URL并且返回是否跳转完整的结果。
	 * 
	 * @param url
	 *            想要打开的网址
	 * @param timeout
	 *            多少秒之后算超时
	 * @return 是否成功加载
	 */
	private boolean navigateAndLoad(String url, int timeout) {
		try {
			driver.manage().timeouts()
					.pageLoadTimeout(timeout, TimeUnit.SECONDS);
			driver.get(url);
			return true;// 跳转并且加载页面成功在返回true
		} catch (TimeoutException e) {
			return false;// 超时的情况下返回false
		} catch (Exception e) {
			throw new RuntimeException(e);// 抛出运行时异常，退出运行
		} finally {
			driver.manage().timeouts()
					.pageLoadTimeout(maxLoadTime, TimeUnit.SECONDS);// 加载全局设置的最大尝试次数
		}
	}

	/**
	 * 进行搜索
	 *
	 * @param homepageString
	 *            主页的url
	 * @param searchString
	 *            要搜索的关键字
	 * @return 搜索后页面的url
	 */
	public String search(String homepageString, String searchString) {
		HtmlUnitDriver driver = new HtmlUnitDriver();
		String inputReg = "input[type=text]"; // 输入框元素的选择规则
		driver.get(homepageString);
		System.out.println("before search:\tPage title is:" + driver.getTitle()
				+ " url: " + driver.getCurrentUrl());
		WebElement element = driver.findElement(By.cssSelector(inputReg));// 找到文本框
		element.sendKeys(searchString);// 输入搜索关键字
		element.submit();// 提交输入
		String curUrl = driver.getCurrentUrl();// 获取输入后的url
		System.out.println("after search\t : page title is:"
				+ driver.getTitle() + " url: " + curUrl);
		driver.close();
		return curUrl;
	}

	/**
	 * 获取网页中所有指向商品的超链接
	 *
	 * @param searchUrl
	 *            搜索后的网页
	 * @return 商品url的集合
	 */
	public HashSet<ProductUrl> getProductPage(String searchUrl) {
		String productUrlReg = "http://www.amazon.cn/.*/dp/.*"; // 商品url对应的格式

		HashSet<ProductUrl> products = new HashSet<ProductUrl>();
		HtmlUnitDriver driver = new HtmlUnitDriver();
		driver.get(searchUrl);
		List<WebElement> link = driver.findElements(By.cssSelector("[href]"));// 获取带有超链接的元素
		for (WebElement webElement : link) {
			String href = webElement.getAttribute("href"); // 获取超链接对应的String
			if (href.matches(productUrlReg)
					&& products.add(new ProductUrl(href))) {// 获取商品url，加入集合中，同一个商品的多个url不重复添加
				System.out.println(webElement.getText() + '\t' + href);
			}
		}
		System.out
				.println("get product url over=================================================================");
		System.out.println("product size: " + products.size());
		driver.close();
		return products;
	}

	/**
	 * 根据关键字和指定商品个数爬取评论
	 * 
	 * @param searchStr
	 *            要搜索的关键字
	 * @param productNum
	 *            商品个数
	 */
	public void runSpider(String searchStr, int productNum) {
		String s = search("http://www.amazon.cn/ref=nav_logo", searchStr);// 设置主页和搜索内容
		HashSet<ProductUrl> productsStrings = getProductPage(s);// 获取搜索后得到的所有商品url

		WritableSheet sheet = null;
		FileDealer fileDealer = new FileDealer();
		fileDealer.openWriteFile("t.xls");
		int i = 0;// 当前已获取多少个商品
		for (ProductUrl productUrl : productsStrings) {
			if (i == productNum) {// 已获取足够多的商品，跳出循环
				break;
			}

			dealReviewPage(getReviewPage(productUrl.getString()));// 处理该商品的所有评论

			// 将获取的评论写到表格中
			sheet = fileDealer.getBook().createSheet("product " + (i + 1), i);// 设置表单名字和编号
			try {
				fileDealer.wirteReviews(reviews, sheet);
			} catch (RowsExceededException e) {
				e.printStackTrace();
			} catch (WriteException e) {
				e.printStackTrace();
			}
			reviews.clear();
			i++;
		}

		// 关闭资源
		fileDealer.closeWriteFile();
		driver.close();
	}

	public static void main(String[] args) {
		ReivewWebDriver reivewWebDriver = new ReivewWebDriver();
		reivewWebDriver.runSpider("手机", 1);
	}
}
