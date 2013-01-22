<%@ page contentType="text/html;charset=UTF-8" import="java.util.*"
	import="com.sohu.tw.elevator.metrics.*"%>
<%@ page import="com.sohu.tw.elevator.ElevatorConfig" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<meta http-equiv="refresh" content="5" />
<title>Elevator</title>
<link rel="stylesheet" type="text/css" href="hbase.css" />
</head>
<body>
	<h1 id="page_title">Elevator <i style="font-size: 50%">version <%out.println(ElevatorConfig.getVersion());%></i></h1>
		<p id="links_menu"><a href="/elevator.jsp">Back</a></p>
	<hr id="head_rule" />
	<%
          Map<String, Map<String, Number>> map = StatusCollector.getInstance().getThreadInfo();
          for(String threadName: map.keySet()){
            out.println("线程名称 : "+threadName+"</br>");
            for (String topic : map.get(threadName).keySet()){
                out.println("|------ "+topic+" : "+map.get(threadName).get(topic)+"</br>");
            }
          }
	%>
	
</body>
</html>