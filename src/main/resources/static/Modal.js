export class Modal {
  constructor(contenido) {
    this.contenido = contenido;
  }

  render() {
    const modal = document.createElement("div");
    modal.classList.add("modal");
    modal.innerHTML = `<div class="modal-content">${this.contenido}</div>`;
    return modal;
  }
}