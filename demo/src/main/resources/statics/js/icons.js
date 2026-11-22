/* Ícones ausentes: troca a imagem quebrada por um "?" para sinalizar
   que o arquivo ainda precisa ser colocado em assets/.
   O "?" herda width/height da imagem original.
   Para conteúdo criado dinamicamente, chame IconFallback.apply(elemento). */
const IconFallback = (() => {
  function showMissing(img) {
    if (img.dataset.missingHandled) return;
    img.dataset.missingHandled = "true";

    const width = parseFloat(img.getAttribute("width")) || 36;
    const height = parseFloat(img.getAttribute("height")) || 35;
    const smallest = Math.min(width, height);

    const marker = document.createElement("span");
    marker.className = "icon-missing";
    marker.textContent = "?";
    marker.title = `Ícone ausente: ${img.getAttribute("src")}`;
    marker.setAttribute("role", "img");
    marker.setAttribute("aria-label", "Ícone ausente");
    marker.style.width = `${width}px`;
    marker.style.height = `${height}px`;
    marker.style.fontSize = `${Math.max(9, Math.round(smallest * 0.55))}px`;
    if (smallest < 24) {
      marker.style.borderRadius = "4px";
      marker.style.borderWidth = "1px";
    }

    img.replaceWith(marker);
  }

  function apply(root = document) {
    root.querySelectorAll("img[data-icon]").forEach((img) => {
      if (img.dataset.missingHandled) return;
      // Caso a falha tenha acontecido antes do script rodar
      if (img.complete && img.naturalWidth === 0) {
        showMissing(img);
        return;
      }
      img.addEventListener("error", () => showMissing(img), { once: true });
    });
  }

  apply();
  return { apply };
})();
