package th.co.krungthaiaxa.api.auth.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Optional;

public class JwtAuthenticationTokenFilter extends UsernamePasswordAuthenticationFilter {
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Value("${jwt.header}")
    private String tokenHeader;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
        SimpleDateFormat secondFormat = new SimpleDateFormat("ss");
        SimpleDateFormat millisecondFormat = new SimpleDateFormat("SSS");
        DecimalFormat dcf = new DecimalFormat("#0.00");
        Date timeApiRequest = new Date();    
        
        String authToken = httpRequest.getHeader(this.tokenHeader);
        Optional<String> username = jwtTokenUtil.getUsernameFromToken(authToken);

        if (username.isPresent() && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username.get());
            if (jwtTokenUtil.validateToken(authToken, userDetails)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpRequest));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        Date timeApiResponse = new Date();
        long s1 = Integer.parseInt(secondFormat.format(timeApiRequest),10);
        long s2 = Integer.parseInt(secondFormat.format(timeApiResponse),10);
        long m1 = Integer.parseInt(millisecondFormat.format(timeApiRequest),10);
        long m2 = Integer.parseInt(millisecondFormat.format(timeApiResponse),10);
        long diffSecond = s2 - s1;
        long diffMillisecond = m2 - m1;
        double diffTotal = Double.parseDouble(Math.abs(diffSecond) + "." + Math.abs(diffMillisecond));
        getAllOfRequestContent(httpRequest);
        logger.info("call to : " + httpRequest.getRequestURI() 
        + " request time is : " + sdf.format(timeApiRequest) 
        + " response time is : " + sdf.format(timeApiResponse)
        + " difference is : " + dcf.format(diffTotal) + " seconds. \n ---------------------------------------");

        chain.doFilter(request, response);
    }
    
    private void getAllOfRequestContent(HttpServletRequest request){
    	logger.info("|'''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''''|");
    	//method
    	String method = request.getMethod();
    	logger.info("Method is : "+method);
    	//header
    	Enumeration headerNames = request.getHeaderNames();
    	while(headerNames.hasMoreElements()) {
    	  String headerName = (String)headerNames.nextElement();
    	  logger.info("Header Name - " + headerName + ", Value - " + request.getHeader(headerName));
    	}
    	//body
    	Enumeration params = request.getParameterNames(); 
    	while(params.hasMoreElements()){
    	 String paramName = (String)params.nextElement();
    	 logger.info("Parameter Name - "+paramName+", Value - "+request.getParameter(paramName));
    	}
    	logger.info("|................................................................|");
    }
}