package controllor.function;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import dao.SpecialDao;

/**
 * Servlet implementation class getdocument_history
 */
@WebServlet("/function/getdocument_history")
public class getdocument_history extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public getdocument_history() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// 해당 문서번호 세션 받아오기	
		int dno = Integer.parseInt(request.getParameter("dno"));
		System.out.println(dno);
		// 문서 내용 DB에서 가져오기
		JSONArray json_content = SpecialDao.getSpecialDao().getcontentlist(dno);//
		System.out.print(json_content);
		if(json_content.toString().equals("[]")) {// 해당글번호의 역사가 없다면
			response.getWriter().print(-1); // -1 리턴
		}else {// 만약해당글번호의 역사가 기록되어있다면
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/json");
			response.getWriter().print(json_content); // json 리턴
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
