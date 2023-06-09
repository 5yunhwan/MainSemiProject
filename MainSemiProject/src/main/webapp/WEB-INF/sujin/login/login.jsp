<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<jsp:include page="../../jaesik/header.jsp"/>

<style>

	table#loginTbl {
         width: 100%;
         margin-top: 50px;
         margin-bottom: 50px;
         text-align: center;
         border-collapse: separate;
         border-spacing: 10px; 
    }
   
    table#loginTbl #th {
         font-weight: bold;
         font-size: 30pt;
         text-align: center;
         height: 30px;
         letter-spacing: 10px;
    }
   
    table#loginTbl td {
         line-height: 35px;
         width: 200px;
    }
    
</style>

<script type="text/javascript">

	$(document).ready(function(){ 
	
		// === 1. 로그인을 하지 않은 상태일때, === //
		// -> 로컬스토리지에 저장된 key가 "saveid" 인 userid 값을 불어와서 input 태그 userid 에 넣어준다.
		if( ${empty sessionScope.loginuser} ) { 	
			const loginUserid = localStorage.getItem("saveid");
			
			if(loginUserid != null) {
				$("input#loginUserid").val(loginUserid); 				/* 넣어주고 */
				$("input:checkbox[id='saveid']").prop("checked", true); /* 체크해준다 */
			}
		}
		
		// 2-1. 로그인 버튼 클릭한 경우 
		$("button#btnSubmit").click(function(){
			goLogin(); 		// 로그인 시도한다.
		});
		
		// 2-2. 암호입력란에 엔터를 했을 경우 
		$("input#loginPwd").bind("keydown", function(e){
			if(e.keyCode == 13) { 
				goLogin(); // 로그인 시도한다.
			}
		});
		
		
		// 3. 'x' 표시를 누르거나 '닫기'를 눌러서 아이디찾기를 끝낼 때
		$("button.idFindClose").click(function(){ 
			
			// 대상 iframe 을 선택한다.
			const iframe_idFind = document.getElementById("iframe_idFind");
			
			const iframe_window = iframe_idFind.contentWindow;
			iframe_window.func_idform_reset_empty(); // func_idform_reset_empty 함수는 idFind.jsp 에 있음 	
		});	
		
		
		// 4. 'x' 표시를 누르거나 '닫기'를 눌러서 비밀번호찾기 끝낼 때
		$("button.pwdFindClose").click(function(){ 
			
			/* // 대상 iframe 을 선택한다.
			const iframe_pwdFind = document.getElementById("iframe_pwdFind");
			
			const iframe_window = iframe_pwdFind.contentWindow;
			iframe_window.func_pwdform_reset_empty(); // func_pwdform_reset_empty 함수는 pwdFind.jsp 에 있음 */ 	
			
			history.go(0); // 일단 그냥 새로고침으로 둠;;
		});	
		
		
	});//end of ready()-------------
	
	
	// 로그인 처리 함수선언
	function goLogin(){
		
		const loginUserid = $("input#loginUserid").val().trim();
		const loginPwd = $("input#loginPwd").val().trim();
		
		if(loginUserid == "") {
			alert("아이디를 입력하세요");
			$("input#loginUserid").val("");
			$("input#loginUserid").focus();
			return; // goLogin() 함수종료
		}
		
		if(loginPwd == "") {
			alert("비밀번호를 입력하세요");
			$("input#loginPwd").val("");
			$("input#loginPwd").focus();
			return; // goLogin() 함수종료
		}
		
		// ~~ 아이디저장하기 체크박스를 눌렀는지 확인해서 ~~
		if( $("input:checkbox[id='saveid']").prop("checked") ) {
			// 체크했으면 값을 저장하고,
			localStorage.setItem("saveid", $("input#loginUserid").val()); 
		}
		else { 
			// 체크안했으면 저장된 값을 삭제하기
			localStorage.removeItem("saveid"); 
		}
				
		const frm = document.loginFrm;
		frm.action = "<%= request.getContextPath()%>/login/login.moc";
		frm.method = "post";
		frm.submit();
		
	}//end of goLogin()-------------------------------------

</script>

<div class="container my-5 py-1 mx-auto bg-white">
 	<form name="loginFrm">
		<table id="loginTbl">
	    	<thead>
	           <tr>
	               <th id="th" style="margin-bottom: 50px;">LOGIN</th>
	           </tr>
	        </thead>
	         
	        <tbody>
	           <tr>
	               <td>
		               <input type="text" id="loginUserid" name="userid" size="40" class="box " autocomplete="off" placeholder = "ID" />
		           </td> 
	           </tr>   
	           <tr>
	               <td>
	               	   <input type="password" id="loginPwd" name="pwd" size="40" class="box" placeholder = "PASSWORD"/>
	               </td>
	           </tr>
	           <tr>
	               <td align="center" style="padding: 10px;">
	               	   <input type="checkbox" id="saveid" name="saveid" /><label for="saveid"> &nbsp;id save </label><br>
	                   <button style="font-weight:bold;" id="btnSubmit" class="btn btn-warning">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CONTINUE&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</button>
	           	   </td>
	           </tr>
	      
	           <%-- === 아이디 찾기, 비밀번호 찾기 === --%>
	           <tr>
	               <td id="userid_pwd_find" class="p2" colspan="2" align="center">
	                   <a id="p2" style="cursor: pointer; text-decoration: underline; color: black;"  data-toggle="modal" data-target="#userIdfind" data-dismiss="modal" data-backdrop="static">Forget your id?</a> 
	                   <a id="p2" style="cursor: pointer; text-decoration: underline; color: black;"  data-toggle="modal" data-target="#userPwdfind" data-dismiss="modal" data-backdrop="static">Forget your passward?</a> 
	                   <br>
	               	   <a id="p2">"Don't have an account?"</a> 
	               	   <a id="p2" style="cursor: pointer; text-decoration: underline; color: black;" href="<%= request.getContextPath()%>/login/memberRegister.moc">Register now.</a>
	               </td>
	           </tr>
	        </tbody>
	    </table>
    </form>

    <%-- ****** 아이디 찾기 Modal ****** --%>
  	<div class="modal fade" id="userIdfind">
    	<div class="modal-dialog">
	      	<div class="modal-content">
	      
	        	<!-- Modal header -->
	        	<div class="modal-header">
		          	<h4 class="modal-title">FORGET YOUR ID?</h4>
		          	<button type="button" class="close idFindClose" data-dismiss="modal">&times;</button>
	        	</div>
	        
	        	<!-- Modal body -->
	        	<div class="modal-body">
		          	<div id="idFind" style="padding-top:50px;">
		             	<iframe id="iframe_idFind" style="border: none; width: 100%; height: 320px;" src="<%= request.getContextPath()%>/login/idFind.moc">
		             	</iframe>
		          	</div>
	        	</div>
	        
	        	<!-- Modal footer -->
	        	<div class="modal-footer">
	          		<button type="button" class="btn btn-secondary idFindClose" data-dismiss="modal">Close</button>
	        	</div>
	      	</div>
    	</div>
  	</div>
  	
  	<%-- ****** 비밀번호 찾기 Modal ****** --%>
  	<div class="modal fade" id="userPwdfind">
    	<div class="modal-dialog">
	      	<div class="modal-content">
	      
	        	<!-- Modal header -->
	        	<div class="modal-header">
		          	<h4 class="modal-title">FORGET YOUR PASSWORD?</h4>
		          	<button type="button" class="close pwdFindClose" data-dismiss="modal">&times;</button>
	        	</div>
	        
	        	<!-- Modal body -->
	        	<div class="modal-body">
		        	<div id="pwdFind" style="padding-top:25px;">
		             	<iframe id="iframe_pwdFind" style="border: none; width: 100%; height: 320px;" src="<%= request.getContextPath()%>/login/pwdFind.moc">
		             	</iframe>
		          	</div>
	        	</div>
	        
	        	<!-- Modal footer -->
	        	<div class="modal-footer">
	          		<button type="button" class="btn btn-secondary pwdFindClose" data-dismiss="modal">Close</button>
	        	</div>
	      	</div>
    	</div>
  	</div>
	  	
</div>	 

<jsp:include page="../../jaesik/footer.jsp"/>