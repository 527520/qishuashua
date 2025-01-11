package com.wqa.qishuashua.esdao;

import com.wqa.qishuashua.model.dto.question.QuestionEsDTO;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 题目ES操作
 */
public interface QuestionEsDao extends ElasticsearchRepository<QuestionEsDTO, Long> {

}
