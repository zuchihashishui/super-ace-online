package vn.emerald.ace;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
/** No cross-origin API access; mutation requests also require custom header and session CSRF token. */
@Component
public class RequestGuard extends OncePerRequestFilter {
 private final String publicOrigin; public RequestGuard(@Value("${ace.public-origin}")String origin){publicOrigin=origin;}
 @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  res.setHeader("X-Content-Type-Options","nosniff");res.setHeader("X-Frame-Options","DENY");res.setHeader("Referrer-Policy","same-origin");
  if(req.getRequestURI().startsWith("/api/")){
   res.setHeader("Cache-Control","no-store");boolean write=!req.getMethod().equals("GET")&&!req.getMethod().equals("HEAD");
   String origin=req.getHeader("Origin");String own=req.getScheme()+"://"+req.getServerName()+((req.getServerPort()==80&&req.getScheme().equals("http")||req.getServerPort()==443&&req.getScheme().equals("https"))?"":":"+req.getServerPort());
   boolean wrongOrigin=origin!=null&&!origin.equals(publicOrigin.isBlank()?own:publicOrigin);
   if(wrongOrigin||"cross-site".equals(req.getHeader("Sec-Fetch-Site"))||(write&&!"web".equals(req.getHeader("X-Game-Client")))){res.setStatus(403);res.setCharacterEncoding("UTF-8");res.setContentType("application/json");res.getWriter().write("{\"code\":\"ORIGIN_REJECTED\",\"message\":\"Mở game từ đúng địa chỉ server.\"}");return;}
  }chain.doFilter(req,res);
 }
}
