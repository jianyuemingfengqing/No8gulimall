package com.learn.gmall.ums.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 用户登陆记录表
 * 
 * @author jianyueming
 * @email jianyueming99@gmail.com
 * @date 2022-12-23 13:42:24
 */
@Data
@TableName("ums_user_login_log")
public class UserLoginLogEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	@TableId
	private Long id;
	/**
	 * 用户id
	 */
	private Long userId;
	/**
	 * 登陆时间
	 */
	private Date createTime;
	/**
	 * 登录ip
	 */
	private String ip;
	/**
	 * 登录城市
	 */
	private String city;
	/**
	 * 登录类型【0-web，1-移动】
	 */
	private Integer type;

}
