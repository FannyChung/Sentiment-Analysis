package spider;

/**
 * 商品的url，不同的商品的url具有不同的hash值
 *
 * @author ZhongFang
 */
public class ProductUrl {
	/**
	 * 商品的url
	 */
	private String string;

	/**
	 * 构造函数
	 * 
	 * @param s
	 *            网址的url
	 */
	public ProductUrl(String s) {
		string = s;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {// 重载，分析亚马逊的商品页面的网址，获得一个唯一识别码，根据该识别码判断是否是同一个商品（因为同一个商品也可能有多个url）
		ProductUrl other = (ProductUrl) arg0;
		String str1 = string.split("/")[5];
		String str2 = other.string.split("/")[5];// 根据/分割后的第六个元素编码来识别不同商品
		if (str1.equals(str2))
			return true;
		else
			return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {// 重载，分析亚马逊的商品页面的网址，获得一个唯一识别码，根据该识别码判断是否是同一个商品（因为同一个商品也可能有多个url）
		String str1 = string.split("/")[5];// 根据/分割后的第六个元素编码来识别不同商品
		return new String(str1).hashCode();
	}

	/**
	 * @return url 商品页面
	 */
	public String getString() {
		return string;
	}
}