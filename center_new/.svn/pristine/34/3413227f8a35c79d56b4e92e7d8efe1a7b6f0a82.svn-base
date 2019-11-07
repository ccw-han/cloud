package net.cyweb.config.mes;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.math.NumberUtils;

/**
 * HTTP方式接收状态报告的方式， 请部署到tomcat
 * 并对web.xml作适当配置
 */
public class ReceiveServlet extends HttpServlet {

	private static final long serialVersionUID = 0x1L;

	private byte[] decodeBytes(String s) {
		if ( s == null )
			return new byte[0];
		try {
			return Hex.decodeHex(s.toCharArray());
		} catch (DecoderException e) {
			throw new RuntimeException(e);
		}
	}

	private String decodeMessage(int dc, byte[] buf) {
		if ( buf == null )
			return "";
		try {
			// 这里只解码了8
			if ( dc == 8 )
				return new String(buf, "UTF-16BE");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String(buf);
	}

	/**
	 * 服务器通过GET方式向客户端投递状态报告和上行
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// 输出请求信息
		System.out.println( req.getQueryString() );

		String op = req.getParameter("op");
		if ( "mo".equals(op) ) {
			// 上行信息
			int dc = NumberUtils.toInt( req.getParameter("dc") );
			String mobile = req.getParameter("sa");
			String extendCode = req.getParameter("da");
			byte[] data = decodeBytes( req.getParameter("sm") );
			String message = decodeMessage(dc, data);
			// 业务处理
			onReceiveMO( extendCode, mobile, message );
		} else if ( "dr".equals(op)) {
			// 状态报告
			String id = req.getParameter("id");
			String status = req.getParameter("su");
			int result = NumberUtils.toInt( req.getParameter("rp"), 9 );
			// 业务处理
			onReceiveReport( id, result, status );
		} else {
			// 未定义,不会到达这里
		}
		resp.getWriter().write("ok");
	}

	private void onReceiveMO(String extendCode, String mobile, String message) {
		// 请迅速返回,一般是放入内存后直接返回
		System.out.println( "extendCode: " + extendCode );
		System.out.println( "mobile: " + mobile );
		System.out.println( "message: " + message );
	}

	private void onReceiveReport(String id, int result, String status) {
		// 请迅速返回,一般是放入内存后直接返回
		System.out.println( "id: " + id );
		System.out.println( "result: " + result ); // 0 为成功，其他失败
		System.out.println( "status: " +  status );
	}

}
