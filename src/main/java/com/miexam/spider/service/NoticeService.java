package com.miexam.spider.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.miexam.spider.domain.Data;
import com.miexam.spider.domain.Notice;
import com.miexam.spider.mapper.NoticeMapper;

@Service
public class NoticeService {
	@Autowired
	private NoticeMapper noticeMapper;

	public Data getData(int page, int limit, String org, String type, String searchword) {
		Data data = new Data();
		int count = noticeMapper.count(org, type, searchword);
		data.setCode("0");
		data.setCount(count);
		int start = (page - 1) * limit;
		List<Notice> notices = noticeMapper.query(start, limit, org, type, searchword);
		data.setData(notices);
		return data;
	}
}
