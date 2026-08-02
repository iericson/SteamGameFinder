<%-- 
    Document   : details
    Created on : Aug 1, 2026, 5:42:02 PM
    Author     : Isaac Ericson
--%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Find a Game<c:if test="${not empty game}"> - ${game.name}</c:if></title>

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="${pageContext.request.contextPath}/css/style.css" rel="stylesheet">
</head>
<body class="bg-dark text-light">

    <c:if test="${not empty error}">
        <div class="container-fluid px-4 mt-4">
            <div class="alert alert-danger">${error}</div>
            <a href="${pageContext.request.contextPath}/index.html" class="btn btn-outline-light">Back to Browse</a>
        </div>
    </c:if>

    <c:if test="${not empty game}">
        <div class="container-fluid px-4 mt-4">

            <img src="${game.headerImage}" alt="${game.name}" class="img-fluid rounded mb-3">

            <h1>${game.name}</h1>

            <p class="text-secondary">
                Released: ${game.releaseDate}<br>
                Developer: ${game.developers}<br>
                Publisher: ${game.publishers}
            </p>

            <p>${game.aboutTheGame}</p>

            <c:if test="${not empty game.tags}">
                <div class="mb-3">
                    <c:forEach var="tag" items="${game.tags}">
                        <span class="badge bg-secondary me-1">${tag}</span>
                    </c:forEach>
                </div>
            </c:if>

            <c:if test="${not empty game.screenshots}">
                <h4>Screenshots</h4>
                <div class="d-flex flex-wrap gap-2 mb-3">
                    <c:forEach var="shot" items="${game.screenshots}">
                        <img src="${shot}" class="rounded" style="width: 200px;">
                    </c:forEach>
                </div>
            </c:if>

            <a href="${pageContext.request.contextPath}/index.html" class="btn btn-outline-light">Back to Browse</a>

        </div>
    </c:if>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/navbar.js"></script>
</body>
</html>

