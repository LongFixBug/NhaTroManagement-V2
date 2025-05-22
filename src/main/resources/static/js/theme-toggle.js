(() => {
  'use strict'

  const storedTheme = localStorage.getItem('theme');

  const getPreferredTheme = () => {
    if (storedTheme) {
      return storedTheme;
    }
    // Default to 'light' if no preference or system preference detection is not robustly needed here
    return 'light'; 
  };

  const setTheme = function (theme) {
    document.documentElement.setAttribute('data-bs-theme', theme);
  };

  const updateToggleButtonIcon = (theme) => {
    const themeToggleButton = document.getElementById('themeToggleButton');
    if (themeToggleButton) {
        // Set button text based on the NEW theme that has been set.
        // Set button icon based on the CURRENT theme.
        if (theme === 'dark') {
            themeToggleButton.innerHTML = '<i class="bi bi-moon-stars-fill"></i>';
        } else {
            themeToggleButton.innerHTML = '<i class="bi bi-sun-fill"></i>';
        }
    }
  };

  // Set initial theme and button text on load
  const initialTheme = getPreferredTheme();
  setTheme(initialTheme);
  // Defer button text update until DOM is loaded

  window.addEventListener('DOMContentLoaded', () => {
    updateToggleButtonIcon(getPreferredTheme()); // Update button icon now that DOM is ready

    const themeToggleButton = document.getElementById('themeToggleButton');
    if (themeToggleButton) {
      themeToggleButton.addEventListener('click', () => {
        const currentTheme = document.documentElement.getAttribute('data-bs-theme');
        const newTheme = currentTheme === 'dark' ? 'light' : 'dark';
        localStorage.setItem('theme', newTheme);
        setTheme(newTheme);
        updateToggleButtonIcon(newTheme);
      });
    }
  });

  // Listen for changes in system preference (optional, but good practice)
  // window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', () => {
  //   if (!storedTheme) { // Only if no explicit user choice
  //     const newSystemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  //     setTheme(newSystemTheme);
  //     updateToggleButtonText(newSystemTheme);
  //   }
  // });

})();
