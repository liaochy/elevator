package com.sohu.tw.elevator.plugin.http.benchmark;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class HttpUtil {
    static final int TIMEOUT = 900000;

    static final String CHARSET = "utf-8";

    public static String methodPost4DJ(String baseUrl, String page, NameValuePair[] data, String beginDate, String endDate) {
        String url = baseUrl + page;
        String response = "";
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(url);
        postMethod.setRequestBody(data);
        int statusCode = 0;
        try {
            statusCode = httpClient.executeMethod(postMethod);
        } catch (HttpException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY
                || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
            Header locationHeader = postMethod.getResponseHeader("location");
            String location = null;
            if (locationHeader != null) {
                location = locationHeader.getValue();
                System.out.println("The page was redirected to:" + location);
                PostMethod pM = new PostMethod(baseUrl + "download_detail.asp?act=srch");
                NameValuePair[] pMData = {
                        new NameValuePair("dt", beginDate),
                        new NameValuePair("dt2", endDate),
                        new NameValuePair("gameid", ""),
                        new NameValuePair("sum", "1"),
                };
                pM.setRequestBody(pMData);
                try {
                    httpClient.executeMethod(pM);
                    System.out.println("Redirect:" + pM.getStatusLine().toString());
                    return pM.getResponseBodyAsString();

                } catch (Exception e) {

                    e.printStackTrace();
                } finally {
                    pM.releaseConnection();
                }

            } else {
                System.err.println("Location field value is null.");
            }
        } else {
            System.out.println(postMethod.getStatusLine());

            try {
                response = postMethod.getResponseBodyAsString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            postMethod.releaseConnection();
        }
        return response;
    }

    /**
     * POST�������
     *
     * @param url
     * @return
     */
    public static byte[] putDataAsStream(String url, byte[] postData) {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(
                TIMEOUT);
        client.getParams().setParameter("http.socket.timeout", TIMEOUT);
        client.getParams().setContentCharset(CHARSET);
        PutMethod method = new PutMethod();
        try {
            method.setURI(new URI(url, true, CHARSET));
        } catch (URIException ex) {
            LogUtil.exception(ex);
        } catch (NullPointerException ex) {
            LogUtil.exception(ex);
        } catch (Exception ex) {
            LogUtil.exception(ex);
        }
        method.setRequestEntity(new ByteArrayRequestEntity(postData));
        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                LogUtil.error("POST DATA FAILED!HTTP STATUS:" + statusCode);
                return null;
            } else {
                byte[] responseBody = null;
                Header contentEncodingHeader = method
                        .getResponseHeader("Content-Encoding");
                if (contentEncodingHeader != null
                        && contentEncodingHeader.getValue().equalsIgnoreCase(
                        "gzip")) {
                    GZIPInputStream is = new GZIPInputStream(method
                            .getResponseBodyAsStream());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    IOUtils.copy(is, os);
                    responseBody = os.toByteArray();
                } else {
                    responseBody = method.getResponseBody();
                }

                byte[] data = formatData(responseBody);
                String encoding = CHARSET;
                Header contentTypeHeader = method
                        .getResponseHeader("Content-Type");
                if (contentTypeHeader != null) {
                    String contentType = contentTypeHeader.getValue();
                    // System.out.println("content-type:" + contentType);
                    int offset = contentType.indexOf("=");
                    if (offset != -1)
                        encoding = contentType.substring(offset + 1);
                    else {
                        String body = new String(data, encoding);
                        offset = body.indexOf("encoding");
                        if (offset != -1) {
                            int begin = body.indexOf("\"", offset);
                            int end = body.indexOf("\"", begin + 1);
                            encoding = body.substring(begin + 1, end);
                        }
                    }
                }
                return data;
            }
        } catch (HttpException ex) {
            LogUtil.exception(ex);
        } catch (IOException ex) {
            LogUtil.exception(ex);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return null;
    }

    /**
     * POST�������
     *
     * @param url
     * @return
     */
    public static byte[] postDataAsStream(String url, byte[] postData) {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(
                TIMEOUT);
        client.getParams().setParameter("http.socket.timeout", TIMEOUT);
        client.getParams().setContentCharset(CHARSET);
        PostMethod method = new PostMethod();
        try {
            method.setURI(new URI(url, true, CHARSET));
        } catch (URIException ex) {
            LogUtil.exception(ex);
        } catch (NullPointerException ex) {
            LogUtil.exception(ex);
        } catch (Exception ex) {
            LogUtil.exception(ex);
        }
        method.setRequestEntity(new ByteArrayRequestEntity(postData));
        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                LogUtil.error("POST DATA FAILED!HTTP STATUS:" + statusCode);
                return null;
            } else {
                byte[] responseBody = null;
                Header contentEncodingHeader = method
                        .getResponseHeader("Content-Encoding");
                if (contentEncodingHeader != null
                        && contentEncodingHeader.getValue().equalsIgnoreCase(
                        "gzip")) {
                    GZIPInputStream is = new GZIPInputStream(method
                            .getResponseBodyAsStream());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    IOUtils.copy(is, os);
                    responseBody = os.toByteArray();
                } else {
                    responseBody = method.getResponseBody();
                }

                byte[] data = formatData(responseBody);
                String encoding = CHARSET;
                Header contentTypeHeader = method
                        .getResponseHeader("Content-Type");
                if (contentTypeHeader != null) {
                    String contentType = contentTypeHeader.getValue();
                    // System.out.println("content-type:" + contentType);
                    int offset = contentType.indexOf("=");
                    if (offset != -1)
                        encoding = contentType.substring(offset + 1);
                    else {
                        String body = new String(data, encoding);
                        offset = body.indexOf("encoding");
                        if (offset != -1) {
                            int begin = body.indexOf("\"", offset);
                            int end = body.indexOf("\"", begin + 1);
                            encoding = body.substring(begin + 1, end);
                        }
                    }
                }
                return data;
            }
        } catch (HttpException ex) {
            LogUtil.exception(ex);
        } catch (IOException ex) {
            LogUtil.exception(ex);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return null;
    }

    /**
     * POST�������
     *
     * @param url
     * @return
     */
    public static String postData(String url, String postData) {
        LogUtil.info("����:" + url);
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(
                TIMEOUT);
        client.getParams().setParameter("http.socket.timeout", TIMEOUT);
        client.getParams().setContentCharset(CHARSET);
        PostMethod method = new PostMethod();
        try {
            method.setURI(new URI(url, true, CHARSET));
        } catch (URIException ex) {
            LogUtil.exception(ex);
        } catch (NullPointerException ex) {
            LogUtil.exception(ex);
        } catch (Exception ex) {
            LogUtil.exception(ex);
        }
        method.setRequestEntity(new StringRequestEntity(postData));
        try {
            // Execute the method.
            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                LogUtil.error("POST DATA FAILED!HTTP STATUS:" + statusCode);
                return null;
            } else {
                byte[] responseBody = null;
                Header contentEncodingHeader = method
                        .getResponseHeader("Content-Encoding");
                if (contentEncodingHeader != null
                        && contentEncodingHeader.getValue().equalsIgnoreCase(
                        "gzip")) {
                    GZIPInputStream is = new GZIPInputStream(method
                            .getResponseBodyAsStream());
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    IOUtils.copy(is, os);
                    responseBody = os.toByteArray();
                } else {
                    responseBody = method.getResponseBody();
                }

                byte[] data = formatData(responseBody);
                String encoding = CHARSET;
                Header contentTypeHeader = method
                        .getResponseHeader("Content-Type");
                if (contentTypeHeader != null) {
                    String contentType = contentTypeHeader.getValue();
                    // System.out.println("content-type:" + contentType);
                    int offset = contentType.indexOf("=");
                    if (offset != -1)
                        encoding = contentType.substring(offset + 1);
                    else {
                        String body = new String(data, encoding);
                        offset = body.indexOf("encoding");
                        if (offset != -1) {
                            int begin = body.indexOf("\"", offset);
                            int end = body.indexOf("\"", begin + 1);
                            encoding = body.substring(begin + 1, end);
                        }
                    }
                }
                return new String(data, encoding);
            }
        } catch (HttpException ex) {
            LogUtil.exception(ex);
        } catch (IOException ex) {
            LogUtil.exception(ex);
        } finally {
            // Release the connection.
            method.releaseConnection();
        }
        return null;
    }

    /**
     * @param url
     * @return
     * @throws java.io.IOException
     */
    public static byte[] requestHttpContentAsStream(String url)
            throws IOException {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(
                TIMEOUT);
        client.getParams().setParameter("http.socket.timeout", TIMEOUT);
        GetMethod method = new GetMethod();
        try {
            method.setURI(new URI(url, false, "utf-8"));

        } catch (URIException ex) {
            LogUtil.exception(ex);
        } catch (NullPointerException ex) {
            LogUtil.exception(ex);
        }
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(8, false));
        try {

            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                LogUtil.error("GET HTTP CONTENT FAILED!HTTP STATUS NOT OK");
                return null;
            }
            byte[] responseBody = null;
            Header contentEncodingHeader = method
                    .getResponseHeader("Content-Encoding");
            if (contentEncodingHeader != null
                    && contentEncodingHeader.getValue()
                    .equalsIgnoreCase("gzip")) {
                GZIPInputStream is = new GZIPInputStream(method
                        .getResponseBodyAsStream());
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                IOUtils.copy(is, os);
                responseBody = os.toByteArray();
            } else {
                responseBody = method.getResponseBody();
            }

            return responseBody;

        } catch (HttpException ex) {
            LogUtil.exception(ex);
        } catch (IOException ex) {
            LogUtil.exception(ex);
        } finally {
            method.releaseConnection();
        }
        return null;
    }


    public static String requestHttpChunkContent(String url) throws Exception {
        HttpClient client = new HttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(
                TIMEOUT);
        client.getParams().setParameter("http.socket.timeout", TIMEOUT);
        GetMethod method = new GetMethod();
        try {
            method.setURI(new URI(url, false, "utf-8"));

        } catch (URIException ex) {
            ex.printStackTrace();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                new DefaultHttpMethodRetryHandler(8, false));
        try {

            int statusCode = client.executeMethod(method);
            if (statusCode != HttpStatus.SC_OK) {
                LogUtil.error("GET HTTP CONTENT FAILED!HTTP STATUS NOT OK . status=" + statusCode);
                return null;
            }
            Header header = method.getResponseHeader("Transfer-Encoding");
            if (header.getValue().equals("chunked")) {
                InputStream is = method.getResponseBodyAsStream();
                byte [] buf = new byte[2048];
                int len = 0;
                while((len=is.read(buf))!=-1){
                    String ss = new String(buf,0,len);
//                    System.out.println(ss);
                }
//                while (is.read() != -1) ;
            }
        } catch(Exception e){

        }
        return null;
    }

    /**
     * @param data
     * @return
     */
    private static byte[] formatData
    (
            byte[] data) {
        return data;
        // if (data == null) {
        // return null;
        // }
        // int k = 0;
        // for (; k < data.length && data[k] <= 32; k++) {
        // ;
        // }
        //
        // if (k == data.length) {
        // return null;
        // }
        // byte[] formatData = new byte[data.length - k];
        // java.lang.System.arraycopy(data, k, formatData, 0,
        // formatData.length);
        //
        // return formatData;
    }
}
