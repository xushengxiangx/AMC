/*
 * Generated by MyEclipse Struts
 * Template path: templates/java/JavaClass.vtl
 */
package com.amc.web.action;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.amc.cfg.Constant;
import com.amc.domain.AmcCustomer;
import com.amc.domain.AmcProduct;
import com.amc.domain.AmcSalesorder;
import com.amc.domain.AmcSodetail;
import com.amc.domain.AmcUser;
import com.amc.service.CustomerService;
import com.amc.service.ProductService;
import com.amc.service.SalesOrderService;
import com.amc.service.UserService;
import com.amc.util.DateUtils;
import com.amc.util.StringUtils;

/** 
 * MyEclipse Struts
 * Creation date: 12-12-2015
 * 
 * XDoclet definition:
 * @struts.action parameter="method"
 */
public class SalesOrderAction extends DispatchAction {
	
	private SalesOrderService salesOrderService;
	private CustomerService customerService;
	private ProductService productService;
	private UserService userService;
	@SuppressWarnings("unchecked")
	public ActionForward list(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		String pageNowString = (String) request.getParameter("pageNow");
		List<AmcSalesorder> orders=null;
		int pageNow,pageCount;
		if (StringUtils.isEmpty(pageNowString)) {
			int count=salesOrderService.getSalesOrderCount();
			if(count%Constant.pageSize==0){
				pageCount = (int) Math.floor( count/ Constant.pageSize) ;
			}else{
				pageCount = (int) Math.floor( count/ Constant.pageSize) + 1;
			}
			pageNow=1;
						
		} else {
			pageCount=Integer.parseInt(request.getParameter("pageCount"));
			pageNow=Integer.parseInt(pageNowString);
		}
		orders=salesOrderService.getSalesOrdersByPage(pageNow, Constant.pageSize);
		request.setAttribute("pageCount",pageCount);
		request.setAttribute("pageNow",pageNow);
		request.setAttribute("salesorders", orders);
		return mapping.findForward("list");
	}
	public ActionForward showDetail(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		String soidString = (String) request.getParameter("soid");
		AmcSalesorder so=salesOrderService.findSalesOrderById(Integer.parseInt(soidString));
		List<AmcSodetail> salesOrderDetail = salesOrderService.getSalesOrderDetail(so.getSoid());
		
		request.setAttribute("salesorder", so);
		request.setAttribute("salesOrderDetail", salesOrderDetail);
		return mapping.findForward("showDetail");
	}
	public ActionForward addForword(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		// TODO Auto-generated method stub
		
		List customers = customerService.getCustomers();
		List<AmcProduct> products = productService.getAllProducts();
		
		request.setAttribute("customers", customers);
		request.setAttribute("products", products);
		return mapping.findForward("addForword");
	}
	public ActionForward add(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException {
		// TODO Auto-generated method stub
		Map<String, String[]> map=request.getParameterMap();
		AmcSalesorder order=new AmcSalesorder();
		order.setAmcUser(userService.loadByName(map.get("cuser")[0]));
		order.setSoctime(DateUtils.parseDateFromString(map.get("ctime")[0]));
		order.setSoReceivePerson(map.get("creceiver")[0]);
		order.setSoaddress(map.get("caddress")[0]);
		order.setSostate("2");
		order.setAmcCustomer(customerService.findCustomerById(Integer.parseInt(map.get("cid")[0])));
		if(salesOrderService.checkIsExisted(order)!=null){
			// 返回已存在
			response.getWriter().write("-1");
			return null;
		}
		String sodetail=map.get("sodetail")[0];
		String[] sods= sodetail.split("&\\^&");
		List<AmcSodetail> sodetailList=new ArrayList<AmcSodetail>();
		for(int i=0;i<sods.length;i++){
			String[] sod=sods[i].split("&");
			AmcSodetail amcSodetail=new AmcSodetail();
			int amount=Integer.parseInt(sod[1]);
			AmcProduct product=productService.loadProductById(Integer.parseInt(sod[0]));
		
			amcSodetail.setAmcProduct(product);
			amcSodetail.setAmcSalesorder(order);
			amcSodetail.setSodamount(amount);
			sodetailList.add(amcSodetail);
			//产品表改变 预计缺货数量
			product.setPshortamount(product.getPshortamount()+amount);
			productService.update(product);
		}
		
		salesOrderService.addSalesOrder(order, sodetailList);
		response.getWriter().write("1");
		return null;
		
		
	    
		
		
	}
	public ActionForward delete(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		String soid=request.getParameter("soid");
		AmcSalesorder salesorder=salesOrderService.findSalesOrderById(Integer.parseInt(soid));
		String state=salesorder.getSostate();
		if(!state.equals("0")){
			salesOrderService.delete(salesorder);
			response.getWriter().write("1");
			return null;
		}
		response.getWriter().write("-1");
		return null;
	}

	public SalesOrderService getSalesOrderService() {
		return salesOrderService;
	}

	public void setSalesOrderService(SalesOrderService salesOrderService) {
		this.salesOrderService = salesOrderService;
	}
	public CustomerService getCustomerService() {
		return customerService;
	}
	public void setCustomerService(CustomerService customerService) {
		this.customerService = customerService;
	}
	public ProductService getProductService() {
		return productService;
	}
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}
	public UserService getUserService() {
		return userService;
	}
	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	
	
}