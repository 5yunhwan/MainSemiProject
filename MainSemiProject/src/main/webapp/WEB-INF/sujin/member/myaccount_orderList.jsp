<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<% String ctxPath = request.getContextPath(); %>


<style type="text/css">

	div#viewmy3 {
		background-color:#fefce7; 
		padding-top:40px;
		padding-bottom:40px;

	}
	
	table > thead > tr > th {
		font-family: 'Noto Sans KR', serif; 
		font-weight: bolder; 
	}
	
	label{
		width: 17%;
		text-align:right;
		margin-right: 5px;
	}
	
</style>

<style>
 	@import url('https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@300;400;500;700&display=swap');
</style>

<script type="text/javascript">
	
	function goshopping() { // 쇼핑하기 누르면 전체 제품페이지로 이동
	
		location.href = "<%= ctxPath%>/shop/allproduct.moc"; 
	}
	
</script>


<div id="viewmy3" class="container tab-pane fade py-4 my-3" style="background-color: #fefce7; border-radius: 1%;">
	
	<h4 class="my-3" style="font-weight: bold; text-align: center;">주문내역 및 배송현황</h4>
	
	<div class="table-responsive py-3 mx-auto">
	
		<%-- [주문내역] 시작 --%>
		<table class="table text-center table-hover" id="tbl_orderList">
			<thead>
				<tr>
		    		<th class="col-2 text-center">주문번호</th>
		    		<th class="col-1 text-center">주문날짜</th>
		    		<th class="col-4 text-center" colspan="3">수령자정보</th>
		    		<th class="col-2 text-center" rowspan="2">배송상태(송장번호)</th>
		    		<th class="col-2 text-center">주문금액</th>
		    	<tr>
		    </thead>
		    <tbody>	
				<tr>
					<c:if test="${not empty requestScope.ovolist}">
						<c:forEach varStatus="status" var="ovo" items="${requestScope.ovolist}" >
							<tr>
								<td style="vertical-align:middle;">
									<button type="button" class="btn" data-toggle="collapse" data-target="#orderno${status.index}">
										<div>${ovo.order_no}</div> 
									</button>
								</td>
								<td style="vertical-align:middle;"><div>${ovo.orderdate}</div></td>
								<td colspan="3" style="font-size: 10pt; margin-top:20px; vertical-align:middle;">
						 			<div>${ovo.delivery_name}</div>
						 			<div>${ovo.delivery_mobile}</div>
						 			<div>${ovo.delivery_address}</div>
			 					</td>
			 					<td style="vertical-align:middle;">
			 						<div style="font-weight:bold; color:orange;">배송준비중</div>
			 						<div>${ovo.delivery_invoice}</div>
			 					</td>
								<td id="total_price" style="font-size: 12pt; font-weight: bold; vertical-align:middle;">
									<div><fmt:formatNumber value="${ovo.total_price}" pattern="###,###" />원</div>
								</td>
							</tr>
							<tr>
								<td colspan="7">
									<div class="collapse mb-1" id="orderno${status.index}" >
		                        	<!-- .collapse 은 내용물을 숨기는 것임. -->
		                            	<div class="card card-body"  id="Q${status.index}_A">
		                              		
		                              		<%-- [각 주문당 상세내역] 시작 --%>
											<table class="table text-center col-md-10 mx-auto">
										      	<thead>
										 	  		<tr>
												 		<th colspan="2" class="col-md-6">주문상품/옵션</th>
												 		<th>주문수량</th>
												 		<th>주문금액</th>
										 	  		</tr>
										      	</thead>
										      	<tbody>
										      		<tr>
										      			<c:forEach var="odlist" items="${requestScope.ovodetaillist}">
											 	  			<c:if test="${ovo.order_no == odlist.fk_order_no}">
													 	  	  	<tr id="orderProduct">
														 			<td class="col-md-3 text-center">
															 			<a href='<%= ctxPath%>/product.moc?product_name=${odlist.pvo.product_name}&product_color=${odlist.pvo.product_color}'>
													                    	<img src="${odlist.pvo.product_image}" id="product_image" name="product_image" width="150" class="img-thumbnail"/>
													                  	</a>
														 			</td>
														 			<td class="col-md-3 text-left">
															 		  	<div class="mt-4 product_Info">
																    		<div id="product_name">${odlist.pvo.product_name}</div>
																      		<div id="product_color">${odlist.pvo.product_color}<span>&nbsp;</span>
																      			<span id="product_size" name="product_size">${odlist.pvo.product_size}</span>
																      		</div>
															 		  	</div>
														 			</td>
														 			<td class="col-md-2" id="order_count"><div class="mt-4">${odlist.order_count} 개</div></td>
														 			<td class="col-md-2" id="order_price" style="font-size: 12pt; font-weight: bold;">
														 				<div class="mt-4"><fmt:formatNumber value="${odlist.order_price}" pattern="###,###" />원</div></td>
													 	  	  	</tr>
												 	  	 	</c:if> 	
											 	  		</c:forEach>
										 	  		</tr>
										      	</tbody>
									 	  	</table>
		                              		<%-- [각 주문당 상세내역] 끝 --%>
		                              		
		                             	</div>
		                        	</div>
								</td>
							</tr>
						</c:forEach>
					</c:if>
				</tr>
			</tbody>
		</table>
		<%-- [주문내역] 끝 --%>
		
		
		<c:if test="${empty requestScope.ovolist}">
			<br><br>
			<div class="text-center my-4">주문 내역이 없습니다.</div>
			<br><br>
			<div class="text-center">
				<button class="btn btn-lg col-4 mx-auto" style="border-radius: 5px; font-weight:bold; background-color:#ffeb99;" onclick="goshopping()">지금 쇼핑하기</button>
			</div>
			<br><br>
		</c:if>
				
	</div>
</div>