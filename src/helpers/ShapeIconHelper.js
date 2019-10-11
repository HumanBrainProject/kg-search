
const replaceColorInSvg = (icon, colorToReplace, replacementColor) => {
  if (typeof icon !== "string" || typeof colorToReplace !== "string" || typeof replacementColor !== "string") {
    return icon;
  }
  const reg = new RegExp(colorToReplace, "g");
  return icon.replace(reg, replacementColor);
};

export const setIconColor = (icon, active) => {
  const hardcodedColor = "#4D4D4D";
  const highlightColor = getComputedStyle(document.documentElement).getPropertyValue("--color-selected").trim();
  const defaultColor = getComputedStyle(document.documentElement).getPropertyValue("--color-unselected").trim();
  const color = active?highlightColor:defaultColor;
  return replaceColorInSvg(icon, hardcodedColor, color);
};
