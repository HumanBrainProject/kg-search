

export const formatHitForHighlight = (str) => str.replace(/<em>/gi, "<span class=\"kgs-hit-highlight\">").replace(/<\/em>/gi, "</span>")