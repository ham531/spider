package com.miexam.spider.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.miexam.spider.service.SpiderService;

@RestController
public class SpiderController {

	private static final Logger log = LoggerFactory.getLogger(SpiderController.class);

	@Autowired
	private SpiderService spiderService;

	@RequestMapping(value = "/spider", method = RequestMethod.GET)
	public void spider(String searchword) {
		spiderService.setParam(searchword);
		try {
			// 以下均为异步线程
			spiderService.spiderMof();
			spiderService.spiderZycg();
			spiderService.spiderCcgp();
			spiderService.spiderCcgpBeijing();
			spiderService.spiderCcgpTianjin();
			spiderService.spiderCcgpShanghai();
			spiderService.spiderCcgpHebei();
			// spiderService.spiderCcgpChongqing();// 地址无效
			spiderService.spiderCcgpHenan();
			spiderService.spiderCcgpHubei();
			spiderService.spiderCcgpHunan();
			spiderService.spiderCcgpShanxi();
			spiderService.spiderCcgpShandong();
			spiderService.spiderCcgpHeilongj();
			spiderService.spiderCcgpJilin();
			// spiderService.spiderCcgpLiaoning();// 无法查询
			spiderService.spiderCcgpGuangdong();
			spiderService.spiderCcgpHainan();
			spiderService.spiderCcgpNeimenggu();
			spiderService.spiderCcgpShaanxi();
			spiderService.spiderCcgpGansu();
			spiderService.spiderCcgpQinghai();
			spiderService.spiderCcgpNingxia();
			spiderService.spiderCcgpJiangxi();
			spiderService.spiderCcgpXinjiang();
			spiderService.spiderCcgpXizang();
			spiderService.spiderCcgpSichuan();
			spiderService.spiderCcgpJiangsu();
			spiderService.spiderCcgpZhejiang();
			spiderService.spiderCcgpGuangxi();
			spiderService.spiderCcgpYunnan();
			// spiderService.spiderCcgpFujian();// csrftoken禁止访问
			spiderService.spiderCcgpGuizhou();
			spiderService.spiderCcgpAnhui();
			spiderService.spiderCcgpDalian();
			spiderService.spiderCcgpNingbo();
			// spiderService.spiderCcgpXiamen();// csrftoken禁止访问
		} catch (Exception e) {
			log.error(e.getMessage());
		}
	}
}
