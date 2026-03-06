package org.cookpro.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.cookpro.entity.CookRecord;
import org.cookpro.mapper.CookRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class CookRecordService extends ServiceImpl<CookRecordMapper, CookRecord> {
}
