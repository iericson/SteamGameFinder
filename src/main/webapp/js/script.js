const container = document.getElementById('categoryContainer');
const sentinel = document.getElementById('categorySentinel');

const CATEGORY_BATCH = 20;
let categoryOffset = 0;
let loadingCategories = false;
let categoriesDone = false;


function buildGameRow(title, href, games, rowId, emptyMessage) {
    const section = document.createElement('section');
    section.className = 'mb-4';

    const heading = document.createElement('a');
    heading.className = 'text-light text-decoration-none';
    heading.href = href;
    const h4 = document.createElement('h4');
    h4.textContent = title;
    heading.appendChild(h4);
    section.appendChild(heading);

    const wrapper = document.createElement('div');
    wrapper.className = 'category-row-wrapper';

    const row = document.createElement('div');
    row.className = 'category-row';
    row.id = 'row-' + rowId;

    if (games.length === 0 && emptyMessage) {
        const placeholder = document.createElement('div');
        placeholder.className = 'mylist-placeholder';
        placeholder.textContent = emptyMessage;
        row.appendChild(placeholder);
    } else {
        games.forEach(game => row.appendChild(buildCard(game)));
    }

    // scrolls by however many cards are currently visible
    function scrollByVisibleCards(direction) {
        const firstCard = row.querySelector('.game-card');
        if (!firstCard) return;
        const cardWidth = firstCard.getBoundingClientRect().width;
        const gap = parseFloat(getComputedStyle(row).gap) || 0;
        const visibleCount = Math.max(1, Math.round(row.clientWidth / (cardWidth + gap)));
        const distance = visibleCount * (cardWidth + gap);
        row.scrollBy({ left: direction * distance, behavior: 'smooth' });
    }

    const leftArrow = document.createElement('button');
    leftArrow.className = 'row-arrow left';
    leftArrow.innerHTML = '&#10094;';
    leftArrow.addEventListener('click', () => scrollByVisibleCards(-1));

    const rightArrow = document.createElement('button');
    rightArrow.className = 'row-arrow right';
    rightArrow.innerHTML = '&#10095;';
    rightArrow.addEventListener('click', () => scrollByVisibleCards(1));

    wrapper.appendChild(row);
    wrapper.appendChild(leftArrow);
    wrapper.appendChild(rightArrow);
    section.appendChild(wrapper);
    return section;
}

async function buildCategoryRow(category, index) {
    const games = await getGamesForCategory(category);
    return buildGameRow(category, 'category.html?name=' + encodeURIComponent(category), games, index);
}

async function buildMyListRow() {
    const games = await getMyList();

    return buildGameRow(
        'My List',
        'mylist.html',
        games,
        'mylist',
        'Add something! Click a game and hit "Add to My List."'
    );
}

// ---- page init ----
async function loadNextCategoryBatch() {
    if (loadingCategories || categoriesDone) return;

    loadingCategories = true;

    let categories;

    try {
        categories = await getCategories(CATEGORY_BATCH, categoryOffset);
    } catch (error) {
        console.error("Failed loading categories:", error);
        loadingCategories = false;
        return;
    }

    if (categories.length === 0) {
        categoriesDone = true;
        loadingCategories = false;
        return;
    }

    for (let i = 0; i < categories.length; i++) {
        const section = await buildCategoryRow(categories[i], categoryOffset + i);
        container.appendChild(section);
    }

    categoryOffset += categories.length;
    loadingCategories = false;
}

function handleCategoryIntersection(entries) {
    if (entries[0].isIntersecting) {
        loadNextCategoryBatch();
    }
}

const categoryObserver = new IntersectionObserver(handleCategoryIntersection);
categoryObserver.observe(sentinel);

async function refreshMyListRow() {
    const oldSection = document.getElementById('row-mylist')?.closest('section');
    const newSection = await buildMyListRow();

    if (oldSection) {
        oldSection.replaceWith(newSection);
    } else {
        container.insertBefore(newSection, container.firstChild);
    }
}

async function init() {
    const myListSection = await buildMyListRow();
    container.appendChild(myListSection);

    await loadNextCategoryBatch();
    attachGameCardClickListener(container);
    document.getElementById('gameModal').addEventListener('hidden.bs.modal', refreshMyListRow);
}

init();