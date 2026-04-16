package io.github.dongxuetaffy.aobihelper.stats.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.dongxuetaffy.aobihelper.stats.entity.UserStats;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserStatsMapper extends BaseMapper<UserStats> {
}
