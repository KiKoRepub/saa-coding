package org.cookpro.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.cookpro.dto.ToolPageDTO;
import org.cookpro.entity.ToolEntity;
import org.cookpro.mapper.ToolMapper;
import org.cookpro.vo.ToolPageListVo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ToolService extends ServiceImpl<ToolMapper, ToolEntity> {


    public List<ToolPageListVo> getToolPageList(ToolPageDTO dto) {

    }
}
