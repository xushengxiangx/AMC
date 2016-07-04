/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package com.amc.web.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.amc.cfg.Constant;
import com.amc.domain.AmcPayable;
import com.amc.domain.AmcPayabledetail;
import com.amc.domain.AmcProduct;
import com.amc.domain.AmcPurchaseorder;
import com.amc.domain.AmcReplenishdetail;
import com.amc.domain.AmcReplenishsheet;
import com.amc.domain.AmcUser;
import com.amc.service.PayableService;
import com.amc.service.ReplenishService;
import com.amc.service.WantSlipService;
import com.amc.util.DateUtils;
import com.amc.util.StringUtils;

/** 
 * MyEclipse Struts
 * Creation date: 12-23-2015
 * 
 * XDoclet definition:
 * @struts.action parameter="method"
 */
public class ReplenishAction extends DispatchAction {
	
	private ReplenishService replenishService;
	private PayableService payableService;
	private WantSlipService wantSlipService;
	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		String pageNowString = (String) request.getParameter("pageNow");
		List<AmcReplenishsheet> replenishsheets=null;
		int pageNow,pageCount;
		if (StringUtils.isEmpty(pageNowString)) {// 如果为空
			int count = replenishService.getReplenishCounts() ;
			if (count % Constant.pageSize == 0) {
				pageCount = (int) Math.floor(count / Constant.pageSize);
			} else {
				pageCount = (int) Math.floor(count / Constant.pageSize) + 1;
			}
			pageNow=1;
		} else {
			pageCount=Integer.parseInt(request.getParameter("pageCount"));
			pageNow=Integer.parseInt(pageNowString);		
		}
		replenishsheets = replenishService.getReplenishsByPage(pageNow, Constant.pageSize);
		request.setAttribute("pageCount",pageCount);
		request.setAttribute("pageNow",pageNow);
		request.setAttribute("rlist", replenishsheets);
		return mapping.findForward("list");
	}
	public ActionForward showDetail(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		String rid=request.getParameter("rid");
		AmcReplenishsheet replenishsheet=replenishService.findReplenishById(Integer.parseInt(rid));
		List<AmcReplenishdetail> rdetails = replenishService.getRdetails(replenishsheet);
		request.setAttribute("rs",replenishsheet);
		request.setAttribute("rslist",rdetails);
		return mapping.findForward("showDetail");
	}
	public ActionForward complete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		String rid=request.getParameter("rid");
		AmcUser user=(AmcUser) request.getSession().getAttribute("user");
		AmcReplenishsheet replenishsheet=replenishService.findReplenishById(Integer.parseInt(rid));
		replenishsheet.setRstate(0);
		replenishsheet.setAmcUser(user);
		//更新收货状态
		replenishService.update(replenishsheet);
		
		//修改采购订单已完成 
		AmcPurchaseorder purchaseorder=replenishsheet.getAmcPurchaseorder();
		purchaseorder.setPostate(0);
		replenishService.update(purchaseorder);
		//添加应付账款
		AmcPayable payable=new AmcPayable();
	    List<AmcPayabledetail> pList=new ArrayList<AmcPayabledetail>();
		payable.setAmcPurchaseorder(purchaseorder);
		payable.setAmcUser(user);
		payable.setPayctime(new Date());
		payable.setPaydate(DateUtils.getDateByOffset(new Date(), 30));//默认三十天内付款
		payable.setPaystate(1);//未付款
		//修改库存  和库存状态和库存缺货数据
		List<AmcReplenishdetail> rdetails = replenishService.getRdetails(replenishsheet);
		float totalAccount=0.0f;
		for(AmcReplenishdetail replenishdetail:rdetails){
			AmcProduct product=replenishdetail.getAmcProduct();
			int amount=replenishdetail.getRdamount();
			Float price=product.getPpurchaseprice();
			product.setPinventoryamount(product.getPinventoryamount()+amount);
			product.setPshortamount(product.getPshortamount()-amount);
			if(product.getPinventoryamount()+amount>=product.getPsafeamount()){
				product.setPinventorystate(1);
			}
			AmcPayabledetail payabledetail=new AmcPayabledetail();
			payabledetail.setAmcPayable(payable);
			payabledetail.setAmcProduct(product);
			payabledetail.setPaydamount(amount);
			payabledetail.setPaydprice(price);
			payabledetail.setPaydsum(price*amount);
			pList.add(payabledetail);
			
			replenishService.update(product);
			totalAccount+=price*amount;
		}
		payable.setPaytotalaccount(totalAccount);		
		payableService.addPayable(payable, pList);
		//修改采购中的缺货单状态为已完成
		wantSlipService.finishPurchasingWantState();
		
		return list(mapping, form, request, response);
	}
	public ReplenishService getReplenishService() {
		return replenishService;
	}
	public void setReplenishService(ReplenishService replenishService) {
		this.replenishService = replenishService;
	}
	public PayableService getPayableService() {
		return payableService;
	}
	public void setPayableService(PayableService payableService) {
		this.payableService = payableService;
	}
	public WantSlipService getWantSlipService() {
		return wantSlipService;
	}
	public void setWantSlipService(WantSlipService wantSlipService) {
		this.wantSlipService = wantSlipService;
	}
	
	
}