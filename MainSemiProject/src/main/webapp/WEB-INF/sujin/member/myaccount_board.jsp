<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<% String ctxPath = request.getContextPath(); %>


<style type="text/css">

	div#viewmy4 {
		background-color:#fefce7; 
		padding-top:40px;
		padding-bottom:40px;
	}

</style>


<script type="text/javascript">
   
	$(document).ready(function(){
      
		// -- 이상한 값이(""이나 null 이나 조건에 맞지 않는 검색어) 들어오면 값을 꽂아주지 않도록 한다 --
   <%-- if ( "${fn:trim(requestScope.searchWord)}" != "" ) --%> 
      	if( "${requestScope.searchType}" != "" && "${requestScope.searchWord}" != "" ) {
         	$("select#searchType").val("${requestScope.searchType}");
         	$("input#searchWord").val("${requestScope.searchWord}");
      	}
      
      	
      	// -- 조회하고자하는 페이지번호 sizePerPage 선택한것이 보이도록 값을 넣어준다. --
      	$("select#sizePerPage").val("${requestScope.sizePerPage}");
      
      	
     	// -- 검색창에서 엔터를 입력하면 검색 함수를 실행 --
      	$("input#searchWord").keydown(function(e){
         	if( e.keyCode == 13 ){ 
            	goSearch();
         	}
      	});
      
      	
      	// select 태그에 대한 이벤트는 click이 아니라 change
      	$("select#sizePerPage").change(function(){
         	goSearch();
      	});
      
      
   	});//end of $(document).ready------------------------------------
   
   	function goSearch(){
      
      	const frm = document.searchFrm;
      	frm.action = "<%= ctxPath%>/member/myaccount.moc"; 
      	frm.method = "get"; // 개인정보이지만 이미 화면에 뿌려진 것을 빨리 찾으려는 용도이므로 GET 방식으로 한다
      	$("form[name='searchFrm']").submit();   

   	}

</script>


<div class="tab-pane container fade" id="viewmy4">
	<h4 class="my-3" style="font-weight: bold; text-align: center;">자주 묻는 질문</h4>
   
   	<form name="searchFrm" class="col-md-10 py-3 mx-auto">
        <div class="row col-md-10 mx-auto">
	        <select id="searchType" name="searchType" class="col-3 form-control" style="height:38px;">
	        	<%-- sql 문의 colname 변수에 잘들어가기 위해서는 DB 에 있는 컬럼명과 동일해야 한다~! --%>
	            <option value="">선택하세요</option> 
	            <option value="fk_userid">아이디</option>
	            <option value="board_title">제목</option>
	            <option value="board_content">내용</option>
	        </select>&nbsp;
	        <input type="text" id="searchWord" name="searchWord" class="col-6 form-control" />
	        <%--
	            form 태그내에서 데이터를 전송해야 할 input 태그가 만약에 1개 밖에 없을 경우에는
	            input 태그내에 값을 넣고나서 그냥 엔터를 해버리면 '유효성 검사'가 있더라도 하지않고 submit 되어져 버린다.
	            이것을 막으려면 input 태그가 2개 이상 존재하면 된다.
	            그런데 실제 화면에 보여질 input 태그는 1개이어야 한다.
	            이럴 경우 (--!무작성 hidden 넣으면 안되고!--) 아래와 같이 display 를 none 으로 해주면 된다. 
	        --%>
	        <input type="text" style="display:none;"/>
	      
	        <button type="button" class="btn btn-secondary col-2" style="margin-left: 30px; margin-right: 30px;" onclick="goSearch();">검색</button>
  		</div>
   </form>
   
   <form name="boardTblFrm">
      	<table id="boardTblFrm" class="table table-hover col-md-10 py-3 mx-auto">
         	<thead>
               	<tr>
                   	<th class="col-1">No</th>
                    <th class="col-2">아이디</th>
                    <th class="col-9 text-center">제목</th>
                </tr>
           	</thead>
           	<tbody>
           
              	<c:if test="${not empty requestScope.boardList}">
                 	<c:forEach varStatus="status" var="bvo" items="${requestScope.boardList}">
                    	<tr>
                       		<td class="col-1">${bvo.board_no}</td>
                     		<td class="col-2">${bvo.fk_userid}</td>
                     		<td class="col-9">
	                        	<button type="button" class="btn" data-toggle="collapse" data-target="#Q${status.index}">
	                          		${bvo.board_title}
	                          	</button>
	                        	<div class="collapse mb-1" id="Q${status.index}" >
	                           	<!-- .collapse 은 내용물을 숨기는 것임. -->
	                             	<div class="card card-body"  id="Q${status.index}_A">
	                              		${bvo.board_content}
	                             	</div>
	                        	</div>
                     		</td>
                  		</tr>
               		</c:forEach>
            	</c:if>
           
              	<c:if test="${empty requestScope.boardList}">
                 	<td colspan="3">등록된 게시물이 없습니다.</td>
            	</c:if>
            	
         	</tbody>
      	</table>
   	</form>
   
   <!-- 페이지네이션 -->
   <nav class="my-5">
      	<div style='display:flex; width:100%;'>
         	<ul class="pagination" style='margin:auto;'>${requestScope.pageBar}</ul>
      	</div>
   </nav>

</div>