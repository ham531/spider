package com.miexam.spider.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.miexam.spider.domain.Notice;

@Mapper
public interface NoticeMapper {

	public void insertBatch(List<Notice> list);

	public List<String> queryHrefByOrg(@Param("org") String org, @Param("type") String type);

	public int count(@Param("org") String org, @Param("type") String type, @Param("searchword") String searchword);

	public List<Notice> query(@Param("start") int start, @Param("limit") int limit, @Param("org") String org,
			@Param("type") String type, @Param("searchword") String searchword);
}
