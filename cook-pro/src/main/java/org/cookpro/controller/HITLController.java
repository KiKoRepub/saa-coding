package org.cookpro.controller;

import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.annotation.Resource;
import org.cookpro.R;
import org.cookpro.dto.HITLEditInfoDTO;
import org.cookpro.dto.HITLPageDTO;
import org.cookpro.dto.HITLReviewDTO;
import org.cookpro.entity.HITLToolArgInfo;
import org.cookpro.service.HITLService;
import org.cookpro.vo.CommonEnumVo;
import org.cookpro.vo.HITLPageVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hitl")
public class HITLController {


    @Resource
    HITLService hitlService;


    @GetMapping("/getToolArgs")
    public R<List<HITLToolArgInfo>> getToolArgInfo(@RequestParam("toolName")String toolName,
                                                   @RequestParam("id")Long id) throws JsonProcessingException {
        return R.ok(hitlService.getToolArgInfo(id,toolName));
    }

    @GetMapping("/getStatusList")
    public R<List<CommonEnumVo>> getStatusList(){
        return R.ok(hitlService.getStatusList());
    }

    @PostMapping("/publish/list")
    public R<Page<HITLPageVo>> getPublishList(@RequestBody HITLPageDTO dto){
        return R.ok(hitlService.getPublishPageList(dto));
    }

    @PostMapping("/review/list")
    public R<Page<HITLPageVo>> getReviewList(@RequestBody HITLPageDTO dto){
            return R.ok(hitlService.getReviewPageList(dto));
    }

    @PostMapping("/review")
    public R<String> review(@RequestBody HITLReviewDTO dto) throws GraphRunnerException {
        return R.ok(hitlService.reviewHitl(dto));
    }

    @PostMapping("/edit")
    public R<String> edit(@RequestBody HITLEditInfoDTO dto) throws GraphRunnerException {
        return R.ok(hitlService.editHitl(dto));
    }

}
