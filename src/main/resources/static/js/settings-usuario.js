document.addEventListener("DOMContentLoaded", async () => {
  const form = document.getElementById("settingsFormProfile");
  const nameIn = document.getElementById("name");
  const lastIn = document.getElementById("lastname");
  const cedIn  = document.getElementById("cedula");
  const telIn  = document.getElementById("phone");

  // 1️⃣ Al cargar la página, traemos los datos actuales
  try {
    const res = await fetch("/auth/me", { credentials:"include" });
    if (!res.ok) throw new Error();
    const u = await res.json();
    nameIn.value     = u.nombre || "";
    lastIn.value     = u.apellido || "";
    cedIn.value      = u.cedula  || "";
    telIn.value      = u.telefono|| "";
  } catch {
    console.error("No se pudo cargar perfil");
  }

  // 2️⃣ Al enviar, validamos y hacemos PUT
  form.addEventListener("submit", async e => {
    e.preventDefault();
    const payload = {
      nombre:   nameIn.value.trim(),
      apellido: lastIn.value.trim(),
      cedula:   cedIn.value.trim(),
      telefono: telIn.value.trim()
    };
    try {
      const res = await fetch("/auth/me", {
        method: "PUT",
        headers: {"Content-Type":"application/json"},
        credentials: "include",
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error(await res.text());
      showAlert("saveAlert","Perfil actualizado correctamente");
    } catch (err) {
      showAlert("errorAlert", err.message);
    }
  });

  // ——— settings-profile.js ———
  document.getElementById('settingsFormProfile').addEventListener('submit', async e => {
    e.preventDefault();
    const payload = {
      nombre:   document.getElementById('name').value.trim(),
      apellido: document.getElementById('lastname').value.trim(),
      cedula:   document.getElementById('cedula').value.trim(),
      telefono: document.getElementById('phone').value.trim()
    };
    try {
      const res = await fetch('/auth/me', {
        method: 'PUT',
        headers: {'Content-Type':'application/json'},
        credentials: 'include',
        body: JSON.stringify(payload)
      });
      if (!res.ok) throw new Error(await res.text());
      const updatedUser = await res.json();

      // ✨ Actualiza cabecera en pantalla
      document.querySelector('.user-name').textContent  =
        `${updatedUser.nombre} ${updatedUser.apellido}`;
      document.querySelector('.user-email').textContent = updatedUser.email;

      showAlert('saveAlert','Perfil actualizado correctamente');
    } catch (err) {
      showAlert('errorAlert', err.message);
    }
  });

  // helper de alertas si no lo tienes ya
  function showAlert(id, mensaje) {
    const a = document.getElementById(id);
    if (!a) return;
    const t = a.querySelector('.alert-text');
    if (t) t.textContent = mensaje;
    a.classList.add('show');
    setTimeout(() => a.classList.remove('show'), 3000);
  }

  // 🛡️ 3) Submit contraseña
  if (passwordForm) {
    passwordForm.addEventListener('submit', async e => {
      e.preventDefault();
      const payload = {
        currentPassword: document.getElementById('current-password').value,
        newPassword:     document.getElementById('new-password').value,
        confirmPassword: document.getElementById('confirm-password').value
      };
      try {
        const res = await fetch('/auth/me/password', {
          method: 'PUT',
          headers: {'Content-Type':'application/json'},
          credentials: 'include',
          body: JSON.stringify(payload)
        });
        if (!res.ok) throw new Error(await res.text());
        showAlert('saveAlert','Contraseña cambiada correctamente');
      } catch (err) {
        showAlert('errorAlert', err.message);
      }
    });
  }

  // — helper de alertas —
  function showAlert(id, mensaje) {
    const a = document.getElementById(id);
    if (!a) return;
    const txt = a.querySelector('.alert-text');
    if (txt) txt.textContent = mensaje;
    a.classList.add('show');
    setTimeout(() => a.classList.remove('show'), 3000);
  }
});
