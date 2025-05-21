// Mobile Menu Toggle
const menuToggle = document.getElementById('menuToggle');
const mobileMenu = document.getElementById('mobileMenu');
const closeMenu = document.getElementById('closeMenu');

// Abrir el menú al hacer clic en el botón de hamburguesa (☰)
menuToggle.addEventListener('click', function() {
    mobileMenu.classList.add('active');
});

// Cerrar el menú al hacer clic en el botón de "X"
closeMenu.addEventListener('click', function() {
    mobileMenu.classList.remove('active');
});

// Cerrar el menú al hacer clic en cualquier enlace del menú móvil
const mobileLinks = document.querySelectorAll('.mobile-nav-list .nav-link');
mobileLinks.forEach(link => {
    link.addEventListener('click', function() {
        mobileMenu.classList.remove('active');
    });
});

// Cerrar el menú automáticamente al redimensionar la pantalla a desktop (≥1024px)
function handleResize() {
    if (window.innerWidth >= 1024 && mobileMenu.classList.contains('active')) {
        mobileMenu.classList.remove('active');
    }
}
window.addEventListener('resize', handleResize);

// Actualizar el año del copyright
document.getElementById('currentYear').textContent = new Date().getFullYear();