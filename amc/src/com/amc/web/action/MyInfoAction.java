/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package com.amc.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.amc.domain.AmcUser;
import com.amc.service.UserService;
import com.amc.util.MDUtil;
import com.amc.web.form.UpdatePwdForm;

/** 
 * MyEclipse Struts
 * Creation date: 12-12-2015
 * 
 * XDoclet definition:
 * @struts.action parameter="method" validate="true"
 */
public class MyInfoAction extends DispatchAction {
	/*
	 * Generated Methods
	 */
	private UserService userService;

	
	public ActionForward updatePassword(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		UpdatePwdForm pwdForm=(UpdatePwdForm)form;
		AmcUser user=(AmcUser) request.getSession().getAttribute("user");
		if(MDUtil.md5Encode(pwdForm.getUnowpassword()).equals(user.getPassword())){
			user.setPassword(MDUtil.md5Encode(pwdForm.getUnewpassword()));
			userService.update(user);
			return mapping.findForward("ok");
		}
		return mapping.findForward("error");
	}


	public UserService getUserService() {
		return userService;
	}


	public void setUserService(UserService userService) {
		this.userService = userService;
	}
	
	
}