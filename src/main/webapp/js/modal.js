/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */
function buildGameModal() {
    const modal = document.createElement("div");
    modal.className = "modal fade";
    modal.id = "gameModal";
    modal.tabIndex = -1;

    modal.innerHTML = `
        <div class="modal-dialog modal-xl modal-dialog-centered">
            <div class="modal-content bg-dark text-light">
                <div class="modal-backdrop-glow" id="modalBackdropGlow"></div>
                <div class="modal-header border-0 justify-content-end">
                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal"></button>
                </div>
                <div class="modal-body" id="gameModalBody">
                    Details go here.
                </div>
            </div>
        </div>
    `;

    document.body.appendChild(modal);
}

buildGameModal();

