package org.cookpro.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.cookpro.R;
import org.cookpro.entity.CookRecord;
import org.cookpro.service.CookRecordService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cook")
public class CookRecordController {

    @Resource
    private CookRecordService cookRecordService;

    @PostMapping
    @Operation(summary = "添加烹饪记录", description = "添加一条新的烹饪记录")
    public R<String> addCookRecord(@RequestBody CookRecord cookRecord) {
        cookRecordService.save(cookRecord);
        return R.ok("烹饪记录添加成功");
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取烹饪记录", description = "根据ID获取烹饪记录")
    public R<CookRecord> getCookRecord(@PathVariable("id") Long id) {
        CookRecord cookRecord = cookRecordService.getById(id);
        return R.ok(cookRecord);
    }

    @GetMapping("/list")
    @Operation(summary = "获取用户烹饪记录列表", description = "根据用户ID获取烹饪记录列表")
    public R<List<CookRecord>> getCookRecordList(@RequestParam("userId") Long userId) {
        List<CookRecord> list = cookRecordService.lambdaQuery()
                .eq(CookRecord::getUserId, userId)
                .eq(CookRecord::getDeleted, 0)
                .list();
        return R.ok(list);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新烹饪记录", description = "根据ID更新烹饪记录")
    public R<String> updateCookRecord(@PathVariable("id") Long id, @RequestBody CookRecord cookRecord) {
        cookRecord.setId(id);
        cookRecordService.updateById(cookRecord);
        return R.ok("烹饪记录更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除烹饪记录", description = "根据ID删除烹饪记录")
    public R<String> deleteCookRecord(@PathVariable("id") Long id) {
        cookRecordService.removeById(id);
        return R.ok("烹饪记录删除成功");
    }

}
