const yearTarget = document.getElementById("build-year");

if (yearTarget) {
  yearTarget.textContent = `Production-style Java backend portfolio project. ${new Date().getFullYear()}`;
}
