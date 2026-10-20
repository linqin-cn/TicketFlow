package org.linqin.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.linqin.business.domain.*;
import org.linqin.business.mapper.DailyTrainSeatMapper;
import org.linqin.business.req.DailyTrainSeatQueryReq;
import org.linqin.business.req.DailyTrainSeatSaveReq;
import org.linqin.business.req.SeatSellReq;
import org.linqin.business.resp.DailyTrainSeatQueryResp;
import org.linqin.business.resp.SeatSellResp;
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
 * 每日列车座位服务
 */
@Service
public class DailyTrainSeatService {

    private static final Logger LOG = LoggerFactory.getLogger(DailyTrainSeatService.class);

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

    @Resource
    private TrainSeatService trainSeatService;

    @Resource
    private TrainStationService trainStationService;

    /**
     * 保存每日列车座位信息
     */
    public void save(DailyTrainSeatSaveReq req) {
        DateTime now = DateTime.now();
        DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(req, DailyTrainSeat.class);
        if (ObjectUtil.isNull(dailyTrainSeat.getId())) {
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        } else {
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeatMapper.updateByPrimaryKey(dailyTrainSeat);
        }
    }

    /**
     * 查询每日列车座位列表
     */
    public PageResp<DailyTrainSeatQueryResp> queryList(DailyTrainSeatQueryReq req) {
        LambdaQueryWrapper<DailyTrainSeat> wrapper = new LambdaQueryWrapper<DailyTrainSeat>()
                .orderByDesc(DailyTrainSeat::getDate)
                .orderByAsc(DailyTrainSeat::getTrainCode,
                        DailyTrainSeat::getCarriageIndex,
                        DailyTrainSeat::getCarriageSeatIndex)
                .eq(ObjectUtil.isNotEmpty(req.getTrainCode()),
                        DailyTrainSeat::getTrainCode, req.getTrainCode());

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<DailyTrainSeat> dailyTrainSeatList = dailyTrainSeatMapper.selectList(wrapper);

        PageInfo<DailyTrainSeat> pageInfo = new PageInfo<>(dailyTrainSeatList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<DailyTrainSeatQueryResp> list = BeanUtil.copyToList(dailyTrainSeatList, DailyTrainSeatQueryResp.class);

        PageResp<DailyTrainSeatQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }


    /**
     * 删除每日列车座位信息
     */
    public void delete(Long id) {
        dailyTrainSeatMapper.deleteById(id);
    }


    /**
     * 生成每日列车座位信息
     */
    @Transactional
    public void genDaily(Date date, String trainCode) {
        LOG.info("生成日期【{}】车次【{}】的座位信息开始", DateUtil.formatDate(date), trainCode);

        // 删除某日某车次的座位信息
        dailyTrainSeatMapper.delete(new LambdaQueryWrapper<DailyTrainSeat>()
                .eq(DailyTrainSeat::getDate, date)
                .eq(DailyTrainSeat::getTrainCode, trainCode));

        List<TrainStation> stationList = trainStationService.selectByTrainCode(trainCode);
        String sell = StrUtil.fillBefore("", '0', stationList.size() - 1);

        // 查出某车次的所有的座位信息
        List<TrainSeat> seatList = trainSeatService.selectByTrainCode(trainCode);
        if (CollUtil.isEmpty(seatList)) {
            LOG.info("该车次没有座位基础数据，生成该车次的座位信息结束");
            return;
        }

        for (TrainSeat trainSeat : seatList) {
            DateTime now = DateTime.now();
            DailyTrainSeat dailyTrainSeat = BeanUtil.copyProperties(trainSeat, DailyTrainSeat.class);
            dailyTrainSeat.setId(SnowUtil.getSnowflakeNextId());
            dailyTrainSeat.setCreateTime(now);
            dailyTrainSeat.setUpdateTime(now);
            dailyTrainSeat.setDate(date);
            dailyTrainSeat.setSell(sell);
            dailyTrainSeatMapper.insert(dailyTrainSeat);
        }
        LOG.info("生成日期【{}】车次【{}】的座位信息结束", DateUtil.formatDate(date), trainCode);
    }


    /**
     * 统计某日某车次的座位数
     */
    public int countSeat(Date date, String trainCode) {
        return countSeat(date, trainCode, null);
    }

    /**
     * 统计某日某车次的座位数，可指定座位类型
     */
    public int countSeat(Date date, String trainCode, String seatType) {
        LambdaQueryWrapper<DailyTrainSeat> wrapper = new LambdaQueryWrapper<DailyTrainSeat>()
                .eq(DailyTrainSeat::getDate, date)
                .eq(DailyTrainSeat::getTrainCode, trainCode)
                .eq(StrUtil.isNotBlank(seatType),
                        DailyTrainSeat::getSeatType, seatType);

        Long count = dailyTrainSeatMapper.selectCount(wrapper);
        if (count == 0L) {
            return -1;
        }
        return count.intValue();
    }


    /**
     * 根据车厢查询每日列车座位信息
     */
    public List<DailyTrainSeat> selectByCarriage(Date date, String trainCode, Integer carriageIndex) {
        return dailyTrainSeatMapper.selectList(new LambdaQueryWrapper<DailyTrainSeat>()
                .eq(DailyTrainSeat::getDate, date)
                .eq(DailyTrainSeat::getTrainCode, trainCode)
                .eq(DailyTrainSeat::getCarriageIndex, carriageIndex)
                .orderByAsc(DailyTrainSeat::getCarriageSeatIndex));
    }


    /**
     * 查询某日某车次的所有座位
     */
    public List<SeatSellResp> querySeatSell(SeatSellReq req) {
        Date date = req.getDate();
        String trainCode = req.getTrainCode();
        LOG.info("查询日期【{}】车次【{}】的座位销售信息", DateUtil.formatDate(date), trainCode);

        List<DailyTrainSeat> list = dailyTrainSeatMapper.selectList(
                new LambdaQueryWrapper<DailyTrainSeat>()
                        .eq(DailyTrainSeat::getDate, date)
                        .eq(DailyTrainSeat::getTrainCode, trainCode)
                        .orderByAsc(DailyTrainSeat::getCarriageIndex,
                                DailyTrainSeat::getCarriageSeatIndex));

        return BeanUtil.copyToList(list, SeatSellResp.class);
    }
}
