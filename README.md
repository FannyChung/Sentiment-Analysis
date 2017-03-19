# Sentiment-Analysis

## 本项目主要内容说明

本项目是基于java实现的本科毕设。首先从亚马逊中文网站爬取了关于“手机”的评论，然后对其进行情感分类。使用用户标记的星级作为情感类别，将这些评论分为了3类（与星级对应关系为1-{1,2},2-{3},3-{4,5}）和5类（与星级一一对应）。预处理包括字符处理（包括全角转半角、过滤无关符号，主要为了避免特征稀疏）以及发现新词、分词、删除停用词，使用的主要分类方法是朴素贝叶斯，特征选择方法包括**信息增益**（Information Gain，IG）和**文档频率**（Document Frequency，DF）以及它们的结合。

分类效果：对亚马逊评论三分类F1值效果为 **79.72%**, 五分类效果为 **63.63%**。为了进一步检验，使用本文分类器对谭松波酒店评论进行二分类，使用信息增益筛选特征，最好情况下， precision、 recall、 F1 值分别达到了 85.89%、 82.33%、 **84.07%**. 

本项目又进一步增加了**卡方统计**和**互信息**的特征选择方法，并进行了**对比分析**。以此为基础，在《计算机应用》发表了[《中文多类别情感分类模型中特征选择方法》](http://d.wanfangdata.com.cn/Periodical/jsjyy2016z2064)。并且相对原来的特征维数间隔取得更细一些，得到的最好的效果为三分类**81.75%**，五分类**65.56%**。

### 主要流程
主要流程如下：
1. 获取评论的语料
2. 预处理
    * 字符处理  
        * 全角转半角
        * 过滤无关符号，只保留中文和?!(与情感相关)
    * 发现新词，使用分词工具自带的功能
    * 分词
    * 删除停用词，但是保留停用词中的情感词（因为情感词很可能对分类有用）
3. 特征选择
    * 文档频率
    * 信息增益
4. 评论向量化
5. 分离训练集合测试集，用于交叉验证
6. 用朴素贝叶斯训练&预测
    * 计算先验概率  
    * 计算条件概率  
    * 计算后验概率
7. 统计效果
    * 计算混淆矩阵
    * 计算precision、recall、F1
    
### 使用的工具&资源

本项目使用的工具有：
* [NLPIR](http://ictclas.nlpir.org/)，用来分词并发现新词
* [WebCollector](https://github.com/CrawlScript/WebCollector)，爬虫框架

本项目使用的资源有：
* 否定词典（为了分词工具能识别更多的词）
* HowNet情感词典，包括主张（感知、认为）、程度、正负面评价&情感（为了分词工具能识别更多的词，删除停用词时候避免删除情感相关的词）
* 停用词典（删除停用词）
* [谭松波的酒店评论语料](http://www.datatang.com/data/11936/)
## 主要功能实现
这里主要贴出实现的函数和思想，具体代码见内部的函数。
### 预处理

* 全角转半角

在AnalysisText类中实现函数`private final String full2HalfChange(String QJstr)`，主要是对除了空格之外的符号根据Unicode编码来进行相关转换

* 删除无关字符

仅保留中文和`?!`,因为数字、英文等在评论中大多作为参数存在，与情感无关，分词工具常常对它们不能很好地识别，保留它们会导致特征稀疏。`reivewText = reivewText.replaceAll("[^\u4e00-\u9fa5?!？！]+"," ");`

* 删除停用词

先从停用词中删除了情感词典，然后再从词集中删除剩下的停用词。这样是为了避免删除了和情感相关的词，影响情感分类的效果。
```java
loadStopWords.filtEmotionWords(loadEmotionRelated.getAllEmotionRelated());// 使停用词不包含情感词
feature.removeStopWords(loadStopWords.getStopWords());//再从词集中删除停用词
```

### 特征选择

* 文档频率

`public void removeByDF(ArrayList<Integer> featureCount, int minDF)`,根据指定的`minDF`,从特征集合中删除文档频率<=`minDF`的特征。

* 信息增益

`public ArrayList<String> IGSelection(ArrayList<String> features, int afterSize, Model model,int minDF)`，根据model中的先验概率、联合概率等信息，分别计算每个特征的信息增益，返回前afterSize个信息增益最大的特征集合。单独使用信息增益的时候，将`minDF`置位0。

* 文档频率+信息增益

先删除<=`minDF`的特征，然后再用信息增益进行筛选。即先调用`removeByDF`，然后调用`IGSelection`。

* 卡方统计与互信息

`public ArrayList<String> CHISel(ArrayList<String> features, int afterSize, Model model,int minDF)`与`public ArrayList<String> MISel(ArrayList<String> features, int afterSize, Model model,int minDF)`，过程与特征选择类似，只是计算的公式不同。
### 朴素贝叶斯分类

* 计算先验概率`P(c)`

`public void calcPc() `,使用拉普拉斯平滑`double p = (double) (diffCateNum.get(i) + 1) / (totalSize + c);`

* 计算条件概率`P(f|c)`

`public void calcPfc() `,使用拉普拉斯平滑`double p = (double) (a + 1) / (b + 2);// 指定特征在指定类别下的条件概率`

* 计算后验概率

`public int predict(AnalReview review)`,对一条评论进行预测，先将评论根据特征集合向量化，得到一个boolean数组，然后对于每个特征，若对应位置是1，则`p+=log(P(f|c))`,否则`p+=1-log(P(f|c))`。实现的时候为了避免溢出，使用log的加法代替了原来的乘法。

### 统计指标
* 生成混淆矩阵

`public int[][] genConfuMatrix(ArrayList<AnalReview> reviews,ArrayList<Integer> results, int a[], int b[][])`,根据原始星级标记a和原始星级与类别标记的对应关系b,检查评论自带的星级标记与results中预测的结果，根据对应关系生成混淆矩阵。`ConfuMtr[level_code][result] `表示行为星级标记，列为预测结果的个数。

* 计算准确率、召回率和F1值

类别i的准确率为预测正确的与预测为i的比，召回率为预测正确的与实际为类别i的比。总体准确率和召回率是对每一个类别的准确率和召回率进行加权平均。具体实现在`public double[] statisticalRate(int k, int a[], int b[][], int ConfuMtr[][])`中。
## 文件目录说明

### 输入输出文件

#### 爬虫输出

│      商品名称和url.txt-->爬虫记录的商品名和url

│      t.xls-->爬虫的结果，所有评论文本

#### 情感分析

##### 输入

│      t.xls-->爬虫的结果，所有评论文本

│      tan.xls-->谭松波酒店评论预料，用于测试模型

│      pre.xls-->待预测文本所在文件

##### 输出

│       pre_result.xls-->待预测文本预测后的结果

│      out.xls-->进行训练时候的相关信息记录

│      result.xls-->模型验证时候的相关信息记录

### 产生的中间文件


#### 训练后的输出，预测需要的输入文件

│       Cate_3_p.txt-->分成三类保存的类别的先验概率

│      Cate_5_p.txt-->

│      Featrue_3.txt-->分为三类时候选择的特征字符串

│      Featrue_3_P.txt-->分为三类时候不同类别下每个特征的条件概率

│      Featrue_5.txt-->

│      Featrue_5_P.txt-->

#### 分类过程中产生的中间文件

│      不同特征的文本个数.txt-->不同特征的文档数

│      不同类别的文本个数.txt-->不同类别的文档数

│      删除DF.txt-->根据DF删除的特征词

│      删除停用词.txt-->根据停用词删除的特征词

│      Pfc.txt-->不同类别下不同特征的概率

│      评论以及其特征的概率.txt 

-->K折交叉验证的结果

│      结果.txt-->平均结果

│      结果0.txt-->第k次的结果

│      结果1.txt

│      结果2.txt

│      结果3.txt

│      结果4.txt



### 使用的相关资源

│  新词.txt-->利用分词工具发现的新词，可以在分词前导入分词工具

├─myresource-->情感词资源和停用词

│      │  StopWords.txt

│      │  自定义否定词.txt

│      │  

│      └─HowNet

│            主张词语（中文）.txt

│            正面情感词语（中文）.txt

│             正面评价词语（中文）.txt

│             程度级别词语（中文）.txt

│            负面情感词语（中文）.txt

│             负面评价词语（中文）.txt

│          

├─.settings-->程序配置

│      org.eclipse.core.resources.prefs

│       org.eclipse.jdt.core.prefs

│      

├─bin-->程序生成的class文件

│          

├─Data-->分词依赖

│          

├─lib-->分词依赖包、配置

│      jna-4.0.0.jar

│      NLPIR.dll

│       NLPIR.lib

│      



├─rl_res-->java编程依赖包

│      │      jxl.jar

│      │      log4j-1.2.17.jar

│      │  

│      └─WebCollector





### 代码        

└─src-->代码源程序

├─gui-->界面部分

│      SentiPan.java---情感分析部分的界面

│      SpiderPan.java---爬虫部分的界面

│      TableHelper.java---表格帮助类，用于表格自动设置长宽，从xls中读取内容、表格筛选

│      TopFrame.java---总体界面

│      

├─sentiAna-->情感分析部分

│      AnalReview.java---分析后的评论的数据结构

│      AnalysisText.java---导入评论，对评论进行分析预处理


│      Controller.java---主控制程序

│      DataSet.java---数据集处理（分离训练集和测试集，并向量化）

│      Feature.java---特征选择

│      Model.java---生成模型（计算先验概率和条件概率）

│      Prediction.java---预测&统计指标


│      

├─spider-->爬虫部分

│      ProductUrl.java---商品链接的数据结构

│      ReivewWebDriver.java---爬取商品内容和链接

│      Review.java---商品评论的数据结构

│      

└─utils-->前三个部分的一些帮助工具

│            FileDealer.java---excel文件处理的帮助类

│            LoadEmotionRelated.java---从文件加载情感词典

│            LoadStopWords.java---从文件加载停用词

│            MyLogger.java---可以将Log写入到指定文件中

│            NlpirTest.java---分词工具的配置

│            ReadConfigUtil.java---分词工具的配置

│            SystemParas.java---分词工具的配置

