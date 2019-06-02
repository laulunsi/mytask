package com.qjxs.filter;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.collect.Maps;
import com.qjxs.common.utils.AuthServiceUtils;
import com.qjxs.utils.KSessionUtil;
import com.qjxs.common.utils.ReqUtil;
import com.qjxs.common.utils.ResponseUtil;

@WebFilter(filterName = "authorizationfilter", urlPatterns = { "/*" }, initParams = {
		@WebInitParam(name = "enable", value = "true") })
public class AuthorizationFilter implements Filter {

	private Map<String, String> requestUriMap;
	@Autowired
	@Qualifier(value = "afp")
	private AuthorizationFilterProperties afp;
	private Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
			throws IOException, ServletException {

		if (null == requestUriMap) {
			requestUriMap = Maps.newHashMap();

			for (String requestUri : afp.getRequestUriList()) {
				requestUriMap.put(requestUri, requestUri);
			}
		}

		// if (!enable) {
		// arg2.doFilter(arg0, arg1);
		// return;
		// }

		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;
		request.setCharacterEncoding("utf-8");
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");
		String accessToken = request.getParameter("access_token");
		long time = NumberUtils.toLong(request.getParameter("time"), 0);
		String secret = request.getParameter("secret");
		// //是否检验接口
		// boolean falg=StringUtils.isNotBlank(secret)&&0<time;
		String requestUri = request.getRequestURI();
		//
		// if("/favicon.ico".equals(requestUri))
		// return;

		// DEBUG**************************************************DEBUG
		StringBuffer sb = new StringBuffer();
		sb.append(request.getMethod()).append(" 请求：" + request.getRequestURI());

		logger.info(sb.toString());

		// DEBUG**************************************************DEBUG

		// 如果访问的是控制台或资源目录
		if (requestUri.startsWith("/console") || requestUri.endsWith(".js") || requestUri.endsWith(".html")
				|| requestUri.endsWith(".css") || requestUri.endsWith(".png")) {
			Object obj = request.getSession().getAttribute("LOGIN_USER");
			// 用户已登录或访问资源目录或访问登录页面
			if (null != obj || requestUri.startsWith("/pages") || requestUri.startsWith("/console/login")
					|| requestUri.startsWith("/console")) {
				arg2.doFilter(arg0, arg1);
				return;
			} else
				response.sendRedirect("/console/login");
		} else {
			if (requestUri.equals("/getImgCode")) {
				arg2.doFilter(arg0, arg1);
				return;
			}

			// 需要登录
			if (isNeedLogin(request.getRequestURI())) {
				// 请求令牌是否包含
				if (StringUtils.isEmpty(accessToken)) {
					logger.info("不包含请求令牌");
					int tipsKey = 1030101;
					renderByErrorKey(response, tipsKey,"不包含请求令牌");
				} else {
					String userId = getUserId(accessToken);
					// 请求令牌是否有效
					if (null == userId) {
						logger.info("请求令牌无效或已过期...");
						int tipsKey = 1030102;
						renderByErrorKey(response, tipsKey,"请求令牌无效或已过期...");
					} else {

						if (!AuthServiceUtils.authRequestApi(userId, time, accessToken, secret, requestUri)) {
							renderByError(response, "授权认证失败");
							return;
						}

						 ReqUtil.setLoginedUserId(Integer.parseInt(userId));
						arg2.doFilter(arg0, arg1);
						return;
					}
				}
			} else {
				/**
				 * 校验没有登陆的接口
				 */
				if (null == accessToken) {
					if (!AuthServiceUtils.authOpenApiSecret(time, secret)) {
						renderByError(response, "授权认证失败");
						return;
					}

				}
			
				arg2.doFilter(arg0, arg1);
			}
		}
	}

	private boolean isNeedLogin(String requestUri) {
		return !requestUriMap.containsKey(requestUri.trim());
	}

	private String getUserId(String _AccessToken) {
		String userId = null;

		try {
			 userId = KSessionUtil.getUserIdBytoken(_AccessToken);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return userId;
	}

	private static final String template = "{\"resultCode\":%1$s,\"resultMsg\":\"%2$s\"}";

	private static void renderByErrorKey(ServletResponse response, int tipsKey,String tipsValue) {
//		 String tipsValue = ConstantUtil.getMsgByCode(tipsKey+"",
//		 "zh").getValue();
//		String tipsValue ="";
		 String s = String.format(template, tipsKey, tipsValue);
		
		 ResponseUtil.output(response, s);
	}

	private static void renderByError(ServletResponse response, String errMsg) {

		String s = String.format(template, 0, errMsg);

		 ResponseUtil.output(response, s);
	}

}