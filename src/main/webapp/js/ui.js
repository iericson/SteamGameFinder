// card

function buildCard(game) {

    const card = document.createElement("div");
    card.className = "game-card";
    card.dataset.id = game.id;

    const thumb = document.createElement("img");
    thumb.className = "thumb";
    thumb.src = game.headerImage;
    thumb.alt = game.name;

    card.appendChild(thumb);

    return card;
}

// modal

async function openGameModal(id) {
    const modal = new bootstrap.Modal(
        document.getElementById("gameModal")
    );

    const modalBody = document.getElementById("gameModalBody");
    modalBody.innerHTML = "<p>Loading...</p>";
    modal.show();

    try {
        const response = await fetch(`display?id=${encodeURIComponent(id)}`);
        const html = await response.text();

        const doc = new DOMParser().parseFromString(html, "text/html");
        const content = doc.getElementById("gameDetailsContent");

        modalBody.innerHTML = content ? content.innerHTML : "<p>Failed to load game details.</p>";
    } catch (error) {
        console.error("Failed loading game details:", error);
        modalBody.innerHTML = "<p>Failed to load game details.</p>";
        return;
    }

    wireMyListToggle(id);
    wireCarousel(modalBody);
    wireReadMore(modalBody);
}

// My List save/remove toggle

async function wireMyListToggle(id) {
    const btn = document.getElementById("myListToggleBtn");
    if (!btn) return;

    let saved = false;

    try {
        const list = await getMyList();
        saved = list.some(game => String(game.id) === String(id));
    } catch (error) {
        console.error("Failed checking My List:", error);
    }

    btn.disabled = false;
    updateMyListButton(btn, saved);

    btn.addEventListener("click", async () => {
        btn.disabled = true;

        try {
            if (saved) {
                await removeFromMyList(id);
                saved = false;
            } else {
                await addToMyList(id);
                saved = true;
            }
            updateMyListButton(btn, saved);
        } catch (error) {
            console.error("Failed updating My List:", error);
        } finally {
            btn.disabled = false;
        }
    });
}

function updateMyListButton(btn, saved) {
    btn.textContent = saved ? "Remove from My List" : "Add to My List";
    btn.classList.toggle("btn-outline-light", !saved);
    btn.classList.toggle("btn-warning", saved);
}

function wireCarousel(container) {
    const mainImg = container.querySelector("#mainScreenshot");
    const thumbs = Array.from(container.querySelectorAll(".detail-thumb"));
    if (!mainImg || thumbs.length === 0) return;

    let current = 0;

    function show(index) {
        current = (index + thumbs.length) % thumbs.length;
        mainImg.src = thumbs[current].src;
    }

    thumbs.forEach((thumb, index) => {
        thumb.addEventListener("click", () => show(index));
    });

    container.querySelector("#prevShot")?.addEventListener("click", () => show(current - 1));
    container.querySelector("#nextShot")?.addEventListener("click", () => show(current + 1));
}

function wireReadMore(container) {
    const about = container.querySelector(".details-about");
    const toggle = container.querySelector("#aboutToggle");
    if (!about || !toggle) return;

    toggle.addEventListener("click", () => {
        const expanded = about.classList.toggle("expanded");
        toggle.textContent = expanded ? "Show less" : "Show more";
    });
}

function attachGameCardClickListener(parent) {
    parent.addEventListener("click", (e) => {
        const card = e.target.closest(".game-card");
        if (!card) return;

        openGameModal(card.dataset.id);
    });
}