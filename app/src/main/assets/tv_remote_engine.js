/**
 * VideoCozumTV - D-Pad Kumanda Navigasyon Motoru
 * Bu script, Android TV kumandası yön tuşlarını (Yukarı, Aşağı, Sol, Sağ, Enter)
 * web sayfalarındaki tıklanabilir butonlara ve video kontrollerine odaklar.
 */
(function() {
    if (window._tvEngineLoaded) return;
    window._tvEngineLoaded = true;

    console.log("[TV_ENGINE] Remote Navigation Engine Aktif.");

    let currentFocus = null;
    const FOCUS_STYLE = "outline: 4px solid #F59E0B !important; outline-offset: 4px !important; box-shadow: 0 0 20px rgba(245, 158, 11, 0.9) !important; transition: all 0.15s ease !important;";

    function getClickableElements() {
        const selector = "a, button, input, select, [tabindex], [onclick], [role='button'], .btn, .soru-btn, .video-item, video, iframe";
        const nodes = Array.from(document.querySelectorAll(selector));
        return nodes.filter(el => {
            const rect = el.getBoundingClientRect();
            const style = window.getComputedStyle(el);
            return rect.width > 0 && rect.height > 0 &&
                   style.display !== 'none' &&
                   style.visibility !== 'hidden' &&
                   style.opacity !== '0';
        });
    }

    function applyFocus(el) {
        if (!el) return;
        if (currentFocus) {
            currentFocus.removeAttribute("data-tv-focused");
            if (currentFocus.dataset.origStyle) {
                currentFocus.style.cssText = currentFocus.dataset.origStyle;
            } else {
                currentFocus.style.outline = "";
                currentFocus.style.boxShadow = "";
            }
        }
        currentFocus = el;
        el.setAttribute("data-tv-focused", "true");
        el.dataset.origStyle = el.style.cssText;
        el.style.cssText += FOCUS_STYLE;

        el.scrollIntoView({ behavior: 'smooth', block: 'center', inline: 'center' });
        if (typeof el.focus === 'function') {
            el.focus();
        }
    }

    function findNextElement(direction) {
        const elements = getClickableElements();
        if (elements.length === 0) return null;

        if (!currentFocus || !document.contains(currentFocus)) {
            return elements[0];
        }

        const currRect = currentFocus.getBoundingClientRect();
        const currCenter = {
            x: currRect.left + currRect.width / 2,
            y: currRect.top + currRect.height / 2
        };

        let bestCandidate = null;
        let minDistance = Infinity;

        elements.forEach(el => {
            if (el === currentFocus) return;
            const r = el.getBoundingClientRect();
            const center = {
                x: r.left + r.width / 2,
                y: r.top + r.height / 2
            };

            const dx = center.x - currCenter.x;
            const dy = center.y - currCenter.y;

            let isValid = false;
            let directionalDist = Infinity;

            switch(direction) {
                case 'ArrowUp':
                    if (dy < -5) { // yukarıda
                        isValid = true;
                        directionalDist = Math.abs(dy) * 1.0 + Math.abs(dx) * 2.0;
                    }
                    break;
                case 'ArrowDown':
                    if (dy > 5) { // aşağıda
                        isValid = true;
                        directionalDist = Math.abs(dy) * 1.0 + Math.abs(dx) * 2.0;
                    }
                    break;
                case 'ArrowLeft':
                    if (dx < -5) { // solda
                        isValid = true;
                        directionalDist = Math.abs(dx) * 1.0 + Math.abs(dy) * 2.0;
                    }
                    break;
                case 'ArrowRight':
                    if (dx > 5) { // sağda
                        isValid = true;
                        directionalDist = Math.abs(dx) * 1.0 + Math.abs(dy) * 2.0;
                    }
                    break;
            }

            if (isValid && directionalDist < minDistance) {
                minDistance = directionalDist;
                bestCandidate = el;
            }
        });

        return bestCandidate;
    }

    // Klavye & Kumanda Tuş Dinleyicisi
    window.addEventListener('keydown', function(e) {
        if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
            const next = findNextElement(e.key);
            if (next) {
                applyFocus(next);
                e.preventDefault();
                e.stopPropagation();
            }
        } else if (e.key === 'Enter') {
            if (currentFocus) {
                if (currentFocus.tagName.toLowerCase() === 'video') {
                    if (currentFocus.paused) currentFocus.play();
                    else currentFocus.pause();
                } else {
                    currentFocus.click();
                }
                e.preventDefault();
                e.stopPropagation();
            }
        }
    }, true);

    // Sayfa hazır olduğunda ilk uygun butona odaklan
    function initFocus() {
        const elements = getClickableElements();
        if (elements.length > 0) {
            // Önce soru veya test butonlarını ara
            const questionBtn = elements.find(el => el.textContent && (el.textContent.includes('Soru') || el.textContent.includes('Test')));
            applyFocus(questionBtn || elements[0]);
        }
    }

    if (document.readyState === 'complete' || document.readyState === 'interactive') {
        setTimeout(initFocus, 600);
    } else {
        window.addEventListener('DOMContentLoaded', () => setTimeout(initFocus, 600));
    }

    // Video kontrol ipuçları: Video başladığında tam ekran butonuna tıkla
    setInterval(() => {
        const videos = document.querySelectorAll('video');
        videos.forEach(v => {
            if (!v.dataset.tvControlsAttached) {
                v.dataset.tvControlsAttached = "true";
                v.setAttribute("controls", "true");
            }
        });
    }, 1500);

})();
