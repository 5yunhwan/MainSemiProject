package jaesik.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import common.controller.AbstractController;
import jaesik.model.JS_InterMemberDAO;
import jaesik.model.JS_MemberDAO;
import yunhwan.model.ProductVO;

public class KidsProductAction extends AbstractController {

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		JS_InterMemberDAO dao = new JS_MemberDAO();
		
		super.setRedirect(false);
		super.setViewPage("/WEB-INF/jaesik/productList.jsp");
	}

}