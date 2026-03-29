export class Carrusel {
  constructor(imagenes) {
    this.imagenes = imagenes;
    this.index = 0;
  }

  render() {
    const cont = document.createElement("div");
    cont.classList.add("carrusel");

    const img = document.createElement("img");
    img.src = this.imagenes[this.index];
    cont.appendChild(img);

    setInterval(() => {
      this.index = (this.index + 1) % this.imagenes.length;
      img.src = this.imagenes[this.index];
    }, 2000);

    return cont;
  }
}