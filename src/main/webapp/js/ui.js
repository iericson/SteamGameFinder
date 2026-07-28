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
    `;

    modal.show();
}

function attachGameCardClickListener(parent) {
    parent.addEventListener("click", (e) => {
        const card = e.target.closest(".game-card");
        if (!card) return;

        openGameModal(card.dataset.id);
    });
}