package org.cookpro.service;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.cloud.ai.graph.agent.hook.hip.HumanInTheLoopHook;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cookpro.dto.ToolPageDTO;
import org.cookpro.entity.ToolEntity;
import org.cookpro.mapper.ToolMapper;
import org.cookpro.utils.ToolFactory;
import org.cookpro.vo.ToolPageListVo;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ToolService extends ServiceImpl<ToolMapper, ToolEntity> {

    @Resource
    ToolFactory toolFactory;

    public Page<ToolPageListVo> getToolPageList(ToolPageDTO dto) {
        int pageNum = dto.getPageNum();
        int pageSize = dto.getPageSize();

        QueryWrapper<ToolEntity> queryWrapper = getPageQueryWrapper(dto);


        Page<ToolEntity> toolList = page(new Page<>(pageNum, pageSize), queryWrapper);


        List<ToolPageListVo> voList = toolList.getRecords().stream()
                        .map(this::toPageVo)
                        .collect(Collectors.toList());


        Page<ToolPageListVo> result = new Page<>(pageNum,pageSize);

        result.setRecords(voList);
        return result;

    }




    public List<ToolEntity> getToolEntities(List<Long> toolIdList) {
        QueryWrapper<ToolEntity> queryWrapper = new QueryWrapper<ToolEntity>()
                .eq("deleted",0)
                .in("id", toolIdList);

        return this.list(queryWrapper);
    }


    private ToolPageListVo toPageVo(ToolEntity toolEntity) {

        ToolPageListVo vo = new ToolPageListVo();

        BeanUtil.copyProperties(toolEntity,vo);




        return vo;
    }
    private static QueryWrapper<ToolEntity> getPageQueryWrapper(ToolPageDTO dto) {
        QueryWrapper<ToolEntity> wrapper =  new QueryWrapper<ToolEntity>()
                .eq("deleted", 0)
                .orderByDesc("created_time");


        if (dto.getStatus() != null){
            wrapper.eq("status", dto.getStatus());
        } if (StringUtils.isNotEmpty(dto.getToolName())){
            wrapper.like("tool_name", dto.getToolName());
        }


        return wrapper;
    }

    public String addTool() {
        ToolEntity entity = new ToolEntity();

        log.warn("正在添加工具..........");
        return entity.getId().toString();
    }
}
