package jaesik.model;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import sujin.model.MemberVO;
import yunhwan.model.BuyerTypeVO;
import yunhwan.model.ProductVO;
import yunhwan.model.ShoesCategoryVO;

public class JS_MemberDAO implements JS_InterMemberDAO {
	
	private DataSource ds;
	private Connection conn;
	private PreparedStatement pstmt;
	private ResultSet rs;
	private AES256 aes;
	
	private void close() {
		
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (pstmt != null) {
				pstmt.close();
				pstmt = null;
			}
			if (conn != null) {
				conn.close();
				conn = null;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public JS_MemberDAO () {
		try {
			Context initContext = new InitialContext();
			Context envContext  = (Context)initContext.lookup("java:/comp/env");
			ds = (DataSource)envContext.lookup("jdbc/semi_oracle");
			
			aes = new AES256(SecretMyKey.KEY);
			// 양방향 암호화하는 인스턴스 생성 - SecretMyKey.KEY 만든 암호화/복호화 키이다.
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e ) {
			e.printStackTrace();
		}
		
	}

	
	// -------------------------------------- 입력받은 이메일로 가입된 유저로 존재하는지 알아오는 메소드 존재하면 해당 id를 리턴
	@Override
	public String isUserExistID (String email) throws SQLException {
		
		String isUserExistID = null;

		try {
			conn = ds.getConnection();
			
			String sql =  " select userid "
						+ " from tbl_member "
						+ " where status = 0 and idle = 0 and email = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, aes.encrypt(email) );
			rs = pstmt.executeQuery();
			
			if(rs.next()) {
				isUserExistID = rs.getString(1);
			}
			
			
		} catch (GeneralSecurityException | UnsupportedEncodingException e){
			e.printStackTrace();
		} finally {
			close();
		}
		
		return isUserExistID;
	}

	
	// ---------------------------- 가입된 유저이므로 해당 유저에게 발급된 쿠폰번호와 같은 쿠폰번호를 쿠폰테이블에 insert
	@Override
	public int sendCouponCode(String isUserExistID, String certificationCode) throws SQLException {

		int n = 0;
		
		try {
			conn = ds.getConnection();
			String sql = " insert into tbl_user_coupon (coupon_no, fk_userid, coupon_name, coupon_dis_percent)"+
						 " values ( ?, ?, '첫 온라인 구매 10% 할인쿠폰', 10 )";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, certificationCode);
			pstmt.setString(2, isUserExistID); 
			
			n = pstmt.executeUpdate();
			
		} finally {
			close();
		}
		return n;
	}

	
	// ----------------------------------------------  신규회원 쿠폰을 이미 지급 받은사람인지 체크하는 메소드 발급된 유저이면 true
	@Override
	public boolean isCouponExist(String isUserExistID) throws SQLException {
		
		boolean isCouponExist = false;
		
		try {
			conn = ds.getConnection();
			
			String sql = " select fk_userid "+
						 " from TBL_USER_COUPON "+
						 " where coupon_no like '%NEW10' and fk_userid = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, isUserExistID);
			
			rs = pstmt.executeQuery();
			
			isCouponExist = rs.next();
			
		} finally {
			close();
		}
		
		return isCouponExist;
	}

	
	
	// ------------------------------------------------------------------------ 해당 유저가 존재하는지 알아오는 메소드
	@Override
	public boolean isUserExist(String userid) throws SQLException {

		boolean isUserExist = false;

		try {
			conn = ds.getConnection();
			
			String sql =  " select userid "
						+ " from tbl_member "
						+ " where status = 0 and idle = 0 and userid = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userid);
			rs = pstmt.executeQuery();
			
			isUserExist = rs.next();
			
			
		} finally {
			close();
		}
		
		return isUserExist;
	}
	
	
	// ----------------------------------------------------------------- 고객센터로 보낸 메시지를 board 테이블에 insert
	@Override
	public int uploadBoard(BoardVO bvo) throws SQLException {
		
		int n = 0;
		
		try {
			conn = ds.getConnection();
			String sql = " insert into tbl_board (board_no, board_title, board_content, board_registerdate, fk_userid)"+
						 " values (seq_board_no.nextval, ? , ? , default, ? )";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, bvo.getBoard_title());
			pstmt.setString(2, bvo.getBoard_content());
			pstmt.setString(3, bvo.getFk_userid());
			
			n = pstmt.executeUpdate();
			
		} finally {
			close();
		}
		return n;
	}

	// ------------------------------------------- 페이징 처리를 위한 검색이 있는 검색이 없는 전체회원에 대한 총 페이지 알아오기

	@Override
	public int getTotalPage(Map<String, String> paraMap) throws SQLException {

		int totalPage = 0;
		
		try {
			conn = ds.getConnection();
			
			
			String sql =  " select ceil (count(*) / 10 )"
						+ " from tbl_member "
						+ " where userid != 'admin' ";
			
			String colname = paraMap.get("searchType");
			String searchWord = paraMap.get("searchWord");
			
			if ( "email".equals(colname) ) {
				// 검색대상이 이메일인 경우 
				searchWord = aes.encrypt(searchWord);
			}
			
			if ( !"".equals(colname) && searchWord != null && !searchWord.trim().isEmpty() ) {
				sql += " and "+colname+" like '%'|| ? || '%' ";
			}
			// 컬럼명과 테이블명은 위치홀더로 사용하면 안된다. 위치홀더로 들어오는 것은 오직 데이터 값이어야 한다.
			
			pstmt = conn.prepareStatement(sql);
			if ( !"".equals(colname) && searchWord != null && !searchWord.trim().isEmpty() ) {
				pstmt.setString(1, searchWord);
			}
			
			rs = pstmt.executeQuery();
			
			rs.next();
			totalPage = rs.getInt(1);
		
			
		} catch ( GeneralSecurityException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return totalPage;

		
	}

	
	// ---------------------------------------- 타입과 단어, 페이징 갯수를 받아 페이징한 모든회원 또는 검색한 회원 목록 보여주기
	@Override
	public List<MemberVO> selectPagingMember(Map<String, String> paraMap) throws SQLException {
	
		List <MemberVO> memberList = new ArrayList<>();
		
		try {
			conn = ds.getConnection();
			
			String sql =  " SELECT userid, name, email, mobile, address"+
						  "  FROM "+
						  "  ("+
						  "   select rownum AS RNO, userid, name, email, mobile, address\n"+
						  "   from "+
						  "   	  ("+
						  "        select userid, name, email, mobile, address"+
						  "        from tbl_member"+
						  "        where userid != 'admin'";
			
			String colname = paraMap.get("searchType");
			String searchWord = paraMap.get("searchWord");
			
			if ( "email".equals(colname) ) {
				// 검색대상이 이메일인 경우 
				searchWord = aes.encrypt(searchWord);
			}
			
			if ( !"".equals(colname) && searchWord != null && !searchWord.trim().isEmpty() ) {
				sql += " and "+colname+" like '%'|| ? || '%' ";
			}
			// 컬럼명과 테이블명은 위치홀더로 사용하면 안된다. 위치홀더로 들어오는 것은 오직 데이터 값이어야 한다.
			
			sql += " order by registerday desc "
				 + "       ) V "
				 + " ) T "
				 + " WHERE RNO between ? and ? ";
			
			pstmt = conn.prepareStatement(sql);
			
			int currentShowPageNo = Integer.parseInt(paraMap.get("currentShowPageNo"));  // 조회하고자하는 페이지번호
			
			
			// 페이징 처리 공식
			// WHERE RNO between (조회하고자하는 페이지번호 * 한페이지당 보여줄 행의개수) - (한페이지당 보여줄 행의개수 - 1) 
			// and (조회하고자하는 페이지번호 * 한페이지당 보여줄 행의개수);
			
			if ( !"".equals(colname) && searchWord != null && !searchWord.trim().isEmpty() ) {
				pstmt.setString(1, searchWord);
				pstmt.setInt(2, (currentShowPageNo * 10) - (10 - 1) );
				pstmt.setInt(3, (currentShowPageNo * 10) );
			}
			else {
				pstmt.setInt(1, (currentShowPageNo * 10) - (10 - 1) );
				pstmt.setInt(2, (currentShowPageNo * 10) );
			}
			
			rs = pstmt.executeQuery();
			
			while ( rs.next() ) {
				MemberVO member = new MemberVO();
				
				member.setUserid(rs.getString(1));
				member.setName(rs.getString(2));
				member.setEmail( aes.decrypt(rs.getString(3)) );
				member.setMobile( aes.decrypt(rs.getString(4)));
				member.setAddress( rs.getString(5));
								
				memberList.add(member);
			}
			
		} catch ( GeneralSecurityException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		return memberList;
	}

	
	
	// ------------------------------------------------------------------------------- 회원 하나의 상세정보를 담아오는 메소드 
	@Override
	public MemberVO memberOneDetailAction(String userid) throws SQLException {

		MemberVO member = null;
		
		try {
			conn = ds.getConnection();
			
			String sql =  " select userid, name, email, mobile, postcode, address, detailaddress, extraaddress, gender"
						+ " 	 , birthday, point, to_char( registerday, 'yyyy-mm-dd') as registerday"
						+ " from tbl_member "
						+ " where userid = ? ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, userid);
			
			rs = pstmt.executeQuery();
			
			if ( rs.next() ) {
				member = new MemberVO();
				
				member.setUserid(rs.getString(1));
				member.setName(rs.getString(2));;
				member.setEmail( aes.decrypt(rs.getString(3)) );
				member.setMobile( aes.decrypt(rs.getString(4)) );
				member.setPostcode(rs.getString(5));
				member.setAddress(rs.getString(6));
				member.setDetailaddress(rs.getString(7));
				member.setExtraaddress(rs.getString(8));
				member.setGender(rs.getString(9));
				member.setBirthday(rs.getString(10));
				member.setPoint( rs.getInt(11) );
				member.setRegisterday(rs.getString(12));
			}
			
		} catch ( GeneralSecurityException | UnsupportedEncodingException e) {
			e.printStackTrace();
		} finally {
			close();
		}
		
		
		return member;
	
	}

	
	// ------------------------------------------------------------ 고객센터 게시판의 게시물 총페이지를 알아오기
	@Override
	public int getBoardTotalPage(Map<String, String> paraMap) throws SQLException {

		int totalPage = 0;
		
		try {
			conn = ds.getConnection();
			
			
			String sql =  " select ceil (count(*) / 10 )"
						+ " from tbl_board ";
			
			String colname = paraMap.get("searchType");
			String searchWord = paraMap.get("searchWord");
			
			
			if ( !"".equals(colname) && searchWord != null && !searchWord.trim().isEmpty() ) {
				sql += " where "+colname+" like '%'|| ? || '%' ";
			}
			// 컬럼명과 테이블명은 위치홀더로 사용하면 안된다. 위치홀더로 들어오는 것은 오직 데이터 값이어야 한다.
			
			pstmt = conn.prepareStatement(sql);
			
			if ( !"".equals(colname) && searchWord != null && !searchWord.trim().isEmpty() ) {
				pstmt.setString(1, searchWord);
			}
			
			rs = pstmt.executeQuery();
			
			rs.next();
			
			totalPage = rs.getInt(1);
		
			
		} finally {
			close();
		}
		
		return totalPage;
		
	}

	
	// 검색form을 받아 페이징한 고객센터 게시판, 검색한 게시판 보여주기
	@Override
	public List<BoardVO> selectPagingBoard(Map<String, String> paraMap) throws SQLException {

		List <BoardVO> boardList = new ArrayList<>();
		
		try {
			conn = ds.getConnection();
			
			String sql = " SELECT board_no, board_title, board_content, fk_userid, board_registerdate"+
						 " FROM"+
						 " ("+
						 " select rownum AS RNO, board_no, board_title, board_content, fk_userid, board_registerdate"+
						 " from("+
						 "     select board_no, board_title, board_content, fk_userid, to_char( board_registerdate, 'yyyy-mm-dd') as board_registerdate"+
						 "     from tbl_board";
			
			String colname = paraMap.get("searchType");
			String searchWord = paraMap.get("searchWord");
			
			
			if ( !"".equals(colname) && searchWord != null && !searchWord.trim().isEmpty() ) {
				sql += " where "+colname+" like '%'||  ?  ||'%' ";
			}
			// 컬럼명과 테이블명은 위치홀더로 사용하면 안된다. 위치홀더로 들어오는 것은 오직 데이터 값이어야 한다.
			
			sql +=  "     order by board_registerdate desc , board_no desc"+
					"     ) V"+
					" ) T"+
					" WHERE RNO between ? and ? ";
			
			pstmt = conn.prepareStatement(sql);
			
			int currentShowPageNo = Integer.parseInt(paraMap.get("currentShowPageNo"));  // 조회하고자하는 페이지번호
			
			
			// 페이징 처리 공식
			// WHERE RNO between (조회하고자하는 페이지번호 * 한페이지당 보여줄 행의개수) - (한페이지당 보여줄 행의개수 - 1) 
			// and (조회하고자하는 페이지번호 * 한페이지당 보여줄 행의개수);
			
			if ( !"".equals(colname) && searchWord != null && !searchWord.trim().isEmpty() ) {
				pstmt.setString(1, searchWord);
				pstmt.setInt(2, (currentShowPageNo * 10) - (10 - 1) );
				pstmt.setInt(3, (currentShowPageNo * 10) );
			}
			else {
				pstmt.setInt(1, (currentShowPageNo * 10) - (10 - 1) );
				pstmt.setInt(2, (currentShowPageNo * 10) );
			}
			
			rs = pstmt.executeQuery();
			
			while ( rs.next() ) {
				BoardVO board = new BoardVO();
				
				board.setBoard_no(rs.getInt(1));
				board.setBoard_title(rs.getString(2));
				board.setBoard_content(rs.getString(3));
				board.setFk_userid(rs.getString(4));
				board.setBoard_registerdate(rs.getString(5));
								
				boardList.add(board);
			}
			
		} finally {
			close();
		}
		
		return boardList;
	}

	
	
	// 고객유형 (제품 대분류) 목록을 조회해오기
	@Override
	public List<BuyerTypeVO> selectBuyerTypeList() throws SQLException {

		List<BuyerTypeVO> buyerType = new ArrayList<>();
		
		try {
			conn = ds.getConnection();
			
			String sql =  " select * "
						+ " from tbl_buyer_type "
						+ " order by buyer_type_no asc ";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				
				BuyerTypeVO bvo = new BuyerTypeVO();
				
				bvo.setBuyer_type_no(rs.getString(1));
				bvo.setBuyer_type_name(rs.getString(2));
				
				buyerType.add(bvo);
			}
			
		} finally {
			close();
		}
		
		return buyerType;
	}

	
	
	// 신발 카테고리 목록 조회해오기
	@Override
	public List<ShoesCategoryVO> selectCategoryList() throws SQLException {

		List<ShoesCategoryVO> categoryList = new ArrayList<>();
		
		try {
			conn = ds.getConnection();
			
			String sql =  " select shoes_category_no, shoes_category_name, buyer_type_name "
						+ " from tbl_shoes_category C "
						+ " join tbl_buyer_type B on c.fk_buyer_type_no = b.buyer_type_no "
						+ " order by shoes_category_no asc ";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				
				ShoesCategoryVO shoevo = new ShoesCategoryVO();
				
				shoevo.setShoes_category_no(rs.getString(1));
				shoevo.setShoes_category_name(rs.getString(2));
				shoevo.setFk_buyer_type_no(rs.getString(3));
				
				categoryList.add(shoevo);
			}
			
		} finally {
			close();
		}
		
		return categoryList;
	}
	
	
	// 색상 컬럼 가져오기
	@Override
	public List<String> selectColorList() throws SQLException {

		List<String> colorList = new ArrayList<>();
		
		try {
			conn = ds.getConnection();
			
			String sql =  " select distinct( product_color) as color "
						+ " from tbl_product "
						+ " order by color asc ";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				colorList.add(rs.getString(1));
			}
			
		} finally {
			close();
		}
	
		return colorList;
	}

	
	
	// 신발 사이즈 컬럼 가져오기
	@Override
	public List<String> selectSizeList() throws SQLException {

		List<String> sizeList = new ArrayList<>();
		
		try {
			conn = ds.getConnection();
			
			String sql =  " select distinct( product_size) as psize "
						+ " from tbl_product "
						+ " order by psize asc ";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while (rs.next()) {
				sizeList.add(rs.getString(1));
			}
			
		} finally {
			close();
		}
	
		return sizeList;
	}

	@Override
	public int getProduct_no() throws SQLException {

		int product_no = 0;
		
		try {
			conn = ds.getConnection();
			
			String sql =  " select seq_product_no.nextval AS product_no "
						+ " from dual ";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			rs.next();
			product_no = rs.getInt(1);
			
			
		} finally {
			close();
		}
		
		return product_no;
	
	}

	
	
	// 제품등록할 pvo를 tbl_product 테이블에 insert
	@Override
	public int productInsert(ProductVO pvo) throws SQLException {

		int result = 0;
	      
	    try {
	    	conn = ds.getConnection();
	         
	        String sql = " insert into tbl_product (product_no, product_name, fk_shoes_category_no"
	        		   + ", product_price, product_color, product_size, product_image, product_date "
	        		   + ", product_content, stock_count) " 
	        		   + " values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	         
	        pstmt = conn.prepareStatement(sql);
	         
	        pstmt.setString(1, pvo.getProduct_no());
	        pstmt.setString(2, pvo.getProduct_name());
	        pstmt.setInt(3, pvo.getFk_shoes_category_no());
	        pstmt.setInt(4, pvo.getProduct_price());
	        pstmt.setString(5, pvo.getProduct_color());
	        pstmt.setInt(6, pvo.getProduct_size());
	        pstmt.setString(7, pvo.getProduct_image());
	        pstmt.setString(8, pvo.getProduct_date());
	        pstmt.setString(9, pvo.getProduct_content());
	        pstmt.setInt(10, pvo.getStock_count());

	        result = pstmt.executeUpdate();
	         
	    } finally {
	    	close();
	    }
	      
	    return result;
	}

	
	
	// ----------------------------------------------------------------------- 타입과 검색단어를 받아와 검색하는 메소드
	@Override
	public List<ProductVO> selectSearchProduct(Map<String, String> paraMap) throws SQLException {

		List<ProductVO> prodList = new ArrayList<>();
		
		try {
			conn = ds.getConnection();
			String sql =  " select product_no, product_name, fk_shoes_category_no, product_color, product_size, product_image"
						+ "		, product_content , stock_count, sale_count, product_price "
						+ " from tbl_product ";
			
			String buyer_type = paraMap.get("buyer_type");
			String search_word = paraMap.get("search_word");
			
			if ( "".equals(buyer_type)) {
				buyer_type = "'%'";
			}
			
			if ( search_word != null && !search_word.trim().isEmpty() ) {
				sql += " where product_no like "+buyer_type+" and product_name like '%'|| ? || '%' ";
			}
			// 컬럼명과 테이블명은 위치홀더로 사용하면 안된다. 위치홀더로 들어오는 것은 오직 데이터 값이어야 한다.
			
			sql += " order by upload_date desc";
			// 신상품으로 정렬
			
			
			pstmt = conn.prepareStatement(sql);
			if ( search_word != null && !search_word.trim().isEmpty() ) {
				pstmt.setString(1, search_word);
			}
			
			rs = pstmt.executeQuery();
			
			while ( rs.next() ) {
				
				ProductVO pvo = new ProductVO();
				
				pvo.setProduct_no(rs.getString("product_no") );
				pvo.setProduct_name(rs.getString("product_name"));  
				pvo.setFk_shoes_category_no(rs.getInt("fk_shoes_category_no"));
			    pvo.setProduct_color(rs.getString("product_color"));
				pvo.setProduct_size(rs.getInt("product_size"));
				pvo.setProduct_image(rs.getString("product_image"));
				pvo.setProduct_content(rs.getString("product_content"));
				pvo.setStock_count(rs.getInt("stock_count"));
				pvo.setSale_count(rs.getInt("sale_count"));
				pvo.setProduct_price(rs.getInt("product_price"));
				
				prodList.add(pvo);
			}
			
		} finally {
			close();
		}
		
		return prodList;
	}

	
	// 전체상품을 select 해오는 메소드
	@Override
	public List<ProductVO> selectAll() throws SQLException {
		
		List<ProductVO> prodList = new ArrayList<>();
		
		try {
			conn = ds.getConnection();
			String sql = " select *"
					   + " from tbl_product"
					   + " order by upload_date";
			
			pstmt = conn.prepareStatement(sql);
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				ProductVO pvo = new ProductVO();
				
				pvo.setProduct_no(rs.getString(1));
				pvo.setProduct_name(rs.getString(2));
				pvo.setFk_shoes_category_no(rs.getInt(3));
				pvo.setProduct_price(rs.getInt(4));
				pvo.setProduct_color(rs.getString(5));
				pvo.setProduct_size(rs.getInt(6));
				pvo.setProduct_image(rs.getString(7));
				pvo.setProduct_date(rs.getString(8));
				pvo.setProduct_content(rs.getString(9));
				pvo.setUpload_date(rs.getString(10));
				pvo.setStock_count(rs.getInt(11));
				pvo.setSale_count(rs.getInt(12));
				
	            prodList.add(pvo);
			}
			
		} finally {
			close();
		}
		
		return prodList;
	
	}

	
	
	// Ajax(JSON)를 이용한 더보기 방식(페이징처리)으로 상품정보를 9개씩 잘라서(start ~ end) 조회해오기
	@Override
	public List<ProductVO> selectAllProduct(Map<String, String> paraMap) throws SQLException {

		List<ProductVO> prodList = new ArrayList<>();

		try {
			conn = ds.getConnection();
			String sql = " select product_no, product_name, fk_shoes_category_no, product_price, product_color, product_size\n"+
						 "     , product_image, product_date, product_content, upload_date, stock_count, sale_count\n"+
						 " from \n"+
						 " (select row_number() over (order by upload_date desc) as RNO, product_no, product_name, fk_shoes_category_no, product_price, product_color, product_size\n"+
						 "     , product_image, product_date, product_content, upload_date, stock_count, sale_count\n"+
						 " from tbl_product)\n"+
						 " where rno between ? and ?";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, paraMap.get("start"));
			pstmt.setString(2, paraMap.get("end"));
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				ProductVO pvo = new ProductVO();
				
				pvo.setProduct_no(rs.getString(1));
				pvo.setProduct_name(rs.getString(2));
				pvo.setFk_shoes_category_no(rs.getInt(3));
				pvo.setProduct_price(rs.getInt(4));
				pvo.setProduct_color(rs.getString(5));
				pvo.setProduct_size(rs.getInt(6));
				pvo.setProduct_image(rs.getString(7));
				pvo.setProduct_date(rs.getString(8));
				pvo.setProduct_content(rs.getString(9));
				pvo.setUpload_date(rs.getString(10));
				pvo.setStock_count(rs.getInt(11));
				pvo.setSale_count(rs.getInt(12));

	            prodList.add(pvo);
			}
			
		} finally {
			close();
		}
		
		return prodList;
	
	}

	
	// 카테고리별로 Ajax(JSON)를 이용한 더보기 방식(페이징처리)으로 상품정보를 9개씩 잘라서(start ~ end) 조회해오기
	@Override
	public List<ProductVO> selectGroupProduct(Map<String, String> paraMap) throws SQLException {

		List<ProductVO> prodList = new ArrayList<>();

		try {
			conn = ds.getConnection();
			String sql = " select product_no, product_name, fk_shoes_category_no, product_price, product_color, product_size"+
						 "     , product_image, product_date, product_content, upload_date, stock_count, sale_count"+
						 " from "+
						 " ("+ 
						 " select row_number() over (order by upload_date desc) as RNO, product_no, product_name, fk_shoes_category_no, product_price, product_color, product_size"+
						 "     , product_image, product_date, product_content, upload_date, stock_count, sale_count"+
						 " from tbl_product "+ 
						 " where fk_shoes_category_no like '%'|| ? || '%' "+ 
						 " ) "+
						 " where rno between ? and ?";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, paraMap.get("category"));
			pstmt.setString(2, paraMap.get("start"));
			pstmt.setString(3, paraMap.get("end"));
			
			rs = pstmt.executeQuery();
			
			while(rs.next()) {
				ProductVO pvo = new ProductVO();
				
				pvo.setProduct_no(rs.getString(1));
				pvo.setProduct_name(rs.getString(2));
				pvo.setFk_shoes_category_no(rs.getInt(3));
				pvo.setProduct_price(rs.getInt(4));
				pvo.setProduct_color(rs.getString(5));
				pvo.setProduct_size(rs.getInt(6));
				pvo.setProduct_image(rs.getString(7));
				pvo.setProduct_date(rs.getString(8));
				pvo.setProduct_content(rs.getString(9));
				pvo.setUpload_date(rs.getString(10));
				pvo.setStock_count(rs.getInt(11));
				pvo.setSale_count(rs.getInt(12));

	            prodList.add(pvo);
			}
			
		} finally {
			close();
		}
		
		return prodList;
	
	}

	
	// 카테고리별 총 페이지 (셀렉트 개수) 알아오기
	@Override
	public int totalCount(String category) throws SQLException {
		
		int count = 0 ;
		
		try {
			conn = ds.getConnection();
			String sql = " select count(*)"+
						 " from tbl_product "+ 
						 " where fk_shoes_category_no like '%'|| ? || '%' ";
			
			pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, category);
			rs = pstmt.executeQuery();
			
			if (rs.next()) {
				count = rs.getInt(1);
			}
			
		} finally {
			close();
		}
		return count;
	}

	
	
	// 모든 사용자들이 주문한 갯수를 알아온다.
	@Override
	public int getTotalCountOrder() throws SQLException {

		int totalCountOrder = 0;
	      
	    try {
	    	conn = ds.getConnection();
	         
	        String sql = " select count(*) "
	        		   + " from tbl_order_detail";
	         
	        pstmt = conn.prepareStatement(sql);
	        rs = pstmt.executeQuery();
	        rs.next();
	                  
	        totalCountOrder = rs.getInt(1);
	         
	    } finally {
	    	close();
	    }

	    return totalCountOrder;
	}

	
	
	
    // 모든 사용자들의 주문내역을 페이징 처리하여 조회해온다.
	@Override
	public List<HashMap<String, String>> getOrderList(int currentShowPageNo, int sizePerPage) throws SQLException {

		List<HashMap<String, String>> orderList = null;
	      
		try {
			conn = ds.getConnection();
	          
			String sql = " select order_no, fk_userid, orderdate, delivery_fee\n"+
						 "     , total_price, order_detail_no, fk_product_no, order_count, order_price, delivery_mobile, delivery_address\n"+
						 "     , delivery_comment, delivery_invoice, product_name, product_color, product_size, product_image, product_content\n"+ 
						 "	   , product_price"+
						 " from \n"+
						 "     (\n"+
						 "     select row_number () over (order by orderdate desc) as RNO, order_no, fk_userid, orderdate, delivery_fee\n"+
						 "         , total_price, order_detail_no, fk_product_no, order_count, order_price, delivery_mobile, delivery_address\n"+
						 "         , delivery_comment, delivery_invoice, product_name, product_color, product_size, product_image, product_content\n"+ 
						 "		   , product_price"+
						 "     from tbl_order O join tbl_order_detail D on O.order_no = D.fk_order_no\n"+
						 "         join tbl_delivery V on V.fk_order_no = O.order_no\n"+
						 "         join tbl_product P on P.product_no = D.fk_product_no\n"+
						 "     )\n"+
						 " where RNO between ? and ?";
	                 
			pstmt = conn.prepareStatement(sql);      

			pstmt.setInt(1, (currentShowPageNo*sizePerPage)-(sizePerPage-1) );
			pstmt.setInt(2, currentShowPageNo*sizePerPage );
	                                      
			rs = pstmt.executeQuery();
	            
			int cnt = 0;
			while(rs.next()) {
				cnt++;
	               
				if(cnt==1) {
					orderList = new ArrayList<>(); 
				}   
	               
				String order_no = rs.getString("order_no");
	            String fk_userid = rs.getString("fk_userid");
	            String orderdate = rs.getString("orderdate");
	            String delivery_fee = rs.getString("delivery_fee");
	            String total_price = rs.getString("total_price");
	            String order_detail_no = rs.getString("order_detail_no");
	            String fk_product_no = rs.getString("fk_product_no");
	            String order_count = rs.getString("order_count");
	            String order_price = rs.getString("order_price");
	            String delivery_mobile = rs.getString("delivery_mobile");
	            String delivery_address = rs.getString("delivery_address");
	            String delivery_comment = rs.getString("delivery_comment");
	            String delivery_invoice = rs.getString("delivery_invoice");
	            String product_name = rs.getString("product_name");
	            String product_color = rs.getString("product_color");
	            String product_size = rs.getString("product_size");
	            String product_image = rs.getString("product_image");
	            String product_content = rs.getString("product_content");
	            String product_price = rs.getString("product_price");

	            HashMap<String,String> odrmap = new HashMap<>();
	            odrmap.put("order_no", order_no);
	            odrmap.put("fk_userid", fk_userid);
	            odrmap.put("orderdate", orderdate);
	            odrmap.put("delivery_fee", delivery_fee);
	            odrmap.put("total_price", total_price);
	            odrmap.put("order_detail_no", order_detail_no);
	            odrmap.put("fk_product_no", fk_product_no);	
	            odrmap.put("order_count", order_count);
	            odrmap.put("order_price", order_price);
	            odrmap.put("delivery_mobile", delivery_mobile);
	            odrmap.put("delivery_address", delivery_address);
	            odrmap.put("delivery_comment", delivery_comment);
	            odrmap.put("delivery_invoice", delivery_invoice);
	            odrmap.put("product_name", product_name);
	            odrmap.put("product_color", product_color);
	            odrmap.put("product_size", product_size);
	            odrmap.put("product_image", product_image);
	            odrmap.put("product_content", product_content);
	            odrmap.put("product_price", product_price);

	            orderList.add(odrmap);

			}// end of while---------------------
	                 
		} finally {
			close();
		}

		return orderList;
	
	}

	
	
	// 추가이미지가 있으면 추가 이미지 테이블에 파일명 인서트하기 
	@Override
	public int product_imagefile_Insert(String product_no_full, String attachFileName) throws SQLException {
		
		int result = 0;
	      
	      try {
	         conn = ds.getConnection();
	         
	         String sql = " insert into tbl_product_imagefile (imgfileno, fk_product_no, imgfilename) "+ 
	        		 	  " values(seqImgfileno.nextval, ?, ?) ";
	         
	         pstmt = conn.prepareStatement(sql);
	         
	         pstmt.setString(1, product_no_full);
	         pstmt.setString(2, attachFileName);
	         
	         result = pstmt.executeUpdate();
	         
	      } finally {
	         close();
	      }
	      
	      return result;
		
	}

}




