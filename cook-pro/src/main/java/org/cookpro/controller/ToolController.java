package org.cookpro.controller;

import jakarta.annotation.Resource;
import org.cookpro.R;
import org.cookpro.dto.ToolPageDTO;
import org.cookpro.service.ToolService;
import org.cookpro.vo.ToolPageListVo;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tool")
public class ToolController {


    @Resource
    ToolService toolService;


    @PostMapping("/pageList")
    public R<List<ToolPageListVo>> getTools(@RequestBody ToolPageDTO dto){
        return  R.ok(toolService.getToolPageList(dto));
    }

}
