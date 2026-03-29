(() => {

  // Función de "debounce": evita ejecutar llamadas mientras el usuario sigue escribiendo
  const debounce = (fn, delay) => {
    let timer;
    return (...args) => {
      clearTimeout(timer);
      timer = setTimeout(() => fn.apply(this, args), delay);
    };
  };

  // Llama al backend /api/autocomplete y llena el datalist
  async function fetchSuggestions(q, datalist) {

    if (!q || q.trim().length === 0) {
      datalist.innerHTML = ''; // limpiar cuando no hay texto
      return;
    }

    try {
      const res = await fetch('/api/autocomplete?q=' + encodeURIComponent(q));
      if (!res.ok) return;

      const items = await res.json();
      datalist.innerHTML = '';

      // Crear opción por cada sugerencia
      items.forEach(text => {
        const opt = document.createElement('option');
        opt.value = text;
        datalist.appendChild(opt);
      });

    } catch (err) {
      console.error('Autocomplete error', err);
    }
  }

  // Configura inputs y datalist
  function setup() {

    // Selecciona cualquier input con name="q"
    const inputs = document.querySelectorAll('input[name="q"]');
    if (inputs.length === 0) return;

    // Datalist único donde se mostrarán sugerencias
    let datalist = document.getElementById('suggestions');
    if (!datalist) {
      datalist = document.createElement('datalist');
      datalist.id = 'suggestions';
      document.body.appendChild(datalist);
    }

    // Handler con debounce de 250ms
    const handler = debounce((e) => fetchSuggestions(e.target.value, datalist), 250);

    // Conectar cada input con el datalist
    inputs.forEach(input => {
      input.setAttribute('list', 'suggestions');
      input.addEventListener('input', handler);
    });
  }

  // Ejecutar cuando el DOM está listo
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', setup);
  } else {
    setup();
  }

})();
