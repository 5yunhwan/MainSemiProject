<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
    
<% 
	String ctxPath = request.getContextPath(); 
%>

<jsp:include page="header.jsp"/>

<style type="text/css">

	@keyframes slidein { 
		from {
			margin-bottom: 100%;
			width: 300%;
		}
	
		to {
			margin-bottom: 0%;
			width: 100%;
		}
	}
	
	nav#header_nav_bar{
		display: flex !important; 
		height: 100%;
		/* animation-duration: 3s;
		animation-name: slidein; */
	}

</style>

<%-- carousel 시작 --%>
<div id="body" >
	
	<div id="carouselView1" class="carousel slide carousel-fade" data-ride="carousel">
		<ol class="carousel-indicators">
			<li data-target="#carouselView1" data-slide-to="0" class="active"></li>
			<li data-target="#carouselView1" data-slide-to="1"></li>
			<li data-target="#carouselView1" data-slide-to="2"></li>
		</ol>
		<div class="carousel-inner">
			<div class="carousel-item active">
				<img src="<%= ctxPath%>/images/img_g.png" class="d-block w-100" alt="...">
			</div>
			<div class="carousel-item">
				<img src="<%= ctxPath%>/images/img_b.png" class="d-block w-100" alt="...">
			</div>
			<div class="carousel-item">
				<img src="<%= ctxPath%>/images/img_r.png" class="d-block w-100" alt="...">
			</div>
		</div>
		<a class="carousel-control-prev" href="#carouselView1" role="button" data-slide="prev">
			<span class="carousel-control-prev-icon" aria-hidden="true"></span>
			<span class="sr-only">Previous</span>
		</a>
		<a class="carousel-control-next" href="#carouselView1" role="button" data-slide="next">
			<span class="carousel-control-next-icon" aria-hidden="true"></span>
			<span class="sr-only">Next</span>
		</a>
	</div>
	
	
	<%-- 두번째 carousel --%>
	<div id="carouselView2" class="carousel slide carousel-fade" data-ride="carousel">
		<ol class="carousel-indicators">
			<li data-target="#carouselView2" data-slide-to="0" class="active"></li>
			<li data-target="#carouselView2" data-slide-to="1"></li>
			<li data-target="#carouselView2" data-slide-to="2"></li>
		</ol>
		<div class="carousel-inner">
			<div class="carousel-item active">
				<img src="https://image.nbkorea.com/NBRB_Site/20230306/NB20230306093539119001.jpg" class="d-block w-100" alt="...">
			</div>
			<div class="carousel-item">
				<img src="https://image.nbkorea.com/NBRB_Site/20230406/NB20230406085126936001.jpg" class="d-block w-100" alt="...">
			</div>
			<div class="carousel-item">
				<img src="https://image.nbkorea.com/NBRB_Site/20230308/NB20230308105355932001.jpg" class="d-block w-100" alt="...">
			</div>
		</div>
		<a class="carousel-control-prev" href="#carouselView2" role="button" data-slide="prev">
			<span class="carousel-control-prev-icon" aria-hidden="true"></span>
			<span class="sr-only">Previous</span>
		</a>
		<a class="carousel-control-next" href="#carouselView2" role="button" data-slide="next">
			<span class="carousel-control-next-icon" aria-hidden="true"></span>
			<span class="sr-only">Next</span>
		</a>
	</div>
	
	<img src="https://image.nbkorea.com/NBRB_Collection/20230413/NB20230413130306777001.jpg	" class="d-block w-100 test1" alt="...">
	
</div>
<%-- carousel 끝 --%>

<jsp:include page="footer.jsp"/>
