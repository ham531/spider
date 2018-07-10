package com.miexam.spider.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.miexam.spider.domain.Data;
import com.miexam.spider.service.NoticeService;

@RestController
public class NoticeController {

	@Autowired
	private NoticeService noticeService;

	@RequestMapping(value = "/data", method = RequestMethod.GET)
	public Data queryNotice(int page, int limit, String org, String type, String searchword) {
		return noticeService.getData(page, limit, org, type, searchword);
	}
}
