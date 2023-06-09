package sujin.controller;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import common.controller.AbstractController;
import sujin.model.InterMemberDAO;
import sujin.model.MemberDAO;

public class PwdUpdateAction extends AbstractController { // 비번변경 하다가 말았음ㅋㅋㅋㅋㅋ 일시중지!!

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) throws Exception {
		
		String method = request.getMethod();  // "GET" 또는 "POST"
		
		HttpSession session = request.getSession();
		
	//	session.setAttribute("loginuser", loginuser); // session 에 로그인된 사용자를 "loginuser" 로 저장

				
		//*** [GET방식일때 form 만 보이다가 버튼을 클릭해 POST 방식으로 암호를 변경하면 변경된게 DB 에도 적용되게 하고 메인으로 로그인창으로 간다 ]
		if("GET".equals(method)) {
			
			request.setAttribute("method", method);
			
			super.setRedirect(false); /* 꼭 써서 비밀번호변경 끝나고 true 로 바뀐것을 다시 false 로 바꿔줌 */
			super.setViewPage("/WEB-INF/sujin/login/pwdUpdateEnd.jsp");	
		}
		else { // 암호변경하기 버튼을 클릭한 경우
			
			String pwd = request.getParameter("pwd"); // 넘어온 새암호
			
			Map<String, String> paraMap = new HashMap<>();
			paraMap.put("pwd", pwd);

			// #### 비밀번호 변경이 성공되면 자동으로 로그인 되도록 하겠다 ####
			try {
				
				InterMemberDAO mdao = new MemberDAO();
				int n = mdao.pwdUpdate(paraMap);
				
				System.out.println("비밀번호 변경 성공하면 n=1 : " + n);
				
				if(n==1) { // 비밀번호 변경이 성공되면,
					
				//	System.out.println("~~~ 요기요3 userid : " + userid);
				//	System.out.println("~~~ 요기요3 pwd : " + pwd);
					
					request.setAttribute("pwd", pwd);
					
					super.setRedirect(false);
					super.setViewPage("/WEB-INF/sujin/login/pwdResetAfterAutoLogin.jsp"); // 이 페이지로간다
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
				
				String message = "SQL구문 오류발생";
				String loc = "javascript:history.go()"; // 새로고침
				
				request.setAttribute("message", message);
				request.setAttribute("loc", loc);
				
				super.setRedirect(false);
				super.setViewPage("/WEB-INF/sujin/msg.jsp");
			}
					
		}//end of "POST" 로 버튼누른경우---------------------------
		
	}

}
