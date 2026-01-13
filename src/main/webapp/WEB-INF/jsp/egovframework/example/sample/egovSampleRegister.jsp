<%@ page contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<%@ taglib prefix="c"         uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form"      uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="validator" uri="http://www.springmodules.org/tags/commons-validator" %>
<%@ taglib prefix="spring"    uri="http://www.springframework.org/tags"%>
<%
  /**
  * @Class Name : egovSampleRegister.jsp
  * @Description : Sample Register 화면
  * @Modification Information
  *
  *   수정일         수정자                   수정내용
  *  -------    --------    ---------------------------
  *  2009.02.01            최초 생성
  *
  * author 실행환경 개발팀
  * since 2009.02.01
  *
  * Copyright (C) 2009 by MOPAS  All right reserved.
  */
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" lang="ko" xml:lang="ko">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <c:set var="registerFlag" value="${empty sampleVO.articleId or sampleVO.articleId == 0 ? 'create' : 'modify'}"/>
    <title>Sample <c:if test="${registerFlag == 'create'}"><spring:message code="button.create" /></c:if>
                  <c:if test="${registerFlag == 'modify'}"><spring:message code="button.modify" /></c:if>
    </title>
    <link type="text/css" rel="stylesheet" href="<c:url value='/css/egovframework/sample.css'/>"/>
    
    <!--For Commons Validator Client Side-->
    <script type="text/javascript" src="<c:url value='/cmmn/validator.do'/>"></script>
    <validator:javascript formName="sampleVO" staticJavascript="false" xhtml="true" cdata="false"/>
    
    <script type="text/javaScript" language="javascript" defer="defer">
        
        /* 글 목록 화면 function */
        function fn_egov_selectList() {
           	document.detailForm.action = "<c:url value='/egovSampleList.do'/>";
           	document.detailForm.method = 'get';
           	document.detailForm.submit();
        }
        
        /* 글 삭제 function */
        function fn_egov_delete() {
        	if(confirm("정말 삭제하시겠습니까?")){
        		document.detailForm.action = "<c:url value='/deleteSample.do'/>";
               	document.detailForm.submit();	
        	} else {
        		return; 
        	}
        }
        
        /* 글 등록 function */
        function fn_egov_save() {
        	const frm = document.detailForm;
        	
        	if(!validateSampleVO(frm)){
                return;
            }else{
            	frm.action = "<c:url value="${registerFlag == 'create' ? '/addSample.do' : '/updateSample.do'}"/>";
                frm.submit();
            }
        }
        
        /* 글 등록 화면 function */
        function fn_egov_requery() {
        	
        	// 1. 폼(detailForm)을 변수에 담습니다.
            const frm = document.detailForm;
            // 2. 폼의 action 주소를 등록 화면 주소로 바꿉니다.
           
            // (리스트 화면에서 봤던 그 주소입니다!)
           
            // 3. 현재 글의 articleId를 parentArticleId라는 이름으로 URL 뒤에 붙여서 보냅니다.
            // 예: action = 주소 + "?parentArticleId=" + 현재ID;
            frm.action = "<c:url value='/addSample.do'/>";
            frm.method = "get";
            frm.parentArticleId.value = frm.articleId.value;
            // 4. 전송(submit) 합니다.
            frm.submit();
     
        }
        
       
    </script>
</head>
<body style="text-align:center; margin:0 auto; display:inline; padding-top:100px;">

<form:form modelAttribute="sampleVO" id="detailForm" name="detailForm" method="post">
	<form:hidden path="parentArticleId" />
    <div id="content_pop">
    	<!-- 타이틀 -->
    	<div id="title">
    		<ul>
    			<li><img src="<c:url value='/images/egovframework/example/title_dot.gif'/>" alt=""/>
                    <c:if test="${registerFlag == 'create'}"><spring:message code="button.create" /></c:if>
                    <c:if test="${registerFlag == 'modify'}"><spring:message code="button.modify" /></c:if>
                </li>
    		</ul>
    	</div>
    	<!-- // 타이틀 -->
    	<div id="table">
    	<table width="100%" border="1" cellpadding="0" cellspacing="0" style="bordercolor:#D3E2EC; bordercolordark:#FFFFFF; BORDER-TOP:#C2D0DB 2px solid; BORDER-LEFT:#ffffff 1px solid; BORDER-RIGHT:#ffffff 1px solid; BORDER-BOTTOM:#C2D0DB 1px solid; border-collapse: collapse;">
    		<colgroup>
    			<col width="150"/>
    			<col width="?"/>
    		</colgroup>
    		<c:if test="${registerFlag == 'modify'}">
    			<!-- // 게시물 아이디 : hidden 처리 -->
        		<tr>
        			<!-- 
        			<td class="tbtd_caption"><label for="articleId"><spring:message code="title.sample.articleId" /></label></td>
        			<td class="tbtd_content">
        				<form:hidden path="articleId" cssClass="essentiality" maxlength="10" readonly="true" />
        			</td>
        			 -->
        			<form:hidden path="articleId"/>
        		</tr>
    		</c:if>
    		<tr>
    			<!-- // 게시글 제목 -->
    			<td class="tbtd_caption"><label for="title"><spring:message code="title.sample.title" /></label></td>
    			<td class="tbtd_content">
    				<form:input path="title" maxlength="30" cssClass="txt"/>
    				&nbsp;<form:errors path="title" />
    			</td>
    		</tr>
    		<tr>
    			<!-- // 게시글 상태: 'REGISTER(신규접수), ANSWERED(답변완료), HOLD(보류), REQUERY(재문의) -->
    			<td class="tbtd_caption"><label for="status"><spring:message code="title.sample.status" /></label></td>
    			<td class="tbtd_content">
    				<!-- // status 이전버전 
    				<form:select path="status" cssClass="use">
    					<form:option value="Y" label="Yes" />
    					<form:option value="N" label="No" />
    				</form:select>
    				-->
    				<!-- // status 게시글 신규 등록시에는 REGISTER readonly 표시-->
    				<c:if test="${registerFlag == 'create'}">
    					<c:if test="${empty sampleVO.parentArticleId or sampleVO.parentArticleId == 0}">
    						<input type="text" value="REGISTER" readonly="readonly" class="essentiality"></input>
    						<form:hidden path="status" value="REGISTER" />
    					</c:if>
    					<c:if test="${sampleVO.parentArticleId > 0}">
                                <input type="text" value="REQUERY" readonly="readonly" class="essentiality" />
                                <form:hidden path="status" value="REQUERY" />
                        </c:if>
    				</c:if>
    				<!-- // status 게시글 수정시에는 REGISTER readonly 표시-->
    				<c:if test="${registerFlag == 'modify'}">
    					<form:select path="status" cssClass="use">
    						<form:option value="REGISTER" label="민원 접수"></form:option>
    						<form:option value="ANSWERED" label="답변 완료"></form:option>
    						<form:option value="HOLD" label="처리 보류"></form:option>
    						<form:option value="REQUERY" label="재문의"></form:option>
    					</form:select>
    				</c:if>	
    			</td>
    		</tr>
    		<tr>
    			<!-- // 게시글 내용 -->
    			<td class="tbtd_caption"><label for="content"><spring:message code="title.sample.content" /></label></td>
    			<td class="tbtd_content">
    				<form:textarea path="content" rows="5" cols="58" />&nbsp;<form:errors path="content" />
                </td>
    		</tr>
    		<tr>
    			<!-- // 게시글 작성자 PK와 아이디 -->
    			<td class="tbtd_caption"><label for="userId"><spring:message code="title.sample.username" /></label></td>
    			<td class="tbtd_content">
                    <c:if test="${registerFlag == 'create'}">
                    	<input type="text" value="임시사용자" maxlength="10" readonly="readonly" class="essentiality" />
        				<!--   &nbsp;<form:errors path="userId" /> -->
                    </c:if>
                    <c:if test="${registerFlag == 'modify'}">
        				<form:input path="username" maxlength="10" cssClass="essentiality" readonly="readonly"/>
        				<!-- &nbsp;<form:errors path="userId" /> -->
                    </c:if>
                </td>    
    		</tr>
    	</table>
      </div>
    	<div id="sysbtn">
    		<ul>
    			<li>
                    <span class="btn_blue_l">
                        <a href="javascript:fn_egov_selectList();"><spring:message code="button.list" /></a>
                        <img src="<c:url value='/images/egovframework/example/btn_bg_r.gif'/>" style="margin-left:6px;" alt=""/>
                    </span>
                </li>
    			<li>
                    <span class="btn_blue_l">
                        <a href="javascript:fn_egov_save();">
                            <c:if test="${registerFlag == 'create'}"><spring:message code="button.create" /></c:if>
                            <c:if test="${registerFlag == 'modify'}"><spring:message code="button.modify" /></c:if>
                        </a>
                        <img src="<c:url value='/images/egovframework/example/btn_bg_r.gif'/>" style="margin-left:6px;" alt=""/>
                    </span>
                </li>
    			<c:if test="${registerFlag == 'modify'}">
                    <li>
                        <span class="btn_blue_l">
                            <a href="javascript:fn_egov_delete();"><spring:message code="button.delete" /></a>
                            <img src="<c:url value='/images/egovframework/example/btn_bg_r.gif'/>" style="margin-left:6px;" alt=""/>
                        </span>
                    </li>
    			</c:if>
    			
    			<c:if test="${registerFlag == 'modify'}">
    				<li>
    					<span class="btn_blue_l">
                            <a href="javascript:fn_egov_requery();"><spring:message code="button.requery" /></a>
                            <img src="<c:url value='/images/egovframework/example/btn_bg_r.gif'/>" style="margin-left:6px;" alt=""/>
                        </span>
    				</li>
    			</c:if>	
    			<li>
                    <span class="btn_blue_l">
                        <a href="javascript:document.detailForm.reset();"><spring:message code="button.reset" /></a>
                        <img src="<c:url value='/images/egovframework/example/btn_bg_r.gif'/>" style="margin-left:6px;" alt=""/>
                    </span>
                </li>
            </ul>
    	</div>
    </div>
    <!-- 검색조건 유지 -->
    <input type="hidden" name="searchCondition" value="<c:out value='${searchVO.searchCondition}'/>"/>
    <input type="hidden" name="searchKeyword" value="<c:out value='${searchVO.searchKeyword}'/>"/>
    <input type="hidden" name="pageIndex" value="<c:out value='${searchVO.pageIndex}'/>"/>
</form:form>
</body>
</html>