<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<% String ctxPath = request.getContextPath(); %>

<jsp:include page="../../jaesik/header.jsp"/>

<script type="text/javascript">

	
	$(document).ready(function(){
		
		// viewmyInfo, viewmyreview, viewmyorderList, viewmyqna
		// 활성화된 탭 정보를 저장
		$("a[data-toggle='pill']").on('shown.bs.tab', function(e) {
		    localStorage.setItem('activeTab', $(e.target).attr('href'));
		});
		
		// 페이지 로드 시 저장된 활성화된 탭 정보를 가져와 활성화
		var activeTab = localStorage.getItem('activeTab');
		if (activeTab) {
		    $('.nav-pills a[href="' + activeTab + '"]').tab('show');
		}
		 
		// 다른 nav 탭 클릭하면 회원정보변경 숨기기
		$("a.accout_nav").click(function(){ 
			$("div#memberEdit").hide();		
		});
		
	});
	
	// == 로그아웃 ==
	function goLogOut() {
		
		// 로그아웃하면 메인으로감
		location.href = "<%= request.getContextPath()%>/login/logout.moc";
		
	}//end of goLogOut()-------------------------------------

</script>

<div class="bg-white container mt-5 mx-auto">
	<div class="container">
		<div class="세로">
			<section>
				<div class="row col-md-10 mx-auto my-5 justify-content-end">
					<div class="col-3"> </div>
					<div class="col-6"><h2 style="font-weight: bold; text-align: center;">YOUR ACCOUNT</h2></div>
					<div class="col-3 text-center"><button class="btn btn-warning btn-lg" id="btnPay" onclick="goLogOut();">LOG OUT</button></div>
	  			</div>
			
				<!-- Pills를 토글 가능하게 만들려면 각 링크에 data-toggle 속성을 data-toggle="pill"로 변경하십시오. 
				     그런 다음 모든 탭에 대해 고유한 ID가 있는 .tab-pane 클래스를 추가하고 .tab-content 클래스가 있는 <div> 요소 안에 래핑합니다.
		             탭을 클릭할 때 탭이 페이드 인 및 페이드 아웃되도록 하려면 .fade 클래스를 .tab-pane에 추가하세요. 
		        -->
				<ul class="nav nav-pills justify-content-around" style="margin-bottom: 80px;">
					<li class="nav-item">
				    	<a class="accout_nav nav-link active" data-toggle="pill" href="#viewmy1" style="font-size:15pt; height: 40px; color:black;">MY INFORMATION</a>
				  	</li>
					<li class="nav-item">
				    	<a class="accout_nav nav-link" data-toggle="pill" href="#viewmy2" style="font-size:15pt; height: 40px; color:black;">MY REVIEW</a>
				  	</li>
				  	<li class="nav-item">
				    	<a class="accout_nav nav-link" data-toggle="pill" href="#viewmy3" style="font-size:15pt; height: 40px; color:black;">MY ORDERLIST</a>
				  	</li>
				  	<li class="nav-item">
				    	<a id="showSearch" class="accout_nav nav-link" data-toggle="pill" href="#viewmy4" style="font-size:15pt; height: 40px; color:black;">FAQs / QNA</a>
				  	</li>
				</ul>
				
				<!-- 탭누르면 각자 태그에 맞게 나오는 곳 -->
				<div class="tab-content py-3" >
					<%-- 나의정보보기 --%>
					<jsp:include page="myaccount_myInfo.jsp"/>
				
					<%-- 내가 작성한 리뷰 보기 --%>
					<jsp:include page="myaccount_myReview.jsp"/>
					
				  	<%-- 주문내역 및 배송현황 --%>
				  	<jsp:include page="myaccount_orderList.jsp"/>
				  	
				  	<%-- 게시판 --%>
				  	<jsp:include page="myaccount_board.jsp"/>
				</div>
						
				<br><br><br><br><br><br><br><br>
				
			</section>

		</div>
	</div>
</div>


<jsp:include page="../../jaesik/footer.jsp"/>