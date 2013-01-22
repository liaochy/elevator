<%@ page contentType="text/html;charset=UTF-8" import="java.util.*"
	import="com.sohu.tw.elevator.metrics.*"%>
<%@ page import="com.sohu.tw.elevator.ElevatorConfig" %>
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" 
  "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html;charset=UTF-8" />
<meta http-equiv="refresh" content="1" />
<title>Elevator</title>
<link rel="stylesheet" type="text/css" href="hbase.css" />
</head>
<body>
	<h1 id="page_title">Elevator <i style="font-size: 50%">version <%out.println(ElevatorConfig.getVersion());%></i></h1>
	<p id="links_menu"><a href="/logLevel">Log Level</a></p>
	<p id="links_menu"><a href="/threads.jsp">Thread Informations</a></p>
	<hr id="head_rule" />
	<%
    	if(StatusCollector.getInstance().getRecord("memory")!=null){

                 					long queueSize = (Long)StatusCollector.getInstance().getRecord("memory")
                 							.get("queueSize");
                 				out.println("队列大小为 :  "+queueSize);
                 				}else{
                 				 out.println("队列大小为 : 0");
                 				}
                 				%>
    	<br/>
	<%

	if(StatusCollector.getInstance().getRecord("all")!=null){
	%>

	<h2>Elevator 相关指标</h2>

	<table>
		<tr>
			<th>发送的日志条数</th>
			<th>
				<%
					out.println(StatusCollector.getInstance().getRecord("all")
							.get("logSum"));
				%>条
			</th>
		</tr>
		<tr>
			<th>每秒日志条数</th>
			<th>
				<%
					out.println(StatusCollector.getInstance().getRecord("all")
							.get("logTPS"));
				%>条
			</th>
		</tr>
		<tr>
			<th>kafka每秒日志条数</th>
			<th>
				<%
					out.println(StatusCollector.getInstance().getRecord("all")
							.get("kafkaTPS"));
				%>条
			</th>
		</tr>
		<tr>
			<th>总流量</th>
			<th>
				<%
					long allSize = (Long)StatusCollector.getInstance().getRecord("all")
							.get("throughPut");
					out.println(MetricsDataUtil.parseThroughPut(allSize));
				%>
				(<% out.print(allSize); %>)
			</th>
		</tr>
		<tr>
			<th>每秒流量</th>
			<th>
				<%
					long size =(Long) StatusCollector.getInstance().getRecord("all")
							.get("throughPutPS");
					out.println(MetricsDataUtil.parseThroughPut(size));
				%>
				(<% out.print(size); %>)
			</th>
		</tr>
	</table>
	
	
	<%List<String> list = StatusCollector.getInstance().getTopics();
		for(String topic : list){
	%>
	<h2><%=topic%> 相关指标<a href="/delete.jsp?topic=<%=topic%>">删除统计</a></h2>
	<table>
		<tr>
			<th>发送的日志条数</th>
			<th>
				<%
					out.println(StatusCollector.getInstance().getRecord(topic)
							.get("logSum"));
				%>条
			</th>
		</tr>
		<tr>
			<th>每秒日志条数</th>
			<th>
				<%
					out.println(StatusCollector.getInstance().getRecord(topic)
							.get("logTPS"));
				%>条
			</th>
		</tr>
		<tr>
			<th>kafka每秒日志条数</th>
			<th>
				<%
					out.println(StatusCollector.getInstance().getRecord(topic)
							.get("kafkaTPS"));
				%>条
			</th>
		</tr>
		<tr>
			<th>总流量</th>
			<th>
				<%
					allSize = (Long)StatusCollector.getInstance().getRecord(topic)
							.get("throughPut");
					out.println(MetricsDataUtil.parseThroughPut(allSize));
				%>
				(<% out.print(allSize); %>)
			</th>
		</tr>
		<tr>
			<th>每秒流量</th>
			<th>
				<%
					 size =(Long) StatusCollector.getInstance().getRecord(topic)
							.get("throughPutPS");
					out.println(MetricsDataUtil.parseThroughPut(size));
				%>
				(<% out.print(size); %>)
			</th>
		</tr>
	</table>
	<%
		}
		}else
		 out.print("there is no source at present");
	%>
	
	
</body>
</html>