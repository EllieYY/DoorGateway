/*
 * 版本:v1.0.1
 *   修改时间:
 *   作者:段焱明
 *   内容:
 *
 *                   CopyRight  Fiberhome  All Rights Reservee
 *                      武汉烽火信息集成技术有限公司版权所有
 */

package com.wimetro.acs.mapper;


import com.wimetro.acs.model.PersonInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 获取设备信息服务
 * User: 焱明
 * Date: 13-8-9
 * Time: 上午10:05
 */
@Service("serviceMapper")
public interface ServiceMapper {

    /**
     * 处理 考勤信息
     *
     * @param items
     * @return
     */
    int insertAttendanceDetail(Map<String, String> items);

    /**
     * 插入门禁打卡记录
     *
     * @param items
     * @return
     */
    int insertAccessCard(Map<String, String> items);

    /**
     * 获取门禁打卡门信息
     *
     * @param interface_no
     * @param reader_no
     * @return
     */
    String selectDoorAddress(@Param("interface_no") String interface_no, @Param("reader_no") String reader_no, @Param("macaddr") String macaddr);
    String testQuery();

    /**
     * 获取卡号对应的员工信息
     *
     * @param card_no
     * @return
     */
    PersonInfo selectPeopleInfo(@Param("card_no") String card_no);


    /**
     * 根据ip地址更新门禁设备在线状态，时间信息
     *
     * @param dev_ip
     * @param dev_time
     * @return
     */
    int updateDeviceState(@Param("dev_ip") String dev_ip, @Param("dev_time") String dev_time, @Param("up_time") String up_time);


    /**
     * 重置所有设备状态为停用
     * @return
     */
    int restoreDeviceState();

    /**
     * 查询返回V100信息
     * @return
     */
    List<Map<String, Object>> queryAllV100Info();
}
