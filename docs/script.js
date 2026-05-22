const yearTarget = document.getElementById("build-year");
const themeToggle = document.getElementById("theme-toggle");
const themeState = themeToggle?.querySelector(".theme-toggle-state");
const root = document.documentElement;
const storageKey = "identity-service-theme";

if (yearTarget) {
  yearTarget.textContent = `Production-style Java backend portfolio project. ${new Date().getFullYear()}`;
}

const getSavedTheme = () => {
  try {
    return localStorage.getItem(storageKey);
  } catch {
    return null;
  }
};

const saveTheme = (theme) => {
  try {
    localStorage.setItem(storageKey, theme);
  } catch {
    // Ignore storage errors so the static page remains usable in restricted browsers.
  }
};

const applyTheme = (theme) => {
  const nextTheme = theme === "light" ? "light" : "dark";
  const isDark = nextTheme === "dark";

  root.dataset.theme = nextTheme;

  if (themeToggle) {
    themeToggle.setAttribute("aria-pressed", String(isDark));
    themeToggle.setAttribute("aria-label", isDark ? "Disable dark mode" : "Enable dark mode");
  }

  if (themeState) {
    themeState.textContent = isDark ? "On" : "Off";
  }
};

applyTheme(getSavedTheme() || root.dataset.theme || "dark");

if (themeToggle) {
  themeToggle.addEventListener("click", () => {
    const nextTheme = root.dataset.theme === "dark" ? "light" : "dark";
    applyTheme(nextTheme);
    saveTheme(nextTheme);
  });
}
