<%-- 
    Document   : details
    Created on : Aug 1, 2026, 5:42:02 PM
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
        <div class="container-fluid px-4 mt-4" id="gameDetailsContent">
            <div class="alert alert-danger">${error}</div>
            <a href="${pageContext.request.contextPath}/index.html" class="btn btn-outline-light">Back to Browse</a>
        </div>
    </c:if>

    <c:if test="${not empty game}">
        <div class="container-fluid px-4 mt-4" id="gameDetailsContent">

            <h1>${game.name}</h1>

            <div class="row mt-3">
                <div class="col-lg-8">
                    <c:choose>
                        <c:when test="${not empty game.screenshots}">
                            <div class="carousel-wrapper mb-2">
                                <img id="mainScreenshot" src="${game.screenshots[0]}" class="img-fluid rounded" alt="${game.name}">
                                <button type="button" class="carousel-nav prev" id="prevShot" aria-label="Previous screenshot">&#10094;</button>
                                <button type="button" class="carousel-nav next" id="nextShot" aria-label="Next screenshot">&#10095;</button>
                            </div>
                            <div class="d-flex gap-2 overflow-auto">
                                <c:forEach var="shot" items="${game.screenshots}" varStatus="loop">
                                    <img src="${shot}" class="rounded" style="width:120px; height:68px; object-fit:cover; cursor:pointer;"
                                         onclick="showScreenshot(${loop.index})">
                                </c:forEach>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <img src="${game.headerImage}" class="img-fluid rounded" alt="${game.name}">
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="col-lg-4">
                    <img src="${game.headerImage}" class="img-fluid rounded mb-3" alt="${game.name}">

                    <p>${game.aboutTheGame}</p>

                    <p class="text-secondary mb-3">
                        Release Date: ${game.releaseDate}<br>
                        Developer: ${game.developers}<br>
                        Publisher: ${game.publishers}
                    </p>

                    <c:if test="${not empty game.tags}">
                        <div class="mb-3">
                            <c:forEach var="tag" items="${game.tags}">
                                <span class="badge bg-secondary me-1">${tag}</span>
                            </c:forEach>
                        </div>
                    </c:if>

                    <div class="d-flex gap-2">
                        <button class="btn btn-outline-light" id="myListToggleBtn" disabled>Loading...</button>
                        <a href="https://store.steampowered.com/app/${game.id}/" target="_blank" rel="noopener" class="btn btn-outline-light">View on Steam Store</a>
                    </div>
                </div>
            </div>

            <a href="${pageContext.request.contextPath}/index.html" class="btn btn-outline-light mt-4">Back to Browse</a>

        </div>
    </c:if>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    <script src="${pageContext.request.contextPath}/js/navbar.js"></script>
    <script src="${pageContext.request.contextPath}/js/api.js"></script>
    <script src="${pageContext.request.contextPath}/js/ui.js"></script>
    <c:if test="${not empty game}">
    <script>
        document.addEventListener("DOMContentLoaded", () => wireMyListToggle(${game.id}));

        const screenshots = [<c:forEach var="shot" items="${game.screenshots}" varStatus="loop">"${shot}"<c:if test="${!loop.last}">,</c:if></c:forEach>];
        let currentShot = 0;

        function showScreenshot(index) {
            if (screenshots.length === 0) return;
            currentShot = (index + screenshots.length) % screenshots.length;
            const img = document.getElementById("mainScreenshot");
            if (img) img.src = screenshots[currentShot];
        }

        document.getElementById("prevShot")?.addEventListener("click", () => showScreenshot(currentShot - 1));
        document.getElementById("nextShot")?.addEventListener("click", () => showScreenshot(currentShot + 1));
    </script>
    </c:if>
</body>
</html>