package com.miexam.spider.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.miexam.spider.HttpUtil;
import com.miexam.spider.domain.Notice;
import com.miexam.spider.mapper.NoticeMapper;

@Service
public class SpiderService {

	private static final String timeType = "2";
	private static int timeRange;
	private String searchword;

	@Autowired
	private NoticeMapper noticeMapper;

	public void setParam(String searchword) {
		timeRange = -7;
		this.searchword = searchword;
	}

	@Async
	public void spiderMof() throws Exception {
		String org = "财政部";
		String url = "http://www.mof.gov.cn/was5/web/search";
		Map<String, String> params = new HashMap<>();
		params.put("channelid", "273753");
		params.put("sortfield", "-loder;-crtime");
		params.put("prepage", "10");
		params.put("page", "1");
		params.put("outlinepage", "10");
		params.put("searchword", "(chnlid=(4088,4101,4150,4151,4152,4153,4154,4155)) and (doctitle/3=like(\"" + searchword + "\",80))");
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html.replace("<![CDATA[", "").replace("]]>", ""));
		Elements tableEles = doc.getElementsByClass("Jsuo_BaiH");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < tableEles.size(); i++) {
			Element tableEle = tableEles.get(i);
			Element tdEle = tableEle.getElementsByClass("Jsuo_DMid_topzi").first();
			Element aEle = tdEle.getElementsByTag("a").first();
			Element spanEle = tdEle.getElementsByTag("span").first();
			String date = spanEle.text().substring(1, 11).replace(".", "-");
			String title = aEle.text();
			String href =  aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderZycg() throws Exception {
		String org = "中央政府采购网";
		String url = "http://www.zycg.gov.cn/article/article_search";
		String host = "http://www.zycg.gov.cn";
		Map<String, String> params = new HashMap<>();
		params.put("page", "1");
		params.put("keyword", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ul = doc.getElementsByClass("lby-list").get(0);
		Elements lis = ul.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size() - 1; i++) {
			Element li = lis.get(i);
			Element spanEle = li.getElementsByTag("span").first();
			if (spanEle == null) {
				break;
			}
			Element aEle = li.getElementsByTag("a").first();
			String date = spanEle.text().substring(1, 11);
			String title = aEle.attr("title");
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgp() {
		String org = "中国政府采购网";
		String url = "http://search.ccgp.gov.cn/bxsearch";
		Map<String, String> params = new HashMap<>();
		params.put("searchtype", "1");
		params.put("bidSort", "0");
		params.put("pinMu", "0");
		params.put("bidType", "1");
		params.put("dbselect", "bidx");
		params.put("pppStatus", "0");
		// params.put("timeType", "2");
		params.put("kw", searchword);
		params.put("page_index", "1");
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ul = doc.getElementsByClass("vT-srch-result-list-bid").get(0);
		Elements lis = ul.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		for (int i = 0; i < lis.size(); i++) {
			Element li = lis.get(i);
			Element aEle = li.getElementsByTag("a").first();
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				String title = aEle.text();
				Element spanEle = li.getElementsByTag("span").first();
				String date = spanEle.text().substring(0, 10).replace(".", "-");
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpBeijing() throws Exception {
		String org = "北京市政府采购网";
		String url = "http://wzjs.bjcz.gov.cn:8080/was5/web/search";
		Map<String, String> params = new HashMap<>();
		params.put("channelid", "212555");
		params.put("orderby", "RELEVANCE");
		params.put("timescope", "week");
		params.put("timescopecolumn", "PubTime");
		params.put("andsen", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements aEles = doc.getElementsByClass("searchresulttitle");
		Elements divEles = doc.getElementsByClass("pubtime");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < aEles.size(); i++) {
			Element aEle = aEles.get(i);
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element divEle = divEles.get(i);
				String title = aEle.text();
				String date = divEle.text().substring(0, 10).replace(".", "-");
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpTianjin() throws Exception {
		String org = "天津市政府采购网";
		String url = "http://www.ccgp-tianjin.gov.cn/portal/topicView.do";
		String host = "http://www.ccgp-tianjin.gov.cn";
		Map<String, String> params = new HashMap<>();
		params.put("method", "findAllTitle");
		params.put("name", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements lis = doc.getElementsByClass("oneData");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element li = lis.get(i);
			Element aEle = li.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				String title = aEle.text();
				Element spanEle = li.getElementsByTag("span").first();
				String date = spanEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpShanghai() throws Exception {
		String org = "上海市政府采购网";
		String url = "http://www.ccgp-shanghai.gov.cn/news.do?method=purchasePracticeMore&treenum=05&flag=cggg&title=采购公告";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element tbodyEle = doc.getElementById("bulletininfotable_table_body");
		Elements trEles = tbodyEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		String hrefPrefix = "http://www.ccgp-shanghai.gov.cn/emeb_bulletin.do?method=showbulletin&bulletin_id=";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < trEles.size(); i++) {
			Element trEle = trEles.get(i);
			Element aEle = trEle.getElementsByTag("a").first();
			String href = hrefPrefix + aEle.attr("value");
			if (!hrefMap.containsKey(href)) {
				String title = aEle.text();
				Element dateEle = trEle.child(2);
				String date = dateEle.text().trim().substring(1, 11);
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpHebei() throws Exception {
		String org = "河北省政府采购网";
		String url = "http://search.hebcz.gov.cn:8080/was5/web/search";
		Map<String, String> params = new HashMap<>();
		params.put("channelid", "228483");
		params.put("perpage", "20");
		params.put("outlinepage", "10");
		params.put("lanmu", "zbgg");
		params.put("page", "1");
		params.put("sydoctitle", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements a3 = doc.getElementsByClass("a3");
		Elements txt1 = doc.getElementsByClass("txt1");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < a3.size(); i++) {
			Element aEle = a3.get(i);
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element tEle = txt1.get(i).child(0);
				String title = aEle.text();
				String date = tEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpChongqing() throws Exception {
		String org = "重庆市政府采购网";
		String url = "http://search.hebcz.gov.cn:8080/was5/web/search";
		Map<String, String> params = new HashMap<>();
		params.put("channelid", "228483");
		params.put("perpage", "20");
		params.put("outlinepage", "10");
		params.put("lanmu", "zbgg");
		params.put("page", "1");
		params.put("sydoctitle", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements a3 = doc.getElementsByClass("a3");
		Elements txt1 = doc.getElementsByClass("txt1");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < a3.size(); i++) {
			Element aEle = a3.get(i);
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element tEle = txt1.get(i).child(0);
				String title = aEle.text();
				String date = tEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpHenan() throws Exception {
		String org = "河南省政府采购网";
		String url = "http://www.ccgp-henan.gov.cn/henan/search";
		String host = "http://www.ccgp-henan.gov.cn";
		Map<String, String> params = new HashMap<>();
		params.put("year", "2018");
		params.put("keyword", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element divEle = doc.getElementsByClass("List2").first();
		Element ulEle = divEle.getElementsByTag("ul").first();
		Elements lis = ulEle.getElementsByTag("li");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element li = lis.get(i);
			Element aEle = li.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element spanEle = li.getElementsByTag("span").first();
				String title = aEle.text();
				String date = spanEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpHubei() throws Exception {
		String org = "湖北省政府采购网";
		String url = "http://www.ccgp-hubei.gov.cn/notice/cggg/pzbgg/index_1.html";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ulEle = doc.getElementsByClass("news-list-content").first();
		Elements liEles = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String host = "http://www.ccgp-hubei.gov.cn";
		for (int i = 0; i < liEles.size(); i++) {
			Element liEle = liEles.get(i);
			Element aEle = liEle.child(0);
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element spanEle = liEle.child(1);
				String title = aEle.text();
				String date = spanEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpHunan() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String org = "湖南省政府采购网";
		String url = "http://www.ccgp-hunan.gov.cn/mvc/getNoticeList4Web.do";
		String host = "http://www.ccgp-hunan.gov.cn";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		params.put("endDate", dateFormat.format(calendar.getTime()));
		calendar.add(Calendar.DATE, timeRange);
		params.put("startDate", dateFormat.format(calendar.getTime()));
		params.put("nType", "prcmNotices");
		params.put("pType", "");
		params.put("prcmPrjName", searchword);
		params.put("prcmItemCode", "");
		params.put("prcmOrgName", "");
		params.put("prcmPlanNo", "");
		params.put("page", "1");
		params.put("pageSize", "18");
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		String jsonData = doc.getElementsByTag("body").first().text();
		List<Notice> list = new ArrayList<>();
		JSONObject jsonObject = new JSONObject(jsonData);
		JSONArray jsonArray = jsonObject.getJSONArray("rows");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject rowObject = jsonArray.getJSONObject(i);
			String href = host + "/page/notice/notice.jsp?noticeId=" + rowObject.get("NOTICE_ID");
			if (!hrefMap.containsKey(href)) {
				String title = rowObject.get("NOTICE_TITLE").toString();
				String date = rowObject.get("NEWWORK_DATE").toString();
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpShanxi() throws Exception {
		String org = "山西省政府采购网";
		String url = "http://www.ccgp-shanxi.gov.cn/view.php?ntype=fnotice&nodeid=5";
		String host = "http://www.ccgp-shanxi.gov.cn";
		Map<String, String> params = new HashMap<>();
		params.put("type", "招标公告");
		params.put("title", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements oddEles = doc.getElementsByClass("odd");
		Elements evenEles = doc.getElementsByClass("even");
		oddEles.addAll(evenEles);
		Elements trEles = oddEles;
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < trEles.size(); i++) {
			Element trEle = trEles.get(i);
			Element aEle = trEle.getElementsByTag("a").first();
			String href = host + "/" + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element inputEle = trEle.getElementsByTag("input").first();
				String title = aEle.attr("title");
				String date = inputEle.val();
				Element fontEle = trEle.getElementsByTag("font").first();
				String status = fontEle.text();
				if ("招标中".equals(status)) {
					long noticeTime = dateFormat.parse(date).getTime();
					if (rangTime > noticeTime) {
						continue;
					}
					if (title.contains(searchword)) {
						Notice notice = new Notice();
						notice.setOrg(org);
						notice.setTitle(title);
						notice.setHref(href);
						notice.setDate(date);
						list.add(notice);
					}
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpShandong() throws Exception {
		String org = "山东省政府采购网";
		String url = "http://www.ccgp-shandong.gov.cn/sdgp2014/site/channelall.jsp";
		String host = "http://www.ccgp-shandong.gov.cn";
		Map<String, String> params = new HashMap<>();
		params.put("curpage", "1");
		params.put("colcode", "0301");
		params.put("projectname", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements tdEles = doc.getElementsByClass("Font9");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < tdEles.size() - 1; i++) {
			Element tdEle = tdEles.get(i);
			Element aEle = tdEle.getElementsByTag("a").first();
			if (aEle == null) {
				break;
			}
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				String title = aEle.text();
				String tdTxt = tdEle.text().trim();
				String date = tdTxt.substring(tdTxt.length() - 10, tdTxt.length());
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpHeilongj() throws Exception {
		String org = "黑龙江省政府采购网";
		String url = "http://www.ccgp-heilongj.gov.cn/xwzs!queryXwxxqx.action";
		String host = "http://www.ccgp-heilongj.gov.cn";
		Map<String, String> params = new HashMap<>();
		params.put("xwzsPage.GJZ", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements divEles = doc.getElementsByClass("xxei");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < divEles.size(); i++) {
			Element divEle = divEles.get(i);
			Element aEle = divEle.getElementsByTag("a").first();
			String onclick = aEle.attr("onclick");
			String href = host + onclick.substring(onclick.indexOf("'") + 1, onclick.lastIndexOf("'"));
			if (!hrefMap.containsKey(href)) {
				Element spanEle = divEle.getElementsByClass("sjej").first();
				String title = aEle.text();
				String date = spanEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpJilin() throws Exception {
		String org = "吉林省政府采购网";
		String url = "http://www.ccgp-jilin.gov.cn/front/cmsArticle/searchArticle.action?categoryId=124"
				+ "&pager.pageNumber=1&pager.keyword=" + searchword + "&keyWord=" + searchword;
		String host = "http://www.ccgp-jilin.gov.cn";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ulEle = doc.getElementsByClass("search_result").first();
		Elements lis = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element liEle = lis.get(i);
			Element aEle = liEle.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element spanEle = liEle.child(0).child(1);
				String title = aEle.text();
				String date = spanEle.text().trim().substring(0, 10);
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpLiaoning() throws Exception {
		String org = "辽宁省政府采购网";
		String url = "http://search.hebcz.gov.cn:8080/was5/web/search";
		Map<String, String> params = new HashMap<>();
		params.put("channelid", "228483");
		params.put("perpage", "20");
		params.put("outlinepage", "10");
		params.put("lanmu", "zbgg");
		params.put("page", "1");
		params.put("sydoctitle", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements a3 = doc.getElementsByClass("a3");
		Elements txt1 = doc.getElementsByClass("txt1");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < a3.size(); i++) {
			Element aEle = a3.get(i);
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element tEle = txt1.get(i).child(0);
				String title = aEle.text();
				String date = tEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpGuangdong() throws Exception {
		String org = "广东省政府采购网";
		String url = "http://www.gdgpo.com/queryMoreInfoList.do";
		String host = "http://www.gdgpo.com";
		Map<String, String> params = new HashMap<>();
		params.put("channelCode", "0005");
		params.put("sitewebId", "-1");
		params.put("title", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ulEle = doc.getElementsByClass("m_m_c_list").first();
		Elements lis = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element liEle = lis.get(i);
			Element aEle = liEle.getElementsByTag("a").get(1);
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element emEle = liEle.getElementsByTag("em").first();
				String title = aEle.attr("title");
				String date = emEle.text().trim().substring(0, 10);
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpHainan() throws Exception {
		String org = "海南省政府采购网";
		String url = "http://www.ccgp-hainan.gov.cn/cgw/cgw_list.jsp?bid_type=101";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ulEle = doc.getElementsByClass("nei02_04_01").first().child(0);
		Elements liEles = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		String host = "http://www.ccgp-hainan.gov.cn";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < liEles.size(); i++) {
			Element liEle = liEles.get(i);
			Element aEle = liEle.getElementsByTag("a").get(2);
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element iEle = liEle.getElementsByTag("i").first();
				String title = aEle.text();
				String date = iEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpNeimenggu() throws Exception {
		String org = "内蒙古自治区政府采购网";
		String url = "http://www.ccgp-neimenggu.gov.cn/zfcgwslave/web/index.php";
		Map<String, String> params = new HashMap<>();
		url += "?r=zfcgw%2Fanndata&type_name=1&byf_page=1&fun=cggg";
		url += "&keyword=" + searchword;
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		List<Notice> list = new ArrayList<>();
		String html = HttpUtil.doPost(url, params);
		JSONArray jsonArray = new JSONArray(html);
		jsonArray = jsonArray.getJSONArray(0);
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		String hrefPrefix = "http://www.nmgp.gov.cn/ay_post/post.php?tb_id=1&p_id=";
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jsonObject = jsonArray.getJSONObject(i);
			String wpMarkId = jsonObject.getString("wp_mark_id");
			String href = hrefPrefix + wpMarkId;
			if (!hrefMap.containsKey(href)) {
				String title = jsonObject.getString("TITLE_ALL");
				String date = jsonObject.getString("SUBDATE").substring(0, 10);
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpShaanxi() throws Exception {
		String org = "陕西省政府采购网";
		String url = "http://www.ccgp-shaanxi.gov.cn/notice/noticeaframe.do?noticetype=3";
		Map<String, String> params = new HashMap<>();
		params.put("parameters['regionguid']", "610001");
		params.put("parameters['title']", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element tbodyEle = doc.getElementsByTag("tbody").first();
		Elements trEles = tbodyEle.getElementsByTag("tr");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < trEles.size(); i++) {
			Element trEle = trEles.get(i);
			Elements tdEles = trEle.children();
			String href = tdEles.get(2).child(0).attr("href");
			if (!hrefMap.containsKey(href)) {
				String title = tdEles.get(2).attr("title");
				String date = tdEles.get(3).text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpGansu() throws Exception {
		String org = "甘肃省政府采购网";
		String url = "http://www.ccgp-gansu.gov.cn/web/doSearch.action";
		url += "?op='1'&articleSearchInfoVo.classname=1280501&articleSearchInfoVo.tflag=1&articleSearchInfoVo.title="
				+ searchword;
		String host = "http://www.ccgp-gansu.gov.cn";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ulEle = doc.getElementsByClass("Expand_SearchSLisi").first();
		Elements lis = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element liEle = lis.get(i);
			Element aEle = liEle.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element spanEle = liEle.getElementsByTag("span").first();
				String title = aEle.text();
				String spanTxt = spanEle.text();
				int index = spanTxt.indexOf("发布时间");
				String date = spanTxt.substring(index + 5, index + 15);
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpQinghai() throws Exception {
		String org = "青海省政府采购网";
		String url = "http://www.ccgp-qinghai.gov.cn/jilin/zbxxController.form?declarationType=GKZBGG&pageNo=0&type=1";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ulEle = doc.getElementsByTag("ul").first();
		Elements lis = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element liEle = lis.get(i);
			Element aEle = liEle.getElementsByTag("a").first();
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element spanEle = liEle.getElementsByClass("news_date").first().child(0);
				String title = aEle.text();
				String date = spanEle.text().trim().replace("年", "-").replace("月", "-").replace("日", "");
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpNingxia() throws Exception {
		String org = "宁夏自治区政府采购网";
		String url = "http://www.ccgp-ningxia.gov.cn/public/NXGPP/dynamic/contents/CGGG/index.jsp?cid=312&sid=1&type=101";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element tableEle = doc.getElementsByClass("list_table").first();
		Elements trEles = tableEle.getElementsByTag("tr");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < trEles.size(); i++) {
			Element trEle = trEles.get(i);
			Element aEle = trEle.getElementsByTag("a").first();
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element tdEle = trEle.child(1);
				String title = aEle.text();
				String date = tdEle.text().trim().substring(0, 10);
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpJiangxi() throws Exception {
		String org = "江西省政府采购网";
		String url = "http://www.ccgp-jiangxi.gov.cn/jxzfcg/services/JyxxWebservice/getList";
		url += "?response=application/json&pageIndex=1&pageSize=22&&categorynum=002006001&xxTitle=" + searchword;
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		JSONObject jsonObject = new JSONObject(html);
		JSONObject returnObj = new JSONObject(jsonObject.get("return").toString());
		JSONArray jsonArray = returnObj.getJSONArray("Table");
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject rowObj = jsonArray.getJSONObject(i);
			String categorynum = rowObj.getString("categorynum");
			String infoid = rowObj.getString("infoid");
			String postdate = rowObj.getString("postdate");
			String title = rowObj.getString("title");

			String href = "http://www.ccgp-jiangxi.gov.cn/web/jyxx/" + categorynum.substring(0, 6) + "/" + categorynum
					+ "/" + postdate.replace("-", "") + "/" + infoid + ".html";
			if (!hrefMap.containsKey(href)) {
				String date = postdate;
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpXinjiang() throws Exception {
		String org = "新疆自治区政府采购网";
		String url = "http://zfcg.xjcz.gov.cn/djl/cmsPublishAction.do?method=selectCmsInfoPublishList&channelId=15";
		String host = "http://zfcg.xjcz.gov.cn";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements divEles = doc.getElementsByClass("layout2_list_row2");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < divEles.size(); i++) {
			Element divEle = divEles.get(i);
			Element aEle = divEle.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element div2Ele = divEle.getElementsByClass("layout2_list_time").first();
				String title = aEle.text();
				String date = div2Ele.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpXizang() throws Exception {
		String org = "西藏自治区政府采购网";
		String url = "http://www.ccgp-xizang.gov.cn/shopHome/morePolicyNews.action?categoryId=124,125&areaParam=xizhang";
		String host = "http://www.ccgp-xizang.gov.cn";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element divEle = doc.getElementById("news_div");
		Element ulEle = divEle.child(0);
		Elements lis = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element liEle = lis.get(i);
			Element aEle = liEle.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element spanEle = liEle.getElementsByTag("span").first();
				String title = aEle.text();
				String date = spanEle.text().trim().substring(0, 10);
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpSichuan() throws Exception {
		String org = "四川省政府采购网";
		String url = "http://www.ccgp-sichuan.gov.cn/CmsNewsController.do?method=search&chnlCodes="
				+ "&type=cggg&searchKey=&distin_like=510000&pageSize=10&searchResultForm=search_result_schuan.ftl&title="
				+ searchword;
		String host = "http://www.ccgp-sichuan.gov.cn";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element divEle = doc.getElementsByClass("colsList").first();
		Element ulEle = divEle.child(0);
		Elements lis = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element liEle = lis.get(i);
			Element aEle = liEle.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element spanEle = liEle.getElementsByClass("date").first();
				String title = aEle.text();
				String date = spanEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpJiangsu() throws Exception {
		String org = "江苏省政府采购网";
		String url = "http://www.ccgp-jiangsu.gov.cn/cgxx/cggg/";
		Map<String, String> params = new HashMap<>();
		params.put("channelid", "228483");
		params.put("perpage", "20");
		params.put("outlinepage", "10");
		params.put("lanmu", "zbgg");
		params.put("page", "1");
		params.put("sydoctitle", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ulEle = doc.getElementById("newsList").child(0);
		Elements liEles = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < liEles.size(); i++) {
			Element liEle = liEles.get(i);
			Element aEle = liEle.child(0);
			String hrefOrigin = aEle.attr("href");
			String href = url + hrefOrigin.substring(2, hrefOrigin.length());
			if (!hrefMap.containsKey(href)) {
				String title = aEle.text();
				String dateOrigin = liEle.text().trim();
				String date = dateOrigin.substring(dateOrigin.length() - 10, dateOrigin.length());
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpZhejiang() throws Exception {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String org = "浙江省政府采购网";
		String url = "http://manager.zjzfcg.gov.cn/cms/api/cors/getRemoteResults";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		String endDate = dateFormat.format(calendar.getTime());
		calendar.add(Calendar.DATE, timeRange);
		String beginDate = dateFormat.format(calendar.getTime());
		url += "?pageNo=1&pageSize=15&type=10&isExact=0&beginDate=" + beginDate + "&endDate=" + endDate + "&keyword="
				+ searchword;
		url += "&url=http://notice.zcy.gov.cn/new/globalFullTextSearch";
		String html = HttpUtil.doPost(url, params);
		List<Notice> list = new ArrayList<>();
		JSONObject jsonObject = new JSONObject(html);
		JSONArray jsonArray = jsonObject.getJSONArray("articles");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject rowObj = jsonArray.getJSONObject(i);
			String href = rowObj.getString("url");
			if (!hrefMap.containsKey(href)) {
				String title = rowObj.getString("title");
				String pubDate = rowObj.getString("pubDate");
				calendar.setTimeInMillis(Long.parseLong(pubDate));
				String date = dateFormat.format(calendar.getTime());
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpGuangxi() throws Exception {
		String org = "广西自治区政府采购网";
		String url = "http://www.ccgp-guangxi.gov.cn/CmsNewsController/getCmsNewsList/channelCode-shengji_cggg/param_bulletin/20/page_1.html";
		String host = "http://www.ccgp-guangxi.gov.cn";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element divEle = doc.getElementsByClass("infoLink").first();
		Element ulEle = divEle.getElementsByTag("ul").first();
		Elements lis = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element liEle = lis.get(i);
			Element aEle = liEle.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element spanEle = liEle.getElementsByClass("date").first();
				String title = aEle.text();
				String date = spanEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpYunnan() throws Exception {
		String org = "云南省政府采购网";
		String url = "http://www.ccgp-yunnan.gov.cn/bulletin.do?method=moreListQuery";
		Map<String, String> params = new HashMap<>();
		params.put("current", "1");
		params.put("rowCount", "10");
		params.put("sign", "1");
		params.put("query_sign", "1");
		params.put("flag", "1");
		params.put("listSign", "1");
		params.put("districtCode", "all");
		params.put("query_bulletintitle", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		JSONObject jsonObject = new JSONObject(html);
		JSONArray jsonArray = jsonObject.getJSONArray("rows");
		String hrefPrefix = "http://www.ccgp-yunnan.gov.cn/bulletin_zz.do?method=showBulletin&bulletin_id=";
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject rowObj = jsonArray.getJSONObject(i);
			String href = hrefPrefix + rowObj.getString("bulletin_id");
			if (!hrefMap.containsKey(href)) {
				String title = rowObj.getString("bulletintitle");
				String date = rowObj.getString("beginday");
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpFujian() throws Exception {
		String org = "福建省政府采购网";
		String url = "http://search.hebcz.gov.cn:8080/was5/web/search";
		Map<String, String> params = new HashMap<>();
		params.put("channelid", "228483");
		params.put("perpage", "20");
		params.put("outlinepage", "10");
		params.put("lanmu", "zbgg");
		params.put("page", "1");
		params.put("sydoctitle", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements a3 = doc.getElementsByClass("a3");
		Elements txt1 = doc.getElementsByClass("txt1");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < a3.size(); i++) {
			Element aEle = a3.get(i);
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element tEle = txt1.get(i).child(0);
				String title = aEle.text();
				String date = tEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpGuizhou() throws Exception {
		String org = "贵州省政府采购网";
		String url = "http://www.ccgp-guizhou.gov.cn/list-1153418052184995.html";
		String host = "http://www.ccgp-guizhou.gov.cn";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element ulEle = doc.getElementsByClass("xnrx").first().child(0);
		Elements lis = ulEle.children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < lis.size(); i++) {
			Element liEle = lis.get(i);
			Element aEle = liEle.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element spanEle = liEle.getElementsByTag("span").first();
				String title = aEle.text();
				String date = spanEle.text().trim().replace(".", "-");
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpAnhui() throws Exception {
		String org = "安徽省政府采购网";
		String url = "http://www.ccgp-anhui.gov.cn/cmsNewsController/getCgggNewsList.do?channelCode=sjcg_cggg&"
				+ "dist_code=340000&bid_type=01&pProviceCode=340000&areacode_prov=340000&title=" + searchword;
		String host = "http://www.ccgp-anhui.gov.cn";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element tableEle = doc.getElementsByTag("table").first();
		Elements trEles = tableEle.getElementsByTag("tr");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < trEles.size(); i++) {
			Element trEle = trEles.get(i);
			Element aEle = trEle.child(0).child(0);
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				String title = aEle.attr("title");
				String date = trEle.child(1).child(0).text().substring(1, 11);
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpDalian() throws Exception {
		String org = "大连市政府采购网";
		String url = "http://www.ccgp-dalian.gov.cn/dlweb/showinfo/bxmoreinfo.aspx?CategoryNum=003001001";
		String host = "http://www.ccgp-dalian.gov.cn";
		Map<String, String> params = new HashMap<>();
		// params.put("MoreInfoList$Titletxt", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element tableEle = doc.getElementById("MoreInfoList_DataGrid1");
		Elements trEles = tableEle.getElementsByTag("tr");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < trEles.size(); i++) {
			Element trEle = trEles.get(i);
			Element aEle = trEle.getElementsByTag("a").first();
			String href = host + aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				String title = aEle.text();
				String date = trEle.child(2).text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpNingbo() throws Exception {
		String org = "宁波市政府采购网";
		String url = "http://www.ccgp-ningbo.gov.cn/project/zcyNotice.aspx?noticetype=2";
		Map<String, String> params = new HashMap<>();
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Element tableEle = doc.getElementById("gdvNotice3");
		Elements trEles = tableEle.child(0).children();
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 1; i < trEles.size() - 1; i++) {
			Element trEle = trEles.get(i);
			Element aEle = trEle.getElementsByTag("a").first();
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element tdEle = trEle.child(3);
				String title = aEle.text();
				String date = tdEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}

	@Async
	public void spiderCcgpXiamen() throws Exception {
		String org = "厦门市政府采购网";
		String url = "http://search.hebcz.gov.cn:8080/was5/web/search";
		Map<String, String> params = new HashMap<>();
		params.put("channelid", "228483");
		params.put("perpage", "20");
		params.put("outlinepage", "10");
		params.put("lanmu", "zbgg");
		params.put("page", "1");
		params.put("sydoctitle", searchword);
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, timeRange);
		long rangTime = calendar.getTimeInMillis();
		String html = HttpUtil.doPost(url, params);
		Document doc = Jsoup.parse(html);
		Elements a3 = doc.getElementsByClass("a3");
		Elements txt1 = doc.getElementsByClass("txt1");
		List<String> hrefs = noticeMapper.queryHrefByOrg(org, timeType);
		Map<String, Object> hrefMap = new HashMap<>();
		for (String href : hrefs) {
			hrefMap.put(href, null);
		}
		List<Notice> list = new ArrayList<>();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		for (int i = 0; i < a3.size(); i++) {
			Element aEle = a3.get(i);
			String href = aEle.attr("href");
			if (!hrefMap.containsKey(href)) {
				Element tEle = txt1.get(i).child(0);
				String title = aEle.text();
				String date = tEle.text().trim();
				long noticeTime = dateFormat.parse(date).getTime();
				if (rangTime > noticeTime) {
					continue;
				}
				if (title.contains(searchword)) {
					Notice notice = new Notice();
					notice.setOrg(org);
					notice.setTitle(title);
					notice.setHref(href);
					notice.setDate(date);
					list.add(notice);
				}
			}
		}
		if (!list.isEmpty()) {
			noticeMapper.insertBatch(list);
		}
	}
}
