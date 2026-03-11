package org.cookpro.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cookpro.dto.ToolAutoPersistDTO;
import org.cookpro.dto.ToolChatDTO;
import org.cookpro.dto.ToolPageDTO;
import org.cookpro.entity.ToolEntity;
import org.cookpro.enums.ToolSourceEnum;
import org.cookpro.mapper.ToolMapper;
import org.cookpro.config.factory.ToolFactory;
import org.cookpro.vo.ToolPageListVo;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
@Slf4j
@Service
public class ToolService extends ServiceImpl<ToolMapper, ToolEntity> {

    @Resource
    ToolFactory toolFactory;

    @Resource
    ToolMapper toolMapper;

    @Resource
    ApplicationContext applicationContext;

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




    public ToolChatDTO getChatDTO(String toolName) {
       return toolMapper.selectChatDTOByName(toolName);
    }

    public List<ToolCallback> selectTools(List<ToolChatDTO> dtoList){
        return toolFactory.selectTools(dtoList);
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

    public String autoAddTool() {


        List<ToolAutoPersistDTO> tools =  getProjectTools();

        List<ToolEntity> entityList = tools.stream()
                .map(this::toAutoEntity)
                .toList();

        int nums = toolMapper.batchSaveOrUpdate(entityList);


        return "总共发现 "+entityList.size()+" 个工具，正在添加到数据库中.......... nums="+ nums;
    }

    private ToolEntity toAutoEntity(ToolAutoPersistDTO dto) {
        ToolEntity entity = new ToolEntity();
        BeanUtil.copyProperties(dto,entity);

        entity.setSource(ToolSourceEnum.IN_PROJECT.description);
        entity.setAuditRemark("待补充");
        entity.setCreateUser("system");
        entity.setAuditorId(0L);
        return entity;
    }

    private List<ToolAutoPersistDTO> getProjectTools() {
        Map<String, Object> beans = applicationContext.getBeansWithAnnotation(Component.class);

        List<ToolAutoPersistDTO> list = new ArrayList<>();

        for (Object bean : beans.values()) {

            Class<?> clazz = bean.getClass();

            for (Method method : clazz.getDeclaredMethods()) {

                Tool tool = method.getAnnotation(Tool.class);

                if (tool != null) {

                    ToolAutoPersistDTO dto = new ToolAutoPersistDTO();

                    dto.setSource(clazz.getName());
                    dto.setToolName(clazz.getSimpleName() + "." + method.getName());
                    dto.setDescription(tool.description());

                    list.add(dto);
                }
            }
        }

        return list;
    }
}
