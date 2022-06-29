package controllor.member;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dao.MemberDao;
import dto.Member;

/**
 * Servlet implementation class Singup
 */
@WebServlet("/member/singup")
public class singup extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public singup() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		request.setCharacterEncoding("UTF-8");		
		String mid = request.getParameter("mid");
		String ch = request.getParameter("ch");
		System.out.println("요고"+ch);
		if(ch.equals("1")) {// 만약 아이디 중복검사를 위한거라면
			boolean result = MemberDao.getmemberDao().idCheck(mid);
			System.out.println(result);
			if(result) {// 동일한아이디가 존재하면
				response.getWriter().print(-1);		
			}else {//동일한아이디가 존재하지않으면
				response.getWriter().print(1);	
			}
		}else {// 회원가입을 위한거라면
			String mpw = request.getParameter("mpw");
			String mname = request.getParameter("mname");
			String mphone = request.getParameter("mphone");
			
			Member member = new Member( // 멤버 객체 생성
					0,mid,mpw,mname,mphone
					);
			boolean result = MemberDao.getmemberDao().signUp(member); //저장
			if(result) {
				response.getWriter().print(1);
			}else {
				response.getWriter().print(-1);
			}
		}		
	}

}
