/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

function buildNavbar() {
    const nav = document.createElement('nav');
    nav.className = 'navbar navbar-expand-lg navbar-dark bg-black';

    nav.innerHTML = `
        <div class="container-fluid px-4">
            <a class="navbar-brand" href="index.html">Find a Game</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navMenu">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navMenu">
                <ul class="navbar-nav ms-auto">
                    <li class="nav-item">
                        <a class="nav-link" href="index.html">Browse</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#list">My List</a>
                    </li>
                    <li class="nav-item d-flex align-items-center">
                        <button class="btn btn-sm btn-outline-warning" id="reimportBtn" type="button">Reimport Data</button>
                    </li>
                </ul>
                <form class="d-flex ms-3" role="search">
                    <input class="form-control me-2" type="search" placeholder="Search games">
                    <button class="btn btn-outline-light" type="submit">Search</button>
                </form>
            </div>
        </div>
    `;

    document.body.insertBefore(nav, document.body.firstChild);

    const currentPage = window.location.pathname.split('/').pop() || 'index.html';
    nav.querySelectorAll('.nav-link').forEach(link => {
        if (link.getAttribute('href') === currentPage) {
            link.classList.add('active');
        }
    });

    wireReimportButton();
}

function wireReimportButton() {
    const reimportBtn = document.getElementById("reimportBtn");
    if (!reimportBtn) return;

    reimportBtn.addEventListener("click", async () => {
        const confirmed = confirm("This wipes and reimports all game data. Continue?");
        if (!confirmed) return;

        reimportBtn.disabled = true;
        reimportBtn.textContent = "Reimporting...";

        try {
            const response = await fetch("api/reimport", { method: "POST" });
            if (response.ok) {
                alert("Reimport complete.");
                location.reload();
            } else {
                alert("Reimport failed, check server logs.");
            }
        } catch (error) {
            alert("Reimport failed: " + error.message);
        } finally {
            reimportBtn.disabled = false;
            reimportBtn.textContent = "Reimport Data";
        }
    });
}

buildNavbar();