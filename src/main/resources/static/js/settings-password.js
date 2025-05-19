document.addEventListener('DOMContentLoaded', () => {
  const formPw = document.getElementById('settingsFormPassword');
  if (!formPw) return;

  formPw.addEventListener('submit', async e => {
    e.preventDefault();
    const payload = {
      currentPassword: document.getElementById('current-password').value.trim(),
      newPassword:     document.getElementById('new-password').value.trim(),
      confirmPassword: document.getElementById('confirm-password').value.trim()
    };

    try {
      const res = await fetch('/auth/me/password', {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
      });
      if (!res.ok) {
        const txt = await res.text();
        throw new Error(txt || res.statusText);
      }
      showAlert('saveAlert', 'Contraseña cambiada correctamente');
      // opcional: limpiar campos
      formPw.reset();
    } catch (err) {
      console.error(err);
      showAlert('errorAlert', err.message);
    }
  });

  function showAlert(id, msg) {
    const a = document.getElementById(id);
    if (!a) return;
    const t = a.querySelector('.alert-text');
    if (t) t.textContent = msg;
    a.classList.add('show');
    setTimeout(() => a.classList.remove('show'), 3000);
  }
});
