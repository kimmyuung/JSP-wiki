<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Date"%>
<%@page import="dao.DebateDao"%>
<%@page import="java.util.ArrayList"%>
<%@page import="dto.Debate"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
	<link href="/wiki/css/debate.css" rel="stylesheet">
</head>
<body>
<%@include file="../header.jsp" %>
<%
	//Deno값 받아오기
	int Deno=Integer.parseInt(request.getParameter("Deno"));	
	String content = DebateDao.getDebateDao().debateIn(Deno);
	
	//날짜 객체 생성
	Date date =new Date();
	SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	String today=sdf.format(date);
	
	//Deno값으로 denostate 값 가져오기
	String state=DebateDao.getDebateDao().getDebateState(Deno);
	
	//세션값
	session.getAttribute("login");
	
%>
<input type="hidden" value="<%=today%>" id="today">
<input type="hidden" value="<%=Deno%>" id="Deno">

<div class="container">
	
	<!-- 토론주제 보여주기 -->
	<h1>토론주제</h1>
	<div class="row">
		<div class="col-md-5 py-5" id="debatetopic">
			<h4><%=content %></h4>
			</div>
		<div class="offset-2 col-md-5">
			<H3><img alt="광고이미지" src="/wiki/img/광고.gif" style="width: 100%; height: 100%;"> </H3>
		</div>
	</div>
	
	<!-- 경계선 -->
	<hr>
	
	<!-- 토론 내용 간판 -->
	<div class="py-5 row">
		<div class="col-md-2 py-4">
			<h1>토론 내용</h1>
		</div>
		<div class="col-md-1 px-2">
			<img alt="debate" src="../img/debate.png" style="width: 80px; height: 80px;">
		</div>
	</div>
	
	<!-- 토론 내용  -->
	<div  class="col-md-12 py-5">
		<div id="debatezone" class="py-3">
			
		</div>
	</div>
	
	<!-- 경계선 -->
	<hr>	
	
	<%
		if(state.equals("1")){
	%>
	
	<!-- 토론 전송 -->
	<div class="col-md-12" id="chatform">
		<!-- 토론 참여 간판 -->
		<div class="col-md-4">
			<h1>토론에 참여할래요!</h1>
		</div>
		
		<!--토론작성 란 -->
			<div class="col-md-12">
				<textarea rows="5" cols="" class="form-control" id="dtcontent" name="dtcontent"></textarea>
			</div>
			
	<%
			if(login!=null){
				if(login.equals("admin")){
	%>
			<!-- 토론 전송 버튼 -->
			<div class="row">
				<div class="offset-8 col-md-2 py-2">
					<button class="form-control py-2" onclick="sendbtn('<%=Deno%>','<%=login%>')">전송</button>
				</div>
				<div class="col-md-2 py-2">
					<button class="form-control" onclick="closeDebate('<%=Deno%>')">토론 닫기</button>
				</div>
			</div>
	<%
				}else{
	%>
			<!-- 토론 전송 버튼 -->
			<div class="offset-10 col-md-2 py-2">
				<button class="form-control py-2" onclick="sendbtn('<%=Deno%>','<%=login%>')">전송</button>
			</div>
	<% 				
				}
			}else{
	%>
			<!-- 토론 전송 버튼 -->
			<div class="offset-10 col-md-2 py-2">
				<button class="form-control py-2" onclick="sendbtn('<%=Deno%>','<%=login%>')">전송</button>
			</div>
	<%
			}
	%>
	</div>
	<% 		
		}else{
	%>
	<!-- 토론 전송 -->
	<div class="col-md-12" id="chatform">
		<!-- 토론 참여 간판 -->
		<div class="col-md-4">
			<h1>토론에 참여할래요!</h1>
		</div>
		
		<!--토론작성 란 -->
			<div class="col-md-12">
				<textarea rows="5" cols="" class="form-control" id="dtcontent" name="dtcontent" disabled="disabled"></textarea>
			</div>
			<!-- 토론 전송 버튼 -->
			<div class="offset-10 col-md-2 py-2">
				<button class="form-control py-2" onclick="sendbtn('<%=Deno%>')" disabled="disabled">토론 종료</button>
			</div>
	</div>
	
	
	<%
		}	
	%>

</div>






<script src="/wiki/js/debate.js" type="text/javascript"></script>
<%@include file="../footer.jsp" %>
</body>
</html>