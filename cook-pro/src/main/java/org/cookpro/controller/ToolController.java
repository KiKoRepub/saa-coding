package org.cookpro.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
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
    public R<Page<ToolPageListVo>> getTools(@RequestBody ToolPageDTO dto){
        return  R.ok(toolService.getToolPageList(dto));
    }

    @PostMapping("/add")
    @Operation(summary = "添加工具(需要鉴权)", description = "添加一个新的工具到系统中")
    public R<String> addTool(){
        return R.ok(toolService.addTool());
    }



    @PostMapping("/add/auto")
    @Operation(summary = "自动添加工具(需要鉴权)", description = "根据系统中已有的工具自动添加新的工具")
    public R<String> autoAddTool() {
        return R.ok(toolService.autoAddTool());
    }
}
