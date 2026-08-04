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

function openGameModal(id) {
    const modal = new bootstrap.Modal(
        document.getElementById("gameModal")
    );

    document.getElementById("gameModalBody").innerHTML = `
        <p><strong>Placeholder</strong></p>
        <p>/display?id=${id}</p>
        <button class="btn btn-outline-light" id="myListToggleBtn" disabled>Loading...</button>
    `;

    modal.show();

    wireMyListToggle(id);
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

function attachGameCardClickListener(parent) {
    parent.addEventListener("click", (e) => {
        const card = e.target.closest(".game-card");
        if (!card) return;

        openGameModal(card.dataset.id);
    });
}