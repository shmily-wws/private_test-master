package com.hnnp.erp.administration.controller;

import com.hnnp.erp.administration.model.DocModel;
import com.hnnp.erp.administration.service.DocService;
import com.hnnp.erp.notice.model.Notice;
import com.hnnp.erp.notice.service.INoticeService;
import com.hnnp.framework.core.controller.BaseController;
import com.hnnp.framework.core.model.DataDictDetail;
import com.hnnp.framework.core.model.User;
import com.hnnp.framework.core.service.IDataDictDetailService;
import com.hnnp.framework.core.service.IDataDictNameService;
import com.hnnp.framework.core.service.OrganizationService;
import com.hnnp.framework.core.service.UserService;
import com.hnnp.framework.core.util.LongCalendar;
import com.hnnp.framework.core.util.Pager;
import com.hnnp.pms_staffconfig.service.StaffConfigService;
import com.hnnp.wf.controller.WfBasicFun;
import com.hnnp.wf.model.WfApprulesLog;
import com.hnnp.wf.service.WfAppgroupService;
import com.hnnp.wf.service.WfApprulesInfoService;
import com.hnnp.wf.service.WfApprulesService;
import com.hnnp.wf.service.WfPublicService;
import com.hnnp.wf.util.WfConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.util.*;

@Controller
@RequestMapping("/doc")
public class DocController extends BaseController {
	@Autowired
	DocService docService;
	
	@Autowired
	UserService userService;
	
	@Autowired
	OrganizationService orgService;
	
	@Autowired
	WfBasicFun wfBasicFun;
	
	@Autowired
	WfAppgroupService wfAppgroupService;
	
	@Autowired
	WfApprulesService wfApprulesService; 
	
	@Autowired
	WfApprulesInfoService wfApprulesInfoService;
	
	@Autowired
	StaffConfigService staffConfigService;
	
	@Autowired
	IDataDictDetailService  iDataDictDetailService;
	
	@Autowired
	IDataDictNameService iDataDictNameService;
	
	@Autowired
	WfPublicService wfPublicService;

	@Autowired
	INoticeService iNoticeService;
	/**
	 * 查看所有数据的list
	 * @param doc
	 * @param pageNo
	 * @param pageSize
	 * @param direction
	 * @param sort
	 * @param session
	 * @return
	 * 2016年3月1日下午3:14:56
	 * 2019-12-24 14:24:46
	 * wyn
	 */
	@RequestMapping("/data")
	@ResponseBody
	public Object data(
			@ModelAttribute(value = "doc") DocModel doc,
			@RequestParam(value = "pager.pageNo", defaultValue = "1") int pageNo,
			@RequestParam(value = "pager.pageSize", defaultValue = "20") int pageSize,
			String direction, String sort,HttpSession session,HttpServletRequest request) {

		String type=request.getParameter("type");
		doc.setDocType(type);
		if (direction == null || "".equals(direction)) {
			direction = "desc";
		}
		if (sort == null || "".equals(sort)) {
			sort = "create_time";
		}
		Pager pager = new Pager();
		pager.setPageNo(pageNo);
		pager.setPageSize(pageSize);
		 
		User user=(User)session.getAttribute("user");
	/*	 
		if(staffConfigService.validateCodeAndUserId("company_leader", user.getId())){
			doc.setWfState("2");
		}else if(staffConfigService.validateCodeAndUserId("department_manager", user.getId())){
			doc.setWfState("2");
		}else {
			//自己创建项目
			doc.setCreateUserId(user.getId());
		}
		*/
		
		List<DocModel> listdoc=docService.getAllList(doc, pager, sort, direction,user);
 		List<Map> resultList = new ArrayList();
		for (int i = 0; i < listdoc.size(); i++) {
			DocModel record = listdoc.get(i);
		    Map map=docToMap(record,user);
			resultList.add(map);
		}
		// 构造返回的json对象
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pager.pageNo", pager.getPageNo());
		map.put("pager.totalRows",
				docService.getAllList(doc,null, sort, direction,user).size());
		map.put("rows", resultList);
		map.put("sort", sort);
		map.put("direction", direction);
		return map;

	}
  
	@RequestMapping("/addDoc")
	public String addDoc(HttpServletRequest request){
	    String type=request.getParameter("type");
		String 	direction = "desc";
		 
		String	sort = "create_time";
		 
		int i=docService.getAllList(null, null, sort, direction, null).size();
		Date now=new Date();
		int y=now.getYear();
		String docLsh="HNNP-HTWJ-"+y+"-"+i;
		request.setAttribute("docLsh", docLsh);
		if(type.equals("0")){
			return "officalDoc/addOfficialDocument";
		}else {
			return "meetingMinutes/addMeetingMinutes";
		}
		
	}
	
	//从数据字典中获取请假的类型
	@RequestMapping("/getdocType")
	public String getAttendanceType(HttpServletRequest request){
 		List<DataDictDetail> listdetail=iDataDictDetailService.getAllDataDictDetailByCode("meetType");
		 request.setAttribute("list", listdetail);
		return "/meetingMinutes/meetType";
	}
	
 /**
  * 添加保存成功
  * @param doc
  * @param session
  * @param request
  * @return
  * @throws ParseException
  * 2016年3月1日下午3:15:42
  * wyn
  */
	@RequestMapping("/saveSuccess")
	@ResponseBody
	public String saveSuccess(DocModel doc,HttpSession session,HttpServletRequest request) throws ParseException{
		User user=(User)session.getAttribute("user");
	 
		doc.setDocTime(request.getParameter("doctime"));
		doc.setCreateUserId(user.getId());
		doc.setWfState("0");
		doc.setRemove("0");
		doc.setCreateOrgId(user.getOrg().getId());
 		docService.addDoc(doc);
		return "success";
	}
	
 /**
  * 添加提交成功
  * @param doc
  * @param session
  * @param request
  * @return
  * @throws Exception
  * 2016年3月1日下午3:15:56
  * wyn
  */
	@RequestMapping("/addSuccess")
	@ResponseBody
	public String addSuccess(DocModel doc,HttpSession session,HttpServletRequest request) throws Exception{
		User user=(User)session.getAttribute("user");
		 
		doc.setDocTime(request.getParameter("doctime"));
		doc.setCreateUserId(user.getId());
		doc.setWfState("1");// 0创建、1审核中、2完成，3拒绝，4退回
		doc.setRemove("0");
		doc.setCreateOrgId(user.getOrg().getId());
		docService.addDoc(doc);
		if(doc.getDocType().equals("0")){
			String wfid = wfBasicFun.startWf(WfConstant.doc_wf,user.getOrg().getName()+user.getName()+WfConstant.doc_wf_dbbt,
					  user.getId(),doc.getId());
			
			wfBasicFun.appPass(wfid, "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
			doc.setWfId(wfid);
		}else{
			String wfid = wfBasicFun.startWf(WfConstant.meet_wf,user.getOrg().getName()+user.getName()+WfConstant.meet_wf_dbbt,
					  user.getId(),doc.getId());
			
			wfBasicFun.appPass(wfid, "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
			doc.setWfId(wfid);
		}
		
 		docService.updateDocById(doc);
 		return "success";
		
	}
    
  /**
   * 删除数据
   * @param id
   * @param request
   * @return
   * 2016年3月1日下午3:16:15
   * wyn
   */
	@RequestMapping("/deleteDoc")
	@ResponseBody
	public String deleteDoc(String id,HttpServletRequest request){
		id=id+"0";
		docService.updateRemoveByPrimaryKey(id);
		String ids[]=id.split(",");
		for(int i=0;i<(ids.length-1);i++){
 			//删除项目时，删除流程信息
			if(nullOrEmpty(docService.getDocById(ids[i]).getWfId())){
				wfPublicService.deleteAllByWfid(docService.getDocById(ids[i]).getWfId());
			}
		}
		return "success";
	}
	
	 /**
	  * 跳转到修改页面
	  * @param request
	  * @return
	  * 2016年3月1日下午3:16:28
	  * wyn
	  */
	@RequestMapping("/editDoc")
	public String editDoc(HttpServletRequest request){
	    String id=request.getParameter("id");
	    DocModel doc=docService.getDocById(id);
	    request.setAttribute("doc", doc);
	    
	    if(doc.getDocType().equals("0")){
	    	return "/officalDoc/editOfficialDocument";
		}else {
			return "/meetingMinutes/editMeetingMinutes";
		}
 		
	}
	
	/**
	 * 修改保存成功
	 * @param doc
	 * @param session
	 * @param request
	 * @return
	 * @throws ParseException
	 * 2016年3月1日下午3:16:42
	 * wyn
	 */
	@RequestMapping("/updateSaveDoc")
	@ResponseBody
	public String updateSaveDoc(DocModel doc,HttpSession session,HttpServletRequest request) throws ParseException{
		User user=(User)session.getAttribute("user");
		doc.setCreateUserId(user.getId());
		if(nullOrEmpty(doc.getWfId())){
			doc.setWfState("4");
		}else{
			doc.setWfState("0");
		}
		doc.setRemove("0");
		doc.setCreateOrgId(user.getOrg().getId());
		doc.setCreateTime(new Date());
 		doc.setDocTime(request.getParameter("doctime"));
 		docService.updateDocById(doc);
		return "success";
	}
	
 /**
  * 修改提交成功
  * @param doc
  * @param session
  * @param request
  * @return
  * @throws Exception
  * 2016年3月1日下午3:16:57
  * wyn
  */
	@RequestMapping("/updateSuccess")
	@ResponseBody
	public String updateSuccess(DocModel doc,HttpSession session,HttpServletRequest request) throws Exception{
		User user=(User)session.getAttribute("user");
		doc.setCreateUserId(user.getId());
		doc.setWfState("1");
		doc.setRemove("0");
		doc.setCreateOrgId(user.getOrg().getId());
		doc.setCreateTime(new Date());
 		doc.setDocTime(request.getParameter("doctime"));
		if(nullOrEmpty(doc.getWfId())){
			if(doc.getDocType().equals("0")){
				wfBasicFun.reStart(doc.getWfId(), WfConstant.doc_wf, user.getOrg().getName()+user.getName()+WfConstant.doc_wf_dbbt, user.getId(), doc.getId());
				wfBasicFun.appPass(doc.getWfId(), "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
			}else{
				wfBasicFun.reStart(doc.getWfId(), WfConstant.meet_wf, user.getOrg().getName()+user.getName()+WfConstant.meet_wf_dbbt, user.getId(), doc.getId());
				wfBasicFun.appPass(doc.getWfId(), "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
			}
			
		}else{
			if(doc.getDocType().equals("0")){
				String wfid = wfBasicFun.startWf(WfConstant.doc_wf,user.getOrg().getName()+user.getName()+WfConstant.doc_wf_dbbt,
						  user.getId(),doc.getId());
				
				wfBasicFun.appPass(wfid, "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
				doc.setWfId(wfid);
			}else{
				String wfid = wfBasicFun.startWf(WfConstant.meet_wf,user.getOrg().getName()+user.getName()+WfConstant.meet_wf_dbbt,
						  user.getId(),doc.getId());
				
				wfBasicFun.appPass(wfid, "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
				doc.setWfId(wfid);
			}
			
			
		}
 		docService.updateDocById(doc);
		return "success";
	}
 
	 /**
	  * 直接提交成功
	  * @param id
	  * @param session
	  * @return
	  * @throws Exception
	  * 2016年3月1日下午3:17:08
	  * wyn
	  */
	@RequestMapping("tjSuccess")
	@ResponseBody
	public String tjSuccess(String id,HttpSession session) throws Exception{
		User user=(User)session.getAttribute("user");
		DocModel doc=docService.getDocById(id);
		if(nullOrEmpty(doc.getWfId())){
			if(doc.getDocType().equals("0")){
				wfBasicFun.reStart(doc.getWfId(), WfConstant.doc_wf, user.getOrg().getName()+userService.getUserInfoById(doc.getCreateUserId()).getName()+WfConstant.doc_wf_dbbt, user.getId(), doc.getId());
				wfBasicFun.appPass(doc.getWfId(), "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
			}else{
				wfBasicFun.reStart(doc.getWfId(), WfConstant.meet_wf, user.getOrg().getName()+userService.getUserInfoById(doc.getCreateUserId()).getName()+WfConstant.meet_wf_dbbt, user.getId(), doc.getId());
				wfBasicFun.appPass(doc.getWfId(), "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
			}
		}else{
			if(doc.getDocType().equals("0")){
				String wfid = wfBasicFun.startWf(WfConstant.doc_wf,user.getOrg().getName()+userService.getUserInfoById(doc.getCreateUserId()).getName()+WfConstant.doc_wf_dbbt,
						  user.getId(),doc.getId());
				wfBasicFun.appPass(wfid, "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
				doc.setWfId(wfid);	
			}else{
				String wfid = wfBasicFun.startWf(WfConstant.meet_wf,user.getOrg().getName()+userService.getUserInfoById(doc.getCreateUserId()).getName()+WfConstant.meet_wf_dbbt,
						  user.getId(),doc.getId());
				wfBasicFun.appPass(wfid, "流程发起", Integer.valueOf("0"), user.getId(),doc.getId());
				doc.setWfId(wfid);	
			}
		}
			doc.setWfState("1");
		docService.updateDocById(doc);
		return "success";
	}
	
	 /**
	  *跳转到审批页面
	  * @param model
	  * @param request
	  * @param session
	  * @return
	  * 2016年3月1日下午3:17:20
	  * wyn
	  */
	@RequestMapping("/approval")
	public String approval(Model model,HttpServletRequest request,HttpSession session) {
		String wfid = request.getParameter("wfid");
		DocModel doc=docService.getDocByWfid(wfid);
		Map mapdoc=docToMap(doc,null);
		String bh = request.getParameter("bh");
		String xjbh = request.getParameter("xjbh");
		List<WfApprulesLog> listAppRecord = wfBasicFun.getAllAppRecord(wfid);
		for(int i=0;i<listAppRecord.size();i++){
			WfApprulesLog log = listAppRecord.get(i);
			log.setSpr(userService.getUserNamesByUserIds(log.getSpr()));
			log.setCkym(Util.getDateToString(log.getSpsj()));
		}
		model.addAttribute("wfid", wfid);
		model.addAttribute("mapdoc", mapdoc);
		model.addAttribute("doc", doc);
		model.addAttribute("bh", bh);
		model.addAttribute("xjbh", xjbh);
		model.addAttribute("listAppRecord", listAppRecord);
        return "/officalDoc/approval";
	}
	
	@RequestMapping("/approvalIssue")
	public String approvalIssue(Model model,HttpServletRequest request,HttpSession session) {
	String wfid = request.getParameter("wfid");
	DocModel doc=docService.getDocByWfid(wfid);
	Map mapdoc=docToMap(doc,null);
	String bh = request.getParameter("bh");
	String xjbh = request.getParameter("xjbh");
	List<WfApprulesLog> listAppRecord = wfBasicFun.getAllAppRecord(wfid);
	for(int i=0;i<listAppRecord.size();i++){
		WfApprulesLog log = listAppRecord.get(i);
		log.setSpr(userService.getUserNamesByUserIds(log.getSpr()));
		log.setCkym(Util.getDateToString(log.getSpsj()));
	}
	model.addAttribute("wfid", wfid);
	model.addAttribute("mapdoc", mapdoc);
	model.addAttribute("doc", doc);
	model.addAttribute("bh", bh);
	model.addAttribute("xjbh", xjbh);
	model.addAttribute("listAppRecord", listAppRecord);
	 return "/officalDoc/approvalIssue";
	}
	

	
	@RequestMapping("/approvalSend")	
	public String approvalSend(Model model,HttpServletRequest request,HttpSession session) {
		String wfid = request.getParameter("wfid");
		DocModel doc=docService.getDocByWfid(wfid);
		Map mapdoc=docToMap(doc,null);
		String bh = request.getParameter("bh");
		String xjbh = request.getParameter("xjbh");
		List<WfApprulesLog> listAppRecord = wfBasicFun.getAllAppRecord(wfid);
		for(int i=0;i<listAppRecord.size();i++){
			WfApprulesLog log = listAppRecord.get(i);
			log.setSpr(userService.getUserNamesByUserIds(log.getSpr()));
			log.setCkym(Util.getDateToString(log.getSpsj()));
		}
		model.addAttribute("wfid", wfid);
		model.addAttribute("mapdoc", mapdoc);
		model.addAttribute("doc", doc);
		model.addAttribute("bh", bh);
		model.addAttribute("xjbh", xjbh);
		model.addAttribute("listAppRecord", listAppRecord);
		 return "/officalDoc/approvalSend";
	}
	@RequestMapping("/approvalReceive")
	
	public String approvalReceive(Model model,HttpServletRequest request,HttpSession session) {
		String wfid = request.getParameter("wfid");
		DocModel doc=docService.getDocByWfid(wfid);
		Map mapdoc=docToMap(doc,null);
		String bh = request.getParameter("bh");
		String xjbh = request.getParameter("xjbh");
		List<WfApprulesLog> listAppRecord = wfBasicFun.getAllAppRecord(wfid);
		for(int i=0;i<listAppRecord.size();i++){
			WfApprulesLog log = listAppRecord.get(i);
			log.setSpr(userService.getUserNamesByUserIds(log.getSpr()));
			log.setCkym(Util.getDateToString(log.getSpsj()));
		}
		model.addAttribute("wfid", wfid);
		model.addAttribute("mapdoc", mapdoc);
		model.addAttribute("doc", doc);
		model.addAttribute("bh", bh);
		model.addAttribute("xjbh", xjbh);
		model.addAttribute("listAppRecord", listAppRecord);
		 return "/officalDoc/approvalReceive";
	}
	
	@RequestMapping("/approvalArchive")
	
	public String approvalArchive(Model model,HttpServletRequest request,HttpSession session) {
		String wfid = request.getParameter("wfid");
		DocModel doc=docService.getDocByWfid(wfid);
		Map mapdoc=docToMap(doc,null);
		String bh = request.getParameter("bh");
		String xjbh = request.getParameter("xjbh");
		List<WfApprulesLog> listAppRecord = wfBasicFun.getAllAppRecord(wfid);
		for(int i=0;i<listAppRecord.size();i++){
			WfApprulesLog log = listAppRecord.get(i);
			log.setSpr(userService.getUserNamesByUserIds(log.getSpr()));
			log.setCkym(Util.getDateToString(log.getSpsj()));
		}
		model.addAttribute("wfid", wfid);
		model.addAttribute("mapdoc", mapdoc);
		model.addAttribute("doc", doc);
		model.addAttribute("bh", bh);
		model.addAttribute("xjbh", xjbh);
		model.addAttribute("listAppRecord", listAppRecord);
		 return "/officalDoc/approvalArchive";
	}
	/**
	 * 审批通过
	 * @param model
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 * @throws Exception
	 * 2016年3月1日下午3:17:52
	 * wyn
	 */
	@RequestMapping("/docPass")
	@ResponseBody
	public String docPass(Model model,HttpServletRequest request,HttpServletResponse response,HttpSession session) throws Exception {
		response.setContentType("text/html;charset=UTF-8");
		User user = (User)session.getAttribute("user");
		String wfid = request.getParameter("wfid");
		String bh = request.getParameter("bh");
		String spyj = request.getParameter("spyj");//意见
		boolean isLastApp = wfBasicFun.isLastApp(wfid);
		System.out.println("验收管理 是否为最后一级审批："+isLastApp);
		String id=request.getParameter("id");
		DocModel doc=docService.getDocById(id);
		//调用审批通过方法进行处理
		wfBasicFun.appPass(wfid,spyj,Integer.valueOf(bh),user.getId(),doc.getId());
		if(isLastApp==true){
			doc.setWfState("2");
			docService.updateDocById(doc);
		}
        return "success";
	}
	
 
	 /**
	  * 审批拒绝（直接跳转到最后一步）
	  * @param model
	  * @param request
	  * @param response
	  * @param session
	  * @return
	  * @throws Exception
	  * 2016年3月1日下午3:18:02
	  * wyn
	  */
	@RequestMapping("/docEnd")
	@ResponseBody
	public String docEnd(Model model,HttpServletRequest request,HttpServletResponse response,HttpSession session) throws Exception {
		response.setContentType("text/html;charset=utf-8");
		User user = (User)session.getAttribute("user");
		String id=request.getParameter("id");
		DocModel doc=docService.getDocById(id);
		wfBasicFun.revokeByFqr(doc.getWfId(),  user.getId());
		doc.setWfState("3");//3是拒绝
		docService.updateDocById(doc);
		
		return "success";
	}
	
	//审核退回给上个审批人
	@RequestMapping("/appback")
	@ResponseBody
	public String appback(Model model,HttpServletRequest request,HttpServletResponse response,HttpSession session) throws Exception {
		response.setContentType("text/html;charset=utf-8");
		User user = (User)session.getAttribute("user");
		String spyj = request.getParameter("spyj");
		String id=request.getParameter("id");
		DocModel doc=docService.getDocById(id);
		String wfid = doc.getWfId();
		String bh = request.getParameter("bh");
		int bhob=wfBasicFun.thzPreNode(wfid, bh, spyj, user.getId(), doc.getId());
	    if(bhob==0){
	    	doc.setWfState("4");//3是拒绝,4是退回
			docService.updateDocById(doc);
		}
		return "success";
	}
	
	 
	    /**
	     * 回退给创建人
	     * @param model
	     * @param request
	     * @param response
	     * @param session
	     * @return
	     * @throws Exception
	     * 2016年3月1日下午3:18:41
	     * wyn
	     */
		@RequestMapping("/backPass")
		@ResponseBody
		public String backPass(Model model,HttpServletRequest request,HttpServletResponse response,HttpSession session) throws Exception {
			response.setContentType("text/html;charset=utf-8");
			User user = (User)session.getAttribute("user");
			String wfid = request.getParameter("wfid");
			String bh = request.getParameter("bh");
			String spyj = request.getParameter("spyj");
			System.out.println("回退工作流----->>"+bh);
			String id=request.getParameter("id");
			DocModel doc=docService.getDocById(id);
			wfBasicFun.thzFqr(wfid, spyj, Integer.valueOf(bh), user.getId(), id);
			doc.setWfState("4");//3是拒绝,4是退回
			docService.updateDocById(doc);
			return "success";
		}
		
		 //回退至发起人
	    @RequestMapping("/appReturnToCreator")
	    @ResponseBody
	    public Object appReturnToCreator(Model model,String wfid,String spyj,String bh,HttpSession session) throws Exception {
	        User user = (User)session.getAttribute("user");
	        System.out.println("回退工作流");
	        DocModel doc=docService.getDocByWfid(wfid);
	        wfBasicFun.thzFqr(wfid, spyj, Integer.valueOf(bh), user.getId(), doc.getId());
	        doc.setWfState("4");
	        docService.updateDocById(doc);
	        //通知发起人部门经理流程回退
	        WfApprulesLog wfApprulesLog = wfBasicFun.getAllAppRecord(wfid).get(0);
	        Notice notice = new Notice();
	        notice.setName("您所在部门" + userService.getUserInfoById(wfApprulesLog.getFqr()).getName() + "发起的" + wfApprulesLog.getDbbt() + "-被" + user.getName() + "回退至发起人");
	        notice.setCreateUserId(user.getId());
	        notice.setCreateTime(new Date());
	        notice.setCreateDptId(user.getOrg().getId());
	        notice.setClassify(wfApprulesLog.getWflogo());
	        notice.setStatus(0);
	        notice.setContent(spyj);
	        List<User> listUser = userService.getDptManager(doc.getCreateOrgId(),"department_manager");
	        for(User user1 : listUser){
	            notice.setReceiverId(user1.getId());
	            iNoticeService.addNotice(notice);
	        }
	        Map map = new HashMap<>();
	        map.put("status","success");
	        return map;
	    }
	 /**
	  * 查看页面
	  * @param request
	  * @param model
	  * @return
	  * 2016年3月1日下午3:19:15
	  * wyn
	  */
	@RequestMapping("/viewDoc")
	public String viewDoc(HttpServletRequest request, Model model) {
		String id = request.getParameter("id");
		DocModel doc=docService.getDocById(id);
		Map mapdoc=docToMap(doc,null);
		List<WfApprulesLog> list=new ArrayList<WfApprulesLog>();
		if(nullOrEmpty(doc.getWfId())){
			if(doc.getWfState().equals("2")||doc.getWfState().equals("3")){
				list=wfBasicFun.getAllAppRecord_isEnd(doc.getWfId());
			}else{
				list=wfBasicFun.getAllAppRecord(doc.getWfId());
			}
			for(int i=0;i<list.size();i++){
				WfApprulesLog log = list.get(i);
				String Strid=(log.getSpr()==null?log.getFqr():log.getSpr());
				log.setSpr(userService.getUserNamesByUserIds(log.getSpr()));
				log.setCkym(LongCalendar.dateToStr(log.getSpsj(),"yyyy-MM-dd HH:mm:ss"));
			}
		}
		model.addAttribute("doc",doc);
		model.addAttribute("mapdoc", mapdoc);
		model.addAttribute("list", list);
		
		if(doc.getDocType().equals("0")){
			return "/officalDoc/detailDoc";
		}else{
			return "/meetingMinutes/meetingMinutesDetail";
		}
		
 		
	} 
	
	/**
	 * 将doc转化为map显示
	 * @param record
	 * @return
	 * wyn
	 *2016年1月28日上午9:24:51
	 */
	public Map docToMap(DocModel record,User user){
		Map map = new HashMap();
		map.put("id", record.getId());
		map.put("createTime", Util.getDateToString(record.getCreateTime()));
		map.put("createUserName",nullOrEmpty(record.getCreateUserId())?userService.getUserInfoById(record.getCreateUserId()).getName():"");
		map.put("createOrgId",nullOrEmpty(record.getCreateOrgId())?record.getCreateOrgId():"");
		map.put("createOrgName",nullOrEmpty(record.getCreateOrgId())?orgService.getOrgById(record.getCreateOrgId()).getName():"");
		map.put("docName",record.getDocName());
		map.put("docBh", record.getBh());
		map.put("docFj", record.getDocFj());
	    if(nullOrEmpty( record.getDocFj())){
	    	map.put("docFjNum", record.getDocFj().split(",").length);
	    }else {
	    	map.put("docFjNum", "0");
		}
		
		map.put("docOrgName", nullOrEmpty(record.getDocOrg())?orgService.getOrgById(record.getDocOrg()).getName():"");
		map.put("docOrg", nullOrEmpty(record.getDocOrg())?record.getDocOrg():"");
		map.put("docTime",nullOrEmpty(record.getDocTime())?(record.getDocTime()):"");
		map.put("docBz", record.getDocBz());
		map.put("docLsh", nullOrEmpty(record.getDocLsh())?record.getDocLsh():"");
		map.put("wfId", nullOrEmpty(record.getWfId())?record.getWfId():"");
		map.put("wfstate", getZt(record.getWfState()));
        map.put("bz", record.getDocBz());
        map.put("docType", record.getDocType());
        if(user!=null){
     			boolean isCre=user.getId().equals(record.getCreateUserId());
     			map.put("isCre",isCre);
     		}
        
        if(nullOrEmpty(record.getDocMeetType())){
			DataDictDetail data=iDataDictDetailService.getDataDictDetailById(record.getDocMeetType());
			 
			map.put("typeName",data.getName());
 
		}
        return map;
	}
	
	
	/**
	 * 
	 * @param zt
	 * @return
	 * wyn
	 *2016年1月27日下午7:51:44
	 */
	public String getZt(String zt){
		//创建、审核中、完成，拒绝，退回
		switch(zt){
		case "0":
			return "创建";
		case "1":
			return "审核中";
		case "2":
			return "完成";
		case "3":
			return "撤消";
		case "4":
			return "退回";
		default:
			return "";
		}
	}
	
	/**
	 * 查看是否为空
	 * @param nr
	 * @return
	 * wyn
	 *2016年1月21日下午3:09:20
	 */
	public boolean nullOrEmpty(Object nr) {
		//true 不为“”和null
		boolean a= false;
		if (nr != null) {
			if (!nr.toString().trim().equals(""))
				 a=  true;
		}  
		if(a){
			return true;
		}else{
			return false;
		}
	}
}
