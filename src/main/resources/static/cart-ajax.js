(function () {

  // Selector rápido
  const qs = (sel, el = document) => el.querySelector(sel);

  // --- Leer meta tags (para CSRF) ---
  function readMeta(name) {
    const el = document.querySelector(`meta[name="${name}"]`);
    return el ? el.getAttribute("content") : null;
  }

  // --- Leer cookie por nombre ---
  function readCookie(name) {
    const match = document.cookie.match(new RegExp("(^| )" + name + "=([^;]+)"));
    return match ? decodeURIComponent(match[2]) : null;
  }

  // --- Construcción de headers para protección CSRF ---
  function getCsrfHeaders() {
    const token = readMeta("_csrf");
    const header = readMeta("_csrf_header");

    // Caso 1: meta tags disponibles
    if (token && header) {
      const h = { "X-Requested-With": "XMLHttpRequest" };
      h[header] = token;
      return h;
    }

    // Caso 2: cookie CSRF disponible
    const cookieToken = readCookie("XSRF-TOKEN") || readCookie("X-XSRF-TOKEN");
    if (cookieToken) {
      return { "X-Requested-With": "XMLHttpRequest", "X-XSRF-TOKEN": cookieToken };
    }

    // Caso 3: sin token → enviar sólo header básico
    return { "X-Requested-With": "XMLHttpRequest" };
  }

  // --- POST genérico al backend del carrito ---
  async function postJson(url) {
    const headers = getCsrfHeaders();

    const res = await fetch(url, {
      method: "POST",
      headers,
      credentials: "include",        // permite enviar cookies
      redirect: "manual"             // evita redirecciones automáticas
    });

    // Si redirige, es señal de login
    if ([302, 303, 307, 308].includes(res.status)) {
      console.error("Redirección detectada");
      throw new Error("Redirect");
    }

    // No autenticado
    if (res.status === 401) {
      window.location.href = "/login";
      return {};
    }

    if (!res.ok) throw new Error(`Error: ${res.status}`);

    const contentType = res.headers.get("content-type");
    if (!contentType || !contentType.includes("application/json")) {
      throw new Error("Respuesta no es JSON");
    }

    return res.json();
  }

  // --- Formatea precio ---
  function formatCurrency(v) {
    return "$" + Number(v).toFixed(2);
  }

  // --- Sanitiza texto para evitar XSS ---
  function escapeHtml(str) {
    if (!str) return "";
    return String(str).replace(/[&<>"']/g, s => ({
      "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;",
    })[s]);
  }

  // --- Construcción visual de cada item del carrito ---
  function buildItemElement(item) {

    const div = document.createElement("div");
    div.className = "list-group-item d-flex justify-content-between align-items-center";

    const info = document.createElement("div");
    info.innerHTML = `
      <strong>${escapeHtml(item.nombre)}</strong><br>
      <small>${formatCurrency(item.precio)}</small><br>
      <small class="text-muted">Cantidad: <span>${item.cantidad}</span></small>
    `;

    // Contenedor derecho del item
    const right = document.createElement("div");
    right.className = "d-flex align-items-center gap-2";

    const img = document.createElement("img");
    img.src = item.imagen;
    img.alt = item.nombre;
    img.style = "width:50px;height:50px;object-fit:cover;border-radius:8px;";

    // Botones + y -
    const controls = document.createElement("div");
    controls.className = "d-flex flex-column";

    const btnPlus = document.createElement("button");
    btnPlus.className = "btn btn-sm btn-outline-success mb-1";
    btnPlus.textContent = "+";
    btnPlus.dataset.action = `/carrito/api/sumar/${item.productoId}`;

    const btnMinus = document.createElement("button");
    btnMinus.className = "btn btn-sm btn-outline-warning";
    btnMinus.textContent = "-";
    btnMinus.dataset.action = `/carrito/api/restar/${item.productoId}`;

    controls.appendChild(btnPlus);
    controls.appendChild(btnMinus);

    // Botón eliminar
    const btnDelete = document.createElement("button");
    btnDelete.className = "btn btn-sm btn-outline-danger";
    btnDelete.innerHTML = '<i class="bi bi-trash"></i>';
    btnDelete.dataset.action = `/carrito/api/eliminar/${item.productoId}`;

    right.appendChild(img);
    right.appendChild(controls);
    right.appendChild(btnDelete);

    div.appendChild(info);
    div.appendChild(right);

    return div;
  }

  // --- Actualización completa de la vista del carrito ---
  function updateCartUI(resp) {
    const countEl = qs("#cart-count");
    const itemsContainer = qs("#cart-items");
    const totalEl = qs("#cart-total");
    const cartEmpty = qs("#cart-empty");
    const cartContent = qs("#cart-content");

    const count = resp.count || 0;

    // Mostrar u ocultar contador
    if (count === 0) countEl.classList.add("d-none");
    else {
      countEl.classList.remove("d-none");
      countEl.textContent = count;
    }

    // Vista vacía o con productos
    if (cartEmpty && cartContent) {
      if (count === 0) {
        cartEmpty.classList.remove("d-none");
        cartContent.classList.add("d-none");
      } else {
        cartEmpty.classList.add("d-none");
        cartContent.classList.remove("d-none");
      }
    }

    // Cargar items
    itemsContainer.innerHTML = "";
    resp.items?.forEach(item => {
      itemsContainer.appendChild(buildItemElement(item));
    });

    // Total
    totalEl.textContent = formatCurrency(resp.total || 0);

    // Reasignar eventos a botones internos
    itemsContainer.querySelectorAll("button[data-action]").forEach(btn => {
      btn.addEventListener("click", async () => {
        try {
          const json = await postJson(btn.dataset.action);
          updateCartUI(json);
        } catch (err) {}
      });
    });
  }

  // --- Intercepta formularios que usan /carrito/... ---
  function interceptForms() {
    document.addEventListener("submit", function (e) {
      const form = e.target;
      if (!form.action.includes("/carrito/")) return;

      e.preventDefault();
      const apiUrl = form.action.replace("/carrito/", "/carrito/api/");

      postJson(apiUrl)
        .then(json => {
          updateCartUI(json);
          try {
            new bootstrap.Offcanvas(document.getElementById("cartOffcanvas")).show();
          } catch (_) {}
        })
        .catch(console.error);
    }, true);
  }

  // --- Intercepta clicks en botones o links del carrito ---
  function interceptClicks() {
    document.addEventListener("click", function (e) {
      const target = e.target.closest("a, button");
      if (!target) return;

      // Links tipo <a href="/carrito/...">
      if (target.tagName === "A" && target.href.includes("/carrito/")) {
        e.preventDefault();
        const apiUrl = target.href.replace("/carrito/", "/carrito/api/");
        postJson(apiUrl).then(updateCartUI);
        return;
      }

      // Botón dentro de form /carrito
      if (target.tagName === "button") {
        const form = target.form;
        if (form && form.action.includes("/carrito/")) {
          e.preventDefault();
          const apiUrl = form.action.replace("/carrito/", "/carrito/api/");
          postJson(apiUrl).then(updateCartUI);
        }
      }
    }, true);
  }

  // --- Botón moderno "Agregar al carrito" sin form ---
  document.addEventListener("click", async e => {
    const btn = e.target.closest(".btn-add-to-cart");
    if (!btn) return;

    const id = btn.dataset.id;
    if (!id) return;

    const json = await postJson(`/carrito/api/agregar/${id}`);
    updateCartUI(json);
  });

  // Inicializar listeners
  document.addEventListener("DOMContentLoaded", () => {
    interceptForms();
    interceptClicks();
  });

  // Exponer función para debug
  window._updateCartUI = updateCartUI;

})();
