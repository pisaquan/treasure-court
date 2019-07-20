package com.sancai.oasystem;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiAttendanceGetsimplegroupsRequest;
import com.dingtalk.api.request.OapiDepartmentGetRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiAttendanceGetsimplegroupsResponse;
import com.dingtalk.api.response.OapiDepartmentGetResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.sancai.oasystem.bean.ExamineBaseVO;
import com.sancai.oasystem.bean.TExamineLeave;
import com.sancai.oasystem.dao.TExamineLeaveMapper;
import com.sancai.oasystem.service.TExamineLeaveService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OaSystemApplicationTests {

	String appkey = "ding9p7vzgvgrx3tvd6a";
	String appsecret = "nGKknhri4XwBjcLxkZFacafx10_k67dUT6B09kJF50xLBrwb9AULlVQxcI4L0W3W";

	@Autowired
	private TExamineLeaveService tExamineLeaveService;


	@Test
	public void testSelect() {
		//System.out.println(("----- selectAll method test ------"));
		//List<User> userList = userMapper.selectList(null);
		//Assert.assertEquals(5, userList.size());
		//userList.forEach(System.out::println);
		List<TExamineLeave> examineBaseVOList = tExamineLeaveService.pullDingTalkLeaveData();
		for(TExamineLeave tExamineLeave : examineBaseVOList){
			tExamineLeaveService.save(tExamineLeave);
		}

	}


	@Test
	public void contextLoads() throws Exception {
		DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/department/get");
		OapiDepartmentGetRequest request = new OapiDepartmentGetRequest();
		request.setId("276194902");
		request.setHttpMethod("GET");
		String accessToken = getAccessToken();
		OapiDepartmentGetResponse response = client.execute(request, accessToken);
		System.out.println(response.getErrcode());
		System.out.println(response.getErrmsg());
		System.out.println(response.getName());
		System.out.println(response.getOrder());
		System.out.println(response.getParentid());
	}

	@Test
	public void testGroup()throws Exception{
		DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/topapi/attendance/getsimplegroups");
		OapiAttendanceGetsimplegroupsRequest request = new OapiAttendanceGetsimplegroupsRequest();
		String accessToken = getAccessToken();
		OapiAttendanceGetsimplegroupsResponse execute = client.execute(request,accessToken);
		System.out.println(execute.getErrcode());
		System.out.println(execute.getErrmsg());
		List<OapiAttendanceGetsimplegroupsResponse.AtGroupForTopVo> list =  execute.getResult().getGroups();
		for(OapiAttendanceGetsimplegroupsResponse.AtGroupForTopVo  atGroupForTopVo : list){
			System.out.println(atGroupForTopVo.getGroupId());
			System.out.println(atGroupForTopVo.getGroupName());
			System.out.println(atGroupForTopVo.getSelectedClass());
		}
		System.out.println(execute.getResult());

	}

	/**
	 * 根据appkey/appSecret获取accessToken
	 * @return
	 */
	public String getAccessToken() throws Exception {
		DefaultDingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
		OapiGettokenRequest request = new OapiGettokenRequest();
		request.setAppkey(appkey);
		request.setAppsecret(appsecret);
		request.setHttpMethod("GET");
		OapiGettokenResponse response = client.execute(request);
		return response.getAccessToken();
	}




}
