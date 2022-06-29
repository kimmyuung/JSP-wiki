package dao;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import dto.Content;
import dto.Document;
import dto.Synonys;

public class DocumentDao extends Dao{

	public DocumentDao() {
		super(); // 부모클래스 생성자 호출 
	}
	public static DocumentDao documentDao = new DocumentDao(); 		// dao 호출시 반복되는 new 연산자 제거 
	public static DocumentDao getdocumentDao() { return documentDao; } 
	
	public boolean titlecheck(String dtitle) {
		try {
			String sql = "select * from document where dtitle = ?";
			ps = con.prepareStatement(sql);
			ps.setString(1, dtitle);
			rs = ps.executeQuery();
			if(rs.next()) {return true;}
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	//문서 생성 메소드
	public boolean docuCreate(String title, Content c) {
		//document 테이블에 제목부터 넣고 생성된 번호 받아오기
		String sql="insert into document(dtitle) value ( '" +title +"' )";
		try {
			ps=con.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			ps.executeUpdate();
			rs=ps.getGeneratedKeys();
			if(rs.next()) {
				int dno=rs.getInt(1); // 받아온 번호 dno에 넣기
				if(setContent(c, dno)) { // 문서 내용 생성하고 결과값 받기(성공시)
					if(insertLocks(dno)) { // 권한테이블에 문서번호 필드 생성하고 결과값 받기(성공시)
						if(insertLink(dno)) { // 링크테이블에 문서번호 필드 생성하고 성공시
							if(SpecialDao.getSpecialDao().isNeedWrite(title)) { // 작성해야 하는 문서 목록에 있는지 물어보고 자동 처리 후
								if(SpecialDao.getSpecialDao().reverseLink(dno, c.getDcontent())) { // 역링크테이블에 역링크 거는부분 판단해서 필드 생성 후 성공시
									return true;
								}else {
									System.out.println("작성이 필요한 문서인지 확인과정 오류"); return false;
								}
							}else {
							System.out.println("역링크 생성 오류"); return false;	
							}
						}else {
							System.out.println("링크에 문서번호 필드 생성 오류 "); return false;
						}
					}else {
						System.out.println("문서권한 필드 생성 오류 "); return false;
					}
				}else { // 문서 내용 넣기 실패시
					System.out.println("문서내용 필드 생성 오류"); return false;
				}
			}
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	
	//문서 내용 넣기 메소드
	public boolean setContent(Content c, int dno) {
		//content 테이블에 해당 번호를 포함한 필드 생성
		String sql="insert into content(dno,mid,dcontent,dgood,dimg) values (?,?,?,?,?)";
		try {
		ps=con.prepareStatement(sql);
		ps.setInt(1, dno);
		ps.setString(2, c.getMid());
		ps.setString(3, c.getDcontent());
		ps.setInt(4, c.getDgood());
		ps.setString(5, c.getDimg());
		ps.executeUpdate();
		return true;
		}catch(Exception e) {System.out.println("sef");}
		return false;
	}
	
	
	//문서 권한 필드 생성 메소드
	public boolean insertLocks(int dno) {
		//처음 생성은 전부 기본값으로, 문서번호만 연결시켜 생성
		String sql="insert into locks(dno) values ("+dno+")";
		try {
		ps=con.prepareStatement(sql);
		ps.executeUpdate();
		return true;
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	//문서 링크 필드 생성 메소드
	public boolean insertLink(int dno) {
		//처음 생성은 전부 기본값으로, 문서번호만 연결시켜 생성
		String sql="insert into link (dno) values ("+dno+")";
		try {
			ps=con.prepareStatement(sql);
			ps.executeUpdate();
			return true;
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	
	// 문서 링크 필드 업데이트 메소드 완전 구현 아님
	public boolean updateLink(int dno) {
		String sql = "update link set dno = ? where dno = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, dno);
			ps.setInt(2, dno);
			ps.executeUpdate();
			return true;
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	
	//해당 번호의 최신 문서정보 불러오기 메소드
	public Content docuLoad(int dno) {
		String sql="select * from content where dno=? order by cid desc";
		try {
			ps=con.prepareStatement(sql);
			ps.setInt(1, dno);
			rs=ps.executeQuery();
			if(rs.next()) {
				Blob blob=rs.getBlob(5);
				String str="";
				String string="";
				BufferedReader br=new BufferedReader(new InputStreamReader(blob.getBinaryStream()));
				while((string=br.readLine())!=null) {
					str+=string;
				}
				Content content=new Content(rs.getInt(1), rs.getInt(2),
						rs.getString(3), rs.getString(4), str.toString(), rs.getInt(6), rs.getString(7));
				return content;
			}
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}
	//해당 제목의 최신 문서정보 불러오기 메소드
	public Content docuLoad(String title) {
		String sql="select dno from document where dtitle=? order by dno desc";
		try {
			ps=con.prepareStatement(sql);
			ps.setString(1, title);
			//제목을 넣어서 가장 최근의 dno값 구하기
			rs=ps.executeQuery();
			if(rs.next()) {
				//그 dno값으로 content 구해서 리턴시키기
				return docuLoad(rs.getInt(1));
			}
		}catch(Exception e) {e.printStackTrace();}
		return null;}
	//해당 제목의 최신 dno 리턴 메소드
	public int getdno(String title) {
		String sql="select dno from document where dtitle='"+title+"'";
		try {
			ps=con.prepareStatement(sql);
			rs=ps.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}else {
				return -1;
			}
		}catch(Exception e) {e.printStackTrace();}
		return -1;
	}
	//작성된 문서들의 리스트 출력
	public ArrayList<Document> doculist() {
		ArrayList<Document> dlist = new ArrayList<>();
		String sql = "select * from document order by dno desc";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()) {
				Document document = new Document(rs.getInt(1), rs.getString(2));
				dlist.add(document);
			}
			return dlist;
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}
	
	//문서 내용 수정 메소드(제목,번호가 같은 새 데이터 생성)
	public boolean docuEdit(Content c) {
		String sql="insert into content(dno,mno,dcontent,dgood) values (?,?,?,?)";
		try {
		ps=con.prepareStatement(sql);
		ps.setInt(1, c.getDno());
		ps.setString(2, c.getMid());
		ps.setString(3, c.getDcontent());
		ps.setInt(4, c.getDgood());
		ps.executeUpdate();
		return true;
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	//문서 삭제 요청 메소드
	public boolean docuDelAsk(int dno) {
		//문서 보이게하는 여부 판단값(seenat) 1로 변경시키기
		String sql="update locks set seenat=1 where dno="+dno;
		try {
			ps=con.prepareStatement(sql);
			ps.executeUpdate();
			return true;
		}catch(Exception e) {e.printStackTrace();}
	    return false;
	}
	//동의어 등록 메소드
	public boolean setSyn(String dno, String syn) {
		//문서번호(텍스트로 이루어져있음)와 동의어(텍스트)를 받아서 등록
		String sql="insert into synonys(dno, synpage) values (?,?)";
		try {
			ps=con.prepareStatement(sql);
			ps.setString(1, dno);
			ps.setString(2, syn);
			ps.executeUpdate();
			return true;
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	public boolean checkSyn(String syn) {
		// 동의어(텍스트)를 받아서 등록 여부를 체크
		String sql="select * from synonys where synpage = ?";
		try {
			ps=con.prepareStatement(sql);
			ps.setString(1, syn);
			rs = ps.executeQuery();
			if(rs.next()) {return true;}
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	//검색한 단어가 동의어라면 리다이렉트용 문서번호 반환 메소드
	public int getsyno(String text) {
		String sql="select dno from synonys where synpage="+text;
		try {
			ps=con.prepareStatement(sql);
			rs=ps.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
		}catch(Exception e) {e.printStackTrace();}
		return 0;
	}
	//문서 역사 리스트 반환 메소드
	public ArrayList<Content> getDocuList(int dno) { // 문서의 번호 받아서 해당 번호의 데이터들 출력
		ArrayList<Content> list=new ArrayList<Content>();
		String sql="select cid, mid, updatetime, dgood from content where dno="+dno;
		try {
			ps=con.prepareStatement(sql);
			rs=ps.executeQuery();
			while(rs.next()) {
				Content c=new Content(rs.getInt(1), 0, rs.getString(2), rs.getString(3), null, rs.getInt(4), rs.getString(5));
				list.add(c);
			}return list;
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}
	//문서 좋아요 숫자 반환 메소드
	public int getGood(int dno) {
		String sql="select dgood from content where dno="+dno+" order by cid desc";
		try {
			ps=con.prepareStatement(sql);
			rs=ps.executeQuery();
			if(rs.next()) {
				return rs.getInt(1);
			}
		}catch(Exception e) {e.printStackTrace();}
		return -1;
	}
	//문서 번호를 통한 문서의 제목 추출 메소드
	public String getTitle(int dno) {
		String sql = "select dtitle from document where dno = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, dno);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}
	// 문서 번호를 통한 문서의 작성자 추출 메소드
	public String getMid(int dno) {
		String sql = "select mid from content where dno = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, dno);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}
	// 페이징처리를 위한 문서 목록 출력 by json
	public JSONArray doculistbyjson() {
		try {
		JSONArray array = new JSONArray(); 
		String sql = "select * from document";
		ps = con.prepareStatement(sql);
		rs = ps.executeQuery();
		while(rs.next()) {
			JSONObject jo = new JSONObject();
			jo.put("dno", rs.getInt(1));
			jo.put("dtitle", rs.getString(2));
			array.put(jo);
		}		
			return array;
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}
	// 페이징처리를 위한 문서 목록 출력 by json
		public JSONArray newdoculistbyjson() {
			try {
			JSONArray array = new JSONArray(); 
			String sql = "select * from document order by dno desc limit 10;";
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()) {
				JSONObject jo = new JSONObject();
				jo.put("dno", rs.getInt(1));
				jo.put("dtitle", rs.getString(2));
				array.put(jo);
			}		
				return array;
			}catch(Exception e) {e.printStackTrace();}
			return null;
		}
		
	// 문서 내용의 길이가 긴 문서 검색
	public ArrayList<Content> doclonglist() { // 200자가 넘는 내용이 긴 문서들을 출력
		ArrayList<Content> list = new ArrayList<Content>();
		String sql = "select * from content where CHAR_LENGTH(dcontent) > 400";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			
			while(rs.next()) {
			Content content = new Content(rs.getInt(1), rs.getInt(2),
					rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6), rs.getString(7));
			list.add(content);
			}
			return list;
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}
	//문서 최근 개수 보여주기
	public String docuFindCount(String today) {
		
		String sql="SELECT count(dno) FROM treewiki.content where date_format(updatetime,'%Y-%m-%d')=? group by date_format(updatetime,'%Y-%m-%d')";
		
		try {
			ps=con.prepareStatement(sql);
			ps.setString(1, today);
			rs=ps.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
		} catch (Exception e) {
			System.out.println("docu find count err!! "+e);
		}
		return null;
	}
	//차트 데이터 구하기
	public ArrayList<Content> docuChart(){
		ArrayList<Content> list = new ArrayList<Content>();
		String sql= "SELECT count(dno),date_format(updatetime,'%Y-%m-%d') FROM treewiki.content where date_format(updatetime,'%Y-%m-%d') group by date_format(updatetime,'%Y-%m-%d')";
		try {
			ps=con.prepareStatement(sql);
			rs=ps.executeQuery();
			while(rs.next()) {
				Content content = new Content(0, rs.getInt(1), sql, rs.getString(2), sql, 0, sql);
				list.add(content);
			}
			return list;
		} catch (Exception e) {
			System.out.println("docu chart err!!" +e);
		}
		return null;
	}
		
	// 문서 내용의 길이가 짧은 문서 검색
	public ArrayList<Content> docshortlist() { // 50자보다 내용이 짧은 문서들을 출력
		ArrayList<Content> list = new ArrayList<Content>();
		String sql = "select * from content where CHAR_LENGTH(dcontent) < 50";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()) {
				Content content = new Content(rs.getInt(1), rs.getInt(2),
						rs.getString(3), rs.getString(4), rs.getString(5), rs.getInt(6), rs.getString(7));
				list.add(content);
			}
			return list;
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}	
		
		
	// 랜덤으로 문서 페이지 추출하는 메소드
	public ArrayList<Document> docrandomlist() {
		ArrayList<Document> list = new ArrayList<Document>();
		String sql = "select dno from document order by rand() limit 10";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()) {
				Document document = new Document(rs.getInt(1), null);
				list.add(document);
			}
			return list;
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}
		
	//문서 삭제 메소드
	public boolean docuDelete(int dno) {
		String sql="delete from link where dno="+dno; // 링크 테이블에서 삭제
		try {
			ps=con.prepareStatement(sql);
			ps.executeUpdate();
			sql="delete from locks where dno="+dno; // 권한 테이블에서 삭제
				ps=con.prepareStatement(sql);
				ps.executeUpdate();
				sql="delete from content where dno="+dno; // 내용 테이블에서 삭제
					ps=con.prepareStatement(sql);
					ps.executeUpdate();
						sql="delete from document where dno="+dno; // 제목 테이블에서 삭제
							ps=con.prepareStatement(sql);
							ps.executeUpdate();
					return true;
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	// 동의어 리스트
	public ArrayList<Synonys> synlist() {
		ArrayList<Synonys> list = new ArrayList<Synonys>();
		String sql = "select * from synonys";
		try {
			ps = con.prepareStatement(sql);
			rs = ps.executeQuery();
			while(rs.next()) {
				Synonys s = new Synonys(rs.getInt(1), rs.getString(2), rs.getString(3));
				list.add(s);
			}
			return list;
		}catch(Exception e) {e.printStackTrace();}
		return null;
	} 
	// 동의어 수정
	public boolean synupdate(String dno, String syn, int sno) {
		String sql = "update synonys set dno = ? synpage = ? where sno = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, dno);
			ps.setString(2, syn);
			ps.setInt(3, sno);
			ps.executeUpdate();
			return true;
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	// 동의어 삭제
	public boolean syndelete(int sno) {
		String sql = "delete * from synonys where sno = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setInt(1, sno);
			ps.executeUpdate();
			return true;
		}catch(Exception e) {e.printStackTrace();}
		return false;
	}
	// 문서번호를 토대로 동의어 출력
	public String synprint(String dno) {
		String sql = "select synpage from synonys where dno = ?";
		try {
			ps = con.prepareStatement(sql);
			ps.setString(1, dno);
			rs = ps.executeQuery();
			if(rs.next()) {
				return rs.getString(1);
			}
		}catch(Exception e) {e.printStackTrace();}
		return null;
	}
}
