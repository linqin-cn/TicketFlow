package org.linqin.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.ObjectUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.linqin.business.domain.DailyTrainStation;
import org.linqin.business.domain.DailyTrainStationExample;
import org.linqin.business.domain.TrainStation;
import org.linqin.business.mapper.DailyTrainStationMapper;
import org.linqin.business.req.DailyTrainStationQueryReq;
import org.linqin.business.req.DailyTrainStationSaveReq;
import org.linqin.business.resp.DailyTrainStationQueryResp;
import org.linqin.common.resp.PageResp;
import org.linqin.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 每日车站信息服务
 * <p>
 * 负责"某日某车次"途经车站信息的增删改查，以及由车次基础车站数据
 * 生成每日车站数据。每日车站数据是后续售票、余票计算的基础。
 */
@Service
public class DailyTrainStationService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainStationService.class);

    @Resource
    private DailyTrainStationMapper dailyTrainStationMapper;

    @Resource
    private TrainStationService trainStationService;

    /**
     * 保存每日车站信息（新增或修改）
     * <p>
     * id 为空时执行新增，使用雪花算法生成主键并初始化创建、更新时间；
     * id 非空时执行按主键更新，仅刷新更新时间。
     *
     * @param req 保存请求参数
     */
    public void save(DailyTrainStationSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(req, DailyTrainStation.class);
        if (ObjectUtil.isNull(dailyTrainStation.getId())) {
            dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.insert(dailyTrainStation);
        } else {
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStationMapper.updateByPrimaryKey(dailyTrainStation);
        }
    }

    /**
     * 分页查询每日车站信息
     * <p>
     * 支持按日期、车次筛选，结果按日期倒序、车次升序、车站序号升序排列。
     * 使用 PageHelper 实现分页。
     *
     * @param req 查询请求参数（含页码、每页条数）
     * @return 分页结果，包含总记录数与当前页数据
     */
    public PageResp<DailyTrainStationQueryResp> queryList(DailyTrainStationQueryReq req) {
        LambdaQueryWrapper<DailyTrainStation> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DailyTrainStation::getDate)
                .orderByAsc(DailyTrainStation::getTrainCode)
                .orderByAsc(DailyTrainStation::getIndex);
        if (ObjUtil.isNotNull(req.getDate())) {
            wrapper.eq(DailyTrainStation::getDate, req.getDate());
        }
        if (ObjUtil.isNotEmpty(req.getTrainCode())) {
            wrapper.eq(DailyTrainStation::getTrainCode, req.getTrainCode());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainStation> dailyTrainStationList = dailyTrainStationMapper.selectList(wrapper);

        PageInfo<DailyTrainStation> pageInfo = new PageInfo<>(dailyTrainStationList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainStationQueryResp> list = BeanUtil.copyToList(dailyTrainStationList, DailyTrainStationQueryResp.class);
        PageResp<DailyTrainStationQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }


    public void delete(Long id) {
        dailyTrainStationMapper.deleteById(id);
    }

    @Transactional
    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的车站信息开始", DateUtil.formatDate(date), trainCode);

        // 删除某日某车次的车站信息：deleteByExample → delete(wrapper)
        dailyTrainStationMapper.delete(new LambdaQueryWrapper<DailyTrainStation>()
                .eq(DailyTrainStation::getDate, date)
                .eq(DailyTrainStation::getTrainCode, trainCode));

        // 查出某车次的所有的车站信息（TrainStation 表的 Service，迁该表时再改）
        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(stationList)) {
            LOG.info("该车次没有车站基础数据，生成该车次的车站信息结束");
            return;
        }

        for (TrainStation trainStation : stationList) {
            DateTime now = DateTime.now();
            DailyTrainStation dailyTrainStation = BeanUtil.copyProperties(trainStation, DailyTrainStation.class);
            dailyTrainStation.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainStation.setCreateTime(now);
            dailyTrainStation.setUpdateTime(now);
            dailyTrainStation.setDate(date);
            dailyTrainStationMapper.insert(dailyTrainStation);   // ✅ 这行不用改
        }
        LOG.info("生成日期【{}】车次【{}】的车站信息结束", DateUtil.formatDate(date), trainCode);
    }


    /**
     * 按车次查询全部车站
     */
    public long countByTrainCode(Date date, String trainCode) {
        Long stationCount = dailyTrainStationMapper.selectCount(new LambdaQueryWrapper<DailyTrainStation>()
                .eq(DailyTrainStation::getDate, date)
                .eq(DailyTrainStation::getTrainCode, trainCode));
        return stationCount;
    }


    /**
     * 按车次日期查询车站列表，用于界面显示一列车经过的车站
     */
    public List<DailyTrainStationQueryResp> queryByTrain(Date date, String trainCode) {
        List<DailyTrainStation> list = dailyTrainStationMapper.selectList(new LambdaQueryWrapper<DailyTrainStation>()
                .eq(DailyTrainStation::getDate, date)
                .eq(DailyTrainStation::getTrainCode, trainCode)
                .orderByAsc(DailyTrainStation::getIndex));
        return BeanUtil.copyToList(list, DailyTrainStationQueryResp.class);
    }

}
