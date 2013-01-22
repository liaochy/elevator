<%@ page contentType="text/html;charset=UTF-8" import="java.util.*"
	import="com.sohu.tw.elevator.metrics.*"%>
<%
	String topic = request.getParameter("topic");
	StatusCollector.getInstance().deleteTopic(topic);
	response.sendRedirect("/elevator.jsp");
%>